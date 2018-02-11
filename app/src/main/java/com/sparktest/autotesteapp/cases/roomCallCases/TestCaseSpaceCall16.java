package com.sparktest.autotesteapp.cases.roomCallCases;

import com.ciscospark.androidsdk.CompletionHandler;
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
 * Created by panzh on 22/12/2017.
 */

public class TestCaseSpaceCall16 extends TestSuite {
    /**
     step 1: P1 call space \n
     step 2: P2 call P3 answer/reject/hangup \n
     step 3:P2 call space again \n
     */

    public TestCaseSpaceCall16() {
        this.add(TestCaseSpaceCall16.TestActorCall16Person1.class);
        this.add(TestCaseSpaceCall16.TestActorCall16Person2.class);
        this.add(TestCaseSpaceCall16.TestActorCall16Person3.class);
    }

    @Description("TestActorCall16Person1")
    public static class TestActorCall16Person1 extends RoomCallingTestActor{
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
                actor.logout();
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipJoinedEvent
                    && ((CallObserver.MembershipJoinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID2)) {
                Ln.d("Call: Person2 Join Detected");
                hangupCall(event.getCall());
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

    }

    @Description("TestActorCall16Person2")
    public static class TestActorCall16Person2 extends RoomCallingTestActor{
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
            Ln.d("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.sparkUser3, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
                actor.logout();
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            if (event instanceof CallObserver.RemoteDecline) {
                mHandler.postDelayed(() -> {
                    actor.getPhone().dial(actor.sparkUser3, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                }, 3000);
            } else if(event instanceof CallObserver.RemoteLeft) {
                mHandler.postDelayed(() -> {
                    actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                },3000);
            } else {
                super.onDisconnected(event);
                actor.logout();
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event) {
            super.onCallMembershipChanged(event);
            if(event instanceof CallObserver.MembershipLeftEvent
                    && ((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)){
                hangupCall(event.getCall());
            }
        }
    }

    public static class TestActorCall16Person3 extends RoomCallingTestActor{
        private boolean rejectOnce = false;
        /**
         * Main test entrance
         */
        @Test
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser3,TestActor.SPARK_USER_PASSWORD,this::onRegistered);
        }

        /**
         *  Dial room when register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.d("Caller onRegistered result: %b" , result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.e("Incoming call");
                    actor.onConnected(this::onConnected);
                    actor.onMediaChanged(this::onMediaChanged);
                    actor.onCallMembershipChanged(this::onCallMembershipChanged);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    if (!rejectOnce) {
                        call.reject(new CompletionHandler<Void>() {
                            @Override
                            public void onComplete(Result<Void> result) {
                                if (result.isSuccessful()) {
                                    Ln.d("Call: rejected call");
                                    Verify.verifyTrue(true);
                                } else {
                                    Ln.d("Call: rejected call fail");
                                    Verify.verifyTrue(false);
                                    actor.logout();
                                }
                            }
                        });
                        rejectOnce = true;
                    } else {
                        call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface), new CompletionHandler<Void>() {
                            @Override
                            public void onComplete(Result<Void> result) {
                                if (result.isSuccessful()) {
                                    Ln.d("Call: Incoming call Detected");
                                    Verify.verifyTrue(true);
                                } else {
                                    Ln.d("Call: Answer call fail");
                                    Verify.verifyTrue(false);
                                    actor.logout();
                                }
                            }
                        });
                    }
                });
            } else {
                Verify.verifyTrue(false);
                actor.logout();
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
            if (event instanceof CallObserver.LocalLeft) {
                super.onDisconnected(event);
                actor.logout();
            }
        }
    }

}
