package com.sparktest.autotestapp.framework;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public abstract class TestRunner {

    protected static List<Method> getAnnotationMethods(Class<?> clazz, Class<? extends Annotation> T) {
        return getAnnotationMethods(clazz, T, method -> true);
    }

    protected static List<Method> getAnnotationMethods(Class<?> clazz, Class<? extends Annotation> T,
                                                       MethodFilter filter) {
        Method methods[] = clazz.getMethods();
        List<Method> methodList = new ArrayList<>();
        for (Method method : methods) {
            if (method.getAnnotation(T) != null && filter.filter(method)) {
                methodList.add(method);
            }
        }
        return methodList;
    }

    protected static long getAnnotationTimeout(Class<?> testClass, Method method) {
        List<Method> methodList = getAnnotationMethods(testClass, com.sparktest.autotestapp.framework.annotation.Test.class);
        for (Method m : methodList) {
            if (m.equals(method)) {
                Annotation annotation = m.getAnnotation(com.sparktest.autotestapp.framework.annotation.Test.class);
                return ((com.sparktest.autotestapp.framework.annotation.Test)annotation).timeout();
            }
        }
        return 0L;
    }

    public TestResult run(TestSuite suite) {
        TestResult result = new TestResult();
        //TODO: async wait for thread
        result.fireTestSuiteStarted();

        for (Test test : suite.cases()) {
            run((TestCase) test, result);
        }

        //TODO: async wait for thread
        result.fireTestSuiteFinished();
        return result;
    }

    public TestResult run(TestCase testCase) {
        TestResult result = new TestResult();
        run(testCase, result);
        return result;
    }

    public TestResult run(Test test) {
        if (test instanceof TestCase) {
            return run((TestCase) test);
        } else {
            return run((TestSuite) test);
        }
    }

    public TestResult run(Class<?> testClass) {
        return run(new TestCase(testClass));
    }

    protected void run(TestCase testCase, TestResult result) {

    }

    protected interface MethodFilter {
        Boolean filter(Method method);
    }
}
