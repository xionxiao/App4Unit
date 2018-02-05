package com.sparktest.autotesteapp.cases.roomCallCases;

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
 * Created by panzh on 18/12/2017.
 */

public class TestCaseSpaceCall1 extends TestSuite {

    /**
     * step 1:P1,P2,P3 call the same room \n
     * step 2: P2 left \n
     * step 3:P3 left \n
     * step 4: P1 left
     */
    public TestCaseSpaceCall1() {
        this.add(TestCaseSpaceCall1.TestActorCall1Person1.class);
        this.add(TestCaseSpaceCall1.TestActorCall1Person2.class);
        this.add(TestCaseSpaceCall1.TestActorCall1Person3.class);
    }

    @Description("TestActorCall1Person1")
    public static class TestActorCall1Person1 extends RoomCallingTestActor{
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipLeftEvent) {
                if (((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID3)
                        && this.person2Joined && this.person3Joined ) {
                    Ln.d("Call: Person3 Left Detected");
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
            for(CallMembership membership:call.getMemberships()) {
                if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID3) && membership.getState() == CallMembership.State.LEFT) {
                    hangupCall(call);
                }
            }
        }
    }

    @Description("TestActorCall1Person2")
    public static class TestActorCall1Person2 extends RoomCallingTestActor {

        @Test
        /**
         * Main test entrance
         */
        public void run() {
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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onConnected(Call call){
            super.onConnected(call);
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipJoinedEvent) {
                if (this.person1Joined && this.person3Joined ) {
                    Ln.d("Call: Person2 Start Leaving");
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
            if (this.person1Joined && this.person3Joined) {
                Ln.d("Call: Person2 Start Leaving");
                hangupCall(call);
            }
        }
    }

    @Description("TestActorCall1Person3")
    public static class TestActorCall1Person3 extends RoomCallingTestActor {

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
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if (event instanceof CallObserver.MembershipLeftEvent) {
                if (((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID2)
                        && this.person1Joined && this.person2Joined ) {
                    Ln.d("Call: Person2 Left Detected");
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
            for(CallMembership membership:call.getMemberships()) {
                if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID2) && membership.getState() == CallMembership.State.LEFT) {
                    hangupCall(call);
                }
            }
        }
    }
}
