package com.sparktest.autotesteapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sparktest.autotesteapp.cases.TestCaseAudioCall;
import com.sparktest.autotesteapp.cases.TestCaseAudioCallUnmuteVideo;
import com.sparktest.autotesteapp.cases.TestCaseCallRejectWhenInit;
import com.sparktest.autotesteapp.cases.TestCaseCallRejectWhenRinging;
import com.sparktest.autotesteapp.cases.TestCaseCallSequence_1;
import com.sparktest.autotesteapp.cases.TestCaseCallSequence_2;
import com.sparktest.autotesteapp.cases.TestCaseCallWhenConnected;
import com.sparktest.autotesteapp.cases.TestCaseCallWhenRinging;
import com.sparktest.autotesteapp.cases.TestCaseHangUpDisconnectedCall;
import com.sparktest.autotesteapp.cases.TestCaseKeepCall;
import com.sparktest.autotesteapp.cases.TestCaseMultiParticipants_1;
import com.sparktest.autotesteapp.cases.TestCaseMultiParticipants_2;
import com.sparktest.autotesteapp.cases.TestCaseMuteAudioVideo;
import com.sparktest.autotesteapp.cases.TestCaseRoom;
import com.sparktest.autotesteapp.cases.TestCaseTeamAndMemberShip;
import com.sparktest.autotesteapp.cases.TestCaseWebhooks;
import com.sparktest.autotesteapp.framework.Test;
import com.sparktest.autotesteapp.framework.TestCase;
import com.sparktest.autotesteapp.framework.TestRunner;
import com.sparktest.autotesteapp.framework.TestState;
import com.sparktest.autotesteapp.framework.TestSuite;
import com.webex.wseclient.WseSurfaceView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

public class TestActivity extends Activity {

    public ListView mListView;
    public WseSurfaceView mRemoteSurface;
    public WseSurfaceView mLocalSurface;

    public Handler mHandler;
    public List<TestSuite> mSuites;
    private static final int FINISH = 1;

    @Inject
    public TestRunner mRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        mListView = (ListView) findViewById(R.id.testcaseListView);
        mRemoteSurface = (WseSurfaceView) findViewById(R.id.remoteView);
        mLocalSurface = (WseSurfaceView) findViewById(R.id.localView);
        Handler.Callback callback = (msg) -> {
            if (msg.what == FINISH) update();
            return true;
        };
        mHandler = new Handler(callback);

        ObjectGraph objectGraph = ObjectGraph.create(new TestModule(this));
        objectGraph.inject(this);
        Log.d("TestActivity", mRunner.getClass().getName());
        ((AppTestRunner) mRunner).setInjector(objectGraph);

        mSuites = new ArrayList<>();
        mSuites.add(new TestCaseCallWhenRinging());
        mSuites.add(new TestCaseCallWhenConnected());
        mSuites.add(new TestCaseCallRejectWhenRinging());
        mSuites.add(new TestCaseCallRejectWhenInit());
        mSuites.add(new TestCaseHangUpDisconnectedCall());
        mSuites.add(new TestCaseAudioCall());
        mSuites.add(new TestCaseAudioCallUnmuteVideo());
        mSuites.add(new TestCaseMuteAudioVideo());
        //mSuites.add(new TestCaseRoom());
        mSuites.add(new TestCaseWebhooks());
        mSuites.add(new TestCaseTeamAndMemberShip());
        mSuites.add(new TestCaseMultiParticipants_1());
        mSuites.add(new TestCaseMultiParticipants_2());
        mSuites.add(new TestCaseCallSequence_1());
        mSuites.add(new TestCaseCallSequence_2());
        mSuites.add(new TestCaseKeepCall());

        TestCaseAdapter adapter = new TestCaseAdapter(this, mSuites);
        mListView.setAdapter(adapter);
    }

    public void update() {
        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    public void runTest(View v) {
        int pos = mListView.getPositionForView(v);
        Test testcase = mSuites.get(pos);
        if (testcase instanceof TestSuite) {
            ViewGroup parent = (ViewGroup) v.getParent();
            ViewGroup parent_parent = (ViewGroup) parent.getParent();
            int index = parent_parent.indexOfChild(parent);
            mRunner.run((TestCase) ((TestSuite) testcase).get(index));
        } else {
            mRunner.run((TestCase) testcase);
        }

        this.update();
    }

    private class TestCaseAdapter extends ArrayAdapter<TestSuite> {

        public TestCaseAdapter(Context context, List<TestSuite> objects) {
            super(context, R.layout.listview_testsuite, objects);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TestSuite testSuite = getItem(position);

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listview_testsuite, null);
            ((TextView) convertView.findViewById(R.id.textView)).setText(testSuite.getDescription());
            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.subListView);
            for (TestCase t : testSuite.cases()) {
                View child = getLayoutInflater().inflate(R.layout.listview_testcase, null);
                ViewHolder holder = ViewHolder.createInstance(child);
                ViewHolder.updateViewHolder(holder, t);
                layout.addView(child);
            }

            return convertView;
        }

    }

    static class ViewHolder {
        Button button;
        TextView textView;
        ProgressBar progress;
        View indicator;

        static ViewHolder createInstance(View root) {
            ViewHolder holder = new ViewHolder();

            holder.button = (Button) root.findViewById(R.id.run);
            holder.textView = (TextView) root.findViewById(R.id.textView);
            holder.progress = (ProgressBar) root.findViewById(R.id.progressBar);
            holder.indicator = root.findViewById(R.id.indicator);

            return holder;
        }

        static void updateViewHolder(ViewHolder holder, TestCase testCase) {
            holder.textView.setText(testCase.getDescription());

            TestState state = testCase.getState();
            switch (state) {
                case NotRun:
                    holder.progress.setVisibility(View.INVISIBLE);
                    holder.button.setText("RUN");
                    holder.indicator.setBackgroundResource(android.R.drawable.presence_away);
                    break;
                case Running:
                    holder.progress.setVisibility(View.VISIBLE);
                    holder.button.setEnabled(false);
                    holder.button.setText("RUNNING");
                    holder.indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case Success:
                    holder.progress.setVisibility(View.INVISIBLE);
                    holder.button.setEnabled(true);
                    holder.button.setTextColor(Color.parseColor("#008800"));
                    holder.button.setText("PASSED");
                    holder.indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case Failed:
                    holder.progress.setVisibility(View.INVISIBLE);
                    holder.button.setEnabled(true);
                    holder.button.setText("FAILED");
                    holder.button.setTextColor(Color.parseColor("#880000"));
                    holder.indicator.setBackgroundResource(android.R.drawable.presence_busy);
                    break;
            }
        }
    }
}
