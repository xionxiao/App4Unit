# AutoTestApp

JUnit like automatic testing application framework.

## Setup

### 1. Pre-request

- Android Studio 3.0

    <https://developer.android.com/studio/index.html>

- JUnit 4.12
- Appium

### 2. Compile & Run

#### Standalone

Clone the repo and import to Android Studio. Connect real device and Run AutoTestApp.

#### Auto run with multiple devices

1. Build the project

    ```shell
    ./gradlew app:assembleDebug
    ```

1. Start Appium Server

1. Get device list with adb

    ```shell
    adb devices
    ```

1. Run autotest script

    ```shell
    python script/autotest.py -d <device serail numbers seperated by comma>
    ```

    for more information execute

    ```shell
    python script/autotest.py -h
    ```

## Usage

### How To Add Test Cases

This test framework is very similar to JUnit4. If you familar with JUnit4 unit testing. You can create your own test use that schema.

#### 1. Add a new test class

```java
@Description("My Simple Test")
public class MyTestCase {
    @BeforeClass
    public static void setupClass() {
        // static setup before test class
    }

    @Before
    public void setup() {
        // Setup code
    }

    @Test
    public void myTestFunction1() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void myTestFunction2() {
        assertNotEquals(4, 2 + 1);
    }

    @After
    public void tearDown() {
        // Clean up
    }

    @AfterClass
    public static void tearDownClass() {
        // static teardown after test class
    }
}
```

#### 2. Create a TestRunner

```java
TestRunner runner = new TestRunner();
```

#### 3. Run test/suite

```java
//Run a single test
runner.run(MyTestCase.class)

//Run test suite
TestSuite suite = new TestSuite();
suite.add(MyTestCase.class)
runner.run(suite);
```

### Asserts

This test framework reuses [org.junit.Assert] as it's assertion mechanism. To use assertion in TestCase:

### Assertions

```java
import static com.sparktest.autotesteapp.framework.*
```

### Injection

You can use dagger dependency injection in test cases.

- Provide a Module

```java
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        injects = {
                TestTest.class,
        }
)
public class TestModule {
    Context context;

    public TestModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return this.context;
    }
}
```

- Use Inject in Test Case

```java
public class TestTest {
    @Inject
    Context context;

    @Test
    public void run() {
        assertNotNull(context);
    }
}
```

## Annotations

### @Description

Descriptions of TestCase and Test methods. Description will be displayed on TestCaseListView. If no @Description annotation, class name will be used.

### @Test

Methods to test. Methods annoted with @Test should be public method.

### @BeforeClass

Method annoted with @BeforeClass will be executed before class instantiated. This method should be public static method.

### @AfterClass

Method annoted with @AfterClass will be executed after all test method are finished or test case failed. This method should be public static method.

### @Before

Method annoted with @Before will be executed before every @Test method execution.

### @After

Method annoted with @After will be executed after every @Test method execution.

### @Ignore

Method or Class annoted with @Ignore will be ignored.

## Async await/resume

## ThreadSafe
