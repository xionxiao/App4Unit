package com.sparktest.autotesteapp.testcase;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ciscospark.CompletionHandler;
import com.ciscospark.Spark;
import com.ciscospark.SparkError;
import com.ciscospark.auth.Authenticator;
import com.ciscospark.auth.JWTAuthenticator;
import com.ciscospark.phone.Call;
import com.ciscospark.phone.CallOption;
import com.ciscospark.phone.DialObserver;
import com.ciscospark.phone.Phone;
import com.ciscospark.phone.RegisterListener;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.TestCase;

import java.util.List;

/**
 * Created on 06/09/2017.
 */

public class DialTest extends TestCase {
    static String jwtKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyMSIsIm5hbWUiOiJ1c2VyICMxIiwiaXNzIjoiY2Q1YzlhZjctOGVkMy00ZTE1LTk3MDUtMDI1ZWYzMGIxYjZhIn0.nQTlT_WwkHdWZTCNi4tVl2IA476nAWo34oxtuTlLSDk";

    static String TAG = "DialTest";
    static String dial_user = "xionxiao@cisco.com";
    TestActivity activity;
    Spark spark;
    Phone phone;

    public DialTest(Context context) {
        super(context);
        activity = (TestActivity)context;
    }

    public String getDescription() {
        return "Dialing Test";
    }

    public void run() {
        super.run();
        spark = new Spark();
        Authenticator auth = new JWTAuthenticator(jwtKey);
        spark.setAuthenticator(auth);
        spark.authorize(new CompletionHandler<String>() {
            @Override
            public void onComplete(String s) {
                Log.d(TAG, "auth:" + s);
                phone = spark.phone();
                phone.register(new RegisterListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "registered");

                        phone.dial(dial_user, new CallOption(CallOption.CallType.VIDEO,
                                        activity.mRemoteSurface,
                                        activity.mLocalSurface),
                                new DialObserver() {
                                    @Override
                                    public void onSuccess(Call call) {
                                        Log.d(TAG, "dail success");
                                        mState = State.PASSED;
                                        activity.update();
                                    }

                                    @Override
                                    public void onFailed(SparkError sparkError) {
                                        Log.e(TAG, "dail failed");
                                        mState = State.FAILED;
                                        activity.update();
                                    }

                                    @Override
                                    public void onPermissionRequired(List<String> list) {
                                        String[] permissionStrings = new String[list.size()];
                                        permissionStrings = list.toArray(permissionStrings);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            activity.requestPermissions(permissionStrings, 0);
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onFailed(SparkError sparkError) {
                        Log.e(TAG, sparkError.toString());
                        mState = State.FAILED;
                        activity.update();

                    }
                });
            }

            @Override
            public void onError(SparkError sparkError) {
                Log.e(TAG, sparkError.toString());
                mState = State.FAILED;
                activity.update();
            }
        });
    }
}
