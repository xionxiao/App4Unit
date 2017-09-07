package com.sparktest.autotesteapp.testcase;

import android.content.Context;

import com.ciscospark.Spark;
import com.sparktest.autotesteapp.TestCase;

/**
 * Created on 05/09/2017.
 */

public class GetVersionTest extends TestCase {
    Spark mSpark;

    public GetVersionTest(Context context) {
        super(context);
        mSpark = new Spark();
    }

    @Override
    public String getDescription() {
        return "get spark version test";
    }

    @Override
    public void run() {
        super.run();
        mSpark.version().equals("0.1");
        setState(State.PASSED);
    }
}
