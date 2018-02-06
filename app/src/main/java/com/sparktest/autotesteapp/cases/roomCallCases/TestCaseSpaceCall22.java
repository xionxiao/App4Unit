package com.sparktest.autotesteapp.cases.roomCallCases;

import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
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

public class TestCaseSpaceCall22 extends TestSuite {
    /**
     step 1: P1 call room\n
     step 2: P1 call P2 \n
     step 3: P2 call P1 \n
     */

    public TestCaseSpaceCall22() {
        this.add(TestCaseSpaceCall22.TestActorCall22Person1.class);
        this.add(TestCaseSpaceCall22.TestActorCall22Person2.class);
    }


    @Description("TestActorCall22Person1")
    public static class TestActorCall22Person1 extends RoomCallingTestActor{
        private Call firstCall = null;
        private boolean personCallMade = false;
        private boolean personCallRejected = false;
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
            Ln.d("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);

                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.e("Incoming call");
                    actor.setDefaultCallObserver(call);
                    call.reject(new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(Result<Void> result) {
                            Verify.verifyTrue(result.isSuccessful());
                            personCallRejected = true;
                            if (personCallMade){
                                hangupCall(firstCall);
                            }
                        }
                    });
                });

            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            firstCall = call;
            mHandler.postDelayed(() -> {
                actor.getPhone().dial(actor.sparkUser2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onSecondCallSetup);
            },10000);

        }

        protected void onSecondCallSetup(Result<Call> result) {
            Ln.d("Caller onCallSetup result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                Verify.verifyTrue("Created other call",false);
            } else {
                Verify.verifyTrue(true);
            }
            personCallMade = true;
            if(personCallRejected){
                hangupCall(firstCall);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if (!personCallMade){
                Verify.verifyTrue(event instanceof CallObserver.LocalDecline);
                Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
            }else {
                super.onDisconnected(event);
            }
            if (event instanceof CallObserver.LocalLeft) {
                Verify.verifyTrue(personCallMade);
                Verify.verifyTrue(personCallRejected);
                actor.logout();
            }
        }

    }

    @Description("TestActorCall22Person2")
    public static class TestActorCall22Person2 extends RoomCallingTestActor {

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
            Ln.d("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.sparkUser1,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            Verify.verifyTrue(event instanceof CallObserver.RemoteDecline);
            Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
            if(event instanceof CallObserver.RemoteDecline){
                actor.logout();
            }
        }
    }
}
