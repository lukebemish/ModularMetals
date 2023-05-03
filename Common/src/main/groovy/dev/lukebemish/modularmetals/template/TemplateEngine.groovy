package dev.lukebemish.modularmetals.template


import dev.lukebemish.modularmetals.ModularMetalsCommon
import dev.lukebemish.modularmetals.data.Metal
import dev.lukebemish.modularmetals.data.tier.ModularTier
import dev.lukebemish.modularmetals.data.variant.tool.ToolVariant
import dev.lukebemish.modularmetals.services.Services
import groovy.text.SimpleTemplateEngine
import groovy.transform.CompileStatic
import groovy.transform.Memoized
import groovy.transform.PackageScope
import net.minecraft.resources.ResourceLocation
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

@CompileStatic
class TemplateEngine {
    public static final CompilerConfiguration COMPILER_CONFIGURATION = new CompilerConfiguration().tap {
        Services.PLATFORM.customize(it)
        it.addCompilationCustomizers(new ImportCustomizer()
            .addStaticStars(Utils.name)
            .addImports(Optional.name))
    }
    public static final SimpleTemplateEngine ENGINE = new SimpleTemplateEngine(new GroovyShell(TemplateEngine.classLoader, COMPILER_CONFIGURATION))
    public static final String CODE_KEY = '__code__'
    public static final String OPTIONAL_KEY = '__optional__'
    public static final String IF_KEY = '__if__'
    public static final String ELSE_KEY = '__else__'
    public static final Set<String> RESERVED = Set.of(CODE_KEY, OPTIONAL_KEY, IF_KEY, ELSE_KEY)

    private TemplateEngine() {}

    @SuppressWarnings('unused')
    static class Utils {
        private Utils() {}

        /**
         * Returns the tier tag for the metal at a given location.
         */
        static ResourceLocation tierTag(String location) {
            ResourceLocation metalLocation = new ResourceLocation(location)
            Metal metal = ModularMetalsCommon.config.metals.get(metalLocation)
            if (metal == null) {
                throw new IllegalArgumentException("No metal found for location ${metalLocation}")
            }
            ModularTier tier = ToolVariant.getTier(metal, metalLocation)
            return tier.getTag().location()
        }
    }

    @PackageScope
    @Memoized static GroovyShell createShell(Map replacements) {
        Binding binding = new Binding(replacements)
        return new GroovyShell(TemplateEngine.classLoader, binding, COMPILER_CONFIGURATION)
    }
}
