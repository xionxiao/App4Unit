package com.sparktest.autotesteapp.cases.roomCallCases;

import android.os.Handler;
import android.support.annotation.NonNull;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallMembership;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.Verify;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

import javax.inject.Inject;

/**
 * Created by panzh on 21/12/2017.
 */

@Description("RoomCallingTestActor")
public class RoomCallingTestActor {
    @Inject
    TestActivity activity;

    @Inject
    AppTestRunner runner;

    TestActor actor;

    protected boolean person1Joined = false;
    protected boolean person2Joined = false;
    protected boolean person3Joined = false;

    Handler mHandler = new Handler();

    /**
     * Main test entrance
     */
    @Test
    public void run() {
    }

    /**
     *  Dial jwtUser1 when register complete
     */
    protected void onRegistered(Result result) {

    }

    /**
     * resume after call disconnected
     */
    protected void onCallSetup(Result<Call> result) {
        Ln.w("Caller onCallSetup result: %b" , result.isSuccessful());
        if (result.isSuccessful()) {
            actor.onConnected(this::onConnected);
            actor.onMediaChanged(this::onMediaChanged);
            actor.onCallMembershipChanged(this::onCallMembershipChanged);
            actor.onDisconnected(this::onDisconnected);
            actor.setDefaultCallObserver(result.getData());
            actor.onRinging(this::onRinging);
        } else {
            Verify.verifyTrue(false);
            actor.logout();
        }
    }

    protected void onRinging(Call call) {
        Ln.w("Caller, onRinging");
    }

    protected void onConnected(Call call){
        Ln.w("Caller, onConnected");
        checkMemberships(call);
    }

    protected void onMediaChanged(CallObserver.CallEvent event){
        Ln.w("Caller onMediaChanged: " + event.toString());
    }

    protected void onCallMembershipChanged(CallObserver.CallEvent event){
        Ln.w("Caller onCallMembershipChanged: " + event.toString());
        if (event instanceof CallObserver.MembershipJoinedEvent) {
            if (((CallObserver.MembershipJoinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID1)) {
                Ln.w("onCallMembershipChanged: person1 Joined");
                this.person1Joined = true;
            } else if (((CallObserver.MembershipJoinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID2)) {
                Ln.w("onCallMembershipChanged: person2 Joined");
                this.person2Joined = true;
            } else if (((CallObserver.MembershipJoinedEvent) event).getCallMembership().getPersonId().equalsIgnoreCase(actor.sparkUserID3)) {
                Ln.w("onCallMembershipChanged: person3 Joined");
                this.person3Joined = true;
            }
        }
    }

    protected void onDisconnected(CallObserver.CallEvent event) {
        Ln.w("Caller onDisconnected: " + event.toString());
        Verify.verifyTrue(event instanceof CallObserver.LocalLeft);
        Verify.verifyTrue(event.getCall().getStatus() == Call.CallStatus.DISCONNECTED);
    }

    protected void checkMemberships(Call call) {
        Ln.w("Caller checkMemberships: caller -> "+call.getFrom().getEmail());
        for(CallMembership membership:call.getMemberships()) {
            if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID1) && membership.getState() == CallMembership.State.JOINED) {
                Ln.w("checkMemberships: person1 Joined");
                this.person1Joined = true;
            } else if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID2) && membership.getState() == CallMembership.State.JOINED) {
                Ln.w("checkMemberships: person2 Joined");
                this.person2Joined = true;
            } else if (membership.getPersonId().equalsIgnoreCase(actor.sparkUserID3) && membership.getState() == CallMembership.State.JOINED) {
                Ln.w("checkMemberships: person3 Joined");
                this.person3Joined = true;
            }
        }
    }

    protected void hangupCall(Call call) {
        Ln.w("hangupCall in");
        if (call.getStatus() == Call.CallStatus.CONNECTED) {
            Ln.w("hangupCall hang up connected call!");
            call.hangup(result -> {
                Ln.w("call hangup finish");
                Verify.verifyTrue(result.isSuccessful());
            });
        }
        Ln.w("hangupCall out");
    }


}