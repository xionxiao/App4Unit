package com.sparktest.autotesteapp.framework;

import com.sparktest.autotesteapp.framework.annotation.Description;

import java.util.ArrayList;
import java.util.List;


public class TestSuite extends Test {
    private final List<TestCase> caseList = new ArrayList();

    @Override
    public String getDescription() {
        Description desc = this.getClass().getAnnotation(Description.class);
        if (desc != null) {
            return desc.value();
        }
        return this.getClass().getSimpleName();
    }

    public void add(Class<?> testClass) {
        caseList.add(new TestCase(testClass));
    }

    public void add(TestCase test) {
        caseList.add(test);
    }

    public List<TestCase> cases() {
        return caseList;
    }

    public Test get(int pos) {
        return caseList.get(pos);
    }
}
