package cleanup;

import cleanup.models.*;
import com.intellij.openapi.util.Pair;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import cleanup.models.ExpTime;
import cleanup.models.ComputedPMResults;
import util.Constants;
import util.FileParser;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;

import static util.Constants.Analysis.*;

public class CleanupRunner {

    private static CleanupRunner instance;
    private static CleanupEventListener eventListener;
    private JList<File> rawFileJList;
    private DefaultListModel<File> rawFileModel;
    private DefaultListModel<File> cleanFileModel;
    private final ArrayList<CPUInfo> cpuList = new ArrayList<>();
    private final ArrayList<NetworkInfo> networkList = new ArrayList<>();
    private final ArrayList<BatteryInfo> batteryList = new ArrayList<>();
    private final ArrayList<MemoryInfo> memoryList = new ArrayList<>();
    private final ArrayList<AvgEnergyInfo> energyList = new ArrayList<>();
    ArrayList <Pair<Double, Double>> appExeReadings = new ArrayList<>();// power, energy
    ArrayList <EnergyInfo> energyInfos = new ArrayList<>();// power, energy
    ArrayList<File> logcatFileList = new ArrayList<>();
    ArrayList<Long> timeList = new ArrayList<>();

    private CleanupRunner(){}

    public static CleanupRunner getInstance(CleanupEventListener myeventListener){
        if (instance == null){
            instance = new CleanupRunner();
            eventListener = myeventListener;
        }
        return instance;
    }

    //resets class
    public void reset(){
        instance = null;
        instance = new CleanupRunner();
        cpuList.clear();
        memoryList.clear();
        batteryList.clear();
        networkList.clear();
        energyList.clear();
        appExeReadings.clear();
        if (rawFileModel != null) {
            rawFileModel.clear();
        }
        if (cleanFileModel != null) {
            cleanFileModel.clear();
        }
    }


    /**
     * Loads files given in the @param resultsFolder into @param rawFileJList
     */
    public void loadRawData(String resultsFolder, JList<File> rawFileJList){
        if (!resultsFolder.isEmpty()){
            File dir = new File(resultsFolder);
            rawFileModel = new DefaultListModel<>();
            this.rawFileJList = rawFileJList;
            rawFileJList.setCellRenderer(new FileCellRenderer());
            for (File file: dir.listFiles()){
                if (file.isFile()){
                    rawFileModel.addElement(file);
                }
            }
            rawFileJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            rawFileJList.setModel(rawFileModel);
        } else{
            eventListener.printCleanupMessage("No files found");
        }
    }

    /**
     * Loads files given in the @param resultsFolder into @param rawFileJList
     */
    public void loadRawData(File [] files, JList<File> rawFileJList){
//        if (rawFileModel == null) {
        rawFileModel = new DefaultListModel<>();
//        }
        this.rawFileJList = rawFileJList;
        rawFileJList.setCellRenderer(new FileCellRenderer());
        for (File file: files){
            if (file.isFile()){
//                if (!rawFileModel.contains(file)) {
                rawFileModel.addElement(file);
//                }
            }
        }
        rawFileJList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        rawFileJList.setModel(rawFileModel);
    }

