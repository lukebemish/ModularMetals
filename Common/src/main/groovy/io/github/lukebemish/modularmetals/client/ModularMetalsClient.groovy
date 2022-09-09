package io.github.lukebemish.modularmetals.client

import com.google.common.base.Suppliers
import com.mojang.datafixers.util.Either
import com.mojang.serialization.DataResult
import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import io.github.lukebemish.dynamic_asset_generator.api.client.AssetResourceCache
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.ITexSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.TexSourceDataHolder
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.ErrorSource
import io.github.lukebemish.dynamic_asset_generator.api.client.generators.texsources.TextureReader
import io.github.lukebemish.modularmetals.Constants
import io.github.lukebemish.modularmetals.ModularMetalsCommon
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.texsources.EasyRecolorSource
import io.github.lukebemish.modularmetals.data.texsources.ResolvedVariantSource
import io.github.lukebemish.modularmetals.data.texsources.VariantTemplateSource
import io.github.lukebemish.modularmetals.data.variant.BlockVariant
import io.github.lukebemish.modularmetals.data.variant.ItemVariant
import io.github.lukebemish.modularmetals.data.variant.Variant
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration

import java.util.function.Supplier

@CompileStatic
class ModularMetalsClient {
    private ModularMetalsClient() {}
    private static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(ModularMetalsClient.classLoader,new CompilerConfiguration()
            .addCompilationCustomizers(Constants.MAP_ACCESS_IMPORT_CUSTOMIZER, Constants.MAP_ACCESS_AST_CUSTOMIZER)))

    private static LangBuilder langBuilder = new LangBuilder()

    static void init() {
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "template"), VariantTemplateSource.CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "resolved"), ResolvedVariantSource.CODEC)
        ITexSource.register(new ResourceLocation(Constants.MOD_ID, "easy_recolor"), EasyRecolorSource.$CODEC)

        AssetResourceCache.INSTANCE.planSource(TexturePlanner.instance)
        AssetResourceCache.INSTANCE.planSource(ModelPlanner.instance)
        AssetResourceCache.INSTANCE.planSource(new ResourceLocation(Constants.MOD_ID, 'lang/en_us.json'), {it -> langBuilder.build()})

        registerTexturePlanners()
    }

    protected static void registerTexturePlanners() {
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
                    textures.each {key, fullTexSource ->
                        ResourceLocation full = new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}")
                        TexturePlanner.instance.plan(full, { ->
                            ITexSource metalTex = metalTexSource.get()
                            ITexSource fullTex = fullTexSource.get()
                            TexSourceDataHolder data = new TexSourceDataHolder()
                            data.put(ResolvedVariantSource.ResolvedVariantData,new ResolvedVariantSource.ResolvedVariantData(metalTex))
                            data.put(VariantTemplateSource.SingleVariantData,new VariantTemplateSource.SingleVariantData(getTemplateToUse(variantRl, metal, variant.texturing.template, key)))
                            return fullTex.getSupplier(data).get()
                        })
                    }

                    Map replacements
                    if (textures.keySet().size() == 1 && textures.containsKey('')) {
                        replacements = ['textures':new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}").toString()]
                    } else {
                        replacements = ['textures': textures.keySet().collectEntries {
                            [it, new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${it == '' ? '' : "_$it"}").toString()]
                        }]
                    }
                    replacements += ModularMetalsCommon.sharedEnvMap

                    Map<String, Map> models = variant.texturing.model.<Map<String, Map>>map {processEither(it).collectEntries {key,holder->
                        [key,holder.map]
                    }}.orElse(Map.<String, Map>of('',['parent':'item/generated', 'textures':['layer0':'${textures}']]))

                    models.each {key, map ->
                        try {
                            ResourceLocation full = new ResourceLocation(fullLocation.namespace, "$header/${fullLocation.path}${key == '' ? '' : "_$key"}")
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

                    String name = metal.name.contains('%s') ?
                            metal.name.replaceAll(/%s/, variant.name) :
                            "${metal.name} ${variant.name}"
                    String key = variant instanceof BlockVariant ?
                            "block.${Constants.MOD_ID}.${fullLocation.path}" :
                            "item.${Constants.MOD_ID}.${fullLocation.path}"
                    langBuilder.add(key,name)
                }
            }
        }
    }

    static ITexSource getTemplateToUse(ResourceLocation variantRl, Metal metal, Either<ResourceLocation,Map<String,ResourceLocation>> either, String key) {
        if (metal.texturing.templateOverrides.containsKey(variantRl)) {
            Map<String,ResourceLocation> overrides = processEither(metal.texturing.templateOverrides.get(variantRl))
            if (overrides.containsKey(key))
                return new TextureReader(overrides.get(key))
        }

        return either.<ITexSource>map({ new TextureReader(it) },{it.get(key)?.with{
            new TextureReader(it)
        }?:new ErrorSource("Model key $key does not have a matching template texture")})
    }

    static <T> Map<String, T> processEither(Either<T,Map<String,T>> either) {
        return either.map({['':it]},{it})
    }
}
