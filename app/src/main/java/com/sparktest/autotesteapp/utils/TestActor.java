package com.sparktest.autotesteapp.utils;


import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.JWTAuthenticator;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.Phone;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;

public class TestActor {
    public static String jwtKey1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbmRyb2lkX3Rlc3R1c2VyXzEiLCJuYW1lIjoiQW5kcm9pZFRlc3RVc2VyMSIsImlzcyI6ImNkNWM5YWY3LThlZDMtNGUxNS05NzA1LTAyNWVmMzBiMWI2YSJ9.eJ99AY9iNDhG4HjDJsY36wgqOnNQSes_PIu0DKBHBzs";
    public static String jwtKey2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbmRyb2lkX3Rlc3R1c2VyXzIiLCJuYW1lIjoiQW5kcm9pZFRlc3RVc2VyMiIsImlzcyI6ImNkNWM5YWY3LThlZDMtNGUxNS05NzA1LTAyNWVmMzBiMWI2YSJ9.fRhBqz52Ha_0ush_KdfqvS3l7N35kRyBJ2J1ekSXX1g";
    public static String jwtUser1 = "android_testuser_1@cd5c9af7-8ed3-4e15-9705-025ef30b1b6a";
    public static String jwtUser2 = "android_testuser_2@cd5c9af7-8ed3-4e15-9705-025ef30b1b6a";

    TestActivity activity;
    AppTestRunner runner;

    Spark spark;
    Phone phone;

    String jwtKey;
    String email;
    String password;

    CallProcessor ringingProcessor;
    CallProcessor connectedProcessor;
    CallProcessor disconnectedProcessor;
    CallProcessor mediaChangedProcessor;

    public TestActor(TestActivity activity, AppTestRunner runner, String jwt) {
        this.activity = activity;
        this.runner = runner;
        jwtKey = jwt;
    }

    public TestActor(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public static TestActor JwtUser(TestActivity activity, AppTestRunner runner, String jwt) {
        return new TestActor(activity, runner, jwt);
    }

    public void login(CompletionHandler<Void> handler) {
        JWTAuthenticator auth = new JWTAuthenticator();
        spark = new Spark(activity.getApplication(), auth);
        auth.authorize(jwtKey);

        phone = spark.phone();
        phone.register(result -> {
            if (result.isSuccessful()) {
                // Device registered
                Ln.e("register success");
                handler.onComplete(result);
            } else {
                // Device not registered, and calls will not be sent or received
                Ln.e("register failed");
                runner.resume();
            }
        });

        Ln.e("Waite for register");
        runner.await();
    }

    public Phone getPhone() {
        return phone;
    }

    public Spark getSpark() {
        return spark;
    }

    public void setDefaultCallObserver(Call call) {
        call.setObserver(defaultObserver);
    }

    public interface CallProcessor {
        void process(Call call);
    }


    public void onRinging(CallProcessor processor) {
        ringingProcessor = processor;
    }

    public void onConnected(CallProcessor processor) {
        connectedProcessor = processor;
    }

    public void onDisconnected(CallProcessor processor) {
        disconnectedProcessor = processor;
    }

    public void onMediaChanged(CallProcessor processor) {
        mediaChangedProcessor = processor;
    }

    public CallObserver defaultObserver = new CallObserver() {
        @Override
        public void onRinging(Call call) {
            Ln.e("Call is ringing");
            if (ringingProcessor != null) ringingProcessor.process(call);
        }

        @Override
        public void onConnected(Call call) {
            Ln.e("Call connected");
            if (connectedProcessor != null) connectedProcessor.process(call);
        }

        @Override
        public void onDisconnected(CallDisconnectedEvent event) {
            Ln.e("Call disconnected");
            if (disconnectedProcessor != null) disconnectedProcessor.process(event.getCall());
        }

        @Override
        public void onMediaChanged(MediaChangedEvent event) {
            Ln.e("Call media changed");
            if (mediaChangedProcessor != null) mediaChangedProcessor.process(event.getCall());
        }
    };
}
