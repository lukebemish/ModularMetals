package io.github.lukebemish.modularmetals

import com.google.common.base.Suppliers
import groovy.text.SimpleTemplateEngine
import io.github.lukebemish.modularmetals.data.Metal
import io.github.lukebemish.modularmetals.data.tier.ModularTier
import io.github.lukebemish.modularmetals.data.variant.ToolVariant
import io.github.lukebemish.modularmetals.util.MapUtil
import net.minecraft.resources.ResourceLocation
import org.apache.groovy.io.StringBuilderWriter
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.util.function.Supplier

class TemplateEngine {
    public static final CompilerConfiguration COMPILER_CONFIGURATION = new CompilerConfiguration().tap {
        it.addCompilationCustomizers(new ImportCustomizer()
            .addStaticStars('io.github.lukebemish.modularmetals.TemplateEngine$Utils'))
    }
    public static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(TemplateEngine.classLoader, COMPILER_CONFIGURATION))

    private TemplateEngine() {}

    @SuppressWarnings('unused')
    static class Utils {
        private Utils() {}

        static ResourceLocation tierTag(ResourceLocation metalLocation) {
            Metal metal = ModularMetalsCommon.config.metals.get(metalLocation)
            if (metal == null) {
                throw new IllegalArgumentException("No metal found for location ${metalLocation}")
            }
            ModularTier tier = ToolVariant.getTier(metal, metalLocation)
            return tier.getTag().location()
        }

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
        return fillReplacementsInner(map, shell, replacements)
    }

    static Map fillReplacementsInner(Map map, Supplier<GroovyShell> shell, Map replacements) {
        map = MapUtil.replaceInMapByType(map, {
            return shell.get().evaluate(it)
        })
        map = MapUtil.replaceInMap(map, {
            var writer = new StringBuilderWriter()
            ENGINE.createTemplate(it).make(replacements).writeTo(writer)
            return writer.builder.toString()
        })
        return map
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
