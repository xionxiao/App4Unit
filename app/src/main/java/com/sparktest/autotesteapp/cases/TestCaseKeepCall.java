package com.sparktest.autotesteapp.cases;

import android.os.Handler;

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
 * Created by qimdeng on 11/24/17.
 */

public class TestCaseKeepCall extends TestSuite {

    public TestCaseKeepCall() {
        this.add(TestCaseKeepCall.Callee.class);
        this.add(TestCaseKeepCall.Caller.class);
    }

    @Description("Caller")
    public static class Caller {

        TestActor actor;
        Handler mHandler = new Handler();
        boolean firstTimeCall = true;
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
                makeCall();
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
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Caller onConnected: " + call.getFrom());
            if (!firstTimeCall){
                call.setSendingAudio(false);
                call.setSendingVideo(false);
            }
            mHandler.postDelayed(() -> {
                call.hangup(r -> Ln.d("Caller hangup"));
            }, 30000);
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller onDisconnected: " + event);
            if (event instanceof  CallObserver.LocalLeft){
                if (firstTimeCall) {
                    firstTimeCall = false;
                    mHandler.postDelayed(() -> {
                        makeCall();
                    }, 5000);
                }else{
                    actor.logout();
                }
            }
        }

        private void makeCall(){
            actor.getPhone().dial(TestActor.jwtUser1,
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
        boolean firstTimeAnswered = true;
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
            if (event instanceof CallObserver.RemoteLeft){
                if (firstTimeAnswered){
                    firstTimeAnswered = false;
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
