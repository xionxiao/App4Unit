package com.sparktest.autotesteapp;

import android.content.Context;

import com.sparktest.autotesteapp.cases.AnswerCallTest;
import com.sparktest.autotesteapp.cases.CallAnswerPairTest;
import com.sparktest.autotesteapp.cases.TestCaseAudioCall;
import com.sparktest.autotesteapp.cases.TestCaseAudioCallUnmuteVideo;
import com.sparktest.autotesteapp.cases.TestCaseCallRejectWhenInit;
import com.sparktest.autotesteapp.cases.TestCaseCallRejectWhenRinging;
import com.sparktest.autotesteapp.cases.TestCaseCallSequence_1;
import com.sparktest.autotesteapp.cases.TestCaseCallSequence_2;
import com.sparktest.autotesteapp.cases.TestCaseCallWhenConnected;
import com.sparktest.autotesteapp.cases.TestCaseCallWhenRinging;
import com.sparktest.autotesteapp.cases.DialTest;
import com.sparktest.autotesteapp.cases.GetVersionTest;
import com.sparktest.autotesteapp.cases.TestCaseHangUpDisconnectedCall;
import com.sparktest.autotesteapp.cases.TestCaseKeepCall;
import com.sparktest.autotesteapp.cases.TestCaseMultiParticipants_1;
import com.sparktest.autotesteapp.cases.TestCaseMultiParticipants_2;
import com.sparktest.autotesteapp.cases.TestCaseMuteAudioVideo;
import com.sparktest.autotesteapp.cases.TestCaseRoom;
import com.sparktest.autotesteapp.cases.TestCaseTeamAndMemberShip;
import com.sparktest.autotesteapp.cases.TestCaseWebhooks;
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
                TestCaseCallWhenRinging.Callee.class,
                TestCaseCallWhenRinging.Caller.class,
                TestCaseCallWhenConnected.Callee.class,
                TestCaseCallWhenConnected.Caller.class,
                TestCaseCallRejectWhenRinging.Callee.class,
                TestCaseCallRejectWhenRinging.Caller.class,
                TestCaseCallRejectWhenInit.Callee.class,
                TestCaseCallRejectWhenInit.Caller.class,
                TestCaseHangUpDisconnectedCall.Callee.class,
                TestCaseHangUpDisconnectedCall.Caller.class,
                TestCaseAudioCall.Callee.class,
                TestCaseAudioCall.Caller.class,
                TestCaseAudioCallUnmuteVideo.Callee.class,
                TestCaseAudioCallUnmuteVideo.Caller.class,
                TestCaseMuteAudioVideo.Callee.class,
                TestCaseMuteAudioVideo.Caller.class,
                TestCaseRoom.MessageSender.class,
                TestCaseRoom.FileSender.class,
                TestCaseWebhooks.WebHooker.class,
                TestCaseTeamAndMemberShip.TeamCreator.class,
                TestCaseMultiParticipants_1.Callee.class,
                TestCaseMultiParticipants_1.Caller1.class,
                TestCaseMultiParticipants_1.Caller2.class,
                TestCaseMultiParticipants_2.Callee.class,
                TestCaseMultiParticipants_2.Caller1.class,
                TestCaseMultiParticipants_2.Caller2.class,
                TestCaseCallSequence_1.Callee.class,
                TestCaseCallSequence_1.Caller.class,
                TestCaseCallSequence_2.Callee.class,
                TestCaseCallSequence_2.Caller.class,
                TestCaseKeepCall.Callee.class,
                TestCaseKeepCall.Caller.class,
                CallAnswerPairTest.Caller.class,
                CallAnswerPairTest.Recipient.class
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
