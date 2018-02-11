package com.sparktest.autotesteapp.cases.roomCallCases;

import android.os.Handler;

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
 * Created by panzh on 22/12/2017.
 */

public class TestCaseSpaceCall13 extends TestSuite {
    /**
     step 1:  P2 call room contains P1 \n
     step 2: P1's device1 answer/reject call \n
     */

    public TestCaseSpaceCall13() {
        this.add(TestCaseSpaceCall13.TestActorCall13Person2.class);
        this.add(TestCaseSpaceCall13.TestActorCall13Person1Device1.class);
        this.add(TestCaseSpaceCall13.TestActorCall13Person1Device2.class);
    }

    @Description("TestActorCall13Person2")
    public static class TestActorCall13Person2 extends RoomCallingTestActor {
        private boolean calledOnce = false;
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipLeftEvent
                    && ((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)) {
                hangupCall(event.getCall());
            } else if (event instanceof CallObserver.MembershipDeclinedEvent
                    && ((CallObserver.MembershipDeclinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)) {
                hangupCall(event.getCall());
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            if (calledOnce) {
                actor.logout();
            }
            else {
                mHandler.postDelayed(()-> {
                    actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                    calledOnce = true;
                },3000);
            }
        }

    }

    @Description("TestActorCall13Person1Device1")
    public static class TestActorCall13Person1Device1 extends RoomCallingTestActor {
        private boolean answerOnce = false;
        @Test
        /**
         * Main test entrance
         */
        public void run() {
            super.run();
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
                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.w("Incoming call");
                    actor.onConnected(this::onConnected);
                    actor.onMediaChanged(this::onMediaChanged);
                    actor.onCallMembershipChanged(this::onCallMembershipChanged);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    if (answerOnce) {
                        call.reject(new CompletionHandler<Void>() {
                            @Override
                            public void onComplete(Result<Void> result) {
                                if (result.isSuccessful()) {
                                    Ln.w("Call: rejected call");
                                    Verify.verifyTrue(true);
                                } else {
                                    Ln.w("Call: rejected call fail");
                                    Verify.verifyTrue(false);
                                    actor.logout();
                                }
                            }
                        });
                    } else {
                        answerOnce = true;
                        call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface), new CompletionHandler<Void>() {
                            @Override
                            public void onComplete(Result<Void> result) {
                                if (result.isSuccessful()) {
                                    Ln.w("Call: Incoming call Detected");
                                    Verify.verifyTrue(true);
                                } else {
                                    Ln.w("Call: Answer call fail");
                                    Verify.verifyTrue(false);
                                    actor.logout();
                                }
                            }
                        });
                    }
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            mHandler.postDelayed(()->{
                hangupCall(call);
            },5000);
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if(event instanceof CallObserver.LocalDecline) {
                actor.logout();
            }
        }
    }

    @Description("TestActorCall13Person1Device2")
    public static class TestActorCall13Person1Device2 extends RoomCallingTestActor {

        private boolean otherConnected = false;
        private boolean otherDecline = false;

        @Test
        /**
         * Main test entrance
         */
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser1,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         * Waiting for incoming call register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.w("Incoming call");
                    actor.onConnected(this::onConnected);
                    actor.onMediaChanged(this::onMediaChanged);
                    actor.onCallMembershipChanged(this::onCallMembershipChanged);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if(event instanceof CallObserver.OtherConnected) {
                otherConnected = true;
            } else if (event instanceof CallObserver.OtherDeclined) {
                otherDecline = true;
                Verify.verifyTrue(otherConnected);
                actor.logout();
            }
        }
    }

}
