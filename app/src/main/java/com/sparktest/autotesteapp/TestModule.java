package com.sparktest.autotesteapp;

import android.content.Context;

import com.sparktest.autotesteapp.cases.AnswerCallTest;
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
import com.sparktest.autotesteapp.cases.roomCallCases.*;
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
                //room call
                TestCaseSpaceCall1.TestActorCall1Person1.class,
                TestCaseSpaceCall1.TestActorCall1Person2.class,
                TestCaseSpaceCall1.TestActorCall1Person3.class,

                TestCaseSpaceCall2.TestActorCall2Person1.class,
                TestCaseSpaceCall2.TestActorCall2Person2.class,
                TestCaseSpaceCall2.TestActorCall2Person3.class,

                TestCaseSpaceCall3.TestActorCall3Person1.class,
                TestCaseSpaceCall3.TestActorCall3Person2.class,
                TestCaseSpaceCall3.TestActorCall3Person3.class,

                TestCaseSpaceCall4.TestActorCall4Person1.class,
                TestCaseSpaceCall4.TestActorCall4Person2.class,
                TestCaseSpaceCall4.TestActorCall4Person3.class,

                TestCaseSpaceCall5.TestActorCall5Person1.class,
                TestCaseSpaceCall5.TestActorCall5Person2.class,
                TestCaseSpaceCall5.TestActorCall5Person3.class,

                TestCaseSpaceCall6.TestActorCall6Person1.class,
                TestCaseSpaceCall6.TestActorCall6Person2.class,
                TestCaseSpaceCall6.TestActorCall6Person3.class,

                TestCaseSpaceCall7.TestActorCall7Person1.class,
                TestCaseSpaceCall7.TestActorCall7Person2.class,
                TestCaseSpaceCall7.TestActorCall7Person3.class,

                TestCaseSpaceCall8.TestActorCall8Person1.class,
                TestCaseSpaceCall8.TestActorCall8Person2.class,
                TestCaseSpaceCall8.TestActorCall8Person3.class,

                TestCaseSpaceCall9.TestActorCall9Person1.class,
                TestCaseSpaceCall9.TestActorCall9Person2.class,
                TestCaseSpaceCall9.TestActorCall9Person3.class,

                TestCaseSpaceCall10.TestActorCall10Person1.class,
                TestCaseSpaceCall10.TestActorCall10Person2.class,

                TestCaseSpaceCall11.TestActorCall11Person1.class,
                TestCaseSpaceCall11.TestActorCall11Person2Device1.class,
                TestCaseSpaceCall11.TestActorCall11Person2Device2.class,

                TestCaseSpaceCall12.TestActorCall12Device1.class,
                TestCaseSpaceCall12.TestActorCall12Device2.class,

                TestCaseSpaceCall13.TestActorCall13Person2.class,
                TestCaseSpaceCall13.TestActorCall13Person1Device1.class,
                TestCaseSpaceCall13.TestActorCall13Person1Device2.class,

                TestCaseSpaceCall14.TestActorCall14Person2.class,
                TestCaseSpaceCall14.TestActorCall14Person1Device1.class,
                TestCaseSpaceCall14.TestActorCall14Person1Device2.class,

                TestCaseSpaceCall15.TestActorCall15Person1.class,
                TestCaseSpaceCall15.TestActorCall15Person2.class,

                TestCaseSpaceCall16.TestActorCall16Person1.class,
                TestCaseSpaceCall16.TestActorCall16Person2.class,
                TestCaseSpaceCall16.TestActorCall16Person3.class,

                TestCaseSpaceCall17.TestActorCall17Person1.class,
                TestCaseSpaceCall17.TestActorCall17Person2.class,
                TestCaseSpaceCall17.TestActorCall17Person3.class,

                TestCaseSpaceCall18.TestActorCall18Person1.class,
                TestCaseSpaceCall18.TestActorCall18Person2.class,
                TestCaseSpaceCall18.TestActorCall18Person3.class,

                TestCaseSpaceCall19.TestActorCall19Person1.class,
                TestCaseSpaceCall19.TestActorCall19Person2.class,
                TestCaseSpaceCall19.TestActorCall19Person3.class,

                TestCaseSpaceCall20.TestActorCall20Person1.class,

                TestCaseSpaceCall21.TestActorCall21Person1.class,
                TestCaseSpaceCall21.TestActorCall21Person2.class,

                TestCaseSpaceCall22.TestActorCall22Person1.class,
                TestCaseSpaceCall22.TestActorCall22Person2.class,

                TestCaseSpaceCall23.TestActorCall23Person1.class,
                TestCaseSpaceCall23.TestActorCall23Person2.class,
                TestCaseSpaceCall23.TestActorCall23Person3.class,
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
