package com.sparktest.autotesteapp.cases.roomCallCases;

import android.os.Handler;
import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.membership.Membership;
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

import me.helloworld.utils.Checker;

/**
 * Created by panzh on 22/12/2017.
 */

public class TestCaseSpaceCall7 extends TestSuite {
    /**
     step 1:P1 call space(contain P1,P2) \n
     step 2: P2 call space \n
     step 3:P1 add P3 into this space \n
     step 4: P3 join\n
     step 5:  P3 leave \n
     step 6: P1 remove P3 \n
     step 7: P1, P2 left
     */

    public TestCaseSpaceCall7() {
        this.add(TestCaseSpaceCall7.TestActorCall7Person1.class);
        this.add(TestCaseSpaceCall7.TestActorCall7Person2.class);
        this.add(TestCaseSpaceCall7.TestActorCall7Person3.class);
    }

    @Description("TestActorCall7Person1")
    public static class TestActorCall7Person1 extends RoomCallingTestActor{
        private String personThreeMembershipID;
        private boolean personThreeInvited = false;
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
            Ln.d("Caller onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2, MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Override
        protected void onCallMembershipChanged(CallObserver.CallEvent event){
            super.onCallMembershipChanged(event);
            if(this.person2Joined){
                this.invitePersonThree();
            }
            if (event instanceof CallObserver.MembershipLeftEvent
                    && ((CallObserver.MembershipLeftEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID3)){
                remotePersonThreeFromRoom(event.getCall());
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
            if(this.person2Joined) {
                this.invitePersonThree();
            }
        }

        protected void invitePersonThree() {
            if (personThreeInvited) {
                return;
            }
            personThreeInvited = true;
            actor.getSpark().memberships().create(actor.SPARK_ROOM_CALL_ROOM_ID2, actor.sparkUserID3, null, false, new CompletionHandler<Membership>() {
                @Override
                public void onComplete(Result<Membership> result) {
                    Ln.d("onTeamMemberShipCreated: " + result.isSuccessful());
                    if (result.isSuccessful()) {
                        if (result.getData().getPersonId().equalsIgnoreCase(actor.sparkUserID3)) {
                            personThreeMembershipID = result.getData().getId();
                            personThreeInvited = true;
                        }
                    } else {
                        personThreeInvited = false;
                        Verify.verifyTrue(false);
                    }
                }
            });
        }

        protected void remotePersonThreeFromRoom(Call call) {
            if (!personThreeInvited || Checker.isEmpty(personThreeMembershipID)) {
                Verify.verifyTrue(false);
                hangupCall(call);
                return;
            }
            actor.getSpark().memberships().delete(personThreeMembershipID, new CompletionHandler<Void>() {
                @Override
                public void onComplete(Result<Void> result) {
                    if (result.isSuccessful()) {
                        hangupCall(call);
                    }
                    else {
                        remotePersonThreeFromRoom(call);
                    }

                }
            });
        }

    }

    @Description("TestActorCall7Person2")
    public static class TestActorCall7Person2 extends RoomCallingTestActor {

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
            Ln.d("Caller onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(actor.SPARK_ROOM_CALL_ROOM_ID2,MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
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
            for(CallMembership membership : call.getMemberships()) {
                if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID1)
                        && this.person1Joined
                        && membership.getState() == CallMembership.State.LEFT) {
                    Ln.d("Call: Person2 hangup");
                    hangupCall(call);
                }
            }
        }
    }

    @Description("TestActorCall7Person3")
    public static class TestActorCall7Person3 extends RoomCallingTestActor {

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
            Ln.d("Caller onRegistered result: " + result.isSuccessful());
            actor.getPhone().setIncomingCallListener(call -> {
                Ln.e("Incoming call");
                actor.onConnected(this::onConnected);
                actor.onMediaChanged(this::onMediaChanged);
                actor.onCallMembershipChanged(this::onCallMembershipChanged);
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(call);

                call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface), new CompletionHandler<Void>() {
                    @Override
                    public void onComplete(Result<Void> result) {
                        if (result.isSuccessful()) {
                            Ln.d("Call: Incoming call Detected");
                            Verify.verifyTrue(true);
                        }
                        else {
                            Ln.d("Call: Answer call fail");
                            Verify.verifyTrue(false);
                            actor.logout();
                        }
                    }
                });
            });
        }

        @Override
        protected void onDisconnected(CallObserver.CallEvent event) {
            super.onDisconnected(event);
            actor.logout();
        }

        @Override
        protected void onConnected(Call call) {
            super.onConnected(call);
            mHandler.postDelayed(()->{
                hangupCall(call);
            },5000);
        }
    }

}
