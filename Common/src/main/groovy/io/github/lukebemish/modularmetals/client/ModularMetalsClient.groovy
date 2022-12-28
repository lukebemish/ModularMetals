package io.github.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.mojang.datafixers.util.Either
import com.mojang.serialization.DataResult
import dev.lukebemish.dynamicassetgenerator.api.ResourceCache
import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import dev.lukebemish.dynamicassetgenerator.api.client.AssetResourceCache
import dev.lukebemish.dynamicassetgenerator.api.client.generators.ITexSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.TexSourceDataHolder
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.ErrorSource
import dev.lukebemish.dynamicassetgenerator.api.client.generators.texsources.TextureReader
import io.github.groovymc.cgl.api.transform.codec.CodecRetriever
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.MapHolder
import io.github.lukebemish.modularmetals.data.texsources.EasyRecolorSource
import io.github.lukebemish.modularmetals.data.texsources.PropertyOrDefaultSource
import io.github.lukebemish.modularmetals.data.texsources.ResolvedVariantSource
import io.github.lukebemish.modularmetals.data.texsources.VariantTemplateSource
import io.github.lukebemish.modularmetals.data.texsources.WithTemplateSource
import io.github.lukebemish.modularmetals.data.variant.BlockVariant
import io.github.lukebemish.modularmetals.data.variant.ItemVariant
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration

import java.util.concurrent.atomic.AtomicReference
import java.util.function.Supplier

