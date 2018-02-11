package com.sparktest.autotestapp.cases;


import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

@Description("Call Answer Test Pair")
public class CallAnswerPairTest extends TestSuite {

    public CallAnswerPairTest() {
        this.add(Recipient.class);
        this.add(Caller.class);
    }

    @Description("Dial Test")
    public static class Caller {

        TestActor actor;

        /**
         * Main test entrance
         */
        @Test
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey2);
            Verify.verifyTrue(false);
            //actor.login(this::onRegistered);
        }

        /**
         * Dial jwtUser1 when register complete
         */
        private void onRegistered(Result result) {
            /*
            actor.getPhone().dial(TestActor.jwtUser1,
                    MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    this::onCallSetup);
                    */
            //actor.logout();
        }

        /**
         * resume after call disconnected
         */
        private void onCallSetup(Result<Call> result) {
            actor.onDisconnected(c -> runner.resume());
            actor.setDefaultCallObserver(result.getData());
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }

    @Description("Answer Call Test")
    public static class Recipient {

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
         * Waiting for incoming call register complete
         */
        private void onRegistered(Result result) {
            /*
            actor.getPhone().setIncomingCallListener(call -> {
                actor.onConnected(this::shutdown);
                actor.onDisconnected(c -> runner.resume());
                actor.setDefaultCallObserver(call);
                call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                        r -> Ln.e("answering call"));
            });
            */
            //actor.logout();
            runner.resume();
        }

        /**
         * Hangup after 5 seconds
         */
        private void shutdown(Call call) {
            Handler handler = new Handler();
            handler.postDelayed(() -> call.hangup(r -> Ln.e("Call hangup")), 10000);
        }

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;
    }
}
