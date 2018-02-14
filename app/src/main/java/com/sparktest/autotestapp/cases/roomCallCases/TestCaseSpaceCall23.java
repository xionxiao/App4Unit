package com.sparktest.autotestapp.cases.roomCallCases;

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
 * Created by panzh on 27/12/2017.
 */

public class TestCaseSpaceCall23 extends TestSuite {
    /**
     step 1: P1 call room  \n
     step 2: P2 call P3 \n
     step 3:P3 call P2 \n
     */

    public TestCaseSpaceCall23() {
        this.add(TestCaseSpaceCall23.TestActorCall23Person1.class);
        this.add(TestCaseSpaceCall23.TestActorCall23Person2.class);
        this.add(TestCaseSpaceCall23.TestActorCall23Person3.class);
    }

    @Description("TestActorCall23Person1")
    public static class TestActorCall23Person1 extends RoomCallingTestActor{
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
            mHandler.postDelayed(() -> {
                hangupCall(call);
            },10000);

        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            if (event instanceof CallObserver.LocalLeft) {
                actor.logout();
            }
        }

    }

    @Description("TestActorCall23Person2")
    public static class TestActorCall23Person2 extends RoomCallingTestActor {

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
                actor.getPhone().dial(actor.sparkUser3,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            Verify.verifyTrue(event instanceof CallObserver.RemoteLeft);
            Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
            if(event instanceof CallObserver.RemoteLeft){
                actor.logout();
            }
        }
    }

    @Description("TestActorCall23Person3")
    public static class TestActorCall23Person3 extends RoomCallingTestActor {

        @Test
        /**
         * Main test entrance
         */
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser3,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         *  Waiting for incoming call register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.w("Callee IncomingCall");
                    call.acknowledge(c -> Ln.w("Callee acknowledge call"));
                    actor.onConnected(this::onConnected);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            r -> Ln.e("Callee answering call"));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            mHandler.postDelayed(() -> {
                hangupCall(call);
            },15000);
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
//            super.onDisconnected(event);
            if(event instanceof CallObserver.LocalLeft){
                actor.logout();
            }
        }
    }

}
