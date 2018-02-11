package com.sparktest.autotestapp.framework;


public interface TestListener {
    void testSuiteStarted(Test test);

    void testSuiteFinished(TestResult result);

    void testRunStarted(Test test);

    void testRunFinished(TestResult result);

    void testStarted(Test Test);

    void testFinished(TestResult result);

    void testFailure(TestFailure failure);

    void testAssumptionFailure(TestFailure failure);

    void testIgnored() throws Exception;
}
