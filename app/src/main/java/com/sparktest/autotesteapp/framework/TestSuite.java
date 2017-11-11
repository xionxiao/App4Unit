package com.sparktest.autotesteapp.framework;

import java.util.ArrayList;
import java.util.List;


public class TestSuite extends Test {
    private final List<TestCase> caseList = new ArrayList();

    public void add(Class<?> testClass) {
        caseList.add(new TestCase(testClass));
    }

    public List<TestCase> cases() {
        return caseList;
    }

    public TestCase get(int pos) {
        return caseList.get(pos);
    }

    public Class<?> getTestClass(int pos) {
        return caseList.get(pos).getTestClass();
    }
}
