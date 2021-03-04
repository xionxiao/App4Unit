package com.sparktest.autotestapp.framework;

import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class TestResult {
    private static final String TAG = "TestResult";
    protected List<TestListener> listeners = new ArrayList<>();
    protected CopyOnWriteArrayList<TestFailure> failures = new CopyOnWriteArrayList<>();

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

    public int getRunCount() {
        return 0;
    }

    public int getFailureCount() {
        return failures.size();
    }

    public int getIgnoreCount() {
        return 0;
    }

    public List<TestFailure> getFailures() {
        return failures;
    }

    public long getRunTime() {
        return 0L;
    }

    public synchronized void addFailure(TestFailure failure) {
        this.failures.add(failure);
    }

    public boolean wasSuccessful() {
        return getFailureCount() == 0;
    }

    private void fire(Statement statement) {
        try {
            for (TestListener listener : listeners) {
                statement.evaluate(listener);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fireTestSuiteStarted() {
        Log.d(TAG, "fireTestSuiteStarted");
        fire(listener -> listener.testSuiteStarted(null));
    }

    public void fireTestSuiteFinished() {
        Log.d(TAG, "fireTestSuiteFinished");
        fire(listener -> listener.testSuiteFinished(null));
    }

    public void fireTestRunStarted() {
        Log.d(TAG, "fireTestRunStarted");
        fire(listener -> listener.testRunStarted(null));
    }

    public void fireTestRunFinished() {
        Log.d(TAG, "fireTestRunFinished");
        fire(listener -> listener.testRunFinished(null));
    }

    public void fireTestStarted() {
        Log.d(TAG, "fireTestStarted");
        fire(listener -> listener.testStarted(null));
    }

    public void fireTestFinished() {
        Log.e(TAG, "fireTestFinished");
        fire(listener -> listener.testFinished(null));
    }

    public void fireTestFailed() {
        Log.d(TAG, "fireTestFailed");
        fire(listener -> listener.testFailure(null));
    }

    public void fireTestIgnored() {
        Log.d(TAG, "fireTestIgnored");
        fire(listener -> listener.testRunFinished(null));
    }
}
