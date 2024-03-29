package dev.lukebemish.modularmetals.client.variant

import com.mojang.blaze3d.platform.NativeImage
import com.mojang.datafixers.util.Either
import com.mojang.serialization.DataResult
import dev.lukebemish.dynamicassetgenerator.api.ResourceGenerationContext
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import dev.lukebemish.modularmetals.Constants
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.client.planner.LangPlanner
import dev.lukebemish.modularmetals.client.planner.ModelPlanner
import dev.lukebemish.modularmetals.client.planner.TexturePlanner
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.texsources.PropertyOrDefaultSource
import dev.lukebemish.modularmetals.data.texsources.ResolvedVariantSource
import dev.lukebemish.modularmetals.data.texsources.VariantTemplateSource
import dev.lukebemish.modularmetals.data.variant.ItemVariant
import dev.lukebemish.modularmetals.data.variant.Variant
import dev.lukebemish.modularmetals.template.MapUtil
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation

import java.util.function.Function
import java.util.function.Supplier

@CompileStatic
class ItemClientVariantHandler implements ClientVariantHandler {
    static void language(Metal metal, ResourceLocation metalLocation, ResourceLocation variantLocation, ItemVariant variant) {
        ResourceLocation fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalLocation, variantLocation)
        var variantLang = variant.name.map({Map.of('en_us',it)}, {it})
        var metalLang = metal.name.map({Map.of('en_us',it)}, {it})
        Set<String> langKeys = variantLang.keySet() + metalLang.keySet()
        for (String key : langKeys) {
            String variantName = variantLang.getOrDefault(key, "modularmetals.variant.${variantLocation.namespace}.${variantLocation.path}" as String)
            String metalName = metalLang.getOrDefault(key, "modularmetals.metal.${metalLocation.namespace}.${metalLocation.path}" as String)
            String name = variantName.contains('%s') ?
                variantName.replaceAll(/%s/, metalName) :
                "${metalName} ${variantName}"
            String translationKey = variant.makeTranslationKey(fullLocation.path)
            LangPlanner.instance.add(key, translationKey, name)
        }
    }

    String getHeader() {
        return 'item'
    }

    Map<String, Map> defaultModel(ResourceLocation fullLocation) {
        return Map.<String, Map> of('', ['parent': 'item/generated', 'textures': ['layer0': '${textures[""]}']])
    }

    boolean processSpecial(ResourceLocation fullLocation, ResourceLocation metalRl, Variant variant, String name, Function<ResourceGenerationContext, NativeImage> imageSource) {
        return false
    }

    void registerPlanners(ResourceLocation metalRl,
                          ResourceLocation variantRl,
                          Supplier<ITexSource> metalTexSource,
                          Variant variant,
                          Metal metal) {
        if (variant !instanceof ItemVariant) {
            return
        }
        ResourceLocation fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalRl, variantRl)
        String header = getHeader()

        Map<String, Supplier<ITexSource>> textures = variant.texturing.simplifiedGenerator
            .<Map<String, Supplier<ITexSource>>> map { mapHolders ->
                return mapHolders.collectEntries { key, mapHolder ->
                    return [key, texSourceFromHolder(mapHolder, "Could not load texturing for variant ${variantRl}${key == '' ? '' : ", texture ${key}"}")]
                }
            }.orElse(Map.of('', metalTexSource))

        Map<String, MapHolder> templateMap = new HashMap<>(variant.texturing.template.value)

        templateMap.putAll(metal.texturing.getResolvedTemplateOverrides(variantRl))

        Set<String> modelTextures = new HashSet<>()
        // iterate over variant texture generators
        for (Map.Entry<String, Supplier<ITexSource>> entry : textures) {
            String key = entry.key
            Supplier<ITexSource> fullTexSource = entry.value

            // collect used textures for texture-meta
            List<String> sourceLocations = extractSourceTextures(key, templateMap, metal, variant)

            ResourceLocation full = new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}")

            Function<ResourceGenerationContext, NativeImage> imageSource = { ResourceGenerationContext context -> generateTexture(context, metalTexSource, fullTexSource, templateMap, metalRl, variantRl, key, metal) }

            if (!processSpecial(fullLocation, metalRl, variant, key, imageSource)) {
                TexturePlanner.instance.plan(full, imageSource, sourceLocations)

                modelTextures.add(key)
            }
        }

        Map replacements = ['textures': modelTextures.collectEntries {
            [it, new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${it == '' ? '' : "_$it"}").toString()]
        }]
        replacements += ModularMetalsCommon.sharedEnvMap
        replacements += ['metal': metalRl, 'location': fullLocation, 'properties':metal.properties] as Map

        // get model map or make defaults if they're missing
        Map<String, Map> models = variant.texturing.simplifiedModel.<Map<String, Map>> map {
            it.collectEntries {key, holder -> [key, holder.map]}
        }.orElseGet({ ->
            defaultModel(fullLocation)
        })

        fillPlanners(models, fullLocation, replacements, metal, variant, metalRl, variantRl)
    }

    protected void fillPlanners(Map<String, Map> models,
                                ResourceLocation fullLocation,
                                Map replacements,
                                Metal metal,
                                ItemVariant variant,
                                ResourceLocation metalRl,
                                ResourceLocation variantRl) {
        // actually generate the model files
        models.each { key, map ->
            try {
                ResourceLocation full = new ResourceLocation(fullLocation.namespace, "${key == 'item' ? 'item' : header}/${fullLocation.path}${key == '' || key == 'item' ? '' : "_$key"}")
                Map out = MapUtil.evaluateEntries(map, replacements+ModularMetalsCommon.sharedEnvMap)
                ModelPlanner.instance.plan(full, out)
            } catch (Exception e) {
                Constants.LOGGER.error("Error writing model '${key}' for metal '${metalRl}', variant '${variantRl}':", e)
            }
        }

        // lang file
        language(metal, metalRl, variantRl, variant)
    }

    static NativeImage generateTexture(ResourceGenerationContext context,
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
        data.put(VariantTemplateSource.TemplateData,new VariantTemplateSource.TemplateData(templateMap.collectEntries { templateKey, template ->
            DataResult<ITexSource> result = template.decode(ITexSource.CODEC)
            [templateKey, result.result().orElseGet({->new ErrorSource("Could not load texturing for template '${key}', metal ${metalRl}, variant ${variantRl}: ${result.error().get().message()}")})]
        }, key))
        data.put(PropertyOrDefaultSource.PropertyGetterData, new PropertyOrDefaultSource.PropertyGetterData(metal, metalRl))
        return fullTex.getSupplier(data, context).get()
    }

    static List<String> extractSourceTextures(String textureKey, Map<String, MapHolder> templateMap, Metal metal, ItemVariant variant) {
        List<String> sourceLocations = []
        sourceLocations.addAll(MapUtil.findFieldsFromMatching('path', metal.texturing.generator.map, { it.get('type') == 'dynamic_asset_generator:texture' }))
        sourceLocations.addAll(variant.texturing.simplifiedGenerator.<Collection<String>> map {
            it.values().collectMany { map ->
                MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
            }
        }.orElse([]))

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

    static Supplier<ITexSource> texSourceFromHolder(MapHolder holder, String error) {
        return () -> {
            DataResult<ITexSource> result = holder.<ITexSource> decode(ITexSource.CODEC)
            return result.result().orElseGet({ -> new ErrorSource("${error}: ${result.error().get().message()}") })
        }
    }

    static <T> Map<String, T> processEither(Either<T,Map<String,T>> either) {
        return either.map({['':it]},{it})
    }
}
