package com.sparktest.autotesteapp.cases;

import android.os.Handler;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

import javax.inject.Inject;

@Description("Answer Call Test")
public class AnswerCallTest {

    TestActor actor;

    @Test
    public void run() {
        actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey1);
        actor.login(this::onRegistered);
    }

    private void onRegistered(Result result) {
        Ln.d(result.toString());
        actor.getPhone().setIncomingCallListener(call -> {
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
