package com.sparktest.autotesteapp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.sparktest.autotesteapp.framework.TestFailure;
import com.sparktest.autotesteapp.framework.Verify;
import com.sparktest.autotesteapp.framework.TestCase;
import com.sparktest.autotesteapp.framework.TestListener;
import com.sparktest.autotesteapp.framework.TestResult;
import com.sparktest.autotesteapp.framework.TestRunner;
import com.sparktest.autotesteapp.framework.annotation.After;
import com.sparktest.autotesteapp.framework.annotation.AfterClass;
import com.sparktest.autotesteapp.framework.annotation.Before;
import com.sparktest.autotesteapp.framework.annotation.BeforeClass;
import com.sparktest.autotesteapp.framework.annotation.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.ObjectGraph;

import static com.sparktest.autotesteapp.framework.TestState.Failed;
import static com.sparktest.autotesteapp.framework.TestState.Running;
import static com.sparktest.autotesteapp.framework.TestState.Success;


public class AppTestRunner extends TestRunner {
    private final TestActivity activity;
    private ObjectGraph injector;
    private AtomicInteger atomic = new AtomicInteger(0);
    private TestCase runningTest;
    private HandlerThread runnerThread;
    private Handler handler;


    public AppTestRunner(Context context) {
        this.activity = (TestActivity) context;
        this.runnerThread = new HandlerThread("Runner Thread");
        runnerThread.start();
        handler = new Handler(runnerThread.getLooper());

        Verify.delegate(s -> {
            runningTest.setState(Failed);
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
        if (testCase.getState().equals(Failed)) {
            throw new Error();
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
        testCase.setState(Running);

        try {
            // Run Started
            activity.runOnUiThread(() -> result.fireTestRunStarted());

            // BeforeClass
            executeStaticAnnotatedMethod(testCase, BeforeClass.class);

            runTestMethod(testCase, result);

            // Run Finished
        } catch (Error e) {
            //TODO: finally handle all Exceptions here
            //e.printStackTrace();
        } finally {
            try {
                // AfterClass
                executeStaticAnnotatedMethod(testCase, AfterClass.class);
            } catch (Error e) {
                //e.printStackTrace();
            } finally {
                // Run Finished
                activity.runOnUiThread(() -> result.fireTestRunFinished());
                if (testCase.getState() != Failed) testCase.setState(Success);
            }
        }
    }

    protected void runTestMethod(TestCase testCase, TestResult result) {
        Object instance = createTestInstance(testCase);
        List<Method> methodList = getAnnotationMethods(testCase.getTestClass(), Test.class);
        for (Method method : methodList) {
            // Before
            executeAnnotatedMethod(testCase, instance, Before.class);

            // Test Start
            activity.runOnUiThread(() -> result.fireTestStarted());

            try {
                // Test
                invokeMethod(testCase, instance, method);
            } finally {

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
                    testCase.setState(Failed);
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
                testCase.setState(Failed);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                testCase.setState(Failed);
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

    interface MethodFilter {
        Boolean filter(Method method);
    }

    private static List<Method> getAnnotationMethods(Class<?> clazz, Class<? extends Annotation> T) {
        return getAnnotationMethods(clazz, T, method -> true);
    }

    private static List<Method> getAnnotationMethods(Class<?> clazz, Class<? extends Annotation> T,
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

    private class AppListener implements TestListener {
        @Override
        public void testStarted(com.sparktest.autotesteapp.framework.Test test) {
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
        public void testSuiteStarted(com.sparktest.autotesteapp.framework.Test test) {

        }

        @Override
        public void testSuiteFinished(TestResult result) {

        }

        @Override
        public void testRunStarted(com.sparktest.autotesteapp.framework.Test test) {

        }

        @Override
        public void testRunFinished(TestResult result) {
            activity.runOnUiThread(() -> activity.update());
        }
    }
}
