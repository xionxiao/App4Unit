package com.sparktest.autotesteapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sparktest.autotesteapp.cases.DialTest;
import com.sparktest.autotesteapp.cases.GetVersionTest;
import com.sparktest.autotesteapp.cases.TestTest;
import com.sparktest.autotesteapp.framework.TestCase;
import com.sparktest.autotesteapp.framework.TestRunner;
import com.sparktest.autotesteapp.framework.TestState;
import com.sparktest.autotesteapp.framework.TestSuite;
import com.webex.wseclient.WseSurfaceView;

import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

public class TestActivity extends Activity {

    public ListView mListView;
    public WseSurfaceView mRemoteSurface;
    public WseSurfaceView mLocalSurface;

    public Handler mHandler;
    public TestSuite mSuite;
    private static final int FINISH = 1;

    @Inject
    public TestRunner mRunner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_test);
        mListView = (ListView) findViewById(R.id.testcaseListview);
        mRemoteSurface = (WseSurfaceView) findViewById(R.id.remoteView);
        mLocalSurface = (WseSurfaceView) findViewById(R.id.localView);
        Handler.Callback callback = (msg) -> {
            if (msg.what == FINISH) update();
            return true;
        };
        mHandler = new Handler(callback);

        ObjectGraph objectGraph = ObjectGraph.create(new TestModule(this));
        objectGraph.inject(this);
        assert (mRunner != null);
        assert (mRunner.getClass().getName().equals(AppTestRunner.class.getName()));
        Log.d("TestActivity", mRunner.getClass().getName());
        ((AppTestRunner) mRunner).setInjector(objectGraph);

        mSuite = new TestSuite();
        mSuite.add(TestTest.class);
        mSuite.add(GetVersionTest.class);
        mSuite.add(DialTest.class);

        TestCaseAdapter adapter = new TestCaseAdapter(this, R.layout.listview_item, mSuite.cases());
        mListView.setAdapter(adapter);
    }

    public void update() {
        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    public void runTest(View v) {
        int pos = mListView.getPositionForView(v);
        TestCase testcase = mSuite.get(pos);
        mRunner.run(testcase);
        this.update();
    }

    private class TestCaseAdapter extends ArrayAdapter<TestCase> {
        private final int mResourceId;

        public TestCaseAdapter(Context context, int resourceId, List<TestCase> objects) {
            super(context, resourceId, objects);
            mResourceId = resourceId;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            TestCase testCase = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(mResourceId, null);

            Button button = (Button) view.findViewById(R.id.run);

            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(testCase.getDescription());

            ProgressBar bar = (ProgressBar) view.findViewById(R.id.progressBar);

            View indicator = view.findViewById(R.id.indicator);
            TestState state = testCase.getState();
            switch (state) {
                case NotRun:
                    bar.setVisibility(View.INVISIBLE);
                    button.setText("RUN");
                    indicator.setBackgroundResource(android.R.drawable.presence_away);
                    break;
                case Running:
                    bar.setVisibility(View.VISIBLE);
                    button.setEnabled(false);
                    button.setText("RUNNING");
                    indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case Success:
                    bar.setVisibility(View.INVISIBLE);
                    button.setEnabled(true);
                    button.setText("PASSED");
                    button.setBackgroundColor(0x00ff00);
                    indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case Failed:
                    bar.setVisibility(View.INVISIBLE);
                    button.setEnabled(true);
                    button.setText("FAILED");
                    button.setBackgroundColor(0xff0000);
                    indicator.setBackgroundResource(android.R.drawable.presence_busy);
                    break;
            }
            return view;
        }
    }
}
