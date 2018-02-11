package com.sparktest.autotestapp.framework;


import android.support.annotation.Nullable;

/**
 * A set of assertion methods useful for writing tests. Only failed assertions
 * are recorded. These methods can be used directly:
 * <code>Verify.assertEquals(...)</code>, however, they read better if they
 * are referenced through static import:
 * <p>
 * <pre>
 * import static org.junit.Verify.*;
 *    ...
 * </pre>
 *
 */
public class Verify {

    public interface VerifyHandler {
        void fail(String message);
    }

    static VerifyHandler delegate;

    /**
     * Delegate assert fail actions
     *
     * @param delegate
     */
    static public void delegate(@Nullable VerifyHandler delegate) {
        Verify.delegate = delegate;
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static public void verifyTrue(String message, boolean condition) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * Asserts that a condition is true. If it isn't it throws
     * an AssertionFailedError.
     */
    static public void verifyTrue(boolean condition) {
        verifyTrue(null, condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError with the given message.
     */
    static public void verifyFalse(String message, boolean condition) {
        verifyTrue(message, !condition);
    }

    /**
     * Asserts that a condition is false. If it isn't it throws
     * an AssertionFailedError.
     */
    static public void verifyFalse(boolean condition) {
        verifyFalse(null, condition);
    }

    /**
     * Fails a test with the given message.
     */
    static public void fail(String message) {
        if (delegate != null) {
            delegate.fail(message);
        } else {
            throw message == null ? new AssertionError() : new AssertionError(message);
        }
    }

    /**
     * Fails a test with no message.
     */
    static public void fail() {
        fail(null);
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, Object expected, Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        failNotEquals(message, expected, actual);
    }

    /**
     * Asserts that two objects are equal. If they are not
     * an AssertionFailedError is thrown.
     */
    static public void verifyEquals(Object expected, Object actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two Strings are equal.
     */
    static public void verifyEquals(String message, String expected, String actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected != null && expected.equals(actual)) {
            return;
        }
        String cleanMessage = message == null ? "" : message;
        failNotEquals(cleanMessage, expected, actual);
    }

    /**
     * Verify that two Strings are equal.
     */
    static public void verifyEquals(String expected, String actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two doubles are equal concerning a delta.  If they are not
     * an AssertionFailedError is thrown with the given message.  If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void verifyEquals(String message, double expected, double actual, double delta) {
        if (Double.compare(expected, actual) == 0) {
            return;
        }
        if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, new Double(expected), new Double(actual));
        }
    }

    /**
     * Asserts that two doubles are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void verifyEquals(double expected, double actual, double delta) {
        verifyEquals(null, expected, actual, delta);
    }

    /**
     * verifys that two floats are equal concerning a positive delta. If they
     * are not an verifyionFailedError is thrown with the given message. If the
     * expected value is infinity then the delta value is ignored.
     */
    static public void verifyEquals(String message, float expected, float actual, float delta) {
        if (Float.compare(expected, actual) == 0) {
            return;
        }
        if (!(Math.abs(expected - actual) <= delta)) {
            failNotEquals(message, new Float(expected), new Float(actual));
        }
    }

    /**
     * Asserts that two floats are equal concerning a delta. If the expected
     * value is infinity then the delta value is ignored.
     */
    static public void verifyEquals(float expected, float actual, float delta) {
        verifyEquals(null, expected, actual, delta);
    }

    /**
     * Asserts that two longs are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, long expected, long actual) {
        verifyEquals(message, Long.valueOf(expected), Long.valueOf(actual));
    }

    /**
     * Asserts that two longs are equal.
     */
    static public void verifyEquals(long expected, long actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two booleans are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, boolean expected, boolean actual) {
        verifyEquals(message, Boolean.valueOf(expected), Boolean.valueOf(actual));
    }

    /**
     * Asserts that two booleans are equal.
     */
    static public void verifyEquals(boolean expected, boolean actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two bytes are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, byte expected, byte actual) {
        verifyEquals(message, Byte.valueOf(expected), Byte.valueOf(actual));
    }

    /**
     * Asserts that two bytes are equal.
     */
    static public void verifyEquals(byte expected, byte actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two chars are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, char expected, char actual) {
        verifyEquals(message, Character.valueOf(expected), Character.valueOf(actual));
    }

    /**
     * Asserts that two chars are equal.
     */
    static public void verifyEquals(char expected, char actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two shorts are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, short expected, short actual) {
        verifyEquals(message, Short.valueOf(expected), Short.valueOf(actual));
    }

    /**
     * Asserts that two shorts are equal.
     */
    static public void verifyEquals(short expected, short actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that two ints are equal. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyEquals(String message, int expected, int actual) {
        verifyEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    /**
     * Asserts that two ints are equal.
     */
    static public void verifyEquals(int expected, int actual) {
        verifyEquals(null, expected, actual);
    }

    /**
     * Asserts that an object isn't null.
     */
    static public void verifyNotNull(Object object) {
        verifyNotNull(null, object);
    }

    /**
     * Asserts that an object isn't null. If it is
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyNotNull(String message, Object object) {
        verifyTrue(message, object != null);
    }

    /**
     * Asserts that an object is null. If it isn't an {@link AssertionError} is
     * thrown.
     * Message contains: Expected: <null> but was: object
     *
     * @param object Object to check or <code>null</code>
     */
    static public void verifyNull(Object object) {
        if (object != null) {
            verifyNull("Expected: <null> but was: " + object.toString(), object);
        }
    }

    /**
     * Asserts that an object is null.  If it is not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifyNull(String message, Object object) {
        verifyTrue(message, object == null);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * an AssertionFailedError is thrown with the given message.
     */
    static public void verifySame(String message, Object expected, Object actual) {
        if (expected == actual) {
            return;
        }
        failNotSame(message, expected, actual);
    }

    /**
     * Asserts that two objects refer to the same object. If they are not
     * the same an AssertionFailedError is thrown.
     */
    static public void verifySame(Object expected, Object actual) {
        verifySame(null, expected, actual);
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown with the
     * given message.
     */
    static public void verifyNotSame(String message, Object expected, Object actual) {
        if (expected == actual) {
            failSame(message);
        }
    }

    /**
     * Asserts that two objects do not refer to the same object. If they do
     * refer to the same object an AssertionFailedError is thrown.
     */
    static public void verifyNotSame(Object expected, Object actual) {
        verifyNotSame(null, expected, actual);
    }

    static public void failSame(String message) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected not same");
    }

    static public void failNotSame(String message, Object expected, Object actual) {
        String formatted = (message != null) ? message + " " : "";
        fail(formatted + "expected same:<" + expected + "> was not:<" + actual + ">");
    }

    static public void failNotEquals(String message, Object expected, Object actual) {
        fail(format(message, expected, actual));
    }

    public static String format(String message, Object expected, Object actual) {
        String formatted = "";
        if (message != null && message.length() > 0) {
            formatted = message + " ";
        }
        return formatted + "expected:<" + expected + "> but was:<" + actual + ">";
    }
}

