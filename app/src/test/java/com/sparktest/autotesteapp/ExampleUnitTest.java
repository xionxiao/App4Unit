package com.sparktest.autotesteapp;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */


public class ExampleUnitTest {

    public ExampleUnitTest() {
        System.out.println("====== Constructor ======");
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("====== finalize ======");
        super.finalize();
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

//    @Test
//    public void test1() throws Exception {
//        System.out.println("====== Test 1 ======");
//        assertEquals(4, 2 + 2);
//    }
//
//    @Test
//    public void test2() throws Exception {
//        System.out.println("====== Test 2 ======");
//        assertEquals(5, 2 + 2);
//    }

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
        //assertFalse(true);
    }

    @AfterClass
    public static void afterClass2() {
        System.out.println("====== After Class 2 ======");
    }
}