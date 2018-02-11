package com.sparktest.autotestapp.cases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

/**
 * Created by qimdeng on 11/16/17.
 */

@Description("Hangup a Disconnected Call")
public class TestCaseHangUpDisconnectedCall extends TestSuite {

    public TestCaseHangUpDisconnectedCall() {
        this.add(TestCaseHangUpDisconnectedCall.Callee.class);
        this.add(TestCaseHangUpDisconnectedCall.Caller.class);
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
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller onDisconnected: " + event);
            if (event instanceof CallObserver.RemoteDecline){
                event.getCall().hangup(result -> {
                    Ln.e("Caller hangup a disconnected call: " + result);
                    Verify.verifyTrue(result != null);
                });
            }else{
                Verify.verifyTrue(false);
            }

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
                    Ln.d("Callee IncomingCall");
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    call.reject(r -> Verify.verifyTrue(r.isSuccessful()));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Callee onDisconnected: " + event);
            if (event instanceof CallObserver.LocalDecline){
                event.getCall().hangup(result -> {
                    Ln.e("Callee hangup a disconnected call: " + result);
                    Verify.verifyTrue(result != null);
                });
            }else{
                Verify.verifyTrue(false);
            }

            actor.logout();
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}