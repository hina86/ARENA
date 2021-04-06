package util;

import java.io.File;

/**
 * @author Iffat Fatima
 * @created on 27/10/2020
 */
public class Constants {
    public static final String DATA_LOCAL_TMP_DIR = "/data/local/tmp/";
    public static final String SCRIPT_FILE_SEPARATOR = "/";
    public static final String LOGCAT_FILE_NAME = "logcat.txt";
    public static final String CPU_FILE_NAME = "cpu.txt";
    //    public static final String PID_FILE_NAME = "pid.txt";
    public static final String NETWORK_FILE_NAME = "network.txt";
    public static final String BATTERY_FILE_NAME = "battery.txt";
    public static final String MEMORY_FILE_NAME = "memory.txt";
    public static final String VOLTAGE_FILE_NAME = "voltage.txt";

    //    public static final String UID_FILE_NAME = "uid.txt";
    public static final String TRACE_FILE_NAME = "tracelog.trace";
    public static final String POWER_MONITOR_OUTPUT_FILE_NAME = "currentvoltage";

    public static final String SCRIPT_RUNNER_APK = "script-runner.apk";

    public static final String SCRIPT_DIR_PATH = System.getProperty("user.home") + File.separator + "scripts";//debug + release
    public static String RES_DIR_PATH = System.getProperty("user.home") + File.separator + "scripts" + File.separator;//debug

    public static final String BASELINE_SH_FILE_NAME = "baseline.sh";
    public static final String INSTALL_SH_FILE_NAME = "install.sh";
    public static final String PARALLEL_SH_FILE_NAME = "parallel.sh";
    public static final String PUSH_BAT_FILE_NAME = "push.bat";
    public static final String LAUNCH_SCRIPT_BAT_FILE_NAME = "launch.bat";
    public static final String PULL_BAT_FILE_NAME = "pull.bat";
    public static final String STOP_BAT_FILE_NAME = "stop.bat";
    public static final String POWER_MONITOR_SCRIPT_FILE_NAME = "start_power_monitor.py";
    public static final String _R = "_R";//for reading number prefix in file names
    public static final String _V = "_V";//for version number prefix in file names
    public static final String BASELINE_ = "BASELINE_"; //prefix for baseline
    public static final String API_VERSION_BAT = "api.bat";
    public static final String GET_COL_NAMES = "getColNames.bat";
    public static final String PLOT_BAT = "plot.bat";
    public static final String GET_COL_VALUES = "getColumnValues.bat";
    public static final String ANA_BAT = "analysis.bat";

    public static String DEVICES_BAT = "devices.bat";

    public static class Analysis {
        public static final String SENT_BYTES = "sentBytes";
        public static final String SENT_PCKTS = "sentPackets";
        public static final String RCV_BYTES = "rcvBytes";
        public static final String RCV_PCKTS = "rcvPackets";
        public static final String TYPE = "type";
        public static final String CLEAN_DIR = "CleanFiles";
        public static String V_ = "V";
        public static String _CLEAN_ = "_CLEAN_";
        public static final String AVG = "_AVG";
    }

