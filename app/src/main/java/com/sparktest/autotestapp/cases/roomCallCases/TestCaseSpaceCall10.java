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
 * Created by panzh on 26/12/2017.
 */

public class TestCaseSpaceCall10 extends TestSuite {

    /**
     step 1:P1 call space \n
     step 2: P2 call P1 \n
     step 3:P1 answer P2 \n
     */

    public TestCaseSpaceCall10() {
        this.add(TestCaseSpaceCall10.TestActorCall10Person1.class);
        this.add(TestCaseSpaceCall10.TestActorCall10Person2.class);
    }

    @Description("TestActorCall10Person1")
    public static class TestActorCall10Person1 extends RoomCallingTestActor{
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

                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface), new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(Result<Void> result) {
                            if (result.isSuccessful()) {
                                Ln.w("Call:Except Answer call fail but success");
                                Verify.verifyTrue(false);
                                if(roomCall != null) {
                                    hangupCall(roomCall);
                                }
                            }
                            else {
                                Ln.w("Call: Answer call failed as except");
                                Verify.verifyTrue(true);
                                if(roomCall != null) {
                                    hangupCall(roomCall);
                                }
                            }
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
        protected void hangupCall(Call call) {
            call.hangup(result -> {
                Ln.w("call hangup");
                Verify.verifyTrue(result.isSuccessful());
                actor.logout();
            });
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
    }

    @Description("TestActorCall10Person2")
    public static class TestActorCall10Person2 extends RoomCallingTestActor {
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
                mHandler.postDelayed(()->{
                    actor.getPhone().dial(actor.sparkUser1,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
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
                    result.getData().hangup(r -> {
                        Ln.w("call hangup finish");
                        Verify.verifyTrue(r.isSuccessful());
                    });
                },15000);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            Verify.verifyTrue(event instanceof CallObserver.LocalCancel);
            actor.logout();
        }

    }
}