package com.sparktest.autotesteapp;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.github.benoitdion.ln.Ln;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dagger.ObjectGraph;

import static com.sparktest.autotesteapp.framework.TestState.Failed;
import static com.sparktest.autotesteapp.framework.TestState.Running;
import static com.sparktest.autotesteapp.framework.TestState.Success;


public class AppTestRunner extends TestRunner {
    private final TestActivity activity;
    private ObjectGraph injector;
    private Handler handler;
    private HandlerThread handlerThread;
    private AtomicInteger atomic = new AtomicInteger(0);
    private static int index = 0;


    public AppTestRunner(Context context) {
        this.activity = (TestActivity) context;
        Thread.setDefaultUncaughtExceptionHandler(this::handleUncaughtException);
        handlerThread = new HandlerThread("[TestRunnerThread]");
        handlerThread.start();
        handler = new MyHandler(handlerThread.getLooper());
        printPermits();
    }

    public void printPermits(String head) {
        //Ln.e(head +" =: " + semaphore.availablePermits());
        Ln.e(head + " =: " + atomic.get());
    }

    public void printPermits() {
        printPermits("");
    }

    public synchronized void await() {
        Ln.e("await [" + Thread.currentThread().getName() + "] " + atomic.incrementAndGet());
    }

    public synchronized void resume(int index) {
        Ln.e("resume [" + Thread.currentThread().getName() + "] (" + index + ") " + atomic.decrementAndGet());
        if (atomic.get() <= 0) {
            Ln.e("continue [" + Thread.currentThread().getName() + "]");
            notify();
        }
    }

    private synchronized void pause(int index) {
        Ln.e("pause (" + index + ") " + atomic.get());

        if (atomic.get() > 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Ln.e("pause resume [" + Thread.currentThread().getName() + "] (" + index + ") " + atomic.get());
    }

    public void setInjector(ObjectGraph injector) {
        this.injector = injector;
    }

    protected void run(TestCase testCase, TestResult result) {
        // Runner Thread
        handler.post(() -> runInThread(testCase, result));
    }

    private void runInThread(TestCase testCase, TestResult result) {
        result.addListener(new AppListener());
        testCase.setState(Running);
        Class<?> metaClass = testCase.getTestClass();

        // Run Started
        activity.runOnUiThread(() -> result.fireTestRunStarted());

        // BeforeClass
        executeStaticAnnotatedMethod(metaClass, BeforeClass.class);

        runTestMethod(createInstance(metaClass), result);

        // AfterClass
        executeStaticAnnotatedMethod(metaClass, AfterClass.class);

        // Run Finished
        if (testCase.getState() != Failed) testCase.setState(Success);
        activity.runOnUiThread(() -> result.fireTestRunFinished());
    }

    protected void runTestMethod(Object testInstance, TestResult result) {
        List<Method> methodList = getAnnotationMethods(testInstance.getClass(), Test.class);
        for (Method method : methodList) {
            // Before
            executeAnnotatedMethod(testInstance, Before.class);
            // Test
            activity.runOnUiThread(() -> result.fireTestStarted());
            invokeMethod(testInstance, method);

            activity.runOnUiThread(() -> result.fireTestFinished());
            // After
            executeAnnotatedMethod(testInstance, After.class);
        }
    }

    private void invokeMethod(Object object, Method method) {
        Ln.e(">>> invoke : " + method.getName() + " <<<");
        await();
        activity.runOnUiThread(() -> {
            try {
                method.invoke(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.getCause().printStackTrace();
                //TODO: notify failure of the test
            } finally {
                resume(index);
            }
        });
        pause(index);
        index++;
    }

    protected Object createInstance(Class<?> metaClass) {
        final Object[] testCase = new Object[1];
        await();
        activity.runOnUiThread(() -> {
            try {
                testCase[0] = metaClass.newInstance();
                injector.inject(testCase[0]);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } finally {
                resume(index);
            }
        });
        pause(index);
        index++;
        return testCase[0];
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

    private void executeStaticAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> T) {
        List<Method> methods = getAnnotationMethods(clazz, T,
                method -> Modifier.isStatic(method.getModifiers()));
        for (Method method : methods) {
            invokeMethod(null, method);
        }
    }

    private void executeAnnotatedMethod(Object object, Class<? extends Annotation> T) {
        List<Method> methods = getAnnotationMethods(object.getClass(), T);
        for (Method method : methods) {
            invokeMethod(object, method);
        }
    }

    private void handleUncaughtException(Thread thread, Throwable e) {
        Ln.e("============= Exit =============");
        e.getCause().printStackTrace();
        Ln.e(e);
        if (e instanceof AssertionError) {
            String stack = Arrays.toString(thread.getStackTrace());
            Ln.e("AssertionError");
            Ln.e(stack.toString());
            Looper.loop();
        }

        System.exit(1);
    }

    private class AppListener extends TestListener {
        @Override
        public void testStarted(com.sparktest.autotesteapp.framework.Test test) throws Exception {
            activity.update();
        }

        @Override
        public void testFailure() throws Exception {
            activity.runOnUiThread(() -> activity.update());
        }

        @Override
        public void testFinished(TestResult result) throws Exception {
            activity.runOnUiThread(() -> activity.update());
        }

        @Override
        public void testRunFinished(TestResult result) throws Exception {
            activity.runOnUiThread(() -> activity.update());
        }
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
        }
    }
}
