package com.sparktest.autotesteapp.framework.annotation;

import com.sparktest.autotesteapp.framework.TestResult;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A <em>Test</em> can be run and collect its results.
 *
 * @see TestResult
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Test {
    /**
     * Counts the number of test cases that will be run by this test.
     */
    int order() default 0;

    long timeout() default 0L;
}