    /**
     * When clean button is clicked @param cleanFileList is populated by cleaned files from the selected raw data list
     */
    public void cleanData(JList<File> cleanFileList,
                          String packageName,
                          int apiVersion,
                          String startTag,
                          String endTag,
                          long stopTime,
                          String analysisVersion,
                          String pcResultsPath) {
        //Clear data lists
        cpuList.clear();
        memoryList.clear();
        batteryList.clear();
        networkList.clear();
        energyList.clear();
        appExeReadings.clear();
        energyInfos.clear();
        logcatFileList.clear();
        timeList.clear();

        //Validate required fields
        if (packageName.isEmpty() ){
            eventListener.printCleanupMessage("Enter package name to proceed");
            return;
        }
        if (apiVersion == 0){
            eventListener.printCleanupMessage("Enter API version");
            return;
        }
        if (stopTime == 0){
            eventListener.printCleanupMessage("Total time must be greater than zero");
            return;
        }
        if (analysisVersion.isEmpty()){
            eventListener.printCleanupMessage("Enter Analysis version");
            return;
        }
        if (pcResultsPath.isEmpty()){
            eventListener.printCleanupMessage("Enter path where clean files will be stored");
            return;
        }
        int[] selectedIndices = rawFileJList.getSelectedIndices();
        if (selectedIndices.length == 0){
            eventListener.printCleanupMessage("Select files from Raw Data Files");
            return;
        }

        eventListener.printCleanupMessage("Cleaning files ... ");
        File cleanDir = new File (pcResultsPath + File.separator + Constants.Analysis.CLEAN_DIR);
//        cleanDir.delete();//deleted previously created directory.
        if (!cleanDir.exists()){
            cleanDir.mkdir();
        }
        if (rawFileJList == null){
            eventListener.printCleanupMessage("Clean failed. No files selected.");
        } else {
            cleanFileModel = new DefaultListModel<>();
            cleanFileList.setCellRenderer(new FileCellRenderer());
            logcatFileList = new ArrayList<>();
            //get all logcat files from app execution to later extract pid and uid
            for (int selectedIndex : selectedIndices) {
                System.out.println("File selected: " + rawFileJList.getModel().getElementAt(selectedIndex));
                String name = rawFileJList.getModel().getElementAt(selectedIndex).getName();
                if (name.contains("logcat") && !name.contains("BASELINE")) {
                    logcatFileList.add(rawFileJList.getModel().getElementAt(selectedIndex));
                }
            }
            if (logcatFileList.isEmpty()){
                eventListener.printCleanupMessage("No Logcat file found. Data cannot be cleaned without this file");
            }
            else {
                //get reading no from each logcat file, then clean all files by order of the reading no.
                for (File file : logcatFileList) {
                    String readingNo = FileParser.getReadingNo(file.getName());
                    System.out.println("Base reading no " + readingNo);
                    //reads PID and UID from logcat file of the specified reading no into a pair
                    PID pid = new PID();
                    MyUID myUid = new MyUID();
                    Pair<PID, MyUID> pair = FileParser.getIdsFromLogcat(packageName, readingNo, analysisVersion, selectedIndices, rawFileJList);
                    if (pair != null) {
                        pid = pair.first;
                        myUid = pair.second;
                    }
                    for (int selectedIndex : selectedIndices) {
                        String curReadingNo = FileParser.getReadingNo(rawFileJList.getModel().getElementAt(selectedIndex).getName());
                        //clean all files except uid and pid files and baseline files.
                        String name = rawFileJList.getModel().getElementAt(selectedIndex).getName();
                        //find file with the current reading no. Ignore pid, uid and baseline files
                        if (readingNo.equals(curReadingNo) &&
                                !name.contains("uid") &&
                                !name.contains("pid") &&
                                !name.contains("BASELINE") ) {
                            System.out.println("pid uid"+ pid.PID + "  " + myUid.UID);
                            //clean files
                            File cleanedFile = cleanFile(rawFileJList.getModel().getElementAt(selectedIndex), packageName, apiVersion, cleanDir, startTag, endTag, readingNo, analysisVersion, pid.PID, myUid.UID, stopTime);
                            if (cleanedFile != null) {
                                cleanFileModel.addElement(cleanedFile);
                            }
                        }
                    }
                }
                //data from clean files is saved in lists, traverse those lists and write their data to csv files for each reading data
                try {
                    if (!cpuList.isEmpty()) {
                        String filePath = cleanDir.getAbsolutePath() + File.separator + V_ + analysisVersion + _CLEAN_ + "cpu" + ".csv";
                        Writer writer = new FileWriter(filePath);
                        StatefulBeanToCsv<CPUInfo> beanToCsv = new StatefulBeanToCsvBuilder<CPUInfo>(writer)
                                .withMappingStrategy(new AnnotationStrategy(CPUInfo.class))
                                .build();

                        beanToCsv.write(cpuList);
                        writer.close();
                        cleanFileModel.addElement(new File(filePath));
                    }
                    if (!networkList.isEmpty()) {
                        String filePath = cleanDir.getAbsolutePath() + File.separator + V_ + analysisVersion + _CLEAN_ +"network" + ".csv";
                        Writer writer = new FileWriter(filePath);
                        StatefulBeanToCsv<NetworkInfo> beanToCsv = new StatefulBeanToCsvBuilder<NetworkInfo>(writer)
                                .withMappingStrategy(new AnnotationStrategy(NetworkInfo.class))
                                .build();
                        beanToCsv.write(networkList);
                        writer.close();
                        cleanFileModel.addElement(new File(filePath));
                    }
                    if (!memoryList.isEmpty()) {
                        String filePath = cleanDir.getAbsolutePath() + File.separator + V_ + analysisVersion + _CLEAN_ +"memory" + ".csv";
                        Writer writer = new FileWriter(filePath);
                        StatefulBeanToCsv<MemoryInfo> beanToCsv = new StatefulBeanToCsvBuilder<MemoryInfo>(writer)
                                .withMappingStrategy(new AnnotationStrategy(MemoryInfo.class))
                                .build();
                        beanToCsv.write(memoryList);
                        writer.close();
                        cleanFileModel.addElement(new File(filePath));
                    }
                    if (!batteryList.isEmpty()) {
                        String filePath = cleanDir.getAbsolutePath() + File.separator + V_ + analysisVersion + _CLEAN_ +"battery" + ".csv";
                        Writer writer = new FileWriter(filePath);
                        StatefulBeanToCsv<BatteryInfo> beanToCsv = new StatefulBeanToCsvBuilder<BatteryInfo>(writer)
                                .withMappingStrategy(new AnnotationStrategy(BatteryInfo.class))
                                .build();
                        beanToCsv.write(batteryList);
                        writer.close();
                        cleanFileModel.addElement(new File(filePath));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    eventListener.printCleanupMessage(e.getMessage());
                }

                //set files in UI list
                cleanFileList.setModel(cleanFileModel);
                //once all files have been cleaned, calculate averages
                if (cleanFileModel.size() != 0) {
                    computeEnergy(selectedIndices, cleanDir, pcResultsPath, analysisVersion, stopTime, packageName, apiVersion);
                    computeExperimentAverage(packageName, analysisVersion, cleanDir, stopTime);
                    eventListener.printCleanupMessage("Finished cleaning data. \nClean files saved in " + pcResultsPath + File.separator + Constants.Analysis.CLEAN_DIR);
                }
            }
        }
    }

    private void computeExperimentAverage(String packageName, String analysisVersion, File cleanDir, long stopTime) {
        //BASELINE
        double baselineAvgPower = 0;
        double baselineAvgEnergy = 0;
        for (EnergyInfo energyInfo: energyInfos){
            baselineAvgPower += energyInfo.baselinePower;
            baselineAvgEnergy += energyInfo.baselineEnergy;
        }
        if (!energyInfos.isEmpty()){
            baselineAvgPower = baselineAvgPower/energyInfos.size();
            baselineAvgEnergy = baselineAvgEnergy/energyInfos.size();
        }
        //APP EXE
        double appExeAvgPower = 0;
        double appExeAvgEnergy = 0;
        double totalAvgPower = 0;
        double totalAvgEnergy = 0;
        double maxAvgPower = 0;
        double maxAvgEnergy = 0;
        double minAvgPower = 0;
        double minAvgEnergy = 0;
        if (energyInfos.size() > 0) {
            maxAvgPower = energyInfos.get(0).appPower;
            maxAvgEnergy = energyInfos.get(0).appEnergy;
            minAvgPower = energyInfos.get(0).appPower;
            minAvgEnergy = energyInfos.get(0).appEnergy;
        }
        for (EnergyInfo energyInfo: energyInfos){
            appExeAvgPower += energyInfo.appPower;
            appExeAvgEnergy += energyInfo.appEnergy;
            totalAvgPower += energyInfo.totalPower;
            totalAvgEnergy += energyInfo.totalEnergy;
            if (energyInfo.appPower > maxAvgPower){
                maxAvgPower = energyInfo.appEnergy;
            } else {
                minAvgPower = energyInfo.appEnergy;
            }
            if (energyInfo.appEnergy > maxAvgEnergy){
                maxAvgEnergy = energyInfo.appEnergy;
            }else {
                minAvgEnergy = energyInfo.appEnergy;
            }
        }
        if (!energyInfos.isEmpty()){
            appExeAvgPower = appExeAvgPower /energyInfos.size();
            appExeAvgEnergy = appExeAvgEnergy /energyInfos.size();
            totalAvgPower = totalAvgPower /energyInfos.size();
            totalAvgEnergy = totalAvgEnergy /energyInfos.size();

            AvgEnergyInfo avgEnergyInfo = new AvgEnergyInfo();
            avgEnergyInfo.packageName = packageName;
            avgEnergyInfo.versionNo = analysisVersion;
            avgEnergyInfo.baselineAvgPower = baselineAvgPower;
            avgEnergyInfo.baselineAvgEnergy = baselineAvgEnergy;
            avgEnergyInfo.totalExeAvgPower = totalAvgPower;
            avgEnergyInfo.totalExeAvgEnergy = totalAvgEnergy;
            avgEnergyInfo.appAvgPower = appExeAvgPower ;
            avgEnergyInfo.appAvgEnergy = appExeAvgEnergy;
            avgEnergyInfo.maxEnergy = maxAvgEnergy;
            avgEnergyInfo.maxPower = maxAvgPower;
            avgEnergyInfo.minEnergy = minAvgEnergy;
            avgEnergyInfo.minPower = minAvgPower;
            double sdPower = 0;double sdEnergy = 0;
            for(Pair<Double, Double> pair: appExeReadings) {
//                double power = pair.first - baselineAvgPower;
                sdPower += Math.pow(pair.first - avgEnergyInfo.appAvgPower, 2);
//                double energy = pair.second - baselineAvgPower;
                sdEnergy += Math.pow(pair.second - avgEnergyInfo.appAvgEnergy, 2);
            }
            sdPower =  Math.sqrt(sdPower/appExeReadings.size());
            sdEnergy =  Math.sqrt(sdEnergy/appExeReadings.size());
            avgEnergyInfo.sdPower = sdPower;
            avgEnergyInfo.sdEnergy = sdEnergy;
//            File file = new File(cleanDir + File.separator + V_ + analysisVersion + AVG + "_" +"energy.csv");
//            MyFilesUtils.writeObjToCsvFile(avgEnergyInfo, file, eventListener);
//            cleanFileModel.addElement(file);
        }

        //CPU, RSS, VSS, MEMORY Average
        double avgVss = 0, avgRss = 0, avgCpuPercentage = 0, avgMemoryPercentage = 0;
        double maxVss = 0, maxRss = 0, maxCpuPercentage = 0, maxMemoryPercentage = 0;
        double minVss = 0, minRss = 0, minCpuPercentage = 0, minMemoryPercentage = 0;
        double sdVss = 0, sdRss = 0, sdCpuPercentage = 0, sdMemoryPercentage = 0;

        if (cpuList.size() >0){
            minVss = maxVss = cpuList.get(0).vss;
            minRss = maxRss = cpuList.get(0).rss;
            minCpuPercentage = maxCpuPercentage = cpuList.get(0).cpuUsage;
            minMemoryPercentage = maxMemoryPercentage = cpuList.get(0).memory;
        }
        for (CPUInfo cpuInfo : cpuList) {
            avgVss += cpuInfo.vss;
            avgRss += cpuInfo.rss;
            avgCpuPercentage += cpuInfo.cpuUsage / cpuInfo.cores;
            avgMemoryPercentage += cpuInfo.memory;
            if (cpuInfo.vss > maxVss){
                maxVss = cpuInfo.vss;
            } else {
                minVss = cpuInfo.vss;
            }
            if (cpuInfo.rss > maxRss){
                maxRss = cpuInfo.rss;
            } else {
                minRss = cpuInfo.rss;
            }
            if (cpuInfo.cpuUsage/cpuInfo.cores > maxCpuPercentage){
                maxCpuPercentage = cpuInfo.cpuUsage/cpuInfo.cores;
            } else {
                minCpuPercentage = cpuInfo.cpuUsage/cpuInfo.cores;
            }
            if (cpuInfo.memory > maxMemoryPercentage){
                maxMemoryPercentage = cpuInfo.memory;
            } else {
                minMemoryPercentage = cpuInfo.memory;
            }
        }
        if (!cpuList.isEmpty()) {
            avgVss = avgVss / cpuList.size();
            avgRss = avgRss / cpuList.size();
            avgCpuPercentage = avgCpuPercentage / cpuList.size();
            avgMemoryPercentage = avgMemoryPercentage / cpuList.size();
            for(CPUInfo cpuInfo: cpuList) {
                sdVss += Math.pow(cpuInfo.vss - avgVss, 2);
                sdRss += Math.pow(cpuInfo.rss - avgRss, 2);
                sdCpuPercentage += Math.pow(cpuInfo.cpuUsage/cpuInfo.cores - avgCpuPercentage, 2);
                sdMemoryPercentage += Math.pow(cpuInfo.memory - avgMemoryPercentage, 2);
            }
            sdVss =  Math.sqrt(sdVss/cpuList.size());
            sdRss =  Math.sqrt(sdRss/cpuList.size());
            sdCpuPercentage =  Math.sqrt(sdCpuPercentage/cpuList.size());
            sdMemoryPercentage =  Math.sqrt(sdMemoryPercentage/cpuList.size());

            AvgCPUMemoryInfo avgCPUMemoryInfo = new AvgCPUMemoryInfo(packageName, analysisVersion,
                    avgVss, avgRss, avgCpuPercentage, avgMemoryPercentage,
                    maxVss, maxRss, maxCpuPercentage, maxMemoryPercentage,
                    minVss, minRss, minCpuPercentage, minMemoryPercentage,
                    sdVss, sdRss, sdCpuPercentage, sdMemoryPercentage
            );
//            File file = new File(cleanDir + File.separator + V_ + analysisVersion + AVG + "_" +"cpu-memory.csv");
//            MyFilesUtils.writeObjToCsvFile(avgCPUMemoryInfo, file, eventListener);
//            cleanFileModel.addElement(file);
        }

        // BATTERY AVERAGE
        double avgWifiDrain = 0,avgUidDrain = 0,avgScreenDrain = 0,avgCellPowerDrain = 0,avgRadioPowerDrain = 0;
        double avgBluetoothPowerDrain = 0,avgActualDrain = 0,avgComputedDrain = 0,avgDischarge = 0,avgScreenOnDischarge = 0;
        double avgScreenOffDischarge = 0,avgScreenDozeDischarge = 0,avgLightDozeDischarge = 0,avgDeepDozeDischarge = 0;
        double avgComputedEnergy = 0, avgActualEnergy = 0, minComputedEnergy = 0, maxComputedEnergy = 0;
        double minActualEnergy = 0, maxActualEnergy = 0, sdComputedEnergy = 0, sdActualEnergy = 0;
        if (batteryList.size() > 0){
            minActualEnergy = maxActualEnergy = (Float.parseFloat(batteryList.get(0).actualDrain)/1000) * stopTime * batteryList.get(0).voltage;
            minComputedEnergy = maxComputedEnergy = (Float.parseFloat(batteryList.get(0).computedDrain)/1000) * stopTime * batteryList.get(0).voltage;
        }
        int capacity = 0;
        for (BatteryInfo batteryInfo : batteryList) {
            avgWifiDrain += Float.parseFloat(batteryInfo.wifiPowerDrain);
            avgUidDrain += batteryInfo.uidPowerDrain;
            avgScreenDrain += Float.parseFloat(batteryInfo.screenPowerDrain);
            avgCellPowerDrain += Float.parseFloat(batteryInfo.cellPowerDrain);
            avgRadioPowerDrain += Float.parseFloat(batteryInfo.radioPowerDrain);
            avgBluetoothPowerDrain += Float.parseFloat(batteryInfo.bluetoothPowerDrain);
            avgActualDrain +=Float.parseFloat(batteryInfo.actualDrain);
            avgComputedDrain +=Float.parseFloat(batteryInfo.computedDrain);
            avgDischarge += Float.parseFloat(batteryInfo.discharge);
            avgScreenOnDischarge += Float.parseFloat(batteryInfo.screenOnDischarge);
            avgScreenOffDischarge += Float.parseFloat(batteryInfo.screenOffDischarge);
            avgScreenDozeDischarge += Float.parseFloat(batteryInfo.screenDozeDischarge);
            avgLightDozeDischarge += Float.parseFloat(batteryInfo.lightDozeDischarge);
            avgDeepDozeDischarge += Float.parseFloat(batteryInfo.deepDozeDischarge);
            capacity = Integer.parseInt(batteryInfo.estimatedBatteryCapacity);
            double computed = (Float.parseFloat(batteryInfo.computedDrain)/1000) * stopTime * batteryInfo.voltage;
            double actual = (Float.parseFloat(batteryInfo.actualDrain)/1000) * stopTime * batteryInfo.voltage;
            avgComputedEnergy += computed;
            avgActualEnergy += actual;
            if (computed > maxComputedEnergy){
                maxComputedEnergy = computed;
            } else {
                minComputedEnergy = computed;
            }
            if (actual > maxActualEnergy){
                maxActualEnergy = actual;
            } else {
                minActualEnergy = actual;
            }
        }
        if (!batteryList.isEmpty()) {
            avgWifiDrain = avgWifiDrain / batteryList.size();
            avgUidDrain = avgUidDrain / batteryList.size();
            avgScreenDrain = avgScreenDrain / batteryList.size();
            avgCellPowerDrain = avgCellPowerDrain / batteryList.size();
            avgRadioPowerDrain = avgRadioPowerDrain / batteryList.size();
            avgBluetoothPowerDrain = avgBluetoothPowerDrain / batteryList.size();
            avgActualDrain = avgActualDrain/ batteryList.size();
            avgComputedDrain = avgComputedDrain/ batteryList.size();
            avgDischarge = avgDischarge/ batteryList.size();
            avgScreenOnDischarge = avgScreenOnDischarge/ batteryList.size();
            avgScreenOffDischarge = avgScreenOffDischarge/ batteryList.size();
            avgScreenDozeDischarge = avgScreenDozeDischarge/ batteryList.size();
            avgLightDozeDischarge = avgLightDozeDischarge/ batteryList.size();
            avgDeepDozeDischarge = avgDeepDozeDischarge/ batteryList.size();
            avgComputedEnergy = avgComputedEnergy/ batteryList.size();
            avgActualEnergy = avgActualEnergy/ batteryList.size();
            for(BatteryInfo batteryInfo: batteryList) {
                double computed = (Float.parseFloat(batteryInfo.computedDrain)/1000) * stopTime * batteryInfo.voltage;
                double actual = (Float.parseFloat(batteryInfo.actualDrain)/1000) * stopTime * batteryInfo.voltage;
                sdActualEnergy += Math.pow(actual - avgActualEnergy, 2);
                sdComputedEnergy += Math.pow(computed - avgComputedEnergy, 2);
            }
            sdActualEnergy =  Math.sqrt(sdActualEnergy/batteryList.size());
            sdComputedEnergy =  Math.sqrt(sdComputedEnergy/batteryList.size());
            AvgBatteryInfo avgBatteryInfo = new AvgBatteryInfo();
            avgBatteryInfo.packageName = packageName;
            avgBatteryInfo.versionNo = analysisVersion;
            avgBatteryInfo.computedEnergy = avgComputedEnergy;
            avgBatteryInfo.minComputedEnergy = minComputedEnergy;
            avgBatteryInfo.maxComputedEnergy = maxComputedEnergy;
            avgBatteryInfo.sdComputedEnergy = sdComputedEnergy;
            avgBatteryInfo.actualEnergy = avgActualEnergy;
            avgBatteryInfo.minActualEnergy = minActualEnergy;
            avgBatteryInfo.maxActualEnergy = maxActualEnergy;
            avgBatteryInfo.sdActualEnergy = sdActualEnergy;

//            AvgBatteryInfo avgBatteryInfo = new AvgBatteryInfo(packageName, analysisVersion, avgWifiDrain, avgUidDrain,
//                    avgScreenDrain, avgCellPowerDrain, avgRadioPowerDrain, avgBluetoothPowerDrain, avgActualDrain,
//                    avgComputedDrain, avgDischarge, avgScreenOnDischarge, avgScreenOffDischarge, avgScreenDozeDischarge,
//                    avgLightDozeDischarge, avgDeepDozeDischarge, capacity, avgComputedEnergy, avgActualEnergy,
//                    minComputedEnergy,minActualEnergy, maxComputedEnergy, maxActualEnergy, sdComputedEnergy, sdActualEnergy);
//            File file = new File(cleanDir + File.separator + V_ + analysisVersion + AVG + "_" +"battery.csv");
//            MyFilesUtils.writeObjToCsvFile(avgBatteryInfo, file, eventListener);
//            cleanFileModel.addElement(file);
        }

        // NETWORK AVERAGE
        long avgSentPcks = 0, avgRecvPcks = 0, avgSentBytes = 0, avgRecvBytes = 0;
        long minSentPcks = 0, minRecvPcks = 0, minSentBytes = 0, minRecvBytes = 0;
        long maxSentPcks = 0, maxRecvPcks = 0, maxSentBytes = 0, maxRecvBytes = 0;
        double sdSentPcks = 0, sdRecvPcks = 0, sdSentBytes = 0, sdRecvBytes = 0;
        if (networkList.size() >0){
            minSentPcks = maxSentPcks = networkList.get(0).sentPackets;
            minRecvPcks = maxRecvPcks = networkList.get(0).receivedPackets;
            minSentBytes = maxSentBytes = networkList.get(0).sentBytes;
            minRecvBytes = maxRecvBytes = networkList.get(0).receivedBytes;
        }
        for(NetworkInfo networkInfo: networkList){
            avgSentPcks += networkInfo.sentPackets;
            avgRecvPcks += networkInfo.receivedPackets;
            avgRecvBytes += networkInfo.receivedBytes;
            avgSentBytes += networkInfo.sentBytes;
            if (networkInfo.sentPackets > maxSentPcks){
                maxSentPcks = networkInfo.sentPackets;
            } else {
                minSentPcks = networkInfo.sentPackets;
            }if (networkInfo.receivedPackets > maxRecvPcks){
                maxRecvPcks = networkInfo.receivedPackets;
            } else {
                minRecvPcks = networkInfo.receivedPackets;
            }if (networkInfo.sentBytes > maxSentBytes){
                maxSentBytes = networkInfo.sentBytes;
            } else {
                minSentBytes = networkInfo.sentBytes;
            }if (networkInfo.receivedBytes > maxRecvBytes){
                maxRecvBytes = networkInfo.receivedBytes;
            } else {
                maxRecvBytes = networkInfo.receivedBytes;
            }
        }
        if (!networkList.isEmpty()){
            avgRecvPcks = avgRecvPcks/networkList.size();
            avgSentPcks = avgSentPcks/networkList.size();
            avgRecvBytes = avgRecvBytes/networkList.size();
            avgSentBytes = avgSentBytes/networkList.size();
            for(NetworkInfo networkInfo: networkList) {
                sdRecvPcks += Math.pow(networkInfo.receivedPackets - avgRecvPcks, 2);
                sdSentPcks += Math.pow(networkInfo.sentPackets - avgSentPcks, 2);
                sdRecvBytes += Math.pow(networkInfo.receivedBytes - avgRecvBytes, 2);
                sdSentBytes += Math.pow(networkInfo.sentBytes - avgSentBytes, 2);
            }
            sdRecvPcks =  Math.sqrt(sdRecvPcks/networkList.size());
            sdSentPcks =  Math.sqrt(sdSentPcks/networkList.size());
            sdRecvBytes =  Math.sqrt(sdRecvBytes/networkList.size());
            sdSentBytes =  Math.sqrt(sdSentBytes/networkList.size());

            AvgNetworkInfo avgNetworkInfo = new AvgNetworkInfo(packageName, analysisVersion,
                    avgRecvPcks, avgSentPcks, avgRecvBytes, avgSentBytes,
                    maxRecvPcks, maxSentPcks, maxRecvBytes, maxSentBytes,
                    minRecvPcks, minSentPcks, minRecvBytes, minSentBytes,
                    sdRecvPcks, sdSentPcks, sdRecvBytes, sdSentBytes);
//            File file = new File(cleanDir + File.separator + V_ + analysisVersion + AVG + "_" +"network.csv");
//            MyFilesUtils.writeObjToCsvFile(avgNetworkInfo, file, eventListener);
//            cleanFileModel.addElement(file);
        }
        ArrayList<ExperimentData> datas = new ArrayList<>();
        for (int i=0; i< logcatFileList.size(); i++){
            ExperimentData experimentData = new ExperimentData();
            experimentData.readingNo = i;
            experimentData.versionNo = analysisVersion;
            experimentData.packageName = packageName;
            if (!energyInfos.isEmpty() &&  i< energyInfos.size()){
                experimentData.energy = energyInfos.get(i).appEnergy;
                experimentData.power = energyInfos.get(i).appEnergy;
            }
            if (!networkList.isEmpty() && i < networkList.size()){
                experimentData.sentBytes = networkList.get(i).sentBytes;
                experimentData.recvBytes = networkList.get(i).receivedBytes;
                experimentData.sentPcks = networkList.get(i).sentPackets;
                experimentData.recvPcks = networkList.get(i).receivedPackets;
            }
            if (!cpuList.isEmpty() && i < cpuList.size()){
                experimentData.cpu = cpuList.get(i).cpuUsage / cpuList.get(i).cores;
                experimentData.memory = cpuList.get(i).memory;
                experimentData.rss = cpuList.get(i).rss;
                experimentData.vss = cpuList.get(i).vss;
            }
            if (!timeList.isEmpty() && i < timeList.size()){
                experimentData.time = timeList.get(i)/1000f;//converting to seconds.
            }
            datas.add(experimentData);
        }
        File file = new File(cleanDir + File.separator + "data.csv");

        try {
            if (!file.exists()){
                Writer writer = new FileWriter(file, true);
                writer.write("version no");
                writer.write(",");
                writer.write("reading no");
                writer.write(",");
                writer.write("packageName");
                writer.write(",");
                writer.write("time");
                writer.write(",");
                writer.write("energy");
                writer.write(",");
                writer.write("cpu %");
                writer.write(",");
                writer.write("memory %");
                writer.write(",");
                writer.write("RSS(KB)");
                writer.write(",");
                writer.write("VSS (KB)");
                writer.write(",");
                writer.write("sent bytes");
                writer.write(",");
                writer.write("received bytes");
                writer.write(",");
                writer.write("sent packets");
                writer.write(",");
                writer.write("received packets");
                writer.write("\n");
                writeCsv(writer, datas);
                writer.close();
            } else {
                Writer writer = new FileWriter(file, true);
                writeCsv(writer, datas);
                writer.close();
            }
            cleanFileModel.addElement(file);
        } catch (IOException e){
            e.printStackTrace();
            eventListener.printCleanupMessage("Writing data file failed. Make sure all files are closed in Results Folder");
        }
        File avgfile = new File(cleanDir + File.separator + "average_data.csv");


        try {
            Writer writer;
            if (!avgfile.exists()){
                writer = new FileWriter(avgfile, true);
                writer.write("version no");
                writer.write(",");
                writer.write("packageName");
                writer.write(",");
                writer.write("energy");
                writer.write(",");
                writer.write("cpu %");
                writer.write(",");
                writer.write("memory %");
                writer.write(",");
                writer.write("RSS(KB)");
                writer.write(",");
                writer.write("VSS (KB)");
                writer.write(",");
                writer.write("sent bytes");
                writer.write(",");
                writer.write("received bytes");
                writer.write(",");
                writer.write("sent packets");
                writer.write(",");
                writer.write("received packets");
                writer.write("\n");

            } else {
                writer = new FileWriter(avgfile, true);
            }
            writer.write(analysisVersion);
            writer.write(",");
            writer.write(packageName);
            writer.write(",");
            writer.write(String.valueOf(appExeAvgEnergy));
            writer.write(",");
            writer.write(String.valueOf(avgCpuPercentage));
            writer.write(",");
            writer.write(String.valueOf(avgMemoryPercentage));
            writer.write(",");
            writer.write(String.valueOf(avgRss));
            writer.write(",");
            writer.write(String.valueOf(avgVss));
            writer.write(",");
            writer.write(String.valueOf(avgSentBytes));
            writer.write(",");
            writer.write(String.valueOf(avgRecvBytes));
            writer.write(",");
            writer.write(String.valueOf(avgSentPcks));
            writer.write(",");
            writer.write(String.valueOf(avgRecvPcks));
            writer.write("\n");
            writer.close();
            cleanFileModel.addElement(avgfile);
        } catch (IOException e){
            e.printStackTrace();
            eventListener.printCleanupMessage("Writing data file failed. Make sure all files are closed in Results Folder");
        }
    }

    private void writeCsv(Writer writer, ArrayList<ExperimentData> datas) throws IOException {
        for (ExperimentData experimentData : datas) {
            writer.write(experimentData.versionNo);
            writer.write(",");
            writer.write(String.valueOf(experimentData.readingNo));
            writer.write(",");
            writer.write(experimentData.packageName);
            writer.write(",");
            writer.write(String.valueOf(experimentData.time));
            writer.write(",");
            writer.write(String.valueOf(experimentData.energy));
            writer.write(",");
            writer.write(String.valueOf(experimentData.cpu));
            writer.write(",");
            writer.write(String.valueOf(experimentData.memory));
            writer.write(",");
            writer.write(String.valueOf(experimentData.rss));
            writer.write(",");
            writer.write(String.valueOf(experimentData.vss));
            writer.write(",");
            writer.write(String.valueOf(experimentData.sentBytes));
            writer.write(",");
            writer.write(String.valueOf(experimentData.recvBytes));
            writer.write(",");
            writer.write(String.valueOf(experimentData.sentPcks));
            writer.write(",");
            writer.write(String.valueOf(experimentData.recvPcks));
            writer.write("\n");
        }
    }

    private void computeEnergy(int[] selectedIndices, File cleanDir, String pcResultsPath, String analysisVersion, long stopTime, String packageName, int apiVersion) {
        HashMap<String, ExpTime> logcatPowerMap = new HashMap<>();
        //get reading no,  start and end time from logcat files and store them as a pair in list
        for (int i=0; i<cleanFileModel.size(); i++){
            File file = cleanFileModel.get(i);
            if (cleanFileModel.get(i).getName().contains("logcat") &&
                    !cleanFileModel.get(i).getName().contains(Constants.BASELINE_ + "logcat")){
                String key = FileParser.getReadingNo(file.getName());
                logcatPowerMap.put(key, FileParser.getStartEndTime(file, stopTime) );
            }
        }
        //for all selected files, read energy files in order of their reading
        for (int i : selectedIndices){
            File file = rawFileModel.get(i);
            if (rawFileModel.get(i).getName().contains(Constants.POWER_MONITOR_OUTPUT_FILE_NAME) &&
                    !rawFileModel.get(i).getName().contains(Constants.BASELINE_ + Constants.POWER_MONITOR_OUTPUT_FILE_NAME)){
                String key = FileParser.getReadingNo(file.getName());
                if (logcatPowerMap.containsKey(key)) {
                    ExpTime expTime = logcatPowerMap.get(key);
                    timeList.add(expTime.endTime - expTime.startTime);
                    System.out.println(key);
                    ComputedPMResults computedPMResults = FileParser.parseCurrentVoltageFile(file,
                            expTime,
                            analysisVersion,
                            cleanDir,
                            pcResultsPath,
                            key,
                            eventListener);
                    System.out.println("App R"+key+" Power "+ computedPMResults.power + ", Energy: "+ computedPMResults.energy + "  Rows: " + computedPMResults.rows);
                    ArrayList <Pair<Double, Double>> baselineReading = new ArrayList<>();// power, energy
                    //calculate baseline for eachlogcat.
                    for (int index : selectedIndices){
                        File baselineFile = rawFileModel.get(index);
                        if (rawFileModel.get(index).getName().contains(Constants.BASELINE_ + Constants.POWER_MONITOR_OUTPUT_FILE_NAME)){
                            String baselineKey = FileParser.getReadingNo(baselineFile.getName());
                            Pair<Double, Double> baselinePair = FileParser.parseBaselineFile(baselineFile, computedPMResults.rows);
                            if (baselinePair != null) {
                                System.out.println("Baseline R"+baselineKey+" Power "+ baselinePair.first + ", Energy: "+ baselinePair.second + "");
                                baselineReading.add(baselinePair);
                            }
                        }
                    }
                    double baselineAvgPower = 0;
                    double baselineAvgEnergy = 0;
                    for (Pair<Double, Double> p: baselineReading){
                        baselineAvgPower += p.first;
                        baselineAvgEnergy += p.second;
                    }
                    if (!baselineReading.isEmpty()){
                        baselineAvgPower = baselineAvgPower/baselineReading.size();
                        baselineAvgEnergy = baselineAvgEnergy/baselineReading.size();
                    }
                    if (computedPMResults != null) {
                        double power = computedPMResults.power - baselineAvgPower;
                        double energy = computedPMResults.energy - baselineAvgEnergy;
                        EnergyInfo energyInfo = new EnergyInfo();
                        energyInfo.totalPower = computedPMResults.power;
                        energyInfo.totalEnergy = computedPMResults.energy;
                        energyInfo.baselinePower = baselineAvgPower;
                        energyInfo.baselineEnergy = baselineAvgEnergy;
                        energyInfo.appPower = power;
                        energyInfo.appEnergy = energy;
                        energyInfos.add(energyInfo);
                        appExeReadings.add(new Pair<>(power, energy));
                    }
                }
            }
        }

    }

    private File cleanFile(File file,
                           String packageName,
                           int apiVersion,
                           File cleanDir,
                           String startTag,
                           String endTag,
                           String readingNo,
                           String analysisVersionNo,
                           String pid,
                           String uid,
                           long stopTime) {
        if (file.getName().contains("cpu")){
            CPUInfo cpuInfo = FileParser.parseCpuPercentage(file.getAbsolutePath(), packageName, apiVersion);
            cpuInfo.packageName = packageName;
            cpuInfo.deviceApiVersion = apiVersion;
            cpuInfo.pid = pid;
            cpuInfo.uid = uid;
            cpuInfo.readingNo = readingNo;
            cpuInfo.versionNo = analysisVersionNo;
            cpuList.add(cpuInfo);
        } else if (file.getName().contains("network")){
            if (!uid.isEmpty()) {
                ArrayList<NetworkInfo> networkInfoList = FileParser.parseNetworkFile(file.getAbsolutePath(), uid);
                for (NetworkInfo networkInfo: networkInfoList) {
                    networkInfo.packageName = packageName;
                    networkInfo.deviceApiVersion = apiVersion;
                    networkInfo.pid = pid;
                    networkInfo.uid = uid;
                    networkInfo.versionNo = analysisVersionNo;
                    networkInfo.readingNo = readingNo;
                    networkList.add(networkInfo);
                    System.out.println("Added");
                }
            } else {
//                eventListener.printAnalysisMessage("UID is not available. Add UID file first and load it");
            }
        } else if (file.getName().contains("memory")){
            MemoryInfo memoryInfo = FileParser.parseMemoryFile(file.getAbsolutePath());
            memoryInfo.packageName = packageName;
            memoryInfo.deviceApiVersion = apiVersion;
            memoryInfo.pid = pid;
            memoryInfo.uid = uid;
            memoryInfo.readingNo = readingNo;
            memoryInfo.versionNo = analysisVersionNo;
            memoryList.add(memoryInfo);
        } else if (file.getName().contains("battery")){
            BatteryInfo batteryInfo = FileParser.parseBatteryFile(file.getAbsolutePath());
            batteryInfo.packageName = packageName;
            batteryInfo.deviceApiVersion = apiVersion;
            batteryInfo.pid = pid;
            batteryInfo.uid = uid;
            batteryInfo.readingNo = readingNo;
            batteryInfo.versionNo = analysisVersionNo;
            batteryList.add(batteryInfo);
        }else if (file.getName().contains("logcat")){
            if (pid != null) {
                return FileParser.parseLogcat(file, pid, startTag, endTag, stopTime, analysisVersionNo, cleanDir, eventListener);
            } else {
                eventListener.printCleanupMessage("PID is not available. Add PID file first and load it");
            }
        }

        return null;
    }


    public void loadSelection() {
    }



    /**
     * Class that renders the data in list cells and performs actions on selection
     */
    private static class FileCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList<?> list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                setText(file.getName());
            }
            return this;
        }
    }

}
