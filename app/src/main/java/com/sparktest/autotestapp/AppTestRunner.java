package com.sparktest.autotestapp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.framework.TestCase;
import com.sparktest.autotestapp.framework.TestFailure;
import com.sparktest.autotestapp.framework.TestListener;
import com.sparktest.autotestapp.framework.TestResult;
import com.sparktest.autotestapp.framework.TestRunner;
import com.sparktest.autotestapp.framework.TestState;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.After;
import com.sparktest.autotestapp.framework.annotation.AfterClass;
import com.sparktest.autotestapp.framework.annotation.Before;
import com.sparktest.autotestapp.framework.annotation.BeforeClass;
import com.sparktest.autotestapp.framework.annotation.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.ObjectGraph;


public class AppTestRunner extends TestRunner {
    private final TestActivity activity;
    private ObjectGraph injector;
    private AtomicInteger atomic = new AtomicInteger(0);
    private TestCase runningTest;
    private Handler handler;


    public AppTestRunner(Context context) {
        this.activity = (TestActivity) context;
        HandlerThread runnerThread = new HandlerThread("Runner Thread");
        runnerThread.start();
        handler = new Handler(runnerThread.getLooper());

        //TODO: Use Assume
        Verify.delegate(s -> {
            runningTest.setState(TestState.Failed);
            resume();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    public synchronized void await() {
        atomic.incrementAndGet();
    }

    public synchronized void resume() {
        atomic.decrementAndGet();
        if (atomic.get() <= 0) {
            atomic.set(0);
            notify();
        }
    }

    private synchronized void pause(TestCase testCase) {
        if (atomic.get() > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //TODO: re-throw exceptions if failed
        if (testCase.getState().equals(TestState.Failed)) {
            throw new Error("********** Failed ***********");
        }
    }

    public void setInjector(ObjectGraph injector) {
        this.injector = injector;
    }

    protected void run(TestCase testCase, TestResult result) {
        // Run in runner thread
        runningTest = testCase;
        handler.post(() -> runInHandlerThread(testCase, result));
    }

    private void runInHandlerThread(TestCase testCase, TestResult result) {
        result.addListener(new AppListener());
        testCase.setState(TestState.Running);

        try {
            // TODO: start calculate testcase class timeout
            // Run Started
            activity.runOnUiThread(() -> result.fireTestRunStarted());

            // BeforeClass
            executeStaticAnnotatedMethod(testCase, BeforeClass.class);

            runTestMethods(testCase, result);

            // Run Finished
        } catch (Error e) {
            //TODO: finally handle all Exceptions here
            e.printStackTrace();
        } finally {
            try {
                // AfterClass
                executeStaticAnnotatedMethod(testCase, AfterClass.class);
            } catch (Error e) {
                // TODO: unknown error
                //e.printStackTrace();
            } finally {
                // Run Finished
                activity.runOnUiThread(() -> result.fireTestRunFinished());
                if (testCase.getState() != TestState.Failed) testCase.setState(TestState.Success);
                // TODO: end calculate testcase timeout
            }
        }
    }

    protected void runTestMethods(TestCase testCase, TestResult result) {
        Object instance = createTestInstance(testCase);
        List<Method> methodList = getAnnotationMethods(testCase.getTestClass(), Test.class);

        Handler mainHandler = new Handler(activity.getMainLooper());
        Runnable timeoutRunnable = null;

        for (Method method : methodList) {
            // Before
            executeAnnotatedMethod(testCase, instance, Before.class);

            // Test Start
            activity.runOnUiThread(() -> result.fireTestStarted());

            try {
                // TODO: start calculate test method timeout
                // long timeout = getAnnotationTimeout(testCase.getTestClass(), method);
                long timeout = 120L * 1000;
                timeoutRunnable = () -> {
                    Ln.e("*****************timeout*********");
                    Verify.fail("Timeout");
                };
                if (timeout > 0) {
                    mainHandler.postDelayed( timeoutRunnable, timeout);
                }
                // Test
                invokeMethod(testCase, instance, method);
            } finally {

                // TODO: end calculate test method timeout
                mainHandler.removeCallbacks(timeoutRunnable);
                // Test Finished
                activity.runOnUiThread(() -> result.fireTestFinished());

                // After
                executeAnnotatedMethod(testCase, instance, After.class);
            }
        }
    }

    private void invokeMethod(TestCase testCase, Object instance, Method method) {
        runSyncOnUiThread(testCase, () -> {
            try {
                method.invoke(instance);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                //TODO: re-throw in pause()
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                if (e.getCause() instanceof AssertionError) {
                    //TODO: notify failure of the test
                    testCase.setState(TestState.Failed);
                }
            } finally {
                resume();
            }
        });
    }

    private Object createTestInstance(TestCase testCase) {
        final Object[] obj = {null};
        runSyncOnUiThread(testCase, () -> {
            try {
                obj[0] = testCase.getTestClass().newInstance();
                injector.inject(obj[0]);
            } catch (InstantiationException e) {
                e.printStackTrace();
                testCase.setState(TestState.Failed);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                testCase.setState(TestState.Failed);
                //TODO: notify failure
            } finally {
                resume();
            }
        });
        return obj[0];
    }

    private void runSyncOnUiThread(TestCase testCase, Runnable r) {
        await();
        activity.runOnUiThread(r);
        pause(testCase);
    }

    private void executeStaticAnnotatedMethod(TestCase testCase, Class<? extends Annotation> T) {
        List<Method> methods = getAnnotationMethods(testCase.getTestClass(), T,
                method -> Modifier.isStatic(method.getModifiers()));
        for (Method method : methods) {
            invokeMethod(testCase, null, method);
        }
    }

    private void executeAnnotatedMethod(TestCase testCase, Object testInstance, Class<? extends Annotation> T) {
        List<Method> methods = getAnnotationMethods(testCase.getTestClass(), T);
        for (Method method : methods) {
            invokeMethod(testCase, testInstance, method);
        }
    }

    private class AppListener implements TestListener {
        @Override
        public void testStarted(com.sparktest.autotestapp.framework.Test test) {
            activity.update();
        }

        @Override
        public void testFinished(TestResult result) {
            activity.runOnUiThread(() -> activity.update());
        }

        @Override
        public void testFailure(TestFailure failure) {
            activity.runOnUiThread(() -> activity.update());
        }

        @Override
        public void testAssumptionFailure(TestFailure failure) {

        }

        @Override
        public void testIgnored() throws Exception {

        }

        @Override
        public void testSuiteStarted(com.sparktest.autotestapp.framework.Test test) {

        }

        @Override
        public void testSuiteFinished(TestResult result) {

        }

        @Override
        public void testRunStarted(com.sparktest.autotestapp.framework.Test test) {

        }

        @Override
        public void testRunFinished(TestResult result) {
            activity.runOnUiThread(() -> activity.update());
        }
    }
}
