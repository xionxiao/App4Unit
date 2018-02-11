package com.sparktest.autotesteapp.cases.roomCallCases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallMembership;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.framework.TestSuite;
import com.sparktest.autotesteapp.framework.Verify;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

/**
 * Created by panzh on 27/12/2017.
 */

public class TestCaseSpaceCall20 extends TestSuite {
    /**
     step 1: P1 call room. \n
     step 2: P1 call another room \n
     */

    public TestCaseSpaceCall20() {
        this.add(TestCaseSpaceCall20.TestActorCall20Person1.class);
    }

    @Description("TestActorCall20Person1")
    public static class TestActorCall20Person1 extends RoomCallingTestActor{
        private Call firstCall = null;
        /**
         * Main test entrance
         */
        @Test
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser1,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         *  Dial room when register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            firstCall = call;
            actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    this::onSecondCallSetup);
        }

        protected void onSecondCallSetup(Result<Call> result) {
            Ln.w("Caller onCallSetup result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                Verify.verifyTrue("Created other call",false);
            } else {
                Verify.verifyTrue(true);
                hangupCall(firstCall);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

    }
}
