package io.github.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.datafixers.util.Either
import com.mojang.serialization.DataResult
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.texsources.*
import io.github.lukebemish.modularmetals.data.variant.BlockVariant
import io.github.lukebemish.modularmetals.data.variant.ItemVariant
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

class ModularMetalsClient {
    private ModularMetalsClient() {}

    private static LangBuilder langBuilder = new LangBuilder()

    public static final AssetResourceCache ASSET_CACHE = ResourceCache.register(new AssetResourceCache(new ResourceLocation(Constants.MOD_ID, "assets")))

    static void init() {
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "template"), VariantTemplateSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "resolved"), ResolvedVariantSource.RESOLVED_CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "easy_recolor"), EasyRecolorSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "property_or_default"), PropertyOrDefaultSource.$CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "with_template"), WithTemplateSource.$CODEC)

        ASSET_CACHE.planSource(TexturePlanner.instance)
        ASSET_CACHE.planSource(ModelPlanner.instance)
        ASSET_CACHE.planSource(BlockstatePlanner.instance)
        ASSET_CACHE.planSource(new ResourceLocation(Constants.MOD_ID, 'lang/en_us.json'), {it, context -> langBuilder.build()})

        registerPlanners()
    }

    protected static void registerPlanners() {
        ModularMetalsCommon.config.metals.each {metalRl, metal ->
            Supplier<ITexSource> metalTexSource = Suppliers.memoize {->
                DataResult<ITexSource> result = metal.texturing.generator.decode(ITexSource.CODEC)
                return result.result().orElseGet({->new ErrorSource("Could not load texturing for metal ${metalRl}: ${result.error().get().message()}")})
            }
            Set<ResourceLocation> variantRls = ModularMetalsCommon.getVariants(metalRl)
            for (final variantRl : variantRls) {
                registerVariantPlanners(metalRl, variantRl, metalTexSource, metal)
            }
        }
    }

    protected static void registerVariantPlanners(ResourceLocation metalRl,
                                                  ResourceLocation variantRl,
                                                  Supplier<ITexSource> metalTexSource,
                                                  Metal metal) {
        Variant variant = ModularMetalsCommon.config.variants.get(variantRl)
        if (variant instanceof ItemVariant) {
            registerItemVariantPlanners(metalRl, variantRl, metalTexSource, variant, metal)
        }
    }

    protected static void registerItemVariantPlanners(ResourceLocation metalRl,
                                                      ResourceLocation variantRl,
                                                      Supplier<ITexSource> metalTexSource,
                                                      ItemVariant variant,
                                                      Metal metal) {
        ResourceLocation fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalRl, variantRl)
        String header = variant instanceof BlockVariant ? "block" : "item"

        Map<String, Supplier<ITexSource>> textures = variant.texturing.generator
            .map(either -> ModularMetalsClient.<MapHolder>processEither(either))
            .<Map<String, Supplier<ITexSource>>>map { mapHolders ->
                return mapHolders.collectEntries { key, mapHolder ->
                    return [key, texSourceFromHolder(mapHolder, "Could not load texturing for variant ${variantRl}${key == '' ? '' : ", texture ${key}"}")]
                }
            }.orElse(Map.of('',metalTexSource))

        Map<String, MapHolder> templateMap = new HashMap<>(variant.texturing.template.value)

        templateMap.putAll(metal.texturing.getResolvedTemplateOverrides(variantRl))

        // iterate over variant texture generators
        for (Map.Entry<String, Supplier<ITexSource>> entry : textures) {
            String key = entry.key
            Supplier<ITexSource> fullTexSource = entry.value

            // collect used textures for texture-meta
            List<String> sourceLocations = extractSourceTextures(key, templateMap, metal, variant)

            ResourceLocation full = new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}")

            TexturePlanner.instance.plan(full, context -> generateTexture(context, metalTexSource, fullTexSource, templateMap, metalRl, variantRl, key, metal), sourceLocations)
        }

        Map replacements = ['textures': textures.keySet().collectEntries {
            [it, new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${it == '' ? '' : "_$it"}").toString()]
        }]
        replacements += ModularMetalsCommon.sharedEnvMap
        replacements += ['metal':metalRl,'location':fullLocation] as Map

        // get model map or make defaults if they're missing
        Map<String, Map> models = variant.texturing.model.<Map<String, Map>>map {processEither(it).collectEntries {key,holder->
            [key,holder.map]
        }}.orElseGet({->
            if (variant instanceof BlockVariant)
                return Map.<String, Map>of('',(Map)['parent':'block/cube_all', 'textures':['all':'${textures[""]}']],'item',(Map)['parent':"${fullLocation.namespace}:block/${fullLocation.path}" as String])
            return Map.<String, Map>of('',['parent':'item/generated', 'textures':['layer0':'${textures[""]}']])
        })

        // generate blockstate files if  necessary
        if (variant instanceof BlockVariant) {
            generateBlockstate(models, fullLocation, replacements, header, variant, metalRl, variantRl)
        }

        // actually generate the model files
        models.each {key, map ->
            try {
                ResourceLocation full = new ResourceLocation(fullLocation.namespace, "${key == 'item' ? 'item' : header}/${fullLocation.path}${key == '' || key == 'item' ? '' : "_$key"}")
                Map out = fillReplacements(map, replacements)
                ModelPlanner.instance.plan(full, out)
            } catch (Exception e) {
                Constants.LOGGER.error("Error writing model '${key}' for metal '${metalRl}', variant '${variantRl}':", e)
            }
        }

        // lang file
        String name = variant.name.contains('%s') ?
            variant.name.replaceAll(/%s/, metal.name) :
            "${metal.name} ${variant.name}"
        String key = variant instanceof BlockVariant ?
            "block.${Constants.MOD_ID}.${fullLocation.path}" :
            "item.${Constants.MOD_ID}.${fullLocation.path}"
        langBuilder.add(key,name)
    }

    private static Map fillReplacements(Map map, Map replacements) {
        MapUtil.replaceInMap(map, {
            var writer = new StringBuilderWriter()
            Constants.ENGINE.createTemplate(it).make(replacements).writeTo(writer)
            return writer.builder.toString()
        })
    }

    private static void generateBlockstate(Map<String, Map> models,
                                           ResourceLocation fullLocation,
                                           Map replacements,
                                           String header,
                                           BlockVariant variant,
                                           ResourceLocation metalRl,
                                           ResourceLocation variantRl) {
        AtomicReference<ResourceLocation> mainModel = new AtomicReference<>(new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}"))
        models = new HashMap<>(models)
        models.computeIfAbsent('item', {
            String key = models.keySet().sort().get(0)
            mainModel.set(new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}"))
            return ['parent':mainModel.get().toString()]
        })

        Map map = variant.blockTexturing.blockstate.map {it.map}.orElseGet {->
            ['variants':['':[
                'model': mainModel.get().toString()
            ]]]}
        try {
            Map out = fillReplacements(map, replacements)
            BlockstatePlanner.instance.plan(fullLocation, out)
        } catch (Exception e) {
            Constants.LOGGER.error("Error writing blockstate for metal '${metalRl}', variant '${variantRl}':", e)
        }
    }

    private static NativeImage generateTexture(ResourceGenerationContext context,
                                               Supplier<ITexSource> metalTexSource,
                                               Supplier<ITexSource> fullTexSource,
                                               Map<String, MapHolder> templateMap,
                                               ResourceLocation metalRl,
                                               ResourceLocation variantRl,
                                               String key,
                                               Metal metal) {
        ITexSource metalTex = metalTexSource.get()
        ITexSource fullTex = fullTexSource.get()
        TexSourceDataHolder data = new TexSourceDataHolder()
        data.put(ResolvedVariantSource.ResolvedVariantData,new ResolvedVariantSource.ResolvedVariantData(metalTex))
        data.put(VariantTemplateSource.TemplateData,new VariantTemplateSource.TemplateData(templateMap.collectEntries {templateKey, template ->
            DataResult<ITexSource> result = template.decode(ITexSource.CODEC)
            [templateKey, result.result().orElseGet({->new ErrorSource("Could not load texturing for template '${key}', metal ${metalRl}, variant ${variantRl}: ${result.error().get().message()}")})]
        }, key))
        data.put(PropertyOrDefaultSource.PropertyGetterData, new PropertyOrDefaultSource.PropertyGetterData(metal, metalRl))
        return fullTex.getSupplier(data, context).get()
    }

    private static List<String> extractSourceTextures(String textureKey, Map<String, MapHolder> templateMap, Metal metal, ItemVariant variant) {
        List<String> sourceLocations = []
        sourceLocations.addAll(MapUtil.findFieldsFromMatching('path', metal.texturing.generator.map, { it.get('type') == 'dynamic_asset_generator:texture' }))
        sourceLocations.addAll((Collection<String>) variant.texturing.generator.orElse(Either.<MapHolder, Map<String, MapHolder>> right([:])).<Collection<String>> map({ map ->
            MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
        }, {
            it.values().collectMany { map ->
                MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
            }
        }))

        // Add from variants
        List<String> usedKeys = [textureKey]
        for (MapHolder holder : variant.texturing.simplifiedGenerator.orElse([:]).values()) {
            usedKeys.addAll(MapUtil.findFieldsFromMatching('path', holder.map, { it.get('type') == 'dynamic_asset_generator:texture' }))
        }
        for (String usedKey : usedKeys) {
            if (templateMap.containsKey(usedKey)) {
                MapHolder holder = templateMap.get(usedKey)
                sourceLocations.addAll(MapUtil.findFieldsFromMatching('path', holder.map, { it.get('type') == 'dynamic_asset_generator:texture' }))
            }
        }

        return sourceLocations
    }

    protected static Supplier<ITexSource> texSourceFromHolder(MapHolder holder, String error) {
        return () -> {
            DataResult<ITexSource> result = holder.<ITexSource> decode(ITexSource.CODEC)
            return result.result().orElseGet({ -> new ErrorSource("${error}: ${result.error().get().message()}") })
        }
    }

    static <T> Map<String, T> processEither(Either<T,Map<String,T>> either) {
        return either.map({['':it]},{it})
    }
}
