package com.sparktest.autotesteapp;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
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

import com.webex.wseclient.WseSurfaceView;

import java.util.ArrayList;
import java.util.List;

public class TestActivity extends Activity {

    public ListView mListView;
    public WseSurfaceView mRemoteSurface;
    public WseSurfaceView mLocalSurface;
    public ArrayList<TestCase> mTestCases;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mListView = (ListView) findViewById(R.id.testcaseListview);
        mRemoteSurface = (WseSurfaceView) findViewById(R.id.remoteView);
        mLocalSurface = (WseSurfaceView) findViewById(R.id.localView);

        mTestCases = new ArrayList();
        mTestCases.add(new GetVersionTest(this));
        for (int i = 0; i < 50; i++) {
            mTestCases.add(new TestCase(this));
        }
        TestCaseAdapter adapter = new TestCaseAdapter(this, R.layout.listview_item, mTestCases);
        mListView.setAdapter(adapter);
    }

    public void runTest(View v) {
        int pos = mListView.getPositionForView(v);
        Log.d("position", "" + pos);
        TestCase testcase = mTestCases.get(pos);
        testcase.run();
        ((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
    }

    private class TestCaseAdapter extends ArrayAdapter<TestCase> {
        private int mResourceId;

        public TestCaseAdapter(Context context, int resourceId, List<TestCase> objects) {
            super(context, resourceId, objects);
            mResourceId = resourceId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TestCase testCase = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(mResourceId, null);

            Button button = (Button) view.findViewById(R.id.run);

            TextView textView = (TextView) view.findViewById(R.id.textView);
            textView.setText(testCase.getDescription());

            ProgressBar bar = (ProgressBar) view.findViewById(R.id.progressBar);

            View indicator = view.findViewById(R.id.indicator);
            TestCase.State state = testCase.getState();
            switch (state) {
                case IDLE:
                    bar.setVisibility(View.INVISIBLE);
                    button.setText("Run");
                    indicator.setBackgroundResource(android.R.drawable.presence_away);
                    break;
                case RUNNING:
                    bar.setVisibility(View.VISIBLE);
                    button.setEnabled(false);
                    button.setText("Running");
                    indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case PASSED:
                    bar.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    button.setText("PASS");
                    button.setBackgroundColor(0x00ff00);
                    indicator.setBackgroundResource(android.R.drawable.presence_online);
                    break;
                case FAILED:
                    bar.setVisibility(View.INVISIBLE);
                    button.setEnabled(false);
                    button.setText("FAIL");
                    button.setBackgroundColor(0xff0000);
                    indicator.setBackgroundResource(android.R.drawable.presence_busy);
                    break;
            }
            return view;
        }
    }
}
