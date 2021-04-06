package util;

import cleanup.CleanupEventListener;
import cleanup.models.*;
import com.intellij.openapi.util.Pair;
import cleanup.models.ExpTime;
import cleanup.models.ComputedPMResults;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static util.Constants.Analysis.*;

public class FileParser {

//    public static void main(String [] args){
//        parseLogcat(new File("C:\\Users\\iffat\\Downloads\\logcat_R0.txt"),
//                "6094",
//                "Timeline: Activity_launch_request id:org.secuso.privacyfriendlysudoku time:531867",
//                "GeneratorService: Generated: Default_12x12,\tHard",
//                "1",
//                new File("C:\\Users\\iffat\\Desktop\\Test\\CleanFiles"),
//                null
//        );
//    }
    /**
     *
     * @param filePath of the file containing UID
     * @return UID as string
     */
    public static String getUid(String filePath) {
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains("userId")) {
                    StringTokenizer tokenizer = new StringTokenizer(strLine);
                    while (tokenizer.hasMoreTokens()){
                        String token = tokenizer.nextToken();
                        if (token.contains("userId")){
                            System.out.println("User id: "+ token.split("=")[1]);
                            return token.split("=")[1];
                        }
                    }
                }
            }
            fstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }


    /**
     *
     * @param filePath of the pid.txt
     * @param packageName of the application whose PID is required.
     * @return PID of the file
     * Note: This format of PID file is same across all versions hence this function works for all
     */
    public static PID getPid(String filePath, String packageName) {
        PID pid = new PID();
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            // Read File Line By Line, Check line that contains package name and gets its second token as it is the PID
            while ((strLine = br.readLine()) != null) {
                if (strLine.contains(packageName)){
                    StringTokenizer st = new StringTokenizer(strLine);
                    st.nextToken();
                    pid.PID = st.nextToken();
                    System.out.println("PID found: "+ pid.PID);
                }
            }
            // Close the input stream
            fstream.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        return pid;
    }

    /**
     *
     * @param filePath on which cpu.txt is present
     * @param packageName of the application under test
     * @param apiVersion of the device on which experiment is run
     * @return CPUInfo object
     */

    public static CPUInfo parseCpuPercentage(String filePath, String packageName, int apiVersion){
        CPUInfo cpuInfo = new CPUInfo();
        cpuInfo.deviceApiVersion = apiVersion;
        int limit = 15;
        int cpuCol = -1;
        int vssCol = -1;
        int vszCol = -1;
        int rssCol = -1;
        int memCol = -1;
        //based on api version we initilize the value for column. Col value was obtained by comparing the formats of api version 19-29
        if (apiVersion <=22) {
            cpuCol = 2;
            vssCol = 5;
        } else if (apiVersion == 23){
            cpuCol = 2;
            vssCol = 5;
            packageName = packageName.substring(packageName.length()-15);//for API 23, the package name is split into two parts such that the last part contains 14 letters. So we split our package name by 14 + 1(as index starts at 0)
            System.out.println(packageName);
        } else if (apiVersion == 24 || apiVersion == 25){
            cpuCol = 4;
            vssCol = 7;
        }else if (apiVersion <29){
            cpuCol = 8;
            memCol = 9;
        } else if (apiVersion >= 29){
            cpuCol = 8;
            memCol = 9;
        }
        //We only match for 15 characters of package name as characters after this are delimited in the adb output
        String delimitedPckName = packageName.substring(0, Math.min(packageName.length(), limit));
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            // Read File Line By Line, Check line that contains package name and gets its second token as it is the PID
            while ((strLine = br.readLine()) != null) {
//                strLine = removeESC(strLine);
                if (strLine.contains("%cpu")){
                    cpuInfo.cores = (int)(Float.parseFloat(strLine.substring(0, strLine.indexOf("%")))/100);
                }
                if (strLine.contains(delimitedPckName)){
                    StringTokenizer st = new StringTokenizer(strLine);
                    int i=0;
                    while (st.hasMoreTokens()){
                        String token = st.nextToken();
                        if (i == cpuCol && cpuCol != -1){
                            cpuInfo.getCpuUsageList().add(getNumericPercentage(token));
                        } else if (i == memCol && memCol != -1){
                            cpuInfo.getMemoryUsageList().add(getNumericPercentage(token));
                        }else if (i == vssCol && vssCol != -1){
                            cpuInfo.getVSSList().add(getKRemoved(token));
                        }else if (i == vssCol+1 && vssCol != -1){//rss col
                            cpuInfo.getRSSList().add(getKRemoved(token));
                        }else if (i == vszCol && vszCol != -1){
//                            cpuInfo.getVSZList().add(getKRemoved(token));
                        }
                        i++;
                    }
                }
            }
            // Close the input stream
            fstream.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        cpuInfo.cpuUsage = cpuInfo.computeCpu();
        cpuInfo.memory = cpuInfo.computeMem();
        cpuInfo.vss = cpuInfo.computeVss();
        cpuInfo.rss = cpuInfo.computeRss();
        System.out.println( cpuInfo.toString());
        return cpuInfo;
    }

    private static String removeESC(String str) {
        StringBuilder resultStr = new StringBuilder();
        for (int i=0;i<str.length();i++) {
            if (str.charAt(i)!=27) {
                resultStr.append(str.charAt(i));
            }
        }
        return resultStr.toString();
    }

    private static Float getKRemoved(String token) {
        token = token.replace("K", "");
        return Float.parseFloat(token);
    }

    /**
     * Reads
     * @param file and prints the log filtered by
     * @param pid
     * param startTag and
     * @param endTag  If start and end tags are not found then only pid filter is applied
     * @param analysisVersion of the app is concatenated with the cleaned file
     * @param cleanDir is where the returned file is saved
     * @return clean logcat file
     */
    public static File parseLogcat(File file,
                                   String pid,
                                   String startTag,
                                   String endTag,
                                   long stopTime,
                                   String analysisVersion,
                                   File cleanDir,
                                   CleanupEventListener eventListener){
        StringBuilder pidMatched = new StringBuilder();
        String cleanFilePath = cleanDir.getAbsolutePath() + File.separator + Constants.Analysis.V_ + analysisVersion + _CLEAN_ +file.getName();
        try {
            FileWriter fileWriter = new FileWriter(cleanFilePath);
            try {
                FileInputStream fstream = new FileInputStream(file.getAbsoluteFile());
                BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                String strLine;
                // Read File Line By Line, Check line that contains package name and gets its second token as it is the PID
                boolean startFound = false;
                boolean endFound = false;
                long startTime = 0;
                long endTime = 0;
                while ((strLine = br.readLine()) != null) {
                    boolean pidFound = false;
                    if (!strLine.isEmpty()) {
                        StringTokenizer st = new StringTokenizer(strLine);
                        if (st.hasMoreTokens()) {
                            int i = 0;
                            while (i < 2) {
                                st.nextToken();
                                i++;
                            }
                            String token = st.nextToken();
                            if (token.contains(pid)) {
                                pidFound = true;
                                pidMatched.append(strLine).append("\n");//keep logcat value in string in case tags are not found, this will be used to write the file to avoid extra lopping
                                if (!endFound) {
                                    if(st.hasMoreTokens()) {
                                        st.nextToken();
                                    }
                                    if (st.hasMoreTokens()) {
                                        st.nextToken();
                                    }
                                    if (st.hasMoreTokens()) {
                                        String line = strLine.substring(strLine.lastIndexOf(st.nextToken()));
                                        if (line.equals(startTag) && !startTag.isEmpty()) {
                                            System.out.println("Start found");
                                            startFound = true;
                                        } else if (line.equals(endTag) && !endTag.isEmpty()) {
                                            System.out.println("end found");
                                            endFound = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (startFound && pidFound) {
                        System.out.println("W: "+ strLine);
                        fileWriter.write(strLine + "\n");
                        if (endFound){
                            System.out.println("Ending");
                            fileWriter.close();
                            fstream.close();
                            break;
                        }
                    }
                }
                //if tags are not found then just write the pid based file
                if (!startFound || !endFound){
                    System.out.println("writing without tags");
                    fileWriter = new FileWriter(cleanFilePath);
                    fileWriter.write(pidMatched.toString());
                    fileWriter.close();
                    fstream.close();
                    eventListener.printCleanupMessage("No tags found in File " + file.getName() + " File is filtered by PID only");
                } else {
                    eventListener.printCleanupMessage("File " + file.getName() + "is filtered by tags and PID");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        return new File(cleanFilePath);
    }

    /**
     *
     * @param filePath on which network.txt is present
     * @param uid of the app
     * @return NetworkInfo object
     */
    public static ArrayList<NetworkInfo> parseNetworkFile(String filePath, String uid) {
//        uid = "1000";
        ArrayList<NetworkInfo> networkInfoList = new ArrayList();
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            boolean nextLineIsActiveInterfaces = false;
            boolean uidStatsStarted = false;
            // Read File Line By Line, Check line that contains package name and gets its second token as it is the PID
            while ((strLine = br.readLine()) != null) {
                //line 1 has uid, line 2 info is not needed, line 3 has snd/rcv byte no.
                if (uidStatsStarted){
                    if (strLine.contains("uid="+uid)){
                        System.out.println("Line "+ strLine);
                        NetworkInfo networkInfo = new NetworkInfo();
                        StringTokenizer tokenizer = new StringTokenizer(strLine);
                        while(tokenizer.hasMoreTokens()) {
                            String token = tokenizer.nextToken();

                            if (token.contains("set")) {
                                String[] splitted = token.split("=");
                                if (splitted != null && splitted.length > 0) {
                                    String set = splitted[splitted.length - 1];
                                    set = set.replace(",", "");
                                    networkInfo.set = set;
                                }
                            }
                        }
                        String data = getBetween(strLine, "{", "}");
                        StringTokenizer dataTokenizer = new StringTokenizer(data);
                        while (dataTokenizer.hasMoreTokens()){
                            String token = dataTokenizer.nextToken();
                            token = token.replace(",", "");
                            System.out.println("Token: "+ token);
                            String[] splitTokens = token.split("=");
                            if (splitTokens.length >=2){
                                switch (splitTokens[0]) {
                                    case "type":
                                        networkInfo.type = splitTokens[1];
                                        break;
                                    case "subType":
                                        networkInfo.subtype = splitTokens[1];
                                        break;
                                    case "networkId":
                                        String id = splitTokens[1];
                                        id = id.replace("\"", "");
                                        networkInfo.networkId = id;
                                        break;
                                    case "metered":
                                        networkInfo.metered = splitTokens[1].equals("true");
                                        break;
                                    case "defaultNetwork":
                                        networkInfo.defaultNetwork = splitTokens[1];
                                        break;
                                }
                            }

                        }

                        br.readLine();//read extra line as information is in third line
                        String dataLine = br.readLine();
                        tokenizer = new StringTokenizer(dataLine);
                        while (tokenizer.hasMoreTokens()){
                            String token = tokenizer.nextToken();
                            String [] splitTokens = token.split("=");
                            if (splitTokens != null && splitTokens.length > 0) {
                                String type = splitTokens[0];
                                //API 19 has rb,rp,tb,tp identifiers
                                switch (type) {
                                    case "rb":
                                    case "rxBytes":
                                        networkInfo.receivedBytes = (Long.parseLong(splitTokens[1]));
                                        break;
                                    case "rp":
                                    case "rxPackets":
                                        networkInfo.receivedPackets = (Long.parseLong(splitTokens[1]));
                                        break;
                                    case "tb":
                                    case "txBytes":
                                        networkInfo.sentBytes = (Long.parseLong(splitTokens[1]));
                                        break;
                                    case "tp":
                                    case "txPackets":
                                        networkInfo.sentPackets = (Long.parseLong(splitTokens[1]));
                                        break;
                                }
                            }
                        }
                        networkInfoList.add(networkInfo);
                    }
                }
//                if (strLine.toLowerCase().contains(("Active Interfaces:").toLowerCase())){
//                    String dataLine = br.readLine();
//                    StringTokenizer tokenizer = new StringTokenizer(dataLine);
//                    while (tokenizer.hasMoreTokens()) {
//                        String token = tokenizer.nextToken();
//                        if (token.contains("ident")){
//                            String [] splitted = token.split("=");
//                            if (splitted != null && splitted.length > 0){
//                                String type = splitted[splitted.length-1];
//                                type = type.replace(",", "");
//                                networkInfo.setType(type);
//                            }
//                        }
//                    }
//                }
                if (strLine.toLowerCase().contains(("UID stats:").toLowerCase())){
                    uidStatsStarted = true;
                }
            }
            // Close the input stream
            fstream.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        System.out.println("Complete");
        for (NetworkInfo networkInfo: networkInfoList){
            System.out.println("Info: "+ networkInfo.toString());
        }
        return networkInfoList;
    }

    private static String getBetween(String string, String prefix, String suffix) {
        string = string.substring(string.indexOf(prefix) + 1);
        string = string.substring(0, string.indexOf(suffix));
        return string;
    }


    /**
     *
     * Reads file on @param filePath with memory data and parses it to
     * @return MemoryInfo object
     */
    public static MemoryInfo parseMemoryFile(String filePath){
        MemoryInfo memoryInfo = new MemoryInfo();
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            boolean summaryFound = false;
            boolean memUsageFound = false;
            while ((strLine = br.readLine()) != null) {
                if (summaryFound){
                    StringTokenizer tokenizer = new StringTokenizer(strLine);
                    while (tokenizer.hasMoreTokens()) {
                        tokenizer.nextToken();
                        if (strLine.contains("TOTAL:")) {
                            String token = tokenizer.nextToken();
                            if (memoryInfo.totalPercentage != 0f) {
                                memoryInfo.totalPercentage = (getNumericPercentage(token));
                            }
                            break;
                        } else if (strLine.contains("Top:")) {
                            memoryInfo.top = (getNumericPercentage(tokenizer.nextToken()));
                            break;
                        } else if (strLine.contains("Imp Fg:")) {
                            tokenizer.nextToken();
                            memoryInfo.impFg = (getNumericPercentage(tokenizer.nextToken()));
                            summaryFound = false;
                            break;
                        } else {
                            if (tokenizer.hasMoreTokens()) {
                                tokenizer.nextToken();
                            }
                        }
                    }
                }
                if (memUsageFound){
                    StringTokenizer tokenizer = new StringTokenizer(strLine);
                    while (tokenizer.hasMoreTokens()) {
                        tokenizer.nextToken();
                        if (strLine.contains("TOTAL")) {
                            summaryFound = false;
                            String[] tokens = strLine.split(":", 2);
                            if (tokens != null && tokens.length >0) {
                                String value = tokens[1];
                                value = value.trim();//removes extra spaces
                                String unit = value.substring(value.length() - 2);
                                value = value.substring(0, value.length() - 2);//remove last 2 characters defining the unit
                                memoryInfo.memoryUsage = (getBytes(value, unit));
                            }
                        } else if (strLine.contains("Start time:")) {
                            String[] time = strLine.split(":", 2);//split applied limit-1 i.e. 1 time
                            if (time != null && time.length > 1){
                                String timeStr = time[1].trim();
                                memoryInfo.startTime = timeStr;
                            }
                        } else if (strLine.contains("Total elapsed time:")) {
                            String[] time = strLine.split(":");
                            if (time != null && time.length > 1){
                                String timeStr = time[1];
                                memoryInfo.elapsedTime = (convertTimetoMs(timeStr));
                            }
                            break;
                        }
                    }
                }
                if (strLine.contains("Summary") || strLine.contains("Process summary")) {
                    summaryFound = true;
                } else if (strLine.contains("Memory usage:")){
                    summaryFound = false;
                    memUsageFound = true;
                }
            }
            // Close the input stream
            fstream.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        System.out.println(memoryInfo.toString());
        return memoryInfo;
    }

    /**
     *
     * Reads file on @param filePath with battery data and parses it to
     * @return Battery Info object
     */
    public static BatteryInfo parseBatteryFile(String filePath){
        File voltageFile = new File(filePath.replace("battery", "voltage"));
        BatteryInfo batteryInfo = new BatteryInfo();
        batteryInfo.voltage = getVoltage(voltageFile);
        try {
            FileInputStream fstream = new FileInputStream(filePath);
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String strLine;
            boolean sinceLastChargedStatsFound = false;
            boolean powerUseFound = false;
            while ((strLine = br.readLine()) != null) {
                if (sinceLastChargedStatsFound){
                    String [] tokens = strLine.split(":|,");
                    for (int i=0; i< tokens.length; i++) {
                        if (tokens[i].contains("System starts")) {
                            batteryInfo.systemStarts = tokens[i+1].trim();
                            if (tokens.length >=i+4) {
                                batteryInfo.currentlyOnBattery = tokens[i+3].trim();
                            }
                        } else if (tokens[i].contains("Time on battery")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.timeOnBattery = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Total run time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("realtime"));
                            batteryInfo.totalRunTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Mobile total received")){
                            batteryInfo.mobileTotalReceived = tokens[i+1].replace("B", "").trim();
                        } else if (tokens[i].contains("Wi-Fi total received")){
                            batteryInfo.wiFiTotalReceived = tokens[i+1].replace("B", "").trim();
                            if (tokens.length >=i+4) {
                                String value = tokens[i+3].substring(0, tokens[i+3].indexOf("("));
                                value = value.replace("B", "");
                                batteryInfo.wiFiTotalSent = value.trim();
                            }
                        } else if (tokens[i].contains("Wifi data received") ){
                            batteryInfo.wiFiTotalReceived = tokens[i+1].replace("B", "").trim();
                        } else if (tokens[i].contains("Wifi data sent") ){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            value = value.replace("B", "");
                            batteryInfo.wiFiTotalSent = value.trim();
                        } else if (tokens[i].contains("Total full wakelock time")){
                            batteryInfo.totalFullWakelockTime = convertTimetoMs(tokens[i+1].trim());
                        } else if (tokens[i].contains("Signal scanning time")){
                            batteryInfo.signalScanningTime = convertTimetoMs(tokens[i+1].trim());
                        } else if (tokens[i].contains("Wifi on")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.wifiOn = convertTimetoMs(value.trim());
                            if (tokens.length >=i+4) {
                                value = tokens[i+1].substring(0, tokens[i+3].indexOf("("));
                                batteryInfo.wifiRunning = convertTimetoMs(value);
                                if (tokens.length >=i+6) {
                                    value = tokens[i+1].substring(0, tokens[i+5].indexOf("("));
                                    batteryInfo.bluetoothOn = convertTimetoMs(value);
                                }
                            }
                        } else if (tokens[i].contains("Radio data uptime when unplugged")){
                            batteryInfo.radioDataUptimeWhenUnplugged = convertTimetoMs(tokens[i+1]);
                        } else if (tokens[i].contains("Bluetooth on")){
                            String  value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.bluetoothOn =convertTimetoMs(value);
                        } else if (tokens[i].contains("Start clock time")){
                            batteryInfo.startClockTime = tokens[i+1].trim();
                        } else if (tokens[i].contains("Mobile radio active time")){
                            String  value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.mobileRadioActiveTime = convertTimetoMs(value);
                        } else if (tokens[i].contains("WiFi Rx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.wifiRxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("WiFi Tx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.wifiTxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Bluetooth Rx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.bluetoothRxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Bluetooth Tx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.bluetoothTxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Radio Rx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.radioRxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Radio Tx time")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.radioTxTime = convertTimetoMs(value.trim());
                        } else if (tokens[i].contains("Bluetooth Power drain")){
                            batteryInfo.bluetoothPowerDrain = tokens[i+1].trim().replace("mAh", "");
                        } else if (tokens[i].contains("WiFi Power drain")){
                            batteryInfo.wifiPowerDrain = tokens[i+1].trim().replace("mAh", "");
                        } else if (tokens[i].contains("Radio Power drain")){
                            batteryInfo.radioPowerDrain = tokens[i+1].trim().replace("mAh", "");
                        } else if (tokens[i].contains("Estimated battery capacity")){
                            batteryInfo.estimatedBatteryCapacity = removeSuffix(tokens[i+1]).replace(",", "");
                        } else if (tokens[i].contains("Discharge")){
                            batteryInfo.discharge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Screen off discharge")){
                            batteryInfo.screenOffDischarge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Screen on discharge")){
                            batteryInfo.screenOnDischarge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Screen doze discharge")){
                            batteryInfo.screenDozeDischarge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Device light doze discharge")){
                            batteryInfo.lightDozeDischarge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Device deep doze discharge")){
                            batteryInfo.deepDozeDischarge = removeSuffix(tokens[i+1]);
                        } else if (tokens[i].contains("Screen on")){
                            String value = tokens[i+1].substring(0, tokens[i+1].indexOf("("));
                            batteryInfo.screenOn = convertTimetoMs(value.trim());
                        }
                    }
                }
                else if (powerUseFound){
                    String [] tokens = strLine.split(":|,");
                    StringTokenizer stringTokenizer = new StringTokenizer(strLine);
                    while (stringTokenizer.hasMoreTokens()) {
                        String token = stringTokenizer.nextToken();
                        if (token.contains("Capacity")) {
                            batteryInfo.estimatedBatteryCapacity = stringTokenizer.nextToken().trim().replace(",", "");
                            token = stringTokenizer.nextToken();
                            if (stringTokenizer.hasMoreTokens()) {
                                stringTokenizer.nextToken();
                                batteryInfo.computedDrain = stringTokenizer.nextToken().trim().replace(",", "");
                                if (stringTokenizer.hasMoreTokens()) {
                                    stringTokenizer.nextToken();
                                    token = stringTokenizer.nextToken();
                                    batteryInfo.actualDrain = stringTokenizer.nextToken().trim().replace(",", "");
                                }
                            }
                        }
                        if (token.contains("Wifi")){
                            if (batteryInfo.wifiPowerDrain == null) {
                                batteryInfo.wifiPowerDrain = stringTokenizer.nextToken().trim();
                            }
                        } else  if (token.contains("Cell")){
                            stringTokenizer.nextToken();
                            batteryInfo.cellPowerDrain = stringTokenizer.nextToken().trim();
                        } else  if (token.contains("Screen")){
                            batteryInfo.screenPowerDrain = stringTokenizer.nextToken().trim();
                        } else  if (token.contains("Uid") && !token.contains("Uid 0:")){
                            stringTokenizer.nextToken();
                            batteryInfo.uidPowerDrain += Float.parseFloat(stringTokenizer.nextToken());
                        }
                    }
//                    for (int i=0; i< tokens.length; i++) {
//                        System.out.println(tokens[i]);
//                        if (tokens[i].contains("Capacity")){
//                            batteryInfo.estimatedBatteryCapacity = tokens[i+1].trim();
//                            if (tokens.length >=i+4) {
//                                batteryInfo.computedDrain = tokens[i+3].trim();
//                                if (tokens.length >=i+6) {
//                                    batteryInfo.actualDrain = tokens[i+5].trim();
//                                }
//                            }
//                        } else  if (tokens[i].contains("Wifi")){
//                            if (batteryInfo.wifiPowerDrain == null) {
//                                batteryInfo.wifiPowerDrain = tokens[i + 1].trim();
//                            }
//                        } else  if (tokens[i].contains("Cell")){
//                            batteryInfo.cellPowerDrain = tokens[i+1].trim();
//                        } else  if (tokens[i].contains("Screen")){
//                            batteryInfo.screenPowerDrain = tokens[i+1].trim();
//                        } else  if (tokens[i].contains("Uid") && !tokens[i].contains("Uid 0:")){
//                            batteryInfo.uidPowerDrain = (tokens[i+1]);
//                        }
//                    }
                }
                if (strLine.contains("Statistics since last charge:")) {
                    sinceLastChargedStatsFound = true;
                } else if (strLine.contains("Estimated power use")){
                    sinceLastChargedStatsFound = false;
                    powerUseFound = true;
                }
            }
            // Close the input stream
            fstream.close();
        } catch (Exception e) {// Catch exception if any
            e.printStackTrace();
        }
        System.out.println(batteryInfo.toString());
        return batteryInfo;
    }


    private static String removeSuffix(String token) {
        if (token.contains("mAh")) {
            token = token.substring(0, token.indexOf("mAh"));
        }
        return token.trim();
    }

    /**
     * Converts @param timeStr of the format +4m29s630ms to ms
     * @return time in milliseconds
     */
    private static long convertTimetoMs(String timeStr) {
        timeStr = timeStr.replaceAll( "[^\\d]", "-" );
        String [] timeArr = timeStr.split("-");
        long time = 0;
        int multiplier = 1;
        for (int i=timeArr.length -1; i >=0 ;  i--){
            if (!timeArr[i].isEmpty()) {
                time += Integer.parseInt(timeArr[i]) * multiplier;
                if (multiplier == 1) {
                    multiplier = multiplier * 1000;
                } else {
                    multiplier = multiplier * 60;
                }
            }
        }
        return time;
    }

    /**
     *
     * @return @param value converted to bytes based on @param unit
     */
    private static long getBytes(String value, String unit) {
        switch (unit) {
            case "KB":
                return (long) (Float.parseFloat(value) * 1024);
            case "MB":
                return (long) (Float.parseFloat(value) * 1024 * 1024);
            case "GB":
                return (long) (Float.parseFloat(value) * 1024 * 1024 * 1024);

        }
        return (long) Float.parseFloat(value);
    }

    private static float getNumericPercentage(String token) {
        if (token.contains("%")){
            token = token.replace("%", "");
        }
        return Float.parseFloat(token);
    }


    private static String getNumeric(String value) {
        Matcher m = Pattern.compile("\\d+").matcher(value);
        if (m.find()) {
            return m.group(0);
        }
        return value;
    }

    public static String getReadingNo(String name) {
        return StringUtils.substringBetween(name, "_R", ".");
    }

    public static ExpTime getStartEndTime(File file, long stopTime) {
        String firstTime = "";
        String lastTime = "";
        try {
            FileInputStream fstream = new FileInputStream(file.getAbsoluteFile());
            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
            String firstLine = br.readLine();
            String lastLine = "";
            StringTokenizer tokenizer = new StringTokenizer(firstLine);
            if (tokenizer.countTokens() >2) {
                firstTime = (LocalDate.now().getYear() + 70) + "-" +tokenizer.nextToken() + " " + tokenizer.nextToken() ;
            }
            String line = "";
            while((line = br.readLine())!= null){
                if (!line.isEmpty()){
                    lastLine = line;
                }
            }
            tokenizer = new StringTokenizer(lastLine);
            if (tokenizer.countTokens() > 2) {
                lastTime = (LocalDate.now().getYear() + 70) + "-" + tokenizer.nextToken() + " " + tokenizer.nextToken() ;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

        try {
            long startTime = format.parse(firstTime).getTime();
            long endTime = format.parse(lastTime).getTime();
            stopTime = 60;
            if (stopTime != 0) {
                if (endTime > (startTime + (stopTime * 1000))) {
                    endTime = startTime + (stopTime * 1000);
                }
            }
            ExpTime time = new ExpTime(startTime, endTime);
            System.out.println(time.toString());
            return time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static ComputedPMResults parseCurrentVoltageFile(File currentVoltageFile,
                                                            ExpTime expTime,
                                                            String analysisVersion,
                                                            File cleanDir,
                                                            String pcResultsPath,
                                                            String readingNo,
                                                            CleanupEventListener eventListener) {
        ComputedPMResults computedPMResults = computeAverage( currentVoltageFile, expTime, eventListener);
        return computedPMResults;
    }

    private static ComputedPMResults computeAverage(File currentVoltageFile,
                                                    ExpTime expTime,
                                                    CleanupEventListener eventListener) {
        System.out.println(currentVoltageFile.getName());
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
        try {
            FileReader fileReader = new FileReader(currentVoltageFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String row = "";
            boolean energyReadingLess = true;
            double powerSum = 0;
            double energySum = 0;
            row = reader.readLine();
            row = reader.readLine();
            int count = 0;
//            long timeDiff = expTime.endTime - expTime.startTime;
            String abc = "";
            int rows = 0;
            while ((row = reader.readLine()) != null) {
                rows++;
                String[] rowData = row.split(",");
                String cvDate = rowData[0].trim();//.substring(5);
                cvDate = cvDate.substring(0, cvDate.length()-3);
                try {
                    long currentVoltageTime = format.parse(cvDate).getTime();
//                    long startTime = 0;
                    if (currentVoltageTime >= expTime.endTime){
                        System.out.println("End time: "+ cvDate + "  " +currentVoltageTime + "  "+ expTime.endTime);
                        energyReadingLess = false;
                        break;
                    }
                    if (currentVoltageTime >= expTime.startTime/* && currentVoltageTime - expTime.startTime < timeDiff*/) {
                        if (abc.isEmpty()){
                            abc = String.valueOf(currentVoltageTime);
                            System.out.println("Start time: "+ abc);
                            System.out.println("Exp start: "+ cvDate + "  "+ expTime.startTime);
                        }
                        double power = getPower(rowData[1], rowData[2]);
                        double energy = getEnergy(power);
                        powerSum += power;
                        energySum += energy;
                        count ++;
                    }
                } catch (ParseException exception) {
                    exception.printStackTrace();
                }
            }
            System.out.println("Rows: "+ rows);
            reader.close();
            System.out.println("App count: "+ count);
            if (energyReadingLess) {
                eventListener.printCleanupMessage("Warning: Data shows energy readings are incomplete in file " + currentVoltageFile.getName() + ". Adjust stop time and delay time of experiment for better results. See Tutorial for details.");
            }
            System.out.println("App Time diff: "+ (expTime.endTime - expTime.startTime));

            return new ComputedPMResults(powerSum, energySum, count);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static double getEnergy(double power) {
        return power * 0.0002;
    }

    private static double getPower(String current, String voltage) {
        return (Double.parseDouble(current)/1000) * Double.parseDouble(voltage);
    }

    public static Pair<PID, MyUID> getIdsFromLogcat(String packageName,
                                                    String readingNo,
                                                    String analysisVersion,
                                                    int[] selectedIndices,
                                                    JList<File> rawFileJList) {
        String fileNameToFind = "logcat"+Constants._R + readingNo + ".txt";
        try {
            for (int index : selectedIndices) {
                if (rawFileJList.getModel().getElementAt(index).getName().equals(fileNameToFind)) {
                    FileInputStream fstream = new FileInputStream(rawFileJList.getModel().getElementAt(index).getAbsoluteFile());
                    BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
                    String line;
                    while ((line = br.readLine())!= null){
                        if (line.contains(packageName) && line.contains("pid") && line.contains("uid")){
                            StringTokenizer tokenizer = new StringTokenizer(line);
                            String pid = "";
                            String uid = "";
                            while (tokenizer.hasMoreTokens()){
                                String token = tokenizer.nextToken();
                                if (token.contains("pid")){
                                    pid = token.split("=")[1];
                                } else if (token.contains("uid")){
                                    uid = token.split("=")[1];
                                }
                            }
                            return new Pair<>(new PID(pid, readingNo, analysisVersion), new MyUID(uid, readingNo, analysisVersion));
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pair<Double, Double> parseBaselineFile(File currentVoltageFile, int totalRows) {
        Pair<Double, Double> baselineAverages = null;
        try {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);
            FileReader fileReader = new FileReader(currentVoltageFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String row = "";
            double powerSum = 0;
            double energySum = 0;
            row = reader.readLine();
            row = reader.readLine();
            long startTime = 0;
            int baselineRow = -1;
            while ((row = reader.readLine()) != null) {
                baselineRow++;
                if (baselineRow <= totalRows) {
                    String[] rowData = row.split(",");
                    double power = getPower(rowData[1], rowData[2]);
                    double energy = getEnergy(power);
                    powerSum += power;
                    energySum += energy;
                } else {
                    break;
                }
            }
            reader.close();
            System.out.println("Rows: "+ baselineRow);
            return new Pair<>(powerSum, energySum);
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //returns voltage in Volts from the file
    public static float getVoltage(File file){
        try {
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader reader = new BufferedReader(fileReader);
                String line = "";
                if ((line = reader.readLine()) != null) {
                    if (line.contains("voltage:")) {
                        String voltage = line.substring(line.indexOf(":"));
                        voltage = voltage.trim();
                        float v = Float.parseFloat(voltage) / 1000;
                        if (v != 0) {
                            return v;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 4.2f;
    }
}
