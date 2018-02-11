package com.sparktest.autotestapp.cases.roomCallCases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

/**
 * Created by panzh on 27/12/2017.
 */

public class TestCaseSpaceCall21 extends TestSuite {

    /**
     step 1: P1 call room\n
     step 2:P2 call room \n
     step 3:P2 left \n
     step 4:  P1 call another room\n
     */

    public TestCaseSpaceCall21() {
        this.add(TestCaseSpaceCall21.TestActorCall21Person1.class);
        this.add(TestCaseSpaceCall21.TestActorCall21Person2.class);
    }


    @Description("TestActorCall21Person1")
    public static class TestActorCall21Person1 extends RoomCallingTestActor{
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
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event) {
            super.onCallMembershipChanged(event);
            if(event instanceof CallObserver.MembershipLeftEvent
                    && ((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID2)) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onSecondCallSetup);
            }
        }

        protected void onSecondCallSetup(Result<Call> result) {
            Ln.w("Caller onCallSetup result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                Verify.verifyTrue("Created other call",false);
            } else {
                Verify.verifyTrue(true);
            }
            hangupCall(firstCall);
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

    }

    @Description("TestActorCall21Person2")
    public static class TestActorCall21Person2 extends RoomCallingTestActor {

        @Test
        /**
         * Main test entrance
         */
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser2,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         *  Waiting for incoming call register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call){
            super.onConnected(call);
            mHandler.postDelayed(() -> {
                hangupCall(call);
            },5000);
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }
    }

}
