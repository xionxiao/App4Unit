package com.sparktest.autotesteapp.cases;


import com.sparktest.autotesteapp.framework.TestRunner;
import com.sparktest.autotesteapp.framework.annotation.*;
import com.sparktest.autotesteapp.framework.annotation.AfterClass;
import com.sparktest.autotesteapp.framework.annotation.Before;
import com.sparktest.autotesteapp.framework.annotation.BeforeClass;
import com.sparktest.autotesteapp.framework.annotation.Description;
import com.sparktest.autotesteapp.framework.annotation.Test;

import javax.inject.Inject;

import static junit.framework.Assert.assertEquals;

@Description("Test for test framework")
public class TestTest {
    @Inject
    TestRunner runner;

    public TestTest() {
        System.out.println("====== Constructor ======");
    }

    @BeforeClass
    public static void beforeClass1() {
        System.out.println("====== Before Class 1 ======");
    }

    @BeforeClass
    public static void beforeClass2() {
        System.out.println("====== Before Class 2 ======");
    }

    @Before
    public void before1() {
        System.out.println("====== Before 1 ======");
    }

    @Before
    public void before2() {
        System.out.println("====== Before 2 ======");
    }

    @Test(order = 1)
    @Description("test case 1")
    public void test1() throws Exception {
        System.out.println("====== Test 1 ======");
        assertEquals(4, 2 + 2);
    }

    @Test(order = 2)
    @Description("test case 2")
    public void test2() throws Exception {
        System.out.println("====== Test 2 ======");
        assertEquals(4, 2 + 1);
    }

    @Test(order = 3)
    @Description("test case 2")
    public void test3() throws Exception {
        System.out.println("====== Test 3 ======");
        assertEquals(4, 2 + 2);
    }

    @After
    public void after1() {
        System.out.println("====== After 1 ======");
    }

    @After
    public void after2() {
        System.out.println("====== After 2 ======");
    }

    @AfterClass
    public static void afterClass1() {
        System.out.println("====== After Class 1 ======");
    }

    @AfterClass
    public static void afterClass2() {
        System.out.println("====== After Class 2 ======");
    }
}