    public static class Scripts {
        public static final String PY_SCRIPT_PM = "import sys\n" +
                "\n" +
                "import subprocess\n" +
                "\n" +
                "def install(package):\n" +
                "    subprocess.check_call([sys.executable, \"-m\", \"pip\", \"install\", package])\n" +
                "install('Monsoon')\n" +
                "\n" +
                "import Monsoon.HVPM as HVPM\n" +
                "import csv,datetime, time\n" +
                "import Monsoon.sampleEngine as sampleEngine\n" +
                "import Monsoon.Operations as op\n" +
                "import Monsoon.pmapi as pmapi\n" +
                "import numpy as np\n" +
                "\n" +
                "    \n" +
                "def testHVPM(serialno,Protocol,STOP_TIME, CSV_FILE_FOR_WRITING_SAMPLES):\n" +
                "    \n" +
                "    HVMON = HVPM.Monsoon()\n" +
                "    HVMON.setup_usb(serialno,Protocol)\n" +
                "    \n" +
                "    print(\"HVPM Serial Number: \" + repr(HVMON.getSerialNumber()))\n" +
                "    HVMON.fillStatusPacket()\n" +
                "    HVMON.setVout(4.2)\n" +
                "    HVengine = sampleEngine.SampleEngine(HVMON)\n" +
                "   \n" +
                "    #Turning on periodic console outputs.\n" +
                "    HVengine.ConsoleOutput(False)\n" +
                "    \n" +
                "    #Setting all channels enabled\n" +
                "    HVengine.enableChannel(sampleEngine.channels.MainCurrent)\n" +
                "    HVengine.enableChannel(sampleEngine.channels.MainVoltage)\n" +
                "    #HVengine.disableChannel(sampleEngine.channels.USBCurrent)\n" +
                "    #HVengine.disableChannel(sampleEngine.channels.USBVoltage)\n" +
                "    HVengine.disableChannel(sampleEngine.channels.AuxCurrent)\n" +
                "    HVengine.enableChannel(sampleEngine.channels.USBCurrent)\n" +
                "    HVengine.enableChannel(sampleEngine.channels.USBVoltage)\n" +
                "    #HVengine.enableChannel(sampleEngine.channels.AuxCurrent)\n" +
                "    HVengine.enableChannel(sampleEngine.channels.timeStamp)\n" +
                "    HVMON.setUSBPassthroughMode(op.USB_Passthrough.Auto)\n" +
                "\n" +
                "    #Setting trigger conditions\n" +
                "    numSamples=sampleEngine.triggers.SAMPLECOUNT_INFINITE\n" +
                "\n" +
                "    HVengine.setStartTrigger(sampleEngine.triggers.GREATER_THAN,0) \n" +
                "    HVengine.setStopTrigger(sampleEngine.triggers.GREATER_THAN,STOP_TIME) # pass 85 as parameter from screen\n" +
                "    HVengine.setTriggerChannel(sampleEngine.channels.timeStamp) \n" +
                "    #Setting trigger conditions\n" +
                "    #numSamples=5000*60\n" +
                "    \n" +
                "    #adb1('C:/Users/hina/AppData/Local/Android/android-sdks/platform-tools/dm.bat')\n" +
                "\n" +
                "    #sampling start at this time\n" +
                "    t=gettime_ntp()\n" +
                "    \n" +
                "    #Actually start collecting samples\n" +
                "    #startSampling() continues until the trigger conditions have been met, and then ends automatically.\n" +
                "    HVengine.startSampling(numSamples)   # progress bar should show this smapling progress till the time runs out.\n" +
                "    \n" +
                "    #Get those samples as a Python list\n" +
                "    #Samples has the format  [[timestamp], [mainCurrent], [usbCurrent], [auxCurrent], [mainVolts],[usbVolts]]\n" +
                "    samples = HVengine.getSamples()\n" +
                "    \n" +
                "    #Perform analysis on the resulting data.  For example, in order to calculate, perform the following:\n" +
                "    #Current = samples[sampleEngine.channels.MainCurrent]\n" +
                "    #mainVoltage = samples[sampleEngine.channels.MainVoltage]\n" +
                "    \n" +
                "    #mainCurrent is given in mA.  Divide by 1000 to convert to Amps\n" +
                "    #scaledMainCurrent = [x / 1000 for x in Current]\n" +
                "    \n" +
                "    #Element-wise multiply to produce Watts. Power = Current * Voltage.  \n" +
                "    #mainPower = np.multiply(scaledMainCurrent, mainVoltage)\n" +
                "    with open(CSV_FILE_FOR_WRITING_SAMPLES, 'wb') as myfile:  #on screen ask for file name after settting and replace tht name\n" +
                "        wr = csv.writer(myfile, delimiter=',')\n" +
                "        wr.writerow(['sampling start at', t])\n" +
                "        #wr.writerow(['timeStamp', 'Current', 'mainVoltage', 'CurrentAmp', 'power','auxCurrent', 'usbCurrent','usbVoltage'])\n" +
                "        wr.writerow(['timeStamp', 'Current', 'mainVoltage'])\n" +
                "        \n" +
                "        for i in range(len(samples[sampleEngine.channels.timeStamp])):\n" +
                "            timeStamp =datetime.datetime.fromtimestamp((samples[sampleEngine.channels.timeStamp][i])+t).strftime('%Y-%m-%d %H:%M:%S.%f')\n" +
                "            #timeStamp = samples[sampleEngine.channels.timeStamp][i]\n" +
                "            Current = samples[sampleEngine.channels.MainCurrent][i]\n" +
                "            #auxCurrent = samples[sampleEngine.channels.AuxCurrent][i]\n" +
                "            #usbCurrent = samples[sampleEngine.channels.USBCurrent][i]\n" +
                "            mainVoltage = samples[sampleEngine.channels.MainVoltage][i]\n" +
                "            #usbVoltage = samples[sampleEngine.channels.USBVoltage][i]\n" +
                "            #CurrentAmp=scaledMainCurrent[i]\n" +
                "            #power_Watts=mainPower[i]\n" +
                "            wr.writerow([timeStamp, Current, mainVoltage])  # show on screen that wrting done and file saved. once the file is save then and only then run dm3.bat\n" +
                "            #wr.writerow([timeStamp, Current, mainVoltage, CurrentAmp, power_Watts])\n" +
                "            #wr.writerow([timeStamp, Current, mainVoltage, CurrentAmp, power, auxCurrent, usbCurrent,usbVoltage])\n" +
                "    time.sleep(3)\n" +
                "#--------------------------------------------------------\n" +
                "\n" +
                "def adb1(filepath= ''):\n" +
                "\n" +
                "    import subprocess\n" +
                "    from subprocess import Popen, PIPE\n" +
                "    p = subprocess.Popen(filepath,stdout=PIPE, stderr=PIPE)\n" +
                "    p.wait()\n" +
                "    stdout, stderr = p.communicate()\n" +
                "    #print stderr\n" +
                "    #print stdout\n" +
                "   # print p.returncode # is 0 if success\n" +
                "\n" +
                "\n" +
                "#----------------------------------------------------------\n" +
                "\n" +
                "def adb2(filepath= ''):\n" +
                "\n" +
                "    import subprocess\n" +
                "    from subprocess import Popen\n" +
                "    p = subprocess.Popen(filepath)\n" +
                "    #print p.returncode # is 0 if success\n" +
                "    \n" +
                "#------------------------------------------------------------\n" +
                "#time https://gist.github.com/guneysus/9f85ab77e1a11d0eebdb\n" +
                "#------------------------------------------------------------\n" +
                "def gettime_ntp(addr='1.ee.pool.ntp.org'):\n" +
                "    # http://code.activestate.com/recipes/117211-simple-very-sntp-client/\n" +
                "    import socket\n" +
                "    import struct\n" +
                "    import sys\n" +
                "    import time\n" +
                "    #TIME1970 = 2208988800L      # Thanks to F.Lundh\n" +
                "    client = socket.socket( socket.AF_INET, socket.SOCK_DGRAM )\n" +
                "    data = '\\x1b' + 47 * '\\0'\n" +
                "    client.sendto( data, (addr, 123))\n" +
                "    data, address = client.recvfrom( 1024 )\n" +
                "    if data:\n" +
                "        t = struct.unpack( '!12I', data )[10]\n" +
                "    #    t -= TIME1970\n" +
                "        #return time.ctime(t),t\n" +
                "        return t\n" +
                "\n" +
                "#------------------------\n" +
                "\n" +
                "def main(STOP_TIME, CSV_FILE_FOR_WRITING_SAMPLES):\n" +
                "\n" +
                "    HVPMSerialNo = 21304\n" +
                "    testHVPM(HVPMSerialNo,pmapi.USB_protocol(),STOP_TIME,CSV_FILE_FOR_WRITING_SAMPLES)\n" +
                "    \n" +
                "\n" +
                "#if __name__ == \"__main__\":\n" +
                "STOP_TIME= sys.argv[1]\n" +
                "CSV_FILE_FOR_WRITING_SAMPLES= sys.argv[2]\n" +
                "print (\"a is:\", STOP_TIME)\n" +
                "print (\"b is:\", CSV_FILE_FOR_WRITING_SAMPLES)\n" +
                "\n" +
                "STOP_TIME= int (STOP_TIME)\n" +
                "#b= int(b)\n" +
                "\n" +
                "main(STOP_TIME,CSV_FILE_FOR_WRITING_SAMPLES)\n" +
                "    \n" +
                "\n" +
                " \n";
    }


}
