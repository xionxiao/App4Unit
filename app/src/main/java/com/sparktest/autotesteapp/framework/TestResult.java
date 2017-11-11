package com.sparktest.autotesteapp.framework;


import com.github.benoitdion.ln.Ln;

import java.util.ArrayList;
import java.util.List;

public class TestResult {
    protected List<TestListener> listeners = new ArrayList<>();

    public TestResult() {
    }

    private interface Statement {
        void evaluate(TestListener listener) throws Exception;
    }

    public synchronized void addListener(TestListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(TestListener listener) {
        listeners.remove(listener);
    }

    public void fire(Statement statement) {
        try {
            for (TestListener listener : listeners) {
                statement.evaluate(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fireTestSuiteStarted() {
        Ln.e("fireTestSuiteStarted");
        fire(listener -> listener.testSuiteStarted(null));
    }

    public void fireTestSuiteFinished() {
        Ln.e("fireTestSuiteFinished");
        fire(listener -> listener.testSuiteFinished(null));
    }

    public void fireTestRunStarted() {
        Ln.e("fireTestRunStarted");
        fire(listener -> listener.testRunStarted(null));
    }

    public void fireTestRunFinished() {
        Ln.e("fireTestRunFinished");
        fire(listener -> listener.testRunFinished(null));
    }

    public void fireTestStarted() {
        Ln.e("fireTestStarted");
        fire(listener -> listener.testStarted(null));
    }

    public void fireTestFinished() {
        Ln.e("fireTestFinished");
        fire(listener -> listener.testFinished(null));
    }

    public void fireTestFailed() {
        Ln.e("fireTestFailed");
        fire(listener -> listener.testFailure());
    }

    public void fireTestIgnored() {
        Ln.e("fireTestIgnored");
        fire(listener -> listener.testRunFinished(null));
    }
}
