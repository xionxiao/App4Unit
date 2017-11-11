package com.sparktest.autotesteapp.framework;


public abstract class TestRunner {

    public TestResult run(TestSuite suite) {
        TestResult result = new TestResult();
        result.fireTestSuiteStarted();

        for (TestCase test : suite.cases()) {
            run(test, result);
        }

        //TODO: async wait for thread
        result.fireTestSuiteFinished();
        return result;
    }

    public TestResult run(Class<?> testClass) {
        return run(new TestCase(testClass));
    }

    public TestResult run(TestCase testCase) {
        TestResult result = new TestResult();
        run(testCase, result);
        return result;
    }

    protected void run(TestCase testCase, TestResult result) {

    }
}
