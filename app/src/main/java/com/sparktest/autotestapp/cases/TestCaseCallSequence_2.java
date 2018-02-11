package com.sparktest.autotestapp.cases;

import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
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
 * Created by qimdeng on 11/24/17.
 */

public class TestCaseCallSequence_2 extends TestSuite {

    public TestCaseCallSequence_2() {
        this.add(TestCaseCallSequence_2.Callee.class);
        this.add(TestCaseCallSequence_2.Caller.class);
    }

    @Description("Caller")
    public static class Caller {

        TestActor actor;
        Handler mHandler = new Handler();
        int callTimes;
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
                makeCall(TestActor.jwtUser1);
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
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(result.getData());
                if (callTimes == 2){
                    mHandler.postDelayed(() -> {
                        result.getData().hangup(r -> Ln.d("Caller hangup"));
                    }, 5000);
                }
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Caller onConnected: " + call.getFrom());
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller onDisconnected: " + event);
            if (event instanceof  CallObserver.RemoteLeft){
                if (callTimes == 1) {
                    mHandler.postDelayed(() -> {
                        makeCall(TestActor.jwtUser3);
                    }, 5000);
                }else if (callTimes == 3){
                    actor.logout();
                }
            }else if (event instanceof  CallObserver.LocalCancel) {
                mHandler.postDelayed(() -> {
                    makeCall(TestActor.jwtUser1);
                }, 5000);
            }
        }

        private void makeCall(String dialUser){
            callTimes++;
            actor.getPhone().dial(dialUser,
                    MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    this::onCallSetup);
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }

    @Description("Callee")
    public static class Callee {

        TestActor actor;
        Handler mHandler = new Handler();
        boolean firstTimeHangup = true;
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
                    actor.onConnected(this::onConnected);
                    actor.onDisconnected(this::onDisconnected);
                    actor.setDefaultCallObserver(call);
                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            r -> Ln.e("Callee answering call"));
                    mHandler.postDelayed(() -> {
                        call.hangup(r -> Ln.d("Callee hangup"));
                    }, 10000);
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Callee onConnected: " + call.getFrom());
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Callee onDisconnected: " + event);
            if (event instanceof CallObserver.LocalLeft){
                if (firstTimeHangup){
                    firstTimeHangup = false;
                }else {
                    actor.logout();
                }
            }
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
