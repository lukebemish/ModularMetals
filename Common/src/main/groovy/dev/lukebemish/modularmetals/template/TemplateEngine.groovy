package dev.lukebemish.modularmetals.template

import com.google.common.base.Suppliers
import dev.lukebemish.modularmetals.ModularMetalsCommon
import groovy.text.SimpleTemplateEngine
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.tier.ModularTier
import dev.lukebemish.modularmetals.data.variant.tool.ToolVariant
import groovy.transform.CompileStatic
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.util.function.Supplier

@CompileStatic
class TemplateEngine {
    public static final CompilerConfiguration COMPILER_CONFIGURATION = new CompilerConfiguration().tap {
        it.addCompilationCustomizers(new ImportCustomizer()
            .addStaticStars('dev.lukebemish.modularmetals.template.TemplateEngine$Utils')
            .addImports('java.util.Optional'))
    }
    public static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(TemplateEngine.classLoader, COMPILER_CONFIGURATION))
    public static final String CODE_KEY = '__code__'
    public static final String OPTIONAL_KEY = '__optional__'
    public static final String IF_KEY = '__if__'

    private TemplateEngine() {}

    @SuppressWarnings('unused')
    static class Utils {
        private Utils() {}

        /**
         * Returns the tier tag for the metal at a given location.
         */
        static ResourceLocation tierTag(ResourceLocation metalLocation) {
            Metal metal = ModularMetalsCommon.config.metals.get(metalLocation)
            if (metal == null) {
                throw new IllegalArgumentException("No metal found for location ${metalLocation}")
            }
            ModularTier tier = ToolVariant.getTier(metal, metalLocation)
            return tier.getTag().location()
        }

        static ResourceLocation tierTag(String metalLocation) {
            return tierTag(resourceLocation(metalLocation))
        }

        /**
         * Creates a resource location from a string
         */
        static ResourceLocation resourceLocation(String location) {
            return new ResourceLocation(location)
        }
    }

    static Supplier<GroovyShell> makeShell(Map replacements) {
        var shell = Suppliers.memoize {->
            Binding binding = new Binding(replacements)
            new GroovyShell(TemplateEngine.classLoader, binding, COMPILER_CONFIGURATION)
        }
        return shell
    }

    static Map fillReplacements(Map map, Map replacements) {
        var shell = makeShell(replacements)
        if (map.containsKey(CODE_KEY))
            return map
        return (Map) fillReplacementsInner(map, shell, replacements)
    }

    static Object fillReplacementsInner(Map map, Supplier<GroovyShell> shell, Map replacements) {
        Object out = MapUtil.replaceInMapByTypeFull(map, {
            return shell.get().evaluate(it)
        })
        out = MapUtil.replaceIn(out, {
            var writer = new StringBuilderWriter()
            ENGINE.createTemplate(it).make(replacements).writeTo(writer)
            return writer.builder.toString()
        })
        return out
    }

    static Object fillReplacements(Object obj, Map replacements) {
        var shell = makeShell(replacements)
        if (obj instanceof Map) {
            return fillReplacementsInner((Map) obj, shell, replacements)
        }
        if (obj instanceof List) {
            List out = []
            for (Object item : (List) obj) {
                out.add(fillReplacements(item, replacements))
            }
            return out
        }
        if (obj instanceof String || obj instanceof GString) {
            var writer = new StringBuilderWriter()
            ENGINE.createTemplate(obj.toString()).make(replacements).writeTo(writer)
            return writer.builder.toString()
        }
        return obj
    }
}
