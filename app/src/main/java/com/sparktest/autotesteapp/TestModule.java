package com.sparktest.autotesteapp;

import android.content.Context;

import com.sparktest.autotesteapp.cases.AnswerCallTest;
import com.sparktest.autotesteapp.cases.CallAnswerPairTest;
import com.sparktest.autotesteapp.cases.DialTest;
import com.sparktest.autotesteapp.cases.GetVersionTest;
import com.sparktest.autotesteapp.cases.TestTest;
import com.sparktest.autotesteapp.framework.TestRunner;
import com.sparktest.autotesteapp.utils.TestActor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                TestActivity.class,
                GetVersionTest.class,
                TestTest.class,
                DialTest.class,
                AnswerCallTest.class,
                TestActor.class,
                CallAnswerPairTest.Recipient.class,
                CallAnswerPairTest.Caller.class
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
    public AppTestRunner provideAppRunner() {
        return runner;
    }

    @Provides
    public Context provideContext() {
        return this.context;
    }

    @Provides
    @Singleton
    public TestActivity provideActivity() {
        return (TestActivity) this.context;
    }
}
