package com.axelor.apps.base.service.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This interface is used on a controller method to send a {@link
 * com.axelor.apps.base.ResponseMessageType#ERROR} instead of a {@link
 * com.axelor.apps.base.ResponseMessageType#INFORMATION} to the view when an exception occurs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ErrorException {}
