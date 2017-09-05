package com.sparktest.autotesteapp;

import android.content.Context;

public class TestCase {
    private Context mContext;
    private State mState = State.IDLE;

    public enum State {IDLE, RUNNING, PASSED, FAILED}

    public TestCase(Context context) {
        mContext = context;
    }

    public String getDescription() {
        return "test case";
    }

    public State getState() {
        return mState;
    }

    public void setState(State state) {
        mState = state;
    }

    public void run() {
        if (getState() == State.IDLE) {
            mState = State.RUNNING;
        }
    }
}
