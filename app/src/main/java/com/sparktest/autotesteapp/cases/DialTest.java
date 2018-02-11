package com.sparktest.autotesteapp.cases;


import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.annotation.After;
import com.sparktest.autotesteapp.framework.annotation.AfterClass;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

import javax.inject.Inject;

import static com.sparktest.autotesteapp.framework.Verify.verifyTrue;
import static junit.framework.Assert.assertTrue;


@Description("Dial Test")
public class DialTest {

    @Inject
    TestActivity activity;

    @Inject
    AppTestRunner runner;

    TestActor actor;

    @Test
    public void run() {
        actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey2);
        //assertTrue(false);
        //actor.loginBySparkId(this::onRegistered);
        actor.login(this::onRegistered);
    }

    private void onRegistered(Result result) {
        Ln.d(result.toString());
        if (result.isSuccessful()) {
            actor.getPhone().dial("xionxiao@cisco.com",//TestActor.jwtUser1,
                    MediaOption.audioVideo(activity.mLocalSurface, activity.mRemoteSurface),
                    this::onCallSetup);
        } else {
            verifyTrue(false);
        }
    }

    private void onCallSetup(Result<Call> result) {
        Ln.e("Call setup");
        if (result.isSuccessful()) {
            Ln.e("call setup success");
            actor.onDisconnected(c -> runner.resume());
            actor.setDefaultCallObserver(result.getData());
        }
    }
}
