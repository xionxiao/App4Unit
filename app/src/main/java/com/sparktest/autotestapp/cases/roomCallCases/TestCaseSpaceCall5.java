package com.sparktest.autotestapp.cases.roomCallCases;

import android.os.Handler;

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
 * Created by panzh on 21/12/2017.
 */

public class TestCaseSpaceCall5 extends TestSuite {

    /**
     step 1:P1(device1,device2),P2 call the same room \n
     step 2: P1(device1) left \n
     step 3:P2 left \n
     step 4: P1(device1) call again \n
     step 5: P1(device2) left \n
     step 6: P1(device1) left
     */

    public TestCaseSpaceCall5() {
        this.add(TestCaseSpaceCall5.TestActorCall5Person1.class);
        this.add(TestCaseSpaceCall5.TestActorCall5Person2.class);
        this.add(TestCaseSpaceCall5.TestActorCall5Person3.class);
    }

    @Description("TestActorCall5Person1")
    public static class TestActorCall5Person1 extends RoomCallingTestActor{

        private boolean leftOnce = false;
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }


        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            mHandler.postDelayed(() -> {
                if (this.leftOnce) {
                    call.hangup(result -> {
                        Ln.w("call hangup");
                        Verify.verifyTrue(result.isSuccessful());
                    });
                }
                else {
                    hangupCall(call);
                }
            }, 5000);
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            CallObserver.CallMembershipChangedEvent membershipEvent = (CallObserver.CallMembershipChangedEvent)event;
            Ln.w("=========Received "+membershipEvent.getClass().getName()+" Event:"+membershipEvent.getCallMembership().getEmail());
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            if (this.leftOnce) {
                actor.logout();
            }
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);
        }

        @Override
        protected void hangupCall(Call call) {
            call.hangup(result -> {
                Ln.w("call hangup");
                Verify.verifyTrue(result.isSuccessful());
                mHandler.postDelayed(() -> {
                    this.leftOnce = true;
                    actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                }, 10000);
            });
        }
    }

    @Description("TestActorCall5Person2")
    public static class TestActorCall5Person2 extends RoomCallingTestActor {
        Handler mHandler = new Handler();
        @Test
        /**
         * Main test entrance
         */
        public void run() {
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser1,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
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
            }, 30000);
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            CallObserver.CallMembershipChangedEvent membershipEvent = (CallObserver.CallMembershipChangedEvent)event;
            Ln.w("=========Received "+membershipEvent.getClass().getName()+" Event:"+membershipEvent.getCallMembership().getEmail());
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);
        }
    }

    @Description("TestActorCall5Person3")
    public static class TestActorCall5Person3 extends RoomCallingTestActor {
        /**
         * Main test entrance
         */
        @Test
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser2,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         *  Dial room when register complete
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
            }, 5000);
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            CallObserver.CallMembershipChangedEvent membershipEvent = (CallObserver.CallMembershipChangedEvent)event;
            Ln.w("=========Received "+membershipEvent.getClass().getName()+" Event:"+membershipEvent.getCallMembership().getEmail());
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);
        }
    }
}
