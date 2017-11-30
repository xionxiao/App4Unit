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
 * Created by qimdeng on 11/22/17.
 */

public class TestCaseMultiParticipants_1 extends TestSuite {

    public TestCaseMultiParticipants_1() {
        this.add(TestCaseMultiParticipants_1.Caller1.class);
        this.add(TestCaseMultiParticipants_1.Caller2.class);
        this.add(TestCaseMultiParticipants_1.Callee.class);
    }

    @Description("Caller1")
    public static class Caller1 {

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
            Ln.d("Caller1 onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                new Handler().postDelayed(()->{
                    actor.getPhone().dial(TestActor.jwtUser1,
                            MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                }, 5000);
            } else {
                Verify.verifyTrue(false);
            }

        }

        /**
         * resume after call disconnected
         */
        private void onCallSetup(Result<Call> result) {
            Ln.d("Caller1 onCallSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.onConnected(new TestActor.CallProcessor(){
                    @Override
                    public void process(Call call) {
                        Ln.d("Caller1 onConnected");
                    }});
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(result.getData());
            } else {
                Ln.d("Call created error: " + result.getError());
                actor.logout();
            }
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller1 onDisconnected: " + event);
            if (event instanceof  CallObserver.RemoteLeft){
                event.getCall().hangup(result -> {
                    Ln.e("Caller1 hangup call: " + result);
                });
            }
            actor.logout();
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }

    @Description("Caller2")
    public static class Caller2 {

        TestActor actor;

        /**
         * Main test entrance
         */
        @Test
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey3);
            actor.login(this::onRegistered);
        }

        /**
         *  Dial jwtUser1 when register complete
         */
        private void onRegistered(Result result) {
            Ln.d("Caller2 onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                new Handler().postDelayed(()->{
                    actor.getPhone().dial(TestActor.jwtUser1,
                            MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            this::onCallSetup);
                }, 5000);
            } else {
                Verify.verifyTrue(false);
            }

        }

        /**
         * resume after call disconnected
         */
        private void onCallSetup(Result<Call> result) {
            Ln.d("Caller2 onCallSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.onConnected(new TestActor.CallProcessor(){
                    @Override
                    public void process(Call call) {
                        Ln.d("Caller2 onConnected");
                    }});
                actor.onDisconnected(this::onDisconnected);
                actor.setDefaultCallObserver(result.getData());
            } else {
                Ln.d("Call created error: " + result.getError());
                actor.logout();
            }
        }

        private void onDisconnected(CallObserver.CallEvent event){
            Ln.d("Caller2 onDisconnected: " + event);
            if (event instanceof  CallObserver.RemoteLeft){
                event.getCall().hangup(result -> {
                    Ln.e("Caller2 hangup call: " + result);
                });
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
        boolean answeredOnce;
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
                    Ln.d("Callee IncomingCall: " + call.getFrom());
                    call.acknowledge(c -> Ln.d("Callee acknowledge call"));
                    call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                            r -> {
                                Ln.e("Callee answering call: " + r.isSuccessful());
                                if (r.isSuccessful()){
                                    Verify.verifyTrue(!answeredOnce);
                                    answeredOnce = true;
                                    actor.onConnected(this::onConnected);
                                    actor.onDisconnected(c -> actor.logout());
                                    actor.setDefaultCallObserver(call);
                                }else{
                                    call.reject(result1 -> Ln.d("Callee reject call when busy"));
                                }
                    });
                });
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onConnected(Call call){
            Ln.d("Callee onConnected: " + call.getFrom());
            new Handler().postDelayed(()->{
                call.hangup(result -> {
                    Ln.d("Callee hangup: " + result.isSuccessful());
                    Verify.verifyTrue(result.isSuccessful());
                });
            }, 30000);
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
