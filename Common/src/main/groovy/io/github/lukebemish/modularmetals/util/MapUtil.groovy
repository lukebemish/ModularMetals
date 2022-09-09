package io.github.lukebemish.modularmetals.util

import java.util.function.Function

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
}
