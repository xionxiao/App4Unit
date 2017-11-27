package com.sparktest.autotesteapp.cases;


import android.os.Handler;
import android.util.Log;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.TestSuite;
import com.sparktest.autotesteapp.framework.Verify;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

import javax.inject.Inject;

@Description("Call When Ringing")
public class TestCaseCallWhenRinging extends TestSuite {

    public TestCaseCallWhenRinging() {
        this.add(Callee.class);
        this.add(Caller.class);
    }

    @Description("Caller")
    public static class Caller {

        TestActor actor;

        /**
         * Main test entrance
         */
        @Test
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey2);
            actor.login(this::onRegistered);
        }

        /**
         *  Dial jwtUser1 when register complete
         */
        private void onRegistered(Result result) {
            Ln.d("Caller onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().dial(TestActor.jwtUser1,
                        MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        this::onCallSetup);
            } else {
                Verify.verifyTrue(false);
            }

        }

        /**
         * resume after call disconnected
         */
        private void onCallSetup(Result<Call> result) {
            Ln.d("Caller onCallSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.onRinging(this::onRinging);
                actor.onDisconnected(c -> actor.logout());
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onRinging(Call call){
            Ln.d("Caller onRinging, make 2nd call when ringing");
            actor.getPhone().dial(TestActor.jwtUser1,
                    MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    r -> {
                        Ln.d("Caller on2ndCallSetup result: " + r.isSuccessful());
                        Verify.verifyFalse(r.isSuccessful());
                        call.hangup(result -> Ln.e("Caller hangup"));
                    });
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }

    @Description("Callee")
    public static class Callee {

        TestActor actor;

        @Test
        /**
         * Main test entrance
         */
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey1);
            actor.login(this::onRegistered);
        }

        /**
         *  Waiting for incoming call register complete
         */
        private void onRegistered(Result result) {
            Ln.d("Callee onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getPhone().setIncomingCallListener(call -> {
                    Ln.d("Callee IncomingCall");
                    actor.onDisconnected(c -> actor.logout());
                    actor.setDefaultCallObserver(call);
                    call.acknowledge(c -> Ln.d("Callee acknowledge call"));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
