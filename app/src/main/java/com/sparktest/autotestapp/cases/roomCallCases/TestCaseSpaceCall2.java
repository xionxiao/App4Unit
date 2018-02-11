package com.sparktest.autotestapp.cases.roomCallCases;

import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallMembership;
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

public class TestCaseSpaceCall2 extends TestSuite {

    /**
     * step 1:P1,P2,P3 call the same room \n
     * step 2: P1 left \n
     * step 3:P2 left \n
     * step 4: P1 call again \n
     * step 5: P3 left \n
     * step 6: P1 left
     */
    public TestCaseSpaceCall2() {
        this.add(TestCaseSpaceCall2.TestActorCall2Person1.class);
        this.add(TestCaseSpaceCall2.TestActorCall2Person2.class);
        this.add(TestCaseSpaceCall2.TestActorCall2Person3.class);
    }

    @Description("TestActorCall2Person1")
    public static class TestActorCall2Person1 extends RoomCallingTestActor {
        private boolean leftOnce = false;
        Handler mHandler = new Handler();

        /**
         * Main test entrance
         */
        @Test
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            actor.loginBySparkId(TestActor.sparkUser1, TestActor.SPARK_USER_PASSWORD, this::onRegistered);
        }

        /**
         * Dial room when register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b", result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event) {
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipJoinedEvent && this.person2Joined && this.person3Joined && !this.leftOnce) {
                hangupCall(event.getCall());
            } else if (event instanceof CallObserver.MembershipLeftEvent) {
                if (((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID3)) {
                    Ln.w("Call: Person3 Left Detected");
                    hangupCall(event.getCall());
                }
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            if (this.leftOnce) {
                actor.logout();
            } else {
                this.leftOnce = true;
                mHandler.postDelayed(() -> {
                    actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                }, 5000);
            }
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);
            if (this.person2Joined && this.person3Joined && !this.leftOnce) {
                hangupCall(call);
            } else {
                for (CallMembership membership : call.getMemberships()) {
                    if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID3) && membership.getState() == CallMembership.State.LEFT) {
                        hangupCall(call);
                    }
                }
            }
        }

        @Override
        protected void hangupCall(Call call) {
            call.hangup(result -> {
                Ln.w("call hangup");
                Verify.verifyTrue(result.isSuccessful());

            });
        }
    }


    @Description("TestActorCall1Person2")
    public static class TestActorCall2Person2 extends RoomCallingTestActor {

        @Test
        /**
         * Main test entrance
         */
        public void run() {
            actor = TestActor.SparkUser(activity, runner);
            mHandler.postDelayed(() ->
                    actor.loginBySparkId(TestActor.sparkUser2, TestActor.SPARK_USER_PASSWORD, this::onRegistered), 1000);
        }

        /**
         * Waiting for incoming call register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b", result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event) {
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipLeftEvent) {
                if (((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                        && this.person1Joined && this.person3Joined) {
                    Ln.w("Call: Person2 Start Leaving");
                    hangupCall(event.getCall());
                }
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);
            for (CallMembership membership : call.getMemberships()) {
                if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID1) && membership.getState() == CallMembership.State.LEFT
                        && this.person1Joined && this.person3Joined) {
                    Ln.w("Call: Person2 Start Leaving");
                    hangupCall(call);
                }
            }
        }
    }

    @Description("TestActorCall1Person3")
    public static class TestActorCall2Person3 extends RoomCallingTestActor {
        private boolean person1LeftOnce = false;

        /**
         * Main test entrance
         */
        @Test
        public void run() {
            super.run();
            actor = TestActor.SparkUser(activity, runner);
            mHandler.postDelayed(() ->
                    actor.loginBySparkId(TestActor.sparkUser3, TestActor.SPARK_USER_PASSWORD, this::onRegistered), 2000);
        }

        /**
         * Dial room when register complete
         */
        @Override
        protected void onRegistered(Result result) {
            Ln.w("Caller onRegistered result: %b", result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event) {
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipJoinedEvent
                    && ((CallObserver.MembershipJoinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                    && this.person1LeftOnce) {
                hangupCall(event.getCall());
            } else if (event instanceof CallObserver.MembershipLeftEvent) {
                if (((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                        && !this.person1LeftOnce) {
                    Ln.w("Call: Person1 first Left Detected");
                    this.person1LeftOnce = true;
                }
            }
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

        @Override
        protected void checkMemberships(Call call) {
            super.checkMemberships(call);

            for (CallMembership membership : call.getMemberships()) {
                if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                        && membership.getState() == CallMembership.State.LEFT
                        && !this.person1LeftOnce) {
                    this.person1LeftOnce = true;
                } else if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                        && membership.getState() == CallMembership.State.JOINED
                        && this.person1LeftOnce) {
                    hangupCall(call);
                }
            }

        }
    }
}