@CompileStatic
class ModularMetalsClient {
    private ModularMetalsClient() {}
    private static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(ModularMetalsClient.classLoader,new CompilerConfiguration()
            .addCompilationCustomizers(Constants.MAP_ACCESS_IMPORT_CUSTOMIZER, Constants.MAP_ACCESS_AST_CUSTOMIZER)))

    private static LangBuilder langBuilder = new LangBuilder()

    public static final AssetResourceCache ASSET_CACHE = ResourceCache.register(new AssetResourceCache(new ResourceLocation(Constants.MOD_ID, "assets")))

    static void init() {
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "template"), VariantTemplateSource.CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "resolved"), ResolvedVariantSource.CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "easy_recolor"), CodecRetriever[EasyRecolorSource])
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "property_or_default"), CodecRetriever[PropertyOrDefaultSource])
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "with_template"), CodecRetriever[WithTemplateSource])

        ASSET_CACHE.planSource(TexturePlanner.instance)
        ASSET_CACHE.planSource(ModelPlanner.instance)
        ASSET_CACHE.planSource(BlockstatePlanner.instance)
        ASSET_CACHE.planSource(new ResourceLocation(Constants.MOD_ID, 'lang/en_us.json'), {it -> langBuilder.build()})

        registerPlanners()
    }

    protected static void registerPlanners() {
        ModularMetalsCommon.config.metals.each {metalRl, metal ->
            Supplier<ITexSource> metalTexSource = Suppliers.<ITexSource>memoize {->
                DataResult<ITexSource> result = metal.texturing.generator.decode(ITexSource.CODEC)
                return result.result().orElseGet({->new ErrorSource("Could not load texturing for metal ${metalRl}: ${result.error().get().message()}")})}
            Set<ResourceLocation> variantRls = ModularMetalsCommon.getVariants(metalRl)
            for (ResourceLocation variantRl : variantRls) {
                ResourceLocation fullLocation = ModularMetalsCommon.assembleMetalVariantName(metalRl, variantRl)
                Variant variant = ModularMetalsCommon.config.variants.get(variantRl)
                if (variant instanceof ItemVariant) {
                    String header = variant instanceof BlockVariant ? 'block' : 'item'
                    Map<String, Supplier<ITexSource>> textures = variant.texturing.generator.<Map<String, Supplier<ITexSource>>>map({ either ->
                        either.<Map<String, Supplier<ITexSource>>>map({['':() -> {
                            DataResult<ITexSource> result = it.<ITexSource>decode(ITexSource.CODEC)
                            return result.result().orElseGet({-> new ErrorSource("Could not load texturing for variant ${variantRl}: ${result.error().get().message()}")})
                        } as Supplier<ITexSource>]},{it.collectEntries{key, value ->
                            [key, () -> {
                                DataResult<ITexSource> result = value.<ITexSource>decode(ITexSource.CODEC)
                                return result.result().orElseGet({-> new ErrorSource("Could not load texturing for variant ${variantRl}, texture ${key}: ${result.error().get().message()}")})
                            } as Supplier<ITexSource>]
                        }})
                    }).orElse(Map<String, Supplier<ITexSource>>.of('',metalTexSource))
                    Map<String, Either<ResourceLocation,MapHolder>> templateMap = variant.texturing.template
                            .<Map<String,Either<ResourceLocation,MapHolder>>>map({ ['':Either.<ResourceLocation, MapHolder>left(it)] }, {new HashMap<>(it)})
                    templateMap.putAll(metal.texturing.getResolvedTemplateOverrides(variantRl))
                    textures.each {key, fullTexSource ->
                        List<String> sourceLocations = []
                        sourceLocations.addAll(MapUtil.findFieldsFromMatching('path',metal.texturing.generator.map, {it.get('type') == 'dynamic_asset_generator:texture'}))
                        sourceLocations.addAll((Collection<String>) variant.texturing.generator.orElse(Either.<MapHolder, Map<String, MapHolder>> right([:])).<Collection<String>> map({ map ->
                            MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
                        }, {
                            it.values().collectMany { map ->
                                MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
                            }
                        }))
                        List<String> usedKeys = [key]
                        usedKeys.addAll(variant.texturing.generator.<Collection<String>>map({ either ->
                            MapHolder holder = either.map({it},{it.get(key)})
                            return MapUtil.findFieldsFromMatching('path',holder.map,{ it.get('type') == 'dynamic_asset_generator:texture' })
                        }).orElse([]))
                        for (String usedKey : usedKeys) {
                            sourceLocations.addAll(templateMap.containsKey(usedKey) ? templateMap.get(usedKey).<List<String>> map({
                                List.<String> of(it.toString())
                            }, { map ->
                                MapUtil.findFieldsFromMatching('path', map.map, { it.get('type') == 'dynamic_asset_generator:texture' })
                            }) : List.<String> of())
                        }
                        ResourceLocation full = new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}")

                        TexturePlanner.instance.plan(full, { context ->
                            ITexSource metalTex = metalTexSource.get()
                            ITexSource fullTex = fullTexSource.get()
                            TexSourceDataHolder data = new TexSourceDataHolder()
                            data.put(ResolvedVariantSource.ResolvedVariantData,new ResolvedVariantSource.ResolvedVariantData(metalTex))
                            data.put(VariantTemplateSource.TemplateData,new VariantTemplateSource.TemplateData(templateMap.collectEntries {templateKey, template ->
                                [templateKey, template.map({
                                    new TextureReader(it)
                                },{
                                    DataResult<ITexSource> result = it.decode(ITexSource.CODEC)
                                    return result.result().orElseGet({->new ErrorSource("Could not load texturing for template '${key}', metal ${metalRl}, variant ${variantRl}: ${result.error().get().message()}")})
                                })]
                            }, key))
                            data.put(PropertyOrDefaultSource.PropertyGetterData, new PropertyOrDefaultSource.PropertyGetterData(metal, metalRl))
                            return fullTex.getSupplier(data, context).get()
                        }, sourceLocations)
                    }

                    Map replacements = ['textures': textures.keySet().collectEntries {
                        [it, new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${it == '' ? '' : "_$it"}").toString()]
                    }]
                    replacements += ModularMetalsCommon.sharedEnvMap
                    replacements += ['metal':metalRl,'location':fullLocation] as Map

                    Map<String, Map> models = variant.texturing.model.<Map<String, Map>>map {processEither(it).collectEntries {key,holder->
                        [key,holder.map]
                    }}.orElseGet({->
                        if (variant instanceof BlockVariant)
                            return Map.<String, Map>of('',['parent':'block/cube_all', 'textures':['all':'${textures[""]}']])
                        return Map.<String, Map>of('',['parent':'item/generated', 'textures':['layer0':'${textures[""]}']])
                    })

                    if (variant instanceof BlockVariant) {
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
                            Map out = MapUtil.replaceInMap(map, {
                                var writer = new StringBuilderWriter()
                                ENGINE.createTemplate(it).make(replacements).writeTo(writer)
                                return writer.builder.toString()
                            })
                            BlockstatePlanner.instance.plan(fullLocation, out)
                        } catch (Exception e) {
                            Constants.LOGGER.error("Error writing blockstate for metal '${metalRl}', variant '${variantRl}':", e)
                        }
                    }

                    models.each {key, map ->
                        try {
                            ResourceLocation full = new ResourceLocation(fullLocation.namespace, "${key == 'item' ? 'item' : header}/${fullLocation.path}${key == '' || key == 'item' ? '' : "_$key"}")
                            Map out = MapUtil.replaceInMap(map, {
                                var writer = new StringBuilderWriter()
                                ENGINE.createTemplate(it).make(replacements).writeTo(writer)
                                return writer.builder.toString()
                            })
                            ModelPlanner.instance.plan(full, out)
                        } catch (Exception e) {
                            Constants.LOGGER.error("Error writing model '${key}' for metal '${metalRl}', variant '${variantRl}':", e)
                        }
                    }

                    String name = variant.name.contains('%s') ?
                            variant.name.replaceAll(/%s/, metal.name) :
                            "${metal.name} ${variant.name}"
                    String key = variant instanceof BlockVariant ?
                            "block.${Constants.MOD_ID}.${fullLocation.path}" :
                            "item.${Constants.MOD_ID}.${fullLocation.path}"
                    langBuilder.add(key,name)
                }
            }
        }
    }

    static <T> Map<String, T> processEither(Either<T,Map<String,T>> either) {
        return either.map({['':it]},{it})
    }
}
