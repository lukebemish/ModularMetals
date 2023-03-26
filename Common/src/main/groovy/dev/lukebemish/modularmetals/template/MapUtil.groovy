package dev.lukebemish.modularmetals.template

import groovy.transform.PackageScope

import java.util.function.Function
import java.util.function.Predicate

class MapUtil {
    @PackageScope
    static Object replaceIn(Object object, Function<String, String> function) {
        if (object instanceof Map)
            return replaceInMap(object, function)
        if (object instanceof List)
            return replaceInList(object, function)
        if (object instanceof String)
            return function.apply(object)
        if (object instanceof GString)
            return function.apply(object as String)
        return object
    }

    @PackageScope
    static Map replaceInMap(Map map, Function<String, String> function) {
        map.collectEntries {key, value ->
            String strKey = key as String
            if (value instanceof Map)
                return [function.apply(strKey), replaceInMap(value, function)]
            if (value instanceof List)
                return [function.apply(strKey), replaceInList(value, function)]
            if (value instanceof String)
                return [function.apply(strKey), function.apply(value)]
            if (value instanceof GString)
                return [function.apply(strKey), function.apply(value as String)]
            return [function.apply(strKey), value]
        }
    }

    @PackageScope
    static Object replaceInMapByTypeFull(Map map, Function<String, Object> mapper) {
        if (map.containsKey(TemplateEngine.CODE_KEY)) {
            String valueString = mapper.apply(map.get(TemplateEngine.CODE_KEY) as String)
            return mapper.apply(valueString)
        }
        return replaceInMapByType(map, mapper)
    }

    @PackageScope
    static Map replaceInMapByType(Map map, Function<String, Object> mapper) {
        map.collectEntries {key, value ->
            String strKey = key as String
            if (value instanceof Map) {
                if (value.containsKey(TemplateEngine.CODE_KEY)) {
                    String valueString = value.get(TemplateEngine.CODE_KEY) as String
                    return [strKey, mapper.apply(valueString)]
                } else if (value.containsKey(TemplateEngine.OPTIONAL_KEY)) {
                    String valueString = value.get(TemplateEngine.OPTIONAL_KEY) as String
                    var optionalValue = mapper.apply(valueString)
                    if (optionalValue instanceof Optional) {
                        if (optionalValue.isPresent()) {
                            return [strKey, optionalValue.get()]
                        } else {
                            return [strKey, RemovalQueued.instance]
                        }
                    } else {
                        return [strKey, optionalValue]
                    }
                } else if (value.containsKey(TemplateEngine.IF_KEY)) {
                    String valueString = value.get(TemplateEngine.IF_KEY) as String
                    var boolValue = mapper.apply(valueString)
                    if (boolValue instanceof Boolean && boolValue) {
                        Map newMap = new LinkedHashMap(value)
                        newMap.remove(TemplateEngine.IF_KEY)
                        return [strKey, newMap]
                    } else {
                        return [strKey, RemovalQueued.instance]
                    }
                }
                return [strKey, replaceInMapByType(value, mapper)]
            }
            if (value instanceof List)
                return [strKey, replaceInListByType(value, mapper)]
            return [strKey, value]
        }.findAll { key, value -> value !== RemovalQueued.instance }
    }

    @PackageScope
    static List replaceInListByType(List list, Function<String, Object> mapper) {
        list.collect {
            if (it instanceof Map) {
                if (it.containsKey(TemplateEngine.CODE_KEY)) {
                    String valueString = it.get(TemplateEngine.CODE_KEY) as String
                    return mapper.apply(valueString)
                }
                if (it.containsKey(TemplateEngine.OPTIONAL_KEY)) {
                    String valueString = it.get(TemplateEngine.OPTIONAL_KEY) as String
                    var optionalValue = mapper.apply(valueString)
                    if (optionalValue instanceof Optional) {
                        if (optionalValue.isPresent()) {
                            return optionalValue.get()
                        } else {
                            return RemovalQueued.instance
                        }
                    } else {
                        return optionalValue
                    }
                } else if (it.containsKey(TemplateEngine.IF_KEY)) {
                    String valueString = it.get(TemplateEngine.IF_KEY) as String
                    var boolValue = mapper.apply(valueString)
                    if (boolValue instanceof Boolean && boolValue) {
                        Map map = new LinkedHashMap(it)
                        map.remove(TemplateEngine.IF_KEY)
                        return map
                    } else {
                        return RemovalQueued.instance
                    }
                }
                return replaceInMapByType(it, mapper)
            }
            if (it instanceof List)
                return replaceInListByType(it, mapper)
            return it
        }.findAll {it !== RemovalQueued.instance}
    }

    @Singleton
    static class RemovalQueued {

    }

    @PackageScope
    static List replaceInList(List list, Function<String, String> function) {
        list.collect {
            if (it instanceof Map)
                return replaceInMap(it, function)
            if (it instanceof List)
                return replaceInList(it, function)
            if (it instanceof String)
                return function.apply(it)
            if (it instanceof GString)
                return function.apply(it as String)
            return it
        }
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
