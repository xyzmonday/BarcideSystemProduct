package com.richfit.common_lib.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * 自定义一个限定符
 */
@Qualifier//限定符
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {
    String value() default "";//默认值为""
}