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
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

/**
 * Created by qimdeng on 11/17/17.
 */

public class TestCaseMuteAudioVideo extends TestSuite {

    public TestCaseMuteAudioVideo() {
        this.add(TestCaseMuteAudioVideo.Callee.class);
        this.add(TestCaseMuteAudioVideo.Caller.class);
    }

    @Description("Caller")
    public static class Caller {

        TestActor actor;
        Handler handler = new Handler();
        boolean isMuteStatus;
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
                actor.onMediaChanged(this::onMediaChanged);
                actor.onDisconnected(c -> actor.logout());
                actor.setDefaultCallObserver(result.getData());
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onMediaChanged(CallObserver.CallEvent event){
            Ln.d("Caller onMediaChanged: " + event + "  isMuteStatus: " + isMuteStatus);
            Call call = event.getCall();
            if (event instanceof CallObserver.RemoteSendingVideoEvent){
                if (!((CallObserver.RemoteSendingVideoEvent)event).isSending() && !isMuteStatus){
                    handler.postDelayed(() -> {
                        isMuteStatus = true;
                        call.setSendingVideo(false);
                        call.setSendingAudio(false);
                        call.setReceivingVideo(false);
                        call.setReceivingAudio(false);
                    }, 2000);
                }
            }else if (event instanceof CallObserver.RemoteSendingAudioEvent) {
                if (((CallObserver.RemoteSendingAudioEvent) event).isSending() && isMuteStatus) {
                    handler.postDelayed(() -> {
                        isMuteStatus = false;
                        call.setSendingVideo(true);
                        call.setSendingAudio(true);
                        call.setReceivingVideo(true);
                        call.setReceivingAudio(true);
                    }, 2000);
                }
            }else if (event instanceof CallObserver.SendingVideo){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.SendingVideo)event).isSending());
            }else if (event instanceof CallObserver.SendingAudio){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.SendingAudio)event).isSending());
            }else if (event instanceof CallObserver.ReceivingVideo){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.ReceivingVideo)event).isReceiving());
            }else if (event instanceof CallObserver.ReceivingAudio){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.ReceivingAudio)event).isReceiving());
            }

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
        boolean isMuteStatus;
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
                    actor.onMediaChanged(this::onMediaChanged);
                    actor.setDefaultCallObserver(call);
                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            r -> Ln.e("Callee answering call"));
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onMediaChanged(CallObserver.CallEvent event){
            Ln.d("Callee onMediaChanged: " + event + "  isMuteStatus: " + isMuteStatus);
            Call call = event.getCall();
            if (event instanceof CallObserver.RemoteVideoViewSizeChanged) {
                handler.postDelayed(() -> {
                    isMuteStatus = true;
                    call.setSendingVideo(false);
                    call.setSendingAudio(false);
                    call.setReceivingVideo(false);
                    call.setReceivingAudio(false);
                }, 2000);
            }else if (event instanceof CallObserver.SendingVideo){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.SendingVideo)event).isSending());
            }else if (event instanceof CallObserver.SendingAudio){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.SendingAudio)event).isSending());
            }else if (event instanceof CallObserver.ReceivingVideo){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.ReceivingVideo)event).isReceiving());
            }else if (event instanceof CallObserver.ReceivingAudio){
                Verify.verifyEquals(isMuteStatus, !((CallObserver.ReceivingAudio)event).isReceiving());
            }else if (event instanceof CallObserver.RemoteSendingVideoEvent){
                if (!((CallObserver.RemoteSendingVideoEvent)event).isSending() && isMuteStatus){
                    handler.postDelayed(() -> {
                        isMuteStatus = false;
                        call.setSendingVideo(true);
                        call.setSendingAudio(true);
                        call.setReceivingVideo(true);
                        call.setReceivingAudio(true);
                    }, 2000);
                }
            }else if (event instanceof CallObserver.RemoteSendingAudioEvent) {
                if (((CallObserver.RemoteSendingAudioEvent) event).isSending() && !isMuteStatus) {
                    handler.postDelayed(() -> call.hangup(r -> Ln.d("Callee hangup")), 3000);
                }
            }

        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
