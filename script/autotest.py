from appium import webdriver
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import WebDriverException 
import os,sys,getopt,time,threading,Queue
from junit_xml import TestSuite, TestCase

APK_PATH = os.getcwd() + '/app/build/outputs/apk/debug/app-debug.apk'
RESULT_FILE = 'app/build/outputs/testresult.xml'

device_list = ["HT7360201945","988627323038534235"]

config = {
    'platformName': 'Android',
    'app': APK_PATH,
    'newCommandTimeout': 3600
}

remote = 'http://127.0.0.1:4723/wd/hub'

package = "com.sparktest.autotesteapp"


def loop(interval=0, timeout=0, times=0):
    if times:
        __max_times = times if not interval else min(timeout/interval, times)
        __max_times = __max_times if __max_times else times
    else:
        __max_times = 0 if not interval else timeout/interval

    def __decorated(fn):
        def __wrapper(*args, **kwargs):
            __try_out = __max_times
            while True:
                # print("#Try %d in max %d interval %d" % (__try_out, __max_times, interval))
                try:
                    # print("run %s" % fn.__name__)
                    result = fn(*args, **kwargs)
                except NoSuchElementException:
                    result = False
                if result:
                    return result
                else:
                    __try_out -= 1
                    if __try_out: 
                        time.sleep(interval)
                    else:
                        return False
        return __wrapper
    return __decorated

class Device:
    def __init__(self, remote, config):
        self.__remote = remote
        self.__config = config

    def start_app(self):
        try:
            self.__driver = webdriver.Remote(self.__remote, self.__config)
        
            # sleep more than 2 seconds waiting for permission grant in app
            time.sleep(3)

            # allow video permission
            self.allow_permission()

            #time.sleep(1)

            # allow audio permission
            self.allow_permission()

            time.sleep(1)
            self.__driver.background_app(1)
            time.sleep(1)

            self.__list_view = self.__driver.find_element_by_class_name("android.widget.ListView")
            print(self.__driver.capabilities["deviceName"], "boot complete!")
            return self.__driver 

        except WebDriverException,e:
            print("WebDriverException:",e, self.__config)
            return None
        except Exception,e:
            print("Exception: thread-%s"%threading.current_thread(),e,e.args)
            return None 

    @loop(1, 3)
    def allow_permission(self):
        el = self.__driver.find_element_by_id("com.android.packageinstaller:id/permission_allow_button")
        el and el.click()
        return el

    @loop(1, 1)
    def run(self, suite_index, case_index):
        case = self.case(suite_index, case_index)
        c_name = self.case_name(case)
        p = self.case_package(case)
        print(c_name)
        b = case.find_element_by_class_name("android.widget.Button")
        b.click()
        start = time.time()
        result = self.check_result(b)
        elapsed = time.time() - start
        print(elapsed)
        return (c_name, p, elapsed, result)

    @loop(1)
    def check_result(self, button):
        return False if button.text == "RUNNING" else button.text


    @loop(1, 1)
    def suite(self, suite_index):
        query = "new UiSelector().className(\"android.view.ViewGroup\").description(\"%d\")" % suite_index 
        return self.__list_view.find_element_by_android_uiautomator(query)


    @loop(1, 1)
    def suite_name(self, suite):
        return suite.find_element_by_class_name("android.widget.TextView").text


    @loop(1, 1)
    def case(self, suite_index, case_index):
        suite = self.suite(suite_index)
        query = "new UiSelector().className(\"android.widget.LinearLayout\").description(\"%d\")" % case_index 
        return suite.find_element_by_android_uiautomator(query)


    @loop(1, 1)
    def case_name(self, case):
        return case.find_element_by_class_name("android.widget.TextView").text


    @loop(1, 1)
    def case_package(self, case):
        return case.find_element_by_class_name("android.widget.TextView").get_attribute("name")


    @loop(1, 1)
    def cases(self, suite):
        query = "new UiSelector().className(\"android.widget.LinearLayout\")"  
        return suite.find_elements_by_android_uiautomator(query)


    @loop(1, 1)
    def suites_on_screen(self):
        query = "new UiSelector().className(\"android.view.ViewGroup\")"
        suites = self.__list_view.find_elements_by_android_uiautomator(query)
        return suites 


@loop(1, 3)
def start_devices(udids):
    devices = []
    que = Queue.Queue()

    for d in udids:
        c = config.copy()
        c['deviceName'] = d
        c['udid'] = d
        device = Device(remote, c)
        que.put(device.start_app())
        devices.append(device)
        time.sleep(5)
    #threads = [threading.Thread(target=lambda q, d: q.put(d.start_app()), args=(que,device)) for device in devices]
    #[t.start() for t in threads]
    #[t.join() for t in threads]

    return devices if all(list(que.queue)) else []


def run_suite(devices, suite_index):
    l = len(devices)
    que = Queue.Queue()
    threads = [threading.Thread(
        target=lambda que, device, suite_index, case_index: que.put(device.run(suite_index, case_index)), 
        args=(que, devices[i], suite_index, i)) for i in range(l)]
    [t.start() for t in threads]
    [t.join() for t in threads]
    results = list(que.queue)
    print(results)
    return results


if __name__ == '__main__':
    try:
        opts, args = getopt.getopt(sys.argv[1:],"p:d:a:o:")
    except getopt.GetoptError:
        sys.exit(2)

    for opt, arg in opts:
        if opt == '-p':
            remote = "http://" + arg + "/wd/hub"
        elif opt == '-d':
            device_list = arg.split(",")
        elif opt == '-a':
            config['app'] = arg
        elif opt == '-o':
            RESULT_FILE = arg

    devices = start_devices(device_list)
    if not devices:
        print("Failed to connect devices")
        sys.exit(-1)

    current = 0
    results = []
    while True:
        suite = devices[0].suite(current)
        name = devices[0].suite_name(suite)
        r = run_suite(devices, current)
        results.append({name:r})
        current += 1
        time.sleep(3)
        suites = devices[0].suites_on_screen()
        last = int(suites[len(suites)-1].get_attribute("name"))
        if current > last:
            break
    print("done!")

    ts = []
    for d in results:
        tc = []
        for r in d.values()[0]:
            t = TestCase(r[0], r[1], r[2], r[3] if r[3] == "PASSED" else "", r[3] if r[3] == "FAILED" else "")
            if r[3] == "FAILED":
                t.add_failure_info(r[1], r[3])
            tc.append(t)
        ts.append(TestSuite(d.keys()[0], tc))

    print(TestSuite.to_xml_string(ts))
    with open(RESULT_FILE, 'w') as f:
        TestSuite.to_file(f, ts, prettyprint=False)
