package com.nytimes.tools.partials.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.CONSTRUCTOR)
public @interface GeneratePartials {
    public static final String DECLARED_TYPE_PARAMETER_NAME = "declaredType";
    Class declaredType() default Void.class;
}
