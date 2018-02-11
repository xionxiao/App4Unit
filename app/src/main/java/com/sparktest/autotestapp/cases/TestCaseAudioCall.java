package com.sparktest.autotestapp.cases;

import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

/**
 * Created by qimdeng on 11/16/17.
 */

@Description("Audio Only Call")
public class TestCaseAudioCall extends TestSuite {

    public TestCaseAudioCall() {
        this.add(TestCaseAudioCall.Callee.class);
        this.add(TestCaseAudioCall.Caller.class);
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
                        MediaOption.audioOnly(),
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
                actor.onMediaChanged(this::onMediaChanged);
                actor.onDisconnected(c -> actor.logout());
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Caller, onConnected");
            Verify.verifyTrue(!call.isSendingVideo() && !call.isReceivingVideo());
        }

        private void onMediaChanged(CallObserver.CallEvent event){
            Ln.d("Caller onMediaChanged: " + event);
            Verify.verifyFalse(event instanceof CallObserver.RemoteVideoViewSizeChanged ||
                    (event instanceof CallObserver.RemoteSendingVideoEvent && ((CallObserver.RemoteSendingVideoEvent)event).isSending()));
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }

    @Description("Callee")
    public static class Callee {

        TestActor actor;
        Handler handler = new Handler();
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
                    actor.onDisconnected(c -> actor.logout());
                    actor.onMediaChanged(this::onMediaChanged);
                    actor.setDefaultCallObserver(call);
                    call.answer(MediaOption.audioOnly(),
                            r -> Ln.e("Callee answering call"));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Callee, onConnected");
            handler.postDelayed(() -> {
                Ln.d("sendingVideo： " + call.isSendingVideo() +
                    "  receivingVideo: " + call.isReceivingVideo());
                Verify.verifyTrue(!call.isSendingVideo() && !call.isReceivingVideo());
                }, 3000);
            handler.postDelayed(() -> call.hangup(r -> Ln.d("Callee hangup")), 5000);
        }

        private void onMediaChanged(CallObserver.CallEvent event){
            Ln.d("Callee onMediaChanged: " + event);
            Verify.verifyFalse(event instanceof CallObserver.RemoteVideoViewSizeChanged ||
                    (event instanceof CallObserver.SendingVideo && ((CallObserver.SendingVideo)event).isSending()));
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
