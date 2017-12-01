package com.sparktest.autotesteapp.utils;


import android.view.View;
import android.webkit.WebView;

import com.ciscospark.androidsdk.CompletionHandler;
import com.ciscospark.androidsdk.Spark;
import com.ciscospark.androidsdk.auth.JWTAuthenticator;
import com.ciscospark.androidsdk.auth.OAuthAuthenticator;
import com.ciscospark.androidsdk.auth.OAuthWebViewAuthenticator;
import com.ciscospark.androidsdk.message.MessageClient;
import com.ciscospark.androidsdk.phone.Call;
import com.ciscospark.androidsdk.phone.CallObserver;
import com.ciscospark.androidsdk.phone.Phone;
import com.ciscospark.androidsdk.room.RoomClient;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.R;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.Verify;

import java.util.logging.Handler;

public class TestActor {
    public static String jwtKey1 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzcGFya1NES1Rlc3Q5IiwibmFtZSI6InNwYXJrU0RLVGVzdDkiLCJpc3MiOiJjZDVjOWFmNy04ZWQzLTRlMTUtOTcwNS0wMjVlZjMwYjFiNmEifQ.4u5cET50gjX8RT3NxTj98ffQ0WnlMm0vr7AxsfZrVOg";
    public static String jwtKey2 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzcGFya1NES1Rlc3QxMCIsIm5hbWUiOiJzcGFya1NES1Rlc3QxMCIsImlzcyI6ImNkNWM5YWY3LThlZDMtNGUxNS05NzA1LTAyNWVmMzBiMWI2YSJ9.ym_v9_txKcqdcBsalNc0tRfa2V3p_9oOATlISq_L03g";
    public static String jwtKey3 = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzcGFya1NES1Rlc3Q4IiwibmFtZSI6InNwYXJrU0RLVGVzdDgiLCJpc3MiOiJjZDVjOWFmNy04ZWQzLTRlMTUtOTcwNS0wMjVlZjMwYjFiNmEifQ.kDo11OYO5NNR9QHXseLqALCOEaabeBWu5hc1e5Bq9po";
    public static String jwtUser1 = "sparkSDKTest9@cd5c9af7-8ed3-4e15-9705-025ef30b1b6a";
    public static String jwtUser2 = "sparkSDKTest10@cd5c9af7-8ed3-4e15-9705-025ef30b1b6a";
    public static String jwtUser3 = "sparkSDKTest8@cd5c9af7-8ed3-4e15-9705-025ef30b1b6a";
    public static String jwtUserID1 = "dce2861a-debb-4834-802b-6f08515c0bf2";
    public static String jwtUserID2 = "11bc13ac-5a84-4a1f-a1be-4b0910e8d10d";
    public static String jwtUserID3 = "6a884170-7a18-470b-a006-52ab4c72b47a";
    public static final String CLIENT_ID = "C416dd36dd57b536a35816978e4f063a98849d285ca191f5566a32c0f0c3481ab";
    public static final String CLIENT_SEC = "bc851e0f4d4bd62c020a45de08e374101910200d43096f32d14b9e08164adac7";
    public static final String REDIRECT_URL = "KitchenSink://response";
    public static final String SCOPE = "spark:all";
    public static final String TOKEN = "MTkyOTc2OTQtMGUwOC00Y2NlLWE2YmYtMDcxY2FlMDFkMTFlMmMyNWQzMjAtOTJk";
    TestActivity activity;
    AppTestRunner runner;

    Spark spark;
    Phone phone;

    String jwtKey;
    String email;
    String password;

    CallProcessor ringingProcessor;
    CallProcessor connectedProcessor;
    EventProcessor disconnectedProcessor;
    EventProcessor mediaChangedProcessor;

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

    public void loginBySparkId(CompletionHandler<Void> handler) {
        OAuthWebViewAuthenticator auth = new OAuthWebViewAuthenticator(CLIENT_ID, CLIENT_SEC, REDIRECT_URL, SCOPE);
        spark = new Spark(activity.getApplication(), auth);
        WebView webView = (WebView) activity.findViewById(R.id.OAuthWebView);
        webView.setVisibility(View.VISIBLE);
        auth.authorize(webView, result -> {
            if (result.isSuccessful()){
                Ln.d("loginBySparkId isSuccessful!");
                phone = spark.phone();
                phone.register(r -> {
                    if (r.isSuccessful()) {
                        // Device registered
                        Ln.e("register success");
                        handler.onComplete(r);
                    } else {
                        // Device not registered, and calls will not be sent or received
                        Ln.e("register failed");
                        runner.resume();
                    }
                });
            }else{
                handler.onComplete(result);
                runner.resume();
            }
            webView.setVisibility(View.INVISIBLE);
        });

        Ln.e("Waite for register");
        runner.await();
    }

    public void logout(){
        new android.os.Handler().postDelayed(()->{
            if (phone != null) {
                phone.deregister(result -> {
                    if (result.isSuccessful()) {
                        // Device registered
                        Ln.e("unregister success");
                    } else {
                        // Device not registered, and calls will not be sent or received
                        Ln.e("unregister failed");
                    }
                    runner.resume();
                });
            }
        }, 2000);
    }

    public Phone getPhone() {
        return phone;
    }

    public Spark getSpark() {
        return spark;
    }

    public void setDefaultCallObserver(Call call) {
        if (call != null)
            call.setObserver(defaultObserver);
        else
            Verify.verifyTrue(false);
    }

    public interface CallProcessor {
        void process(Call call);
    }

    public interface EventProcessor {
        void process(CallObserver.CallEvent event);
    }


    public void onRinging(CallProcessor processor) {
        ringingProcessor = processor;
    }

    public void onConnected(CallProcessor processor) {
        connectedProcessor = processor;
    }

    public void onDisconnected(EventProcessor processor) {
        disconnectedProcessor = processor;
    }

    public void onMediaChanged(EventProcessor processor) {
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
            if (disconnectedProcessor != null) disconnectedProcessor.process(event);
        }

        @Override
        public void onMediaChanged(MediaChangedEvent event) {
            Ln.e("Call media changed");
            if (mediaChangedProcessor != null) mediaChangedProcessor.process(event);
        }
    };
}
