package com.sparktest.autotestapp.framework;


import junit.framework.AssertionFailedError;

import java.io.PrintWriter;
import java.io.StringWriter;

public class TestFailure {
    protected Test failedTest;
    protected Throwable thrownException;

    public TestFailure(Test failedTest, Throwable thrownException) {
        this.failedTest = failedTest;
        this.thrownException = thrownException;
    }

    public Test getFailedTest() {
        return failedTest;
    }

    public Throwable getThrownException() {
        return thrownException;
    }

    @Override
    public String toString() {
        return failedTest + ":" + thrownException.getMessage();
    }

    public String getTrace() {
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        getThrownException().printStackTrace(printWriter);
        return printWriter.toString();
    }

    public String getMessage() {
        return getThrownException().getMessage();
    }

    public boolean isFailure() {
        return getThrownException() instanceof AssertionFailedError;
    }
}
