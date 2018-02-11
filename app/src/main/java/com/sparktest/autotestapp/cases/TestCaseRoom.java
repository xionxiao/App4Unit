package com.sparktest.autotestapp.cases;

import com.ciscospark.androidsdk.Result;
import com.ciscospark.androidsdk.message.Message;
import com.ciscospark.androidsdk.room.Room;
import com.github.benoitdion.ln.Ln;
import com.sparktest.autotestapp.TestActivity;
import com.sparktest.autotestapp.framework.TestSuite;
import com.sparktest.autotestapp.framework.Verify;
import com.sparktest.autotestapp.framework.annotation.Description;
import com.sparktest.autotestapp.framework.annotation.Test;
import com.sparktest.autotestapp.utils.TestActor;
import com.sparktest.autotestapp.AppTestRunner;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static com.sparktest.autotestapp.framework.Verify.verifyTrue;

/**
 * Created by qimdeng on 11/20/17.
 */

public class TestCaseRoom extends TestSuite {
    public TestCaseRoom() {
        this.add(TestCaseRoom.MessageSender.class);
        this.add(TestCaseRoom.FileSender.class);
    }

    @Description("Send Message")
    public static class MessageSender {

        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;

        TestActor actor;
        Room room;
        List<Message> messageList = new ArrayList<>();
        @Test
        public void run() {
            actor = new TestActor(activity, runner, null);
            actor.loginBySparkId(this::onRegistered);
        }

        private void onRegistered(Result result) {
            Ln.d("onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getSpark().rooms().create("TestRoom", null, this::onRoomSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onRoomSetup(Result<Room> result) {
            Ln.d("onRoomSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                room = result.getData();
                Ln.d("title: " + room.getTitle());
                Verify.verifyTrue(room.getTitle().equals("TestRoom"));
                sendMsgs();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void sendMsgs(){
            Ln.d("sendMsgs");
            for (int i = 0; i < 3; i++) {
                actor.getSpark().messages().post(room.getId(), null, null, String.format("Hello(%d)", i),
                        null, null, this::onMessagePostCompleted);
            }
        }

        private void onMessagePostCompleted(Result<Message> result){
            Ln.d("onMessagePostCompleted: " + result.isSuccessful());
            if (result.isSuccessful()) {
                messageList.add(result.getData());
                if (messageList.size() == 3){
                    Ln.d("sendMsgs complete");
                    deleteMsg(1);
                }
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void deleteMsg(int index){
            Ln.d("deleteMsg index: " + index);
            actor.getSpark().messages().delete(messageList.get(index).getId(), result -> {
                if (result.isSuccessful()) {
                    Ln.d("deleteMsg complete");
                    listMsgs();
                } else {
                    Verify.verifyTrue(false);
                }
            });
        }

        private void listMsgs(){
            Ln.d("listMsgs");
            actor.getSpark().messages().list(room.getId(), null, null, null, 0, this::onMessageListed);
        }

        private void onMessageListed(Result<List<Message>> result){
            Ln.d("onMessageListed: " + result.isSuccessful());
            if (result.isSuccessful()) {
                Ln.d("List size: " + result.getData().size());
                Verify.verifyEquals(2, result.getData().size());
                deleteRoom();
            } else {
                Verify.verifyTrue(false);
                actor.logout();
            }
        }

        private void deleteRoom(){
            Ln.d("deleteRoom");
            actor.getSpark().rooms().delete(room.getId(), result -> {
                Ln.d("deleteRoom: " + result.isSuccessful());
                Verify.verifyTrue(result.isSuccessful());
                actor.logout();
            });
        }
    }

    @Description("Send File")
    public static class FileSender {
        private final static String fileStr = "https://api.ciscospark.com/v1/contents/Y2lzY29zcGFyazovL3VzL0NPTlRFTlQvODI2YzRkYzAtNzBlZi0xMWU3LTg4YWEtM2Y3MmVjNzA3MWU2LzA";
        @Inject
        TestActivity activity;

        @Inject
        AppTestRunner runner;

        TestActor actor;
        Room room;
        List<Message> messageList = new ArrayList<>();

        @Test
        public void run() {
            actor = new TestActor(activity, runner, null);
            actor.loginBySparkId(this::onRegistered);
        }

        private void onRegistered(Result result) {
            Ln.d("onRegistered result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                actor.getSpark().rooms().create("TestRoom", null, this::onRoomSetup);
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void onRoomSetup(Result<Room> result) {
            Ln.d("onRoomSetup result: " + result.isSuccessful());
            if (result.isSuccessful()) {
                room = result.getData();
                Ln.d("title: " + room.getTitle());
                Verify.verifyTrue(room.getTitle().equals("TestRoom"));
                sendFile();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void sendFile() {
            Ln.d("sendFile");
            actor.getSpark().messages().post(room.getId(), null, null, null,
                    null, new String[]{fileStr}, this::onFilePostCompleted);
        }

        private void onFilePostCompleted(Result<Message> result){
            Ln.d("onFilePostCompleted: " + result.isSuccessful());
            if (result.isSuccessful()) {
                messageList.add(result.getData());
                listFile();
            } else {
                Verify.verifyTrue(false);
            }
        }

        private void listFile(){
            Ln.d("listFile");
            actor.getSpark().messages().list(room.getId(), null, null, null, 2, this::onFileListed);
        }

        private void onFileListed(Result<List<Message>> result){
            Ln.d("onFileListed: " + result.isSuccessful());
            if (result.isSuccessful()) {
                Verify.verifyEquals(messageList.get(0).getId(), result.getData().get(0).getId());
                deleteRoom();
            } else {
                Verify.verifyTrue(false);
                actor.logout();
            }
        }

        private void deleteRoom(){
            Ln.d("deleteRoom");
            actor.getSpark().rooms().delete(room.getId(), result -> {
                Ln.d("deleteRoom: " + result.isSuccessful());
                Verify.verifyTrue(result.isSuccessful());
                actor.logout();
            });
        }
    }
}
