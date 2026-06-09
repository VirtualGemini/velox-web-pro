package com.velox.module.system.log.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {
    String module();
    String action();
    OperationType type() default OperationType.OTHER;
    boolean saveRequest() default true;
    boolean saveResponse() default false;
    String[] excludeParamNames() default {};
    String[] queryParamNames() default {};
    String targetType() default "";
    String targetIdExpression() default "";
}
