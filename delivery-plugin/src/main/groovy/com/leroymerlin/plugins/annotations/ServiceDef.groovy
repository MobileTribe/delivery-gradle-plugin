package com.leroymerlin.plugins.annotations

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Created by florian on 30/03/2017.
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@interface ServiceDef {
    /**
     * This provides description when generating docs.
     */
    public String desc() default "";
    /**
     * This provides params when generating docs.
     */
    public Param[] params();


}

@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@interface Param {
    public String name();
    public String desc() default "";

}