package com.sparktest.autotestapp;

import android.content.Context;

import com.sparktest.autotestapp.cases.TestTest;
import com.sparktest.autotestapp.framework.TestRunner;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                TestActivity.class,
                TestTest.class
        }
)
public class TestModule {
    Context context;
    AppTestRunner runner;

    public TestModule(Context context) {
        this.context = context;
        runner = new AppTestRunner(context);
    }

    @Provides
    @Singleton
    public TestRunner provideRunner() {
        return runner;
    }


    @Provides
    @Singleton
    public TestActivity provideActivity() {
        return (TestActivity) this.context;
    }
}
