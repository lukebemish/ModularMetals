package io.github.lukebemish.modularmetals

import blue.endless.jankson.Jankson
import com.electronwill.nightconfig.toml.TomlFormat
import com.electronwill.nightconfig.toml.TomlParser
import com.electronwill.nightconfig.toml.TomlWriter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@CompileStatic
class Constants {
    public static final String MOD_ID = "modularmetals"
    public static final String MOD_NAME = "Modular Metals"
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME)
    public static final Jankson JANKSON = Jankson.builder().build()
    public static final TomlParser TOML_PARSER = TomlFormat.instance().createParser()
    public static final TomlWriter TOML_WRITER = TomlFormat.instance().createWriter()
    public static final Gson GSON = new GsonBuilder().setLenient().setPrettyPrinting().create()

    // For GroovyShell stuff
    public static final ImportCustomizer MAP_ACCESS_IMPORT_CUSTOMIZER = new ImportCustomizer().addImports('java.lang.Math')
    public static final SecureASTCustomizer MAP_ACCESS_AST_CUSTOMIZER = new SecureASTCustomizer().tap {
        closuresAllowed = false
        methodDefinitionAllowed = false

        allowedImports = ['java.lang.Math']
        allowedStaticImports = []
        allowedStaticStarImports = []
        disallowedExpressions = [
                ClassExpression,
                StaticMethodCallExpression,
                MethodReferenceExpression
        ]
        addExpressionCheckers(new SecureASTCustomizer.ExpressionChecker() {
            @Override
            boolean isAuthorized(Expression expression) {
                if (expression instanceof MethodCallExpression) {
                    Expression object = expression.objectExpression
                    return expression.methodAsString == 'print' && object instanceof VariableExpression && object.name == 'out'
                } else if (expression instanceof PropertyExpression) {
                    if (expression.static)
                        return false
                }
                return true
            }
        })
    }
}
