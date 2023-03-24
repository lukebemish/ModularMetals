package dev.lukebemish.modularmetals.data

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import net.minecraft.resources.ResourceLocation

import java.util.function.Function

@CompileStatic
@TupleConstructor
class TexSourceMap {
    final Map<String, MapHolder> value

    @ExposeCodec
    static final Codec<TexSourceMap> CODEC = Codec.either(
        ResourceLocation.CODEC,
        Codec.<String, MapHolder>unboundedMap(
            Codec.STRING,
            Codec.either(
                ResourceLocation.CODEC,
                MapHolder.CODEC).<MapHolder>xmap(either -> either.map(TexSourceMap::wrapSingleFile, Function.<MapHolder>identity()), holder -> Either.<ResourceLocation,MapHolder>right(holder))))
        .<TexSourceMap>xmap(TexSourceMap::unwrapEither as Function<Either<ResourceLocation, Map<String,MapHolder>>, TexSourceMap>, TexSourceMap::getValue as Function<TexSourceMap, Either<ResourceLocation, Map<String,MapHolder>>>)

    static final Codec<TexSourceMap> NONEMPTY_CODEC = Codec.either(
        ResourceLocation.CODEC,
        Codec.<String, MapHolder>unboundedMap(
            Codec.STRING,
            MapHolder.CODEC))
        .<TexSourceMap>xmap(TexSourceMap::unwrapEither as Function<Either<ResourceLocation, Map<String,MapHolder>>, TexSourceMap>, TexSourceMap::getValue as Function<TexSourceMap, Either<ResourceLocation, Map<String,MapHolder>>>)

    @PackageScope
    static MapHolder wrapSingleFile(ResourceLocation rl) {
        Map map = [
            'type':'dynamic_asset_generator:texture',
            'path':rl.toString()
        ]
        return new MapHolder(map)
    }

    @PackageScope
    static TexSourceMap unwrapEither(Either<ResourceLocation, Map<String,MapHolder>> either) {
        return either.map(rl -> new TexSourceMap(['':wrapSingleFile(rl)]), TexSourceMap::new)
    }
}
