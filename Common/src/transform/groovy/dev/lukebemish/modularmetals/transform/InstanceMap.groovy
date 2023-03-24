package dev.lukebemish.modularmetals.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@CompileStatic
@Retention(RetentionPolicy.SOURCE)
@GroovyASTTransformationClass('dev.lukebemish.modularmetals.transform.InstanceMapTransform')
@Target(ElementType.FIELD)
@interface InstanceMap {

}
