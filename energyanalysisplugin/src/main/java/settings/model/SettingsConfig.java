package settings.model;

import util.APKReader;
import util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Iffat Fatima
 * @created on 22/10/2020
 */
public class SettingsConfig {
    ExperimentType experimentType;
    ExperimentStage experimentStage;
    List<DataType> dataTypes = new ArrayList<>();
    int numberOfRuns = 0;
    String apkPath = "";
    String testApkPath = "";
    String apkFileName = "";
    String testApkFileName = "";
    RunConfig runConfig;
    String pcResultsPath = "";
    long stopTime = 0; //in seconds
    String packageName = "";
    String launcherClass = "";
    String deviceDataPath = "/sdcard/Download/";
    String deviceTmpPath = Constants.DATA_LOCAL_TMP_DIR;
    String testClass = "";
    String testRunner = "";
    long delayTime = 10; //in seconds

    public SettingsConfig(){
        this.experimentType = ExperimentType.HARDWARE;
        this.experimentStage = ExperimentStage.BASELINE;
        dataTypes.add(DataType.ADB_LOGS);
        dataTypes.add(DataType.CURRENT_VOLTAGE);
    }

    public ExperimentType getExperimentType() {
        return experimentType;
    }

    public void setExperimentType(ExperimentType experimentType) {
        this.experimentType = experimentType;
    }

    public ExperimentStage getExperimentStage() {
        return experimentStage;
    }

    public void setExperimentStage(ExperimentStage experimentStage) {
        this.experimentStage = experimentStage;
    }

    public List<DataType> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(List<DataType> dataTypes) {
        this.dataTypes = dataTypes;
    }

    public int getNumberOfRuns() {
        return numberOfRuns;
    }

    public void setNumberOfRuns(int numberOfRuns) {
        this.numberOfRuns = numberOfRuns;
    }

    public String getApkPath() {
        return apkPath;
    }

    //reads apk path when apk is loaded.
    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
        packageName = APKReader.getInstance().getPackageName(apkPath);
        System.out.println("Package name extracted: "+ packageName);

        launcherClass = APKReader.getInstance().getLauncherClass(apkPath);
        launcherClass = launcherClass.replace(packageName, "/");
        System.out.println("Launcher name extracted: "+ launcherClass);
    }

    public String getTestApkPath() {
        return testApkPath;
    }

    public void setTestApkPath(String testApkPath) {
        this.testApkPath = testApkPath;
    }

    public String getDeviceDataPath() {
        return deviceDataPath;
    }

//    public void setDeviceDataPath(String deviceDataPath) {
//        this.deviceDataPath = deviceDataPath;
//    }

    public RunConfig getRunConfig() {
        return runConfig;
    }

    public void setRunConfig(RunConfig runConfig) {
        this.runConfig = runConfig;
    }

    public String getPcResultsPath() {
        return pcResultsPath;
    }

    public void setPcResultsPath(String pcResultsPath) {
        this.pcResultsPath = pcResultsPath;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getLauncherClass() {
        return launcherClass;
    }

    public void setLauncherClass(String launcherClass) {
        this.launcherClass = launcherClass;
    }

    public String getApkFileName() {
        return apkFileName;
    }

    public void setApkFileName(String apkFileName) {
        this.apkFileName = apkFileName;
    }

    public String getTestApkFileName() {
        return testApkFileName;
    }

    public void setTestApkFileName(String testApkFileName) {
        this.testApkFileName = testApkFileName;
    }

    public String getTestClass() {
        return testClass;
    }

    public void setTestClass(String testClass) {
        this.testClass = testClass;
    }

    public String getTestRunner() {
        return testRunner;
    }

    public void setTestRunner(String testRunner) {
        this.testRunner = testRunner;
    }

    public String getDeviceTmpPath() {
        return deviceTmpPath;
    }

    public void setDeviceTmpPath(String deviceTmpPath) {
        this.deviceTmpPath = deviceTmpPath;
    }

    public long getDelayTime() {
        return delayTime;
    }

    public void setDelayTime(long delayTime) {
        this.delayTime = delayTime;
    }

    @Override
    public String toString() {
        return "SettingsConfig{" +
                ", experimentType=" + experimentType +
                ", experimentStage=" + experimentStage +
                ", dataTypes=" + dataTypes +
                ", numberOfRuns=" + numberOfRuns +
                ", apkPath='" + apkPath + '\'' +
                ", testApkPath='" + testApkPath + '\'' +
                ", apkFileName='" + apkFileName + '\'' +
                ", testApkFileName='" + testApkFileName + '\'' +
                ", runConfig=" + runConfig +
                ", pcResultsPath='" + pcResultsPath + '\'' +
                ", stopTime=" + stopTime +
                ", packageName='" + packageName + '\'' +
                ", launcherClass='" + launcherClass + '\'' +
                ", deviceDataPath='" + deviceDataPath + '\'' +
                ", deviceTmpPath='" + deviceTmpPath + '\'' +
                ", testClass='" + testClass + '\'' +
                ", testRunner='" + testRunner + '\'' +
                '}';
    }

    /**
     *
     * @param settingsConfig - checks settingConfig fields and checks them for validity based on experiment type, stage, data and mandatory fields
     * @return ConfigStatus which has 2 values, a boolean for data validity and a message explaining validity success and failure type
     */
    public static ConfigStatus validate(SettingsConfig settingsConfig){
        if (settingsConfig == null){
            return new ConfigStatus(false, "No configuration found");
        }
        if (settingsConfig.dataTypes.isEmpty()){
            return new ConfigStatus(false, "Choose data type to be selected");
        }
        if (settingsConfig.numberOfRuns <= 0){
            return new ConfigStatus(false, "Number of runs should be greater than 0");
        }
        if (settingsConfig.pcResultsPath.isEmpty()){
            return new ConfigStatus(false, "Choose path where results will be saved");
        }
        if (settingsConfig.stopTime <=0){
            return new ConfigStatus(false, "Choose total time greater than zero");
        }
        if (settingsConfig.experimentStage == ExperimentStage.APP_EXECUTION){
            if (settingsConfig.apkPath.isEmpty()){
                return new ConfigStatus(false, "Choose APK Path");
            }
            if (settingsConfig.testApkPath.isEmpty()){
                return new ConfigStatus(true, "Application needs to be operated manually");
            }
            else{
                if (settingsConfig.testClass.isEmpty()){
                    return new ConfigStatus(false, "Test class is missing");
                }
                if (settingsConfig.testRunner.isEmpty()) {
                    return new ConfigStatus(false, "Test runner is missing");
                }
            }
        }
        return new ConfigStatus(true, "Configuration setup successfully");
    }

    public String getDataCollectionTime() {
        String time = "1";
        int hours = (int)Math.ceil(stopTime/3600f);
        if (hours > 0){
            time = String.valueOf(hours);
        }
        return time;
    }

    public void setDataPath(String path) {
        deviceDataPath = path;
    }
}
