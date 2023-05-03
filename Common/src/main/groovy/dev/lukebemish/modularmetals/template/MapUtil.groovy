package dev.lukebemish.modularmetals.template


import groovy.transform.TupleConstructor
import org.apache.groovy.io.StringBuilderWriter

import java.util.function.Predicate

class MapUtil {
    @TupleConstructor
    static class MapEvalEnvironment {
        Map replacements

        String evaluateTemplate(String initial, Map additional) {
            var writer = new StringBuilderWriter()
            TemplateEngine.ENGINE.createTemplate(initial).make(replacements+additional).writeTo(writer)
            return writer.builder.toString()
        }

        String evaluateTemplate(String initial) {
            return evaluateTemplate(initial, [:])
        }

        Object evaluateShell(String initial, Map additional) {
            return TemplateEngine.createShell(replacements+additional).evaluate(initial)
        }

        Object evaluateShell(String initial) {
            return evaluateShell(initial, [:])
        }
    }

    static Object evaluate(Object obj, Map replacements) {
        return evaluate(obj, new MapEvalEnvironment(replacements))
    }

    static Map evaluateEntries(Map map, Map replacements) {
        return evaluateEntries(map, new MapEvalEnvironment(replacements))
    }

    static Object evaluate(Object obj, MapEvalEnvironment env) {
        if (obj instanceof Map)
            return evaluateInner((Map) obj, env)
        if (obj instanceof List)
            return evaluateInner((List) obj, env)
        if (obj instanceof String)
            return env.evaluateTemplate(obj)
        if (obj instanceof GString)
            return env.evaluateTemplate(obj as String)
        return obj
    }

    private static Object evaluateInner(Map map, MapEvalEnvironment env) {
        if (map.containsKey(TemplateEngine.IF_KEY)) {
            String valueString = map.get(TemplateEngine.IF_KEY) as String
            Map extra = new HashMap(map)
            extra.removeAll {key, value -> TemplateEngine.RESERVED.contains(key)}
            Object value = env.evaluateShell(valueString, extra)
            if (value !instanceof Boolean || !value) {
                if (map.containsKey(TemplateEngine.ELSE_KEY)) {
                    return evaluate(map.get(TemplateEngine.ELSE_KEY), env)
                } else {
                    return RemovalQueued.instance
                }
            }
            Map newMap = new HashMap(map)
            newMap.remove(TemplateEngine.IF_KEY)
            newMap.remove(TemplateEngine.ELSE_KEY)
            return evaluate(newMap, env)
        } else if (map.containsKey(TemplateEngine.OPTIONAL_KEY)) {
            String valueString = map.get(TemplateEngine.OPTIONAL_KEY) as String
            Map extra = new HashMap(map)
            extra.removeAll {key, value -> TemplateEngine.RESERVED.contains(key)}
            Object optionalValue = env.evaluateShell(valueString, extra)
            if (optionalValue instanceof Optional)
                if (optionalValue.present)
                    return optionalValue.get()
                else
                    return RemovalQueued.instance
            return optionalValue
        } else if (map.containsKey(TemplateEngine.CODE_KEY)) {
            String valueString = map.get(TemplateEngine.CODE_KEY) as String
            Map extra = new HashMap(map)
            extra.removeAll {key, value -> TemplateEngine.RESERVED.contains(key)}
            return env.evaluateShell(valueString, extra)
        }
        Map out = map.collectEntries {key, value ->
            [evaluate(key, env), evaluate(value, env)]
        }
        out.removeAll {key, value -> value === RemovalQueued.instance}
        return out
    }

    private static Object evaluateInner(List list, MapEvalEnvironment env) {
        List out = list.collect {value ->
            evaluate(value, env)
        }
        out.removeAll {value -> value === RemovalQueued.instance}
        return out
    }

    static Map evaluateEntries(Map map, MapEvalEnvironment env) {
        Map out = map.collectEntries {key, value ->
            [evaluate(key, env), evaluate(value, env)]
        }
        out.removeAll {key, value -> value === RemovalQueued.instance}
        return out
    }

    @Singleton
    static class RemovalQueued {

    }

    static List<String> findFieldsFromMatching(String field, Map mapToSearch, Predicate<Map> checkMap) {
        List<String> out = []
        if (checkMap.test(mapToSearch)) {
            Object value = mapToSearch.get(field)
            if (value !== null)
                out.add(value as String)
        }
        for (Object object : mapToSearch.values()) {
            if (object instanceof Map) {
                out += findFieldsFromMatching(field, object, checkMap)
            }
        }
        return out
    }
}
