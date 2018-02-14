package com.sparktest.autotestapp.cases.roomCallCases;

import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

/**
 * Created by panzh on 22/12/2017.
 */

public class TestCaseSpaceCall15 extends TestSuite {
    /**
     step 1:P1 call space \n
     step 2: P2 call P1 \n
     step 3:P1 reject P2
     */

    public TestCaseSpaceCall15() {
        this.add(TestCaseSpaceCall15.TestActorCall15Person1.class);
        this.add(TestCaseSpaceCall15.TestActorCall15Person2.class);
    }

    @Description("TestActorCall15Person1")
    public static class TestActorCall15Person1 extends RoomCallingTestActor{
        private boolean roomCallComplated = false;
        private Call roomCall = null;
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);

                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.w("Incoming call");
                    actor.setDefaultCallObserver(call);

                    call.reject(new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(Result<Void> result) {
                            Verify.verifyTrue(result.isSuccessful());
                            hangupCall(roomCall);
                        }
                    });
                });

            } else {
                Verify.verifyTrue(false);
            }

        }

        @Override
        protected void onCallSetup(Result<Call> result) {
            super.onCallSetup(result);
            if (result.isSuccessful()) {
                roomCall = result.getData();
            }
            else {
                actor.logout();
            }
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            if(roomCallComplated) {
                Ln.w("Call: received onConnected second time");
                Verify.verifyTrue(false);
            } else {
                roomCallComplated = true;
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if(event instanceof CallObserver.LocalLeft) {
                actor.logout();
            }
        }
    }

    @Description("TestActorCall15Person2")
    public static class TestActorCall15Person2 extends RoomCallingTestActor {
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
                mHandler.postDelayed(()-> {
                    actor.getPhone().dial(actor.sparkUser1, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                },10000);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallSetup(Result<Call> result) {
            super.onCallSetup(result);
            if (result.isSuccessful()) {
                mHandler.postDelayed(()->{
                    hangupCall(result.getData());
                },10000);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if(event instanceof CallObserver.RemoteDecline) {
                actor.logout();
            }
        }
    }
}
