package com.sparktest.autotesteapp.cases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
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

/**
 * Created by qimdeng on 11/15/17.
 */

@Description("Call Reject When Ringing")
public class TestCaseCallRejectWhenRinging extends TestSuite {

    public TestCaseCallRejectWhenRinging() {
        this.add(TestCaseCallRejectWhenRinging.Callee.class);
        this.add(TestCaseCallRejectWhenRinging.Caller.class);
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
                actor.onRinging(c -> Ln.d("Caller onRinging, acknowledge successed"));
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller onDisconnected: " + event);
            Verify.verifyTrue(event instanceof CallObserver.RemoteDecline);
            actor.logout();
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
                    //actor.onConnected(this::shutdown);
                    Ln.d("Callee IncomingCall");
                    actor.onRinging(this::onRinging);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    call.acknowledge(c -> Ln.d("Callee acknowledge call"));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onRinging(Call call){
            Ln.d("Callee onRinging");
            call.reject(result -> Verify.verifyTrue(result.isSuccessful()));
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Callee onDisconnected: " + event);
            Verify.verifyTrue(event instanceof CallObserver.LocalDecline);
            actor.logout();
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}