package com.sparktest.autotestapp.cases;

import android.content.Context;

import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.Authenticator;
import com.ciscospark.androidsdk.auth.JWTAuthenticator;
import com.ciscospark.androidsdk.phone.Phone;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.annotation.Test;

public class IncomingCallTest {
    static String jwtKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJ1c2VyICMxIiwiaXNzIjoiY2Q1YzlhZjctOGVkMy00ZTE1LTk3MDUtMDI1ZWYzMGIxYjZhIn0.nQTlT_WwkHdWZTCNi4tVl2IA476nAWo34oxtuTlLSDk";

    static String TAG = "DialTest";
    private final TestActivity activity;

    private Spark spark;
    Phone phone;

    public IncomingCallTest(Context context) {
        activity = (TestActivity) context;
    }

    @Test
    public void run() throws Error {
        Authenticator auth = new JWTAuthenticator();
        spark = new Spark(activity.getApplication(), auth);
    }
}
