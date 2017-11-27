package com.sparktest.autotesteapp.cases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.message.Message;
import com.ciscospark.androidsdk.room.Room;
import com.ciscospark.androidsdk.webhook.Webhook;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotesteapp.AppTestRunner;
import com.sparktest.autotesteapp.TestActivity;
import com.sparktest.autotesteapp.framework.TestSuite;
import com.sparktest.autotesteapp.framework.Verify;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;
import com.sparktest.autotesteapp.utils.TestActor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.sparktest.autotesteapp.framework.Verify.verifyTrue;

/**
 * Created by qimdeng on 11/21/17.
 */

public class TestCaseWebhooks extends TestSuite {
    public TestCaseWebhooks() {
        this.add(TestCaseWebhooks.WebHooker.class);
    }

    @Description("Web Hook")
    public static class WebHooker {
        private final static String SparkWebhookTestTargetUrl = "https://ios-demo-pushnoti-server.herokuapp.com/webhook";
        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;

        TestActor actor;
        Webhook webHook;
        List<Message> messageList = new ArrayList<>();
        @Test
        public void run() {
            actor = TestActor.JwtUser(activity, runner, TestActor.jwtKey2);
            actor.login(this::onRegistered);
        }

        private void onRegistered(Result result) {
            Ln.d("onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getSpark().webhooks().create("TestWebHook", SparkWebhookTestTargetUrl,
                        "messages", "all", null, null, this::onWebHookSetup);
            } else {
                verifyTrue(false);
            }
        }

        private void onWebHookSetup(Result<Webhook> result) {
            Ln.d("onWebHookSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                webHook = result.getData();
                Ln.d("WebHook name: " + webHook.getName());
                Verify.verifyTrue(webHook.getName().equals("TestWebHook"));
                updateWebHook();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void updateWebHook(){
            Ln.d("updateWebHook");
            actor.getSpark().webhooks().update(webHook.getId(), "UpdatedWebHook",
                    SparkWebhookTestTargetUrl, this::onWebHookUpdateCompleted);
        }

        private void onWebHookUpdateCompleted(Result<Webhook> result){
            Ln.d("onWebHookUpdateCompleted: " + result.isSuccessful());
            if (result.isSuccessful()) {
                webHook = result.getData();
                Ln.d("WebHook updated name: " + webHook.getName());
                Verify.verifyTrue(webHook.getName().equals("UpdatedWebHook"));
                deleteWebhook();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void deleteWebhook(){
            Ln.d("deleteWebhook");
            actor.getSpark().webhooks().delete(webHook.getId(), result -> {
                if (result.isSuccessful()) {
                    Ln.d("deleteWebhook complete");
                    listWebHooks();
                } else {
                    Verify.verifyTrue(false);
                }
            });
        }

        private void listWebHooks(){
            Ln.d("listWebHooks");
            actor.getSpark().webhooks().list(0, this::onWebhookListed);
        }

        private void onWebhookListed(Result<List<Webhook>> result){
            Ln.d("onWebhookListed: " + result.isSuccessful() + "  list size: " + result.getData().size());
            if (result.isSuccessful()) {
                for (Webhook wh : result.getData()){
                    Verify.verifyEquals(wh.getId(), webHook.getId());
                }
            } else {
                Verify.verifyTrue(false);
            }
            actor.logout();
        }
    }
}
