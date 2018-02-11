package com.sparktest.autotestapp.cases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

/**
 * Created by qimdeng on 11/15/17.
 */
@Description("Call When Connected")
public class TestCaseCallWhenConnected extends TestSuite {

    public TestCaseCallWhenConnected() {
        this.add(TestCaseCallWhenConnected.Callee.class);
        this.add(TestCaseCallWhenConnected.Caller.class);
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
                actor.onConnected(this::onConnected);
                actor.onDisconnected(c -> actor.logout());
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Caller onConnected, make 2nd call when connected");
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
                    call.acknowledge(c -> Ln.d("Callee acknowledge call"));
                    actor.onDisconnected(c -> actor.logout());
                    actor.setDefaultCallObserver(call);
                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            r -> Ln.e("Callee answering call"));
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
