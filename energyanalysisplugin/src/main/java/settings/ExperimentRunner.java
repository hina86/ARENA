package settings;

import com.intellij.openapi.ui.Messages;
import org.apache.commons.io.FileUtils;
import org.jsoup.internal.StringUtil;
import settings.model.*;
import util.Constants;
import util.MyFilesUtils;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ExperimentRunner {

    private boolean isRerun;
    private String scriptRunenrConfig = "";
    private boolean getTrace;
    private boolean getCpu;

    public static String resultsFolder;
    public static String apiVersion;


    private static ExperimentEventListener eventListener;
    private static ExperimentRunner experimentRunner;
    private static File scriptDir = new File(Constants.SCRIPT_DIR_PATH);

    private int readingNo = -1;
    private boolean isScriptPushed = false;
    private ExperimentStatus experimentStatus = ExperimentStatus.IDLE;
    private String baselineScriptText = "";
    private String experimentScriptText = "";

    private ExperimentRunner(){
    }

    /**
     * Creates a singleton instance of the ExperimentRunner class. It also initializes the callback object
     * and creates a directory where scripts will be saved.
     * @param experimentEventListener is used to call callback functions to display output in the tool window
     * @return instance of ExperimentRunner.
     */
    public static ExperimentRunner getInstance(ExperimentEventListener experimentEventListener){

        if (experimentRunner == null){
            eventListener = experimentEventListener;
            experimentRunner = new ExperimentRunner();
            scriptDir = new File(Constants.SCRIPT_DIR_PATH);
            if (!scriptDir.exists()){
                scriptDir.mkdir();
            }
//            createAPIVersionBat();
        }
        return experimentRunner;
    }

    // check if any device is connected to the PC. It used ls command. If the command runs successfully, it means that the device is connected
    public boolean isDeviceConnected(){
        String myScriptText = "adb shell ls";
        String filePath = Constants.RES_DIR_PATH + Constants. SCRIPT_FILE_SEPARATOR + Constants.DEVICES_BAT;
//        MyFilesUtils.writeFileData(filePath, myScriptText, false);
        boolean isConnected = MyFilesUtils.isDeviceConnected(filePath, eventListener);
        if(!isConnected){
            eventListener.printMessage("Error: No device connected.");
        }
        return isConnected;
    }
    //creates .bat file to get the api version of the device
    private static void createAPIVersionBat() {
        String myScriptText = "adb shell getprop ro.build.version.sdk";//command to get API version
        String filePath = Constants.RES_DIR_PATH + Constants. SCRIPT_FILE_SEPARATOR + Constants.API_VERSION_BAT;
        MyFilesUtils.writeFileData(filePath, myScriptText, true);
    }

    /**
     * Creates a new object of experimentRunner Class and nullifies the previous one.
     */
    public void reset(ExperimentEventListener experimentEventListener) {
        experimentRunner = null;
        eventListener = null;
        getInstance(experimentEventListener);
        resultsFolder = "";
    }

    /**
     * This class creates the script based on the @param settingsConfig
     * Creates bat files to push those scripts
     * Pushes the scripts to the device by running the bat file
     */
    public void pushScriptsWithConfigs(SettingsConfig settingsConfig) {
//        deleteScripts();
        isScriptPushed = false;
        boolean areFilesPushed = false;
        apiVersion = "";
        resultsFolder = settingsConfig.getPcResultsPath();
        //Basic script that needs to run before every kind of experiment type, experiment stage and data type.
        baselineScriptText = "#!/system/bin/sh" + "\n"+
                "chmod 777 " + Constants.DATA_LOCAL_TMP_DIR + "\n"+
                "dumpsys batterystats --reset &" + "\n"+    //clear battery stats before each run
                "dumpsys procstats --reset &" + "\n"+       //clear memory stats before each run
                "dumpsys netstats --reset &" + "\n"+        //clear network stats before each run
                //remove data files from any previous runs
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.LOGCAT_FILE_NAME + "\n" +
//                "rm " + settingsConfig.getDeviceDataPath()+ Constants.PID_FILE_NAME + "\n" +
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.CPU_FILE_NAME + "\n" +
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.MEMORY_FILE_NAME + "\n" +
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.BATTERY_FILE_NAME + "\n" +
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.NETWORK_FILE_NAME + "\n" +
                "rm " + settingsConfig.getDeviceDataPath()+ Constants.TRACE_FILE_NAME + "\n" +//todo: remove this line
                "rm " + Constants.DATA_LOCAL_TMP_DIR + Constants.TRACE_FILE_NAME + "\n" +
                "logcat -b all -c " + "\n";     //clear logcat
        experimentScriptText = "";
        getTrace = false;
        getCpu = false;
        boolean hasOtherTypes = false;
        for (DataType dataType: settingsConfig.getDataTypes()) {
            if (dataType == DataType.CPU) {
                getCpu = true;
            }
            else if (dataType.equals(DataType.MEMORY) ||
                    dataType.equals(DataType.NETWORK) ||
                    dataType.equals(DataType.BATTERY) ||
                    dataType.equals(DataType.TRACE) ){
                hasOtherTypes = true;
            }
        }
        String sleepPrefix = "";
        if (hasOtherTypes) {
            sleepPrefix = "sleep "+ settingsConfig.getStopTime() + " && ";
            experimentScriptText = experimentScriptText + sleepPrefix + "\n";
        }
        //based on type of data type(s) seleccted by user, add scripts to collect them in for install.sh file
        for (DataType dataType: settingsConfig.getDataTypes()) {
            switch (dataType){
                case TRACE:
                    getTrace = true; //script not required here. Script added in respective functions createInstallSh and createRerun sh
                    break;
                case MEMORY:
                    experimentScriptText = experimentScriptText.concat("dumpsys procstats --hours " +
                            settingsConfig.getDataCollectionTime() + " " +settingsConfig.getPackageName()+" > "+ settingsConfig.getDeviceDataPath()+ Constants.MEMORY_FILE_NAME + "" + "\n");
                    break;
                case BATTERY:
                    if(settingsConfig.getExperimentStage() == ExperimentStage.APP_EXECUTION) {
                        //get application specific stats if app is executing by adding packageName in the command
                        experimentScriptText = experimentScriptText.concat("dumpsys batterystats " +
                                settingsConfig.getPackageName() + " > " + settingsConfig.getDeviceDataPath() + Constants.BATTERY_FILE_NAME + "" + "\n");
                    } else {
                        //if it is baseline reading, package name is not added (otherwise exception is thrown)
                        experimentScriptText = experimentScriptText.concat("dumpsys batterystats " +
                                " > " + settingsConfig.getDeviceDataPath() + Constants.BATTERY_FILE_NAME + "" + "\n");
                    }
                    experimentScriptText = experimentScriptText.concat("dumpsys battery > "+settingsConfig.getDeviceDataPath() + Constants.VOLTAGE_FILE_NAME + "\n");
                    break;
                case NETWORK:
                    experimentScriptText = experimentScriptText.concat("dumpsys netstats detail > " +
                            settingsConfig.getDeviceDataPath()+ Constants.NETWORK_FILE_NAME + "" + "\n");
                    break;
            }
        }
        createScriptRunnerConfig(settingsConfig);//sets the configuration of the running experiment i.e. re-run resume, baseline, install etc to pass to the script runner app at runtime
        //Create bat file to push apk files to the device if not pushed already
        createPushApkBatFile(settingsConfig);
        //execute bat file that pushed the apks to the device
        boolean fileCreated = false;
        if (settingsConfig.getExperimentStage() == ExperimentStage.BASELINE) {
            fileCreated = createBaselineShFile(settingsConfig);   //Create sh file that gets baseline readings from the device
        }
        else if (settingsConfig.getExperimentStage() == ExperimentStage.APP_EXECUTION){
            fileCreated = createInstallShFile(settingsConfig);    //Create sh file that installs the app and runs the experiment
        }
        if (fileCreated) {
            areFilesPushed = MyFilesUtils.executeBatFile(scriptDir.getAbsolutePath() + File.separator + Constants.PUSH_BAT_FILE_NAME, eventListener);
            createLaunchBatFile(settingsConfig);      //Create bat file that pushes the install script on device
            //Execute BAT files for pushing apk files and scripts to the device
            eventListener.printMessage("Running experiment in " + settingsConfig.getDelayTime() + " seconds");
            isScriptPushed = MyFilesUtils.executeBatFile(scriptDir.getAbsolutePath() + File.separator + Constants.LAUNCH_SCRIPT_BAT_FILE_NAME, eventListener);
            if (isScriptPushed) {
                experimentStatus = ExperimentStatus.SCRIPT_PUSH_SUCCESS;
            } else {
                experimentStatus = ExperimentStatus.SCRIPT_PUSH_ERROR;
                eventListener.printMessage("Process failed. Make sure device is connected");
            }
        }
    }

//    private void deleteScripts() {
//
//        for (File file: exeDir.listFiles()){
//            String extension = FilenameUtils.getExtension(String.valueOf(file));
//            if (extension.equals("sh") || extension.equals(".bat")){
//                file.delete();
//            }
//        }
//
//
//    }

    /**
     * sets the configuration of the running experiment i.e. re-run resume, baseline, install etc
     * to pass to the script runner app at runtime
     * This tells the script runner to run which script
     * The strings "baseline_rerun", "baseline", "rerun", "install" if changed here,
     * must also be changed in the script runner application source code
     * @param settingsConfig
     */
    private void createScriptRunnerConfig(SettingsConfig settingsConfig) {
        if (settingsConfig.getExperimentStage() == ExperimentStage.BASELINE){
            if (isRerun){
                scriptRunenrConfig = "baseline_rerun";
                isRerun = false;
            }
            else{
                scriptRunenrConfig = "baseline";
            }
        } else if (settingsConfig.getExperimentStage() == ExperimentStage.APP_EXECUTION){
            if (isRerun){
                scriptRunenrConfig = "rerun";
                isRerun = false;
            }
            else{
                scriptRunenrConfig = "install";
            }
        }
    }
    //Executes python file with arguments in the settingsConfig. If python file is not already present, it is also created.
    public boolean runPowerMonitor(SettingsConfig settingsConfig) {

        if (isScriptPushed) {
            try {
                //Create python file if not created already
                File scriptFile = new File(Constants.RES_DIR_PATH + Constants.POWER_MONITOR_SCRIPT_FILE_NAME);
                String filePath = scriptFile.getAbsolutePath();
                experimentStatus = ExperimentStatus.POWER_MONITOR_START_SUCCESS;
                String prefix = "";
                if (settingsConfig.getExperimentStage() == ExperimentStage.BASELINE){
                    prefix = Constants.BASELINE_;
                }
                //Create output file for power monitor
                File outputFile = new File(settingsConfig.getPcResultsPath() + File.separator + prefix + Constants.POWER_MONITOR_OUTPUT_FILE_NAME + Constants._R + readingNo + ".csv");
                if (outputFile.exists()) {
                    boolean del = outputFile.delete();
                }
                System.out.println(outputFile.getAbsolutePath());
                //After output file has been created, execute thr script to start power monitor.
                if (outputFile.createNewFile()) {
                    String arguments = settingsConfig.getStopTime() + " " + outputFile.getAbsolutePath();//arguments of python script, (each argument is separated by space)
                    //run the python script that runs the power monitor. Pass stop time and output file path to the running script as arguments
                    return MyFilesUtils.runPythonScript(filePath, arguments, eventListener);
                }
            } catch (IOException exception) {
                exception.printStackTrace();
                eventListener.printError(exception);
            }
        } else {
            eventListener.printMessage("Can not run power monitor. Check your settings");
        }
        return false;
    }

    public void startExperiment(SettingsConfig settingsConfig) {
        readingNo = -1;
        readingNo++;
        eventListener.printMessage("Preparing Run # " + Constants._R + readingNo);
        if (isDeviceConnected() && hasScriptRunner()) {
            eventListener.stopTimers();
            if(canRun()) {
                isRerun = false;
                pushScriptsWithConfigs(settingsConfig);
                SwingUtilities.invokeLater(() -> {
                    if (settingsConfig.getExperimentType() == ExperimentType.HARDWARE) {
                        if (runPowerMonitor(settingsConfig)) {
                            eventListener.printMessage("Experiment ended. Click on 'Pull Data' button to pull data files to the results folder ");
                        } else {
                            eventListener.stopTimers();
                            readingNo--;
                        }
                    } else {
                        eventListener.startStartDelayTimer(settingsConfig.getDelayTime());//starts a timer that tells when to pull data
                    }
                });

            }
        } else {
            readingNo --;
        }

    }
    //update configurations if any changes are made. Re run the power monitor.
    public void rerun(SettingsConfig settingsConfig){
        eventListener.printMessage("Preparing Run # " + Constants._R + readingNo);
        if(readingNo != -1) {
            if (isDeviceConnected()) {
                //show dialog to prompt user that experiment iteration is going to be re-run
                int result = Messages.showYesNoDialog("Are you sure you want to re run the experiment iteration?\nNote: Re-run would run the experiment again for previous reading and remove previous data files from device (if any)",
                        "Rerun", "OK", "Cancel", Messages.getQuestionIcon());
                if (result == Messages.OK) {
                    eventListener.stopTimers();
                    if (canRun()) {
                        isRerun = true;
                        SwingUtilities.invokeLater(() -> {
                            pushScriptsWithConfigs(settingsConfig);//pushes new scripts with re run configs to device
                            if (settingsConfig.getExperimentType() == ExperimentType.HARDWARE) {
                                if (runPowerMonitor(settingsConfig)) {
                                    eventListener.printMessage("Experiment ended. Click on 'Pull Data' button to pull data files to the results folder ");
                                } else {
                                    eventListener.stopTimers();
                                }
                            } else {
                                eventListener.startStartDelayTimer(settingsConfig.getDelayTime());//starts a timer that tells when to pull data
                            }
                        });
                    }
                }
            }
        }

    }
    //update configurations if any changes are made. Re run the power monitor. Update reading number.
    public void resume(SettingsConfig settingsConfig){
        if (readingNo < settingsConfig.getNumberOfRuns() -1) {
            int result = Messages.showYesNoDialog("Are you sure you want to resume the experiment iterations?\n Note: Resume would run the experiment for collection of next reading and remove previous data files from device (if any)",
                    "Resume", "OK", "Cancel", Messages.getQuestionIcon());
            if (result == Messages.OK) {
                eventListener.stopTimers();
                if (canRun()) {
                    readingNo++;
                    eventListener.printMessage("Preparing Run # " + Constants._R + readingNo);
                    SwingUtilities.invokeLater(() -> {
                        if (isDeviceConnected()) {
                            isRerun = true;
                            pushScriptsWithConfigs(settingsConfig);//pushes scripts with resume configs to device
                            if (settingsConfig.getExperimentType() == ExperimentType.HARDWARE) {
                                if (runPowerMonitor(settingsConfig)) {
                                    eventListener.printMessage("Experiment ended. Click on 'Pull Data' button to pull data files to the results folder ");
                                } else {
                                    eventListener.stopTimers();
                                    readingNo--;
                                }
                            } else {
                                eventListener.startStartDelayTimer(settingsConfig.getDelayTime());//starts a timer that tells when to pull data
                            }
                        } else {
                            readingNo--;
                        }
                    });
                }
            }
        } else {
            eventListener.printMessage("All runs have completed.");
        }
    }
    //pull data from the device path to the results path in settings configs. Rename the files as per reading number.
    //IMPORTANT: FOR THIS FUNCTION TO WORK MAKE SURE THAT THE RESULT PATH DIRECTORY OR ANY OF ITS FILES ARE NOT OPEN IN ANY APPLICATION
    public void pullData(SettingsConfig settingsConfig){
        if (isScriptPushed) {
            createPullBatFile(settingsConfig);      //Create bat file that pulls data from the device
            try {
                //Get api version from device, if not found ask user to enter in analysis tab
                apiVersion = MyFilesUtils.executeAPIBatFile(Constants.RES_DIR_PATH + Constants.API_VERSION_BAT, eventListener);
                if (!StringUtil.isNumeric(apiVersion)) {
                    eventListener.printMessage("Unable to read API version, provide manually in analysis");
                    apiVersion = "";
                }
                //Execute bat file to pull data
                if (MyFilesUtils.executeBatFile(scriptDir.getAbsolutePath() + File.separator + Constants.PULL_BAT_FILE_NAME, eventListener)) {
                    eventListener.printMessage("Files pulled successfully to: " + settingsConfig.getPcResultsPath());
                    if (settingsConfig.getExperimentType() == ExperimentType.HARDWARE) {
                        String prefix = "";
                        if (settingsConfig.getExperimentStage() == ExperimentStage.BASELINE) {
                            prefix = Constants.BASELINE_;
                        }
                        validateEnergyFile(prefix, settingsConfig.getPcResultsPath(), settingsConfig.getStopTime(), readingNo, eventListener);
                    }
                }
                else {
                    eventListener.printMessage("Pull failed. Make sure device is connected.");
                }
            }
            catch (Exception e){
                System.out.println(e.getMessage());
                eventListener.printMessage("Pull failed. Try again");
            }
        }
        else{
            eventListener.printMessage("Run experiment to pull data");
        }
    }

    private static void validateEnergyFile(String prefix , String pcResultPath, long stopTime, int readingNo, ExperimentEventListener eventListener) {
        File dir = new File(pcResultPath);
        if (dir.exists()){
            for (File resFile: dir.listFiles()){
                if (resFile.getName().equals(prefix + Constants.POWER_MONITOR_OUTPUT_FILE_NAME + Constants._R + readingNo + ".csv")){
                    try {
                        FileReader fileReader = new FileReader(resFile);
                        BufferedReader reader = new BufferedReader(fileReader);
                        String line = "";
                        int count = 0;
                        while ((line = reader.readLine()) != null){
                            count++;
                        }
                        count = count - 2;//removing first 2 lines;
                        //if energy readings lie in the range (stoptime * 5000) -1000 <= count <= (stoptime*5000 + 1000)
                        int lowerLimit = (int) ((stopTime * 5000) - 1000);
                        int upperLimit = (int) ((stopTime * 5000) + 1000);
                        if (count < lowerLimit || count > upperLimit ){
                            if (eventListener != null) {
                                eventListener.printMessage("Warning: Power monitor dropped some readings. Re-run the iteration for better results" + "\n" +
                                        "No of rows in file "+ resFile.getName() + " is "+ count +" which is not in the required range ( "+ lowerLimit + " - " + upperLimit + " )");
                                eventListener.showDialog("Experiment", "Warning: Power monitor dropped some readings. Re-run the iteration for better results" + "\n" +
                                        "No of rows in file "+ resFile.getName() + " is "+ count +" which is not in the required range ( "+ lowerLimit + " - " + upperLimit + " )");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    //stop the app from running and uninstall it.
    public void stop(SettingsConfig settingsConfig){
        String stopScript = "adb shell am force-stop " + settingsConfig.getPackageName() + " \n" + //stops application
                "adb shell pm uninstall "+ settingsConfig.getPackageName()+  "\n" +                // uninstalls application apk
                "adb shell pm uninstall "+  settingsConfig.getPackageName() + ".test";             // uninstalls test apk
        String filePath = scriptDir.getAbsolutePath() + Constants. SCRIPT_FILE_SEPARATOR + Constants.STOP_BAT_FILE_NAME;
        MyFilesUtils.writeFileData(filePath, stopScript, true);//writes file to script folder
        if(MyFilesUtils.executeBatFile(filePath, eventListener)){//execute file and show the message based on its execution success (true/false)
            eventListener.printMessage("Application stopped successfully");
        } else {
            eventListener.printMessage("Process failed. Make sure device is connected.");
        }
    }
    public void viewResults(){
        //todo:
    }


    //Creates a install.sh file that removes all previous files, installs application,
    // runs it and collects ADB logs and other specified data types
    private boolean createInstallShFile(SettingsConfig settingsConfig) {
        String myScriptText = baselineScriptText;// +
//                "pm install -t -r \""+ Constants.DATA_LOCAL_TMP_DIR+ settingsConfig.getApkFileName()+  "\"\n" ;//command to install apk file after removing any previous installations
        //installs and runs test apk only if it is supplied.
        if (!settingsConfig.getTestApkPath().isEmpty()) {
            myScriptText = myScriptText.concat(/*"pm install -t -r \"" + Constants.DATA_LOCAL_TMP_DIR + settingsConfig.getTestApkFileName() + "\"\n" +*/
                    "am instrument -r -e debug false -e class '" + settingsConfig.getTestClass() + "' " + settingsConfig.getTestRunner() + "\n"
            );
        }
        //if test apk is not supplied then start the app and wait for stop time to collect data, user will test app manually
        else{
            myScriptText = myScriptText.concat("am start -a android.intent.action.MAIN -n " +settingsConfig.getPackageName() + settingsConfig.getLauncherClass() + "" + "\n"/*+
                    "sleep "+ settingsConfig.getStopTime() + " && "*/);
        }
        //command to output logcat with thread time
/*        myScriptText = myScriptText.concat(*//*"logcat -v threadtime -d > "+ settingsConfig.getDeviceDataPath()+ Constants.LOGCAT_FILE_NAME + " &" + "\n"+*//*
                "sleep 4 && ps > "+ settingsConfig.getDeviceDataPath()+ Constants.PID_FILE_NAME + " &" + "\n" +
                        "sleep 4 && dumpsys package " +settingsConfig.getPackageName()+ " > "+ settingsConfig.getDeviceDataPath() + Constants.UID_FILE_NAME + " &" + "\n"
        );*/

        if (getTrace) { //if trace datd is required, add its start command
            myScriptText = myScriptText.concat("sleep 3 && am profile " + settingsConfig.getPackageName() + " start " + Constants.DATA_LOCAL_TMP_DIR + Constants.TRACE_FILE_NAME + " & " + "\n");
        }
        if (getCpu){ //if cpu data is required then add its command
            myScriptText = myScriptText.concat("top -n "+ settingsConfig.getStopTime() +" > " + settingsConfig.getDeviceDataPath() + Constants.CPU_FILE_NAME + " & " + "\n");
        }
        myScriptText = myScriptText.concat(experimentScriptText);
        if (getTrace){ //if trace command is required, then add its stop command after the stoptime as passed
            myScriptText = myScriptText.concat("am profile stop "+ settingsConfig.getPackageName() +" &");
        }

        String filePath = scriptDir.getPath() + Constants. SCRIPT_FILE_SEPARATOR + Constants.INSTALL_SH_FILE_NAME;
        return MyFilesUtils.writeFileData(filePath, myScriptText, true);//saves script file in scripts directory
    }
    //Creates a baseline.sh file that removes all previous file and collects ADB logs and other specified data types
    private boolean createBaselineShFile(SettingsConfig settingsConfig) {
        //runs the baseline script and collects data when stop time ends (hence sleep is added at the begining)
        String myScriptText = baselineScriptText +
//                "sleep 4 && ps > "+ settingsConfig.getDeviceDataPath()+ Constants.PID_FILE_NAME + " &" + "\n" +
//                "sleep 4 && dumpsys package " +settingsConfig.getPackageName()+ " > "+ settingsConfig.getDeviceDataPath() + Constants.UID_FILE_NAME + " &" + "\n"+
                experimentScriptText;//experiment text contains command for other data types
        String filePath = scriptDir.getPath() + Constants. SCRIPT_FILE_SEPARATOR + Constants.BASELINE_SH_FILE_NAME;
        return MyFilesUtils.writeFileData(filePath, myScriptText, true);
    }

    //Creates a bat file that pushes the apks to the device,
    private void createPushApkBatFile(SettingsConfig settingsConfig) {
        String scriptText = "adb devices" + "\n" +
                "adb push "+ settingsConfig.getApkPath() + " " + Constants.DATA_LOCAL_TMP_DIR + "\n";
        if (!settingsConfig.getTestApkPath().isEmpty()) {
            scriptText = scriptText.concat("adb push " + settingsConfig.getTestApkPath() + " " + Constants.DATA_LOCAL_TMP_DIR + "\n");
        }
        if(settingsConfig.getExperimentStage() == ExperimentStage.APP_EXECUTION) {
            if(isRerun){
                if (settingsConfig.getRunConfig() == RunConfig.CLEAN_DATA){
                    scriptText = scriptText+  "pm clear "+ settingsConfig.getPackageName()+  "\n";//clears application data for which package name is specified
                } else {
                    scriptText = scriptText + "adb shell pm install -r \"" + Constants.DATA_LOCAL_TMP_DIR + settingsConfig.getApkFileName() + "\"\n";//command to install apk file after removing any previous installations
                }
            } else {
                scriptText = scriptText + "adb shell pm install -r \"" + Constants.DATA_LOCAL_TMP_DIR + settingsConfig.getApkFileName() + "\"\n";//command to install apk file after removing any previous installations
            }
            if (!settingsConfig.getTestApkPath().isEmpty()) {
                scriptText = scriptText.concat("adb shell pm install -t -r \"" + Constants.DATA_LOCAL_TMP_DIR + settingsConfig.getTestApkFileName() + "\"\n");
            }
        }
        scriptText = scriptText.concat("adb push "+ Constants.SCRIPT_DIR_PATH + File.separator + Constants.SCRIPT_RUNNER_APK + " "+ Constants.DATA_LOCAL_TMP_DIR + "\n");
        scriptText = scriptText.concat("adb shell pm install -r \""+ Constants.DATA_LOCAL_TMP_DIR+ Constants.SCRIPT_RUNNER_APK+  "\"\n");
        scriptText = scriptText + "adb shell rm " + Constants.DATA_LOCAL_TMP_DIR + Constants.SCRIPT_FILE_SEPARATOR + Constants.INSTALL_SH_FILE_NAME + "\n" +
                "adb shell rm " + Constants.DATA_LOCAL_TMP_DIR + Constants.SCRIPT_FILE_SEPARATOR + Constants.BASELINE_SH_FILE_NAME + "\n" +
                "adb shell rm " + Constants.DATA_LOCAL_TMP_DIR + Constants.SCRIPT_FILE_SEPARATOR + Constants.PARALLEL_SH_FILE_NAME + "\n" +
                "adb push "+ Constants.SCRIPT_DIR_PATH + File.separator + Constants.INSTALL_SH_FILE_NAME + " "+ Constants.DATA_LOCAL_TMP_DIR + "\n" +
                "adb push "+ Constants.SCRIPT_DIR_PATH + File.separator + Constants.BASELINE_SH_FILE_NAME + " "+ Constants.DATA_LOCAL_TMP_DIR + "\n" +
                "adb push "+ Constants.SCRIPT_DIR_PATH + File.separator + Constants.PARALLEL_SH_FILE_NAME + " "+ Constants.DATA_LOCAL_TMP_DIR + "\n";

        String filePath = scriptDir.getAbsolutePath() + Constants. SCRIPT_FILE_SEPARATOR + Constants.PUSH_BAT_FILE_NAME;
        MyFilesUtils.writeFileData(filePath, scriptText, true);
    }
    //creates a bat file that pushes the sh scripts to the device
    private void createLaunchBatFile(SettingsConfig settingsConfig) {
        //removes previous scripts before pushing new script files.
        String pushScriptText = "adb shell am start -a android.intent.action.MAIN -n com.example.scriptrunner/.MainActivity -e key "+ scriptRunenrConfig + " --el stop_time "+ settingsConfig.getStopTime() * 1000 + " --el delay " + settingsConfig.getDelayTime()*1000;//passing the time in ms to app
        //if trace or cpu data needs to be collected only then it runs parallel.sh script
        if (getTrace || getCpu){
            pushScriptText = pushScriptText.concat(" -e parallel true" + "\n");
        }
        else{
            pushScriptText = pushScriptText.concat(" -e parallel false" + "\n");
        }

        String filePath = scriptDir.getAbsolutePath() + Constants.SCRIPT_FILE_SEPARATOR + Constants.LAUNCH_SCRIPT_BAT_FILE_NAME;
        MyFilesUtils.writeFileData(filePath, pushScriptText, true);
    }
    //creates a bat file that pulls the output files into result folder
    private void createPullBatFile(SettingsConfig settingsConfig) {
        String prefix = "";
        if (settingsConfig.getExperimentStage() == ExperimentStage.BASELINE){
            prefix = Constants.BASELINE_;
        }
        String dataText = "";
        dataText = dataText.concat("adb shell logcat -v threadtime -d > " + settingsConfig.getPcResultsPath() + File.separator + prefix + "logcat"+Constants._R + readingNo + ".txt" + " &" + "\n");

        String pushScriptText = dataText +
                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.CPU_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "cpu"+Constants._R + readingNo + ".txt" + "\n" +
                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.MEMORY_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "memory"+Constants._R + readingNo + ".txt" + "\n"+
                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.BATTERY_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "battery"+Constants._R +  readingNo + ".txt" + "\n" +
                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.NETWORK_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "network"+Constants._R + readingNo + ".txt" + "\n" +
                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.VOLTAGE_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "voltage"+Constants._R + readingNo + ".txt" + "\n" +

//                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.PID_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "pid"+Constants._R + readingNo + ".txt" + "\n" +
//                "adb pull " + settingsConfig.getDeviceDataPath() + Constants.UID_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "uid"+Constants._R + readingNo + ".txt" + "\n" +
                "adb pull "+ Constants.DATA_LOCAL_TMP_DIR + Constants.TRACE_FILE_NAME + " " + settingsConfig.getPcResultsPath() + File.separator + prefix + "trace"+Constants._R+ readingNo+ ".trace";
        String filePath = scriptDir.getAbsolutePath() + Constants.SCRIPT_FILE_SEPARATOR + Constants.PULL_BAT_FILE_NAME;
        MyFilesUtils.writeFileData(filePath, pushScriptText, true);
    }
    public ExperimentStatus getExperimentStatus() {
        return  experimentStatus;
    }
    public String getPowerMonitorText() {
        return "Connect power monitor with the device and PC to start data collection";
    }
    //checks if script runner apk is present in scripts directory
    public boolean hasScriptRunner() {
        boolean hasIt = new File(scriptDir, Constants.SCRIPT_RUNNER_APK).exists();
        if (!hasIt){
            eventListener.printMessage("Place script-runner.apk file in "+ scriptDir.getAbsolutePath() + " to proceed.");
        }
        return hasIt;
    }
    private boolean canRun() {
        File file = new File(scriptDir.getPath() + File.separator + Constants.SCRIPT_RUNNER_APK);
        if (!file.exists()){
            eventListener.printMessage("Place script-runner.apk in user.home/scripts directory before running the experiment");
            return false;
        }
        try{
            File scriptFile = new File(scriptDir.getAbsolutePath() + File.separator + Constants.POWER_MONITOR_SCRIPT_FILE_NAME);
            if (!scriptFile.exists()) {
                if (scriptFile.createNewFile()) {
                    //creates the python file that runs the power monitor.
                    FileUtils.writeStringToFile(scriptFile, Constants.Scripts.PY_SCRIPT_PM, StandardCharsets.UTF_8);
                } else {
                    eventListener.printMessage("Creation of python script failed");
                    return false;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            eventListener.printError(e);
            return false;
        }
        return true;
    }

    public String getDataTypes(SettingsConfig settingsConfig) {
        StringBuilder data = new StringBuilder();
        for (DataType datatype: settingsConfig.getDataTypes()){
            data.append(datatype.name()).append(", ");
        }
        if (data.length() > 2){
            data = new StringBuilder(data.substring(0, data.length() - 2));
        }

        return data.toString();
    }
}
