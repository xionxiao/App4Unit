package com.sparktest.autotestapp.framework;


public abstract class TestRunner {

    public TestResult run(TestSuite suite) {
        TestResult result = new TestResult();
        //TODO: async wait for thread
        result.fireTestSuiteStarted();

        for (Test test : suite.cases()) {
            run((TestCase)test, result);
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
            return run((TestCase)test);
        } else {
            return run((TestSuite)test);
        }
    }

    public TestResult run(Class<?> testClass) {
        return run(new TestCase(testClass));
    }

    protected void run(TestCase testCase, TestResult result) {

    }
}
