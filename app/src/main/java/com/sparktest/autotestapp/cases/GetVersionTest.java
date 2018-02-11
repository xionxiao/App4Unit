package com.sparktest.autotestapp.cases;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.JWTAuthenticator;
import com.sparktest.autotestapp.framework.annotation.Test;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;

public class GetVersionTest {
    @Inject
    public Context context;

    @Test
    public void getVersion() {
        Application app = ((Activity)context).getApplication();
        JWTAuthenticator auth = new JWTAuthenticator();
        Spark spark = new Spark(app, auth);
        assertEquals(spark.getVersion(), "0.1.0");
    }
}
