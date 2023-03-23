package io.github.lukebemish.modularmetals

import com.google.common.base.Suppliers
import groovy.text.SimpleTemplateEngine
import io.github.lukebemish.modularmetals.util.MapUtil
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration

class TemplateEngine {
    public static final CompilerConfiguration COMPILER_CONFIGURATION = new CompilerConfiguration()
    public static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(TemplateEngine.classLoader, COMPILER_CONFIGURATION))

    private TemplateEngine() {}

    static Object fillReplacements(Object obj, Map replacements) {
        if (obj instanceof Map) {
            return fillReplacements((Map) obj, replacements)
        }
        if (obj instanceof List) {
            List out = []
            for (Object item : (List) obj) {
                out.add(fillReplacements(item, replacements))
            }
            return out
        }
        return obj
    }

    static Map fillReplacements(Map map, Map replacements) {
        var shell = Suppliers.memoize {->
            Binding binding = new Binding(replacements)
            new GroovyShell(TemplateEngine.classLoader, binding, COMPILER_CONFIGURATION)
        }
        MapUtil.replaceInMapByType(map, {
            return shell.get().evaluate(it)
        })
        MapUtil.replaceInMap(map, {
            var writer = new StringBuilderWriter()
            ENGINE.createTemplate(it).make(replacements).writeTo(writer)
            return writer.builder.toString()
        })
    }
}
