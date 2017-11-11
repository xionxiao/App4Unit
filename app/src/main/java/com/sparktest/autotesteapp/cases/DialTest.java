package com.sparktest.autotesteapp.cases;


import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.JWTAuthenticator;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.MediaOption;
import com.ciscospark.androidsdk.phone.Phone;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;

import java.util.concurrent.TimeoutException;

import javax.inject.Inject;

import static com.sparktest.autotesteapp.framework.Assert.assertFalse;
import static com.sparktest.autotesteapp.framework.Assert.assertTrue;
import static com.sparktest.autotesteapp.framework.Assert.assertNotNull;

@Description("Dial Test")
public class DialTest {
    static String jwtKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJ1c2VyICMxIiwiaXNzIjoiY2Q1YzlhZjctOGVkMy00ZTE1LTk3MDUtMDI1ZWYzMGIxYjZhIn0.nQTlT_WwkHdWZTCNi4tVl2IA476nAWo34oxtuTlLSDk";

    static String dial_user = "xionxiao@cisco.com";

    @Inject
    TestActivity activity;

    @Inject
    AppTestRunner runner;

    Spark spark;
    Phone phone;

    //Semaphore semaphore = new Semaphore(0);
    //final CountDownLatch signal = new CountDownLatch(1);

    @Test
    public void run() {
        JWTAuthenticator auth = new JWTAuthenticator();
        spark = new Spark(activity.getApplication(), auth);
        if (!auth.isAuthorized()) {
            auth.authorize(jwtKey);
        }

        phone = spark.phone();
        phone.register(this::onRegistered);

        runner.await();
        /*
        try {
            runner.await(0);
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        Ln.e("Dial Finish");
        //signal.await();
    }

    private void onRegistered(Result result) {
        Ln.d(result.toString());
        if (result.isSuccessful()) {
            // Device registered
            Ln.e("register success");
            runner.resume(3);
            //assertTrue(true);
        } else {
            // Device not registered, and calls will not be sent or received
            //signal.countDown();
            //assertFalse(true);
            Ln.e("register failed");
            runner.resume(4);
        }
    }

    private void dial(String dial_user) {
        phone.dial(dial_user, MediaOption.audioOnly(), this::onCallSetup);
    }

    private void onCallSetup(Result<Call> call) {
        assertNotNull(call);
        assertTrue(true);
    }
}
