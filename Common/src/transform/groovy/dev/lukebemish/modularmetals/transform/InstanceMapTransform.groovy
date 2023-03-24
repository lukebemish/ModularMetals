package dev.lukebemish.modularmetals.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching

@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class InstanceMapTransform extends AbstractASTTransformation {
    static final ClassNode MY_TYPE = makeWithoutCaching(InstanceMap)
    static final ClassNode FIELD_TYPE = makeWithoutCaching('com.google.common.collect.BiMap')
    static final ClassNode IMMUTABLE_TYPE = makeWithoutCaching('com.google.common.collect.ImmutableBiMap')
    static final ClassNode KEY_TYPE = makeWithoutCaching(String)

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)
        AnnotatedNode parent = (AnnotatedNode) nodes[1]
        AnnotationNode anno = (AnnotationNode) nodes[0]
        if (MY_TYPE != anno.getClassNode()) return

        if (parent instanceof FieldNode) {
            var type = parent.type
            if (!type.usingGenerics)
                throw new RuntimeException("InstanceMap field must be parameterized")
            if (type.redirect() != FIELD_TYPE)
                throw new RuntimeException("InstanceMap field must be of type ${FIELD_TYPE.name}")
            if (type.genericsTypes[0].type != KEY_TYPE)
                throw new RuntimeException("InstanceMap field must have a String key")
            var value = type.genericsTypes[1].type
            var targetFields = value.fields.findAll {
                it.static && it.type == value
            }
            List<MapEntryExpression> mapEntryExpressions = []
            for (FieldNode field : targetFields) {
                var key = field.name.toLowerCase(Locale.ROOT)
                mapEntryExpressions << new MapEntryExpression(
                    new ConstantExpression(key),
                    new FieldExpression(field)
                )
            }
            var map = new MapExpression(mapEntryExpressions)
            var immutable = new StaticMethodCallExpression(IMMUTABLE_TYPE, 'copyOf', new ArgumentListExpression(List.<Expression>of(map)))
            parent.initialValueExpression = immutable
        }
    }
}
