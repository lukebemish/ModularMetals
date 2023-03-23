package io.github.lukebemish.modularmetals.util

import java.util.function.Function
import java.util.function.Predicate

class MapUtil {
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

    static Map replaceInMapByType(Map map, Function<String, Object> mapper) {
        map.collectEntries {key, value ->
            String strKey = key as String
            if (value instanceof Map) {
                if (value.containsKey('__value__')) {
                    String valueString = (value.get('__value__') ?: '') as String
                    return [strKey, mapper.apply(valueString)]
                }
                return [strKey, replaceInMapByType(value, mapper)]
            }
            if (value instanceof List)
                return [strKey, replaceInListByType(value, mapper)]
            return [strKey, value]
        }
    }

    static List replaceInListByType(List list, Function<String, Object> mapper) {
        list.collect {
            if (it instanceof Map) {
                if (it.containsKey('__value__')) {
                    String valueString = (it.get('__value__') ?: '') as String
                    return mapper.apply(valueString)
                }
                return replaceInMapByType(it, mapper)
            }
            if (it instanceof List)
                return replaceInListByType(it, mapper)
            return it
        }
    }

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
