package dev.lukebemish.modularmetals.util

import com.mojang.serialization.Codec
import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.data.MapHolder
import dev.lukebemish.modularmetals.template.TemplateEngine
import groovy.transform.CompileStatic
import groovy.transform.TupleConstructor
import io.github.groovymc.cgl.api.transform.codec.ExposeCodec
import org.jetbrains.annotations.Nullable

import java.util.function.Predicate

@TupleConstructor
@CompileStatic
class ObjPredicate implements Predicate<Map> {
    final Map existing
    @Nullable
    final String code

    @ExposeCodec
    static Codec<ObjPredicate> CODEC = MapHolder.CODEC.<ObjPredicate>xmap({
        Map newMap = new HashMap(it.map)
        String predicate = newMap.remove('__predicate__')
        new ObjPredicate(newMap, predicate)
    },{
        new MapHolder(it.existing + it.code===null ? ['__predicate__':it.code] : [:])
    })

    @Override
    boolean test(Map map) {
        if (existing.any { key, value -> map[key] != value }) {
            return false
        }
        if (code === null) {
            return true
        }

        Map props = ['it':map]
        props += ModularMetalsCommon.sharedEnvMap
        Object out = TemplateEngine.makeShell(props).get().evaluate(code)
        if (out instanceof Boolean) {
            return out
        }

        return false
    }
}
