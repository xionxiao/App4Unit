package com.sparktest.autotesteapp.framework;


public abstract class Test {
    private TestState state = TestState.NotRun;

    public String getDescription() {
        return "";
    }

    public TestState getState() {
        return state;
    }

    public void setState(TestState state) {
        this.state = state;
    }

    public TestResult getResult() {
        return new TestResult();
    }
}
