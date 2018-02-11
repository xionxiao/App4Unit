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
 * Created by panzh on 22/12/2017.
 */

public class TestCaseSpaceCall14 extends TestSuite {
    /**
     step 1: P2 call room contains P1 \n
     step 2: P1's device1 reject call \n
     step 3: P1's device2 answer/reject the call \n
     */

    public TestCaseSpaceCall14() {
        this.add(TestCaseSpaceCall14.TestActorCall14Person2.class);
        this.add(TestCaseSpaceCall14.TestActorCall14Person1Device1.class);
        this.add(TestCaseSpaceCall14.TestActorCall14Person1Device2.class);
    }

    @Description("TestActorCall14Person2")
    public static class TestActorCall14Person2 extends RoomCallingTestActor {
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
                mHandler.postDelayed(()-> {
                    actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                },8000);

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
                },10000);
            }
        }

    }

    @Description("TestActorCall14Person1Device1")
    public static class TestActorCall14Person1Device1 extends RoomCallingTestActor {
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
                                Ln.w("Call: rejected call result:", result.isSuccessful());

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
            Ln.w("Caller onDisconnected: " + event.toString());
            Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
            if(event instanceof CallObserver.LocalDecline && answerOnce == true) {
                actor.logout();
            } else if(event instanceof CallObserver.OtherDeclined && answerOnce == true) {
                actor.logout();
            }
        }
    }

    @Description("TestActorCall14Person1Device2")
    public static class TestActorCall14Person1Device2 extends RoomCallingTestActor {

        private boolean rejectedOnce = false;
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
                    call.reject(new CompletionHandler<Void>() {
                        @Override
                        public void onComplete(Result<Void> result) {
                            Ln.w("Caller reject: " + result.isSuccessful());
                        }
                    });
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            Ln.w("Caller onDisconnected: " + event.toString());
            Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
            if (rejectedOnce) {
                actor.logout();
            }
            else {
                rejectedOnce = true;
            }
        }
    }

}
