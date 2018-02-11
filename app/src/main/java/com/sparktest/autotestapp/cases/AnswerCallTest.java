package com.sparktest.autotestapp.cases;

import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.AppTestRunner;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;

import javax.inject.Inject;

@Description("Answer Call Test")
public class AnswerCallTest {

    TestActor actor;

    @Test
    public void run() {
        actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey2);
        actor.login(this::onRegistered);
    }

    private void onRegistered(Result result) {
        Ln.d(result.toString());
        actor.getPhone().setIncomingCallListener(call -> {
            Ln.e("Incoming call");
            actor.onConnected(this::shutdown);
            actor.onDisconnected(c -> runner.resume());
            actor.setDefaultCallObserver(call);
            call.answer(MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    r -> Ln.e("answering call"));
        });
    }

    private void shutdown(Call call) {
        Handler handler = new Handler();
        handler.postDelayed(() -> call.hangup(r -> Ln.e("Call hangup")), 5000);
    }

    @Inject
    TestActivity activity;

    @Inject
    AppTestRunner runner;
}
