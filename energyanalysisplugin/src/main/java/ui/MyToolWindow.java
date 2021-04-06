package ui;

import analysis.ANALYSIS_TYPE;
import analysis.AnalysisArgs;
import analysis.AnalysisEventListener;
import analysis.AnalysisRunner;
import cleanup.CleanupEventListener;
import cleanup.CleanupRunner;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import settings.ExperimentEventListener;
import settings.ExperimentRunner;
import settings.model.*;
import util.MyFilesUtils;
import visualization.VisualizationEventListener;
import visualization.VisualizationRunner;
import visualization.helpers.ColorListRenderer;
import visualization.models.GRAPH_TYPE;
import visualization.models.VisConfig;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

public class MyToolWindow implements ActionListener, MouseListener, ExperimentEventListener, CleanupEventListener, VisualizationEventListener, ListSelectionListener, AnalysisEventListener {
    private JPanel myToolWindow;
    private JTabbedPane tabbedPane;
    private JPanel analysisPanel;
    private JPanel preprocessPanel;
    private JRadioButton hardwareBasedRadioButton;
    private JRadioButton softwareBasedRadioButton;
    private JRadioButton baselineRadioButton;
    private JRadioButton applicationExecutionRadioButton;
    private JCheckBox ADBLogsCheckBox;
    private JCheckBox currentVoltageDataCheckBox;
    private JCheckBox traceFilesCheckBox;
    private JCheckBox CPUStatisticsCheckBox;
    private JCheckBox batteryStatisticsCheckBox;
    private JCheckBox wifiStatisticsCheckBox;
    private JCheckBox memoryStatisticsCheckBox;
    private JTextField noOfRuns;
    private JTextField apkPath;
    private JTextField testApkPath;
    private JTextField dataPath;
    private JTextField resultsPath;
    private JTextField stopTime;
    private JRadioButton reInstallAppRadioButton;
    private JRadioButton clearDataRadioButton;
    private JLabel testApkFileExpBtn;
    private JLabel apkFileExpBtn;
    private JLabel resultsFileExpBtn;
    private JButton runButton;
    private JButton reRunButton;
    private JButton resumeButton;
    private JButton stopButton;
    private JButton pullDataButton;
    private JButton viewResultsButton;
    private JTextArea terminal;
    private JTextField testClass;
    private JTextField testRunner;
    private JPanel experimentTypeLabel;
    private JPanel settingsPanel;
    private JPanel experimentConfigLabel;
    private JPanel configPanel;
    private JButton resetBtn;
    private JPanel terminalLabel;
    private JTextPane warningLabel;
    private JTextField delayTime;
    private JLabel stopTimeTimerLabel;
    private JLabel startTimeTimerLabel;
    private JList<File> rawFileList;
    private JList<File> cleanFileList;
    private JTextArea analysisTerminal;
    private JTextField startTag;
    private JTextField endTag;
    private JTextField analysisVersion;
    private JButton cleanDataButton;
    private JPanel dataAndResults;
    private JPanel dataCleanupOptions;
    private JLabel addRawBtn;
    private JPanel terminalPanel;
    private JTextField apiVersion;
    private JTextField packageNameTF;
    private JTextField prepResultFolder;
    private JPanel statisticalAnalysisOptions;
    private JPanel visualizationPanel;
    private JPanel visualizationOptions;
    private JComboBox<String> xAxisCB;
    private JComboBox<GRAPH_TYPE>  graphTypeCB;
    private JComboBox<String>  yAxisCB;
    private JComboBox<String> groupByCB1;
    private JComboBox<String> groupByCB2;
    private JButton preprocessResetBtn;
    private JTextField expTime;
    private JTextField xAxisLabel;
    private JTextField yAxisLabel;
    private JTextField visFilePath;
    private JComboBox<String>  orderCB;
    private JList<String> orderList;
    private JButton genGraphBtn;
    private JLabel dataFileChooserBtn;
    private JLabel graphFileChooserBtn;
    private JTextField graphResultPath;
    private JTextArea visterminal;
    private JList colorList;
    private JButton resetVisBtn;
    private JTextField graphCaption;
    private JTextField graphHeight;
    private JTextField graphWidth;
    private JComboBox unitCB;
    private JComboBox<ANALYSIS_TYPE> analysisTypeCb;
    private JComboBox<String> indPropCB;
    private JComboBox<String> depPropCB;
    private JButton anaResetbtn;
    private JButton anaReportBtn;
    private JComboBox<String> groupValCB;
    private JComboBox<String> filter1PropCB;
    private JTextArea anaTerminal;
    private JEditorPane anaHelpPane;
    private JLabel anaResExpBtn;
    private JLabel anaFileExpBtn;
    private JTextField anaResultPath;
    private JTextField anaDataFilePath;
    private JCheckBox legendCheckBox;
    private JTextArea facet1Vals;
    private JComboBox<String> filter1CB;
    private JComboBox<String> filter2CB;
    private JTextArea facet2Vals;
    private JTextArea depTA;
    private JTextArea indTA;
    private JComboBox filter2PropCB;
    private JComboBox group2ValCB;
    private JCheckBox showLegendCB;
    private JLabel prepFileChooser;
    private JPanel visPanel;
    private JTextPane experimentStatus;
    private SettingsConfig settingsConfig;
    ButtonGroup experimentTypeButtonGroup;
    ButtonGroup experimentStageButtonGroup;
    ButtonGroup reRunConfigButtonGroup;
    ArrayList<JCheckBox> checkBoxes;
    private JFileChooser fileChooser;

    private Timer startTimeTimer;
    private Timer stopTimeTimer;
    private TableModel tableModel = new DefaultTableModel();
    AtomicReference<DefaultListModel<Color>> colorModel = new AtomicReference<>(new DefaultListModel<>());
    AtomicReference<DefaultListModel<String>> orderModel = new AtomicReference<>(new DefaultListModel<>());
    private ArrayList<String> filter1List= new ArrayList<>();
    private ArrayList<String> filter2List= new ArrayList<>();
    private ArrayList<String> depVarList = new ArrayList<>();
    private ArrayList<String> indVarList = new ArrayList<>();
    private String path = "";

    public MyToolWindow(ToolWindow toolWindow) {
        setupUI();
        addListeners();
        setUpDefaults();
        setupAnalysisUI();
    }

    private void setupAnalysisUI() {
        // Fetch values for analysis types to show in drop down
        DefaultComboBoxModel<ANALYSIS_TYPE> anaComboBox = new DefaultComboBoxModel<ANALYSIS_TYPE>();
        for(ANALYSIS_TYPE analysis_type: AnalysisRunner.getAnalysisTypes()){
            anaComboBox.addElement(analysis_type);
        }
        analysisTypeCb.setModel(anaComboBox);
        //add selection listeners for dropdowns.
        analysisTypeCb.addItemListener(e -> {
                    //on selection of analysis type, get the url of the help html file and show in info UI
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        SwingUtilities.invokeLater(() -> {
                            if (analysisTypeCb.getSelectedItem() != null) {
                                //fetch url of local html file
                                URL helpUrl = AnalysisRunner.getInstance(MyToolWindow.this).getHelp((ANALYSIS_TYPE) analysisTypeCb.getSelectedItem());
                                try {
                                    anaHelpPane.setPage(helpUrl);//show html file in the UI
                                } catch (IOException exception) {
                                    exception.printStackTrace();
                                }
                            }
                        });
                    }
                }
        );
        //add selection listener to the filter 1 drop down
        filter1PropCB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(() -> {
                    //upon selection get values of the filter property and populate in value drop down
                    String[] values = AnalysisRunner.getInstance(MyToolWindow.this).getUniqueValsFromCol(anaDataFilePath.getText(), (String) filter1PropCB.getSelectedItem());
                    if (values != null) {
                        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
                        for (String value : values) {
                            comboBoxModel.addElement(value);
                        }
                        groupValCB.setModel(comboBoxModel);
                    }
                });
            }
        });
        //add selection listener to the filter 1 drop down
        filter2PropCB.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                SwingUtilities.invokeLater(() -> {
                    //upon selection get values of the filter property and populate in value drop down
                    String[] values = AnalysisRunner.getInstance(MyToolWindow.this).getUniqueValsFromCol(anaDataFilePath.getText(), (String) filter2PropCB.getSelectedItem());
                    if (values != null) {
                        DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
                        for (String value : values) {
                            comboBoxModel.addElement(value);
                        }
                        group2ValCB.setModel(comboBoxModel);
                    }
                });
            }
        });
        anaFileExpBtn.addMouseListener(this);
        anaResExpBtn.addMouseListener(this);
    }

    //sets up default values of some fields in the form.
    private void setUpDefaults() {
        this.settingsConfig = new SettingsConfig();
        //Clear all inputs
        for(Component control : configPanel.getComponents()) {
            if(control instanceof JTextField) {
                JTextField ctrl = (JTextField) control;
                ctrl.setText("");
            }
        }
        for(Component control : settingsPanel.getComponents()) {
            if(control instanceof JCheckBox) {
                JCheckBox ctrl = (JCheckBox) control;
                ctrl.setSelected(false);
            }
        }
        //set default values
        experimentTypeButtonGroup.setSelected(hardwareBasedRadioButton.getModel(), true);//hardware is selected by default
        experimentStageButtonGroup.setSelected(applicationExecutionRadioButton.getModel(), true);//app execution is selected by default
        ADBLogsCheckBox.setSelected(true);
        ADBLogsCheckBox.setEnabled(false);//adb log is mandatory hence disabled for check/uncheck by user
        currentVoltageDataCheckBox.setSelected(true);
        currentVoltageDataCheckBox.setEnabled(false);//current voltage is mandatory hence disabled for check/uncheck by user (as hardware is selected)
        reRunConfigButtonGroup.setSelected(reInstallAppRadioButton.getModel(), true);//re-install is selected by default on every iteration
        reInstallAppRadioButton.setEnabled(true);
        clearDataRadioButton.setEnabled(true);
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("APK Files", "apk");
        fileChooser.setFileFilter(filter);
        testClass.setEnabled(true);
        testRunner.setEnabled(true);
        dataPath.setText(settingsConfig.getDeviceDataPath());
        delayTime.setText(String.valueOf(settingsConfig.getDelayTime()));
        terminal.setText("");
        stopTimeTimerLabel.setText("");
        startTimeTimerLabel.setText("");
        warningLabel.setText("");
        apiVersion.setText("");
        packageNameTF.setText("");
        batteryStatisticsCheckBox.setEnabled(false);
    }

    /**
     * Creating groups for radio buttons
     */
    private void setupUI() {
        experimentTypeButtonGroup = new ButtonGroup();
        experimentTypeButtonGroup.add(hardwareBasedRadioButton);
        experimentTypeButtonGroup.add(softwareBasedRadioButton);
        experimentStageButtonGroup = new ButtonGroup();
        experimentStageButtonGroup.add(baselineRadioButton);
        experimentStageButtonGroup.add(applicationExecutionRadioButton);
        reRunConfigButtonGroup = new ButtonGroup();
        reRunConfigButtonGroup.add(reInstallAppRadioButton);
        reRunConfigButtonGroup.add(clearDataRadioButton);

        checkBoxes = new ArrayList<>();
        checkBoxes.add(ADBLogsCheckBox);
        checkBoxes.add(currentVoltageDataCheckBox);
        checkBoxes.add(CPUStatisticsCheckBox);
        checkBoxes.add(memoryStatisticsCheckBox);
        checkBoxes.add(batteryStatisticsCheckBox);
        checkBoxes.add(traceFilesCheckBox);
        checkBoxes.add(wifiStatisticsCheckBox);

        terminal.setLineWrap(true);
    }

    /**
     * Adding click listeners on all buttons
     */
    private void addListeners() {
        //Settings tab
        testApkFileExpBtn.addMouseListener(this);
        apkFileExpBtn.addMouseListener(this);
        resultsFileExpBtn.addMouseListener(this);
        runButton.addActionListener(this);
        reRunButton.addActionListener(this);
        resumeButton.addActionListener(this);
        stopButton.addActionListener(this);
        pullDataButton.addActionListener(this);
        viewResultsButton.addActionListener(this);
        hardwareBasedRadioButton.addActionListener(this);
        softwareBasedRadioButton.addActionListener(this);
        baselineRadioButton.addActionListener(this);
        applicationExecutionRadioButton.addActionListener(this);
        reInstallAppRadioButton.addActionListener(this);
        clearDataRadioButton.addActionListener(this);
        resetBtn.addActionListener(this);
        traceFilesCheckBox.addActionListener(this);

        //Analysis tab
        cleanDataButton.addActionListener(this);
        preprocessResetBtn.addActionListener(this);
        addRawBtn.addMouseListener(this);
        prepResultFolder.addMouseListener(this);

        genGraphBtn.addActionListener(this);
        dataFileChooserBtn.addMouseListener(this);
        graphFileChooserBtn.addMouseListener(this);
        resetVisBtn.addActionListener(this);

        anaResetbtn.addActionListener(this);
        anaReportBtn.addActionListener(this);
    }

    public JComponent getContent() {
        return myToolWindow;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
//        if(path.isEmpty()) {
//            path = getClass().getResource("/scripts/").getPath();
//            path = path.replace("file:/", "");
//            Constants.RES_DIR_PATH = path;
//            printMessage(path);
//        }
        //Adding listeners to buttons
        if(e.getSource() == runButton){
            //Starts experiment
            ConfigStatus configStatus = createSettingsConfig();//gets settings from UI and gets their validation status
            if (configStatus.isValid()) {
                ExperimentRunner.getInstance(this).startExperiment(settingsConfig);
            } else {
                printMessage(configStatus.getMessage());
            }

        } else if(e.getSource() == reRunButton){
            //Reruns expetiment
            ConfigStatus configStatus = createSettingsConfig(); //gets settings from UI and gets their validation status
            if (configStatus.isValid()) {//validates settings config data
                ExperimentRunner.getInstance(this).rerun(settingsConfig);
            } else {
                printMessage(configStatus.getMessage());
            }
        }else if(e.getSource() == resumeButton){
            //resumes experiment
            ConfigStatus configStatus = createSettingsConfig(); //gets settings from UI and gets their validation status
            if (configStatus.isValid()) {//validates settings config data
                ExperimentRunner.getInstance(this).resume(settingsConfig);
            } else {
                printMessage(configStatus.getMessage());
            }
        }else if(e.getSource() == stopButton){
            printMessage("Stopping application ...");
            if (ExperimentRunner.getInstance(this).isDeviceConnected()) {
                ExperimentRunner.getInstance(this).stop(settingsConfig);
            }
        } else if (e.getSource() == pullDataButton){
            if (ExperimentRunner.getInstance(this).isDeviceConnected()) {
                ExperimentRunner.getInstance(this).pullData(settingsConfig);
            }
        }else if (e.getSource() == viewResultsButton){
            System.out.println("View");

            if (!settingsConfig.getPcResultsPath().isEmpty()) {
                CleanupRunner.getInstance(this).reset();//resets the analysis runner, clearing all previously loaded data
                CleanupRunner.getInstance(this).loadRawData(settingsConfig.getPcResultsPath(), rawFileList);// loads the files from the results folder on analysis screen
                tabbedPane.setSelectedIndex(1);
                if (!ExperimentRunner.apiVersion.isEmpty()) {
                    apiVersion.setText(ExperimentRunner.apiVersion);//sets api version of the device in analysis UI
                    packageNameTF.setText(settingsConfig.getPackageName());//sets package name of device in analysis UI
                    prepResultFolder.setText(settingsConfig.getPcResultsPath());//sets analysis results folder same as results folder (upon analysis new folders will be created inside this folder)
                    expTime.setText(String.valueOf(settingsConfig.getStopTime()));
                }
            } else {
                printMessage("Run experiment to view results");
            }
        }else if (e.getSource() == hardwareBasedRadioButton){
            System.out.println("Hardware");
            //set defaults
            currentVoltageDataCheckBox.setSelected(hardwareBasedRadioButton.isSelected());
            currentVoltageDataCheckBox.setEnabled(!hardwareBasedRadioButton.isSelected());
            batteryStatisticsCheckBox.setSelected(false);
            batteryStatisticsCheckBox.setEnabled(false);
            if (traceFilesCheckBox.isSelected()){
                warningLabel.setText("Warning: Collection of trace files consumes large memory leading to energy spikes. Its collection with along with current voltage data can skew the results of energy readings");
            }
            else{
                warningLabel.setText("");
            }

        }else if (e.getSource() == softwareBasedRadioButton){
            System.out.println("Software");
            //set defaults
            currentVoltageDataCheckBox.setSelected(!softwareBasedRadioButton.isSelected());
            currentVoltageDataCheckBox.setEnabled(!softwareBasedRadioButton.isSelected());
            batteryStatisticsCheckBox.setEnabled(true);
            if (traceFilesCheckBox.isSelected()){
                warningLabel.setText("Warning: Collection of trace files consumes large memory");
            }
            else{
                warningLabel.setText("");
            }

        }else if (e.getSource() == baselineRadioButton){
            System.out.println("Baseline");
            //set defaults
            traceFilesCheckBox.setSelected(false);
            traceFilesCheckBox.setEnabled(false);
            warningLabel.setText("");
            testApkFileExpBtn.removeMouseListener(this);
            apkFileExpBtn.removeMouseListener(this);
            testClass.setEnabled(false);
            testRunner.setEnabled(false);
            reInstallAppRadioButton.setEnabled(!baselineRadioButton.isSelected());
            clearDataRadioButton.setEnabled(!baselineRadioButton.isSelected());
        }else if (e.getSource() == applicationExecutionRadioButton){
            System.out.println("App Execution");
            //set defaults
            traceFilesCheckBox.setEnabled(true);
            testApkFileExpBtn.addMouseListener(this);
            apkFileExpBtn.addMouseListener(this);
            testClass.setEnabled(true);
            testRunner.setEnabled(true);
            reInstallAppRadioButton.setEnabled(applicationExecutionRadioButton.isSelected());
            clearDataRadioButton.setEnabled(applicationExecutionRadioButton.isSelected());
        }else if (e.getSource() == reInstallAppRadioButton){
            System.out.println("Re install");
        }else if (e.getSource() == clearDataRadioButton){
            System.out.println("Clean app");
        }
        else if(e.getSource() == resetBtn){
            System.out.println("Resetting fields");
            settingsConfig = new SettingsConfig();
            //stop timers
            if (startTimeTimer != null && !stopTimeTimer.isRunning()) {
                startTimeTimer.stop();
            }
            if (stopTimeTimer != null && !stopTimeTimer.isRunning()) {
                stopTimeTimer.stop();
            }
            ExperimentRunner.getInstance(this).reset(this);
            setUpDefaults();
        }
        else if (e.getSource() == traceFilesCheckBox){
            if (traceFilesCheckBox.isSelected()){
                if (currentVoltageDataCheckBox.isSelected()){
                    warningLabel.setText("Warning: Collection of trace files consumes large memory leading to energy spikes. Its collection with along with current voltage data can skew the results of energy readings");
                } else {
                    warningLabel.setText("Warning: Collection of trace files consumes large memory");
                }
            }
            else{
                warningLabel.setText("");
            }
        } else if (e.getSource() == cleanDataButton){
            if (apiVersion.getText().isEmpty()){
                analysisTerminal.append("Enter API version to proceed \n");
            } else {
                long stopTime = 0;
                if (!StringUtils.isNumeric(expTime.getText()) || expTime.getText().isEmpty()){
                    printCleanupMessage("Enter valid total time");
                } else{
                    stopTime = Long.parseLong(expTime.getText());
                }
                //runs the command to clean data and passed all informaiton in the UI
                CleanupRunner.getInstance(this).cleanData(cleanFileList,
                        packageNameTF.getText(),
                        Integer.parseInt(apiVersion.getText()),
                        startTag.getText(),
                        endTag.getText(),
                        stopTime,
                        analysisVersion.getText(),
                        prepResultFolder.getText());
            }
        } else if (e.getSource() == preprocessResetBtn){
            //resets all fields of preprocess tab
            CleanupRunner.getInstance(this).reset();
            packageNameTF.setText("");
            analysisVersion.setText("");
            apiVersion.setText("");
            startTag.setText("");
            endTag.setText("");
            prepResultFolder.setText("");
            analysisTerminal.setText("");
            expTime.setText("");

        } else if (e.getSource() == genGraphBtn){
            //reads data from UI into the VisConfig object
            VisConfig visConfig = new VisConfig();
            visConfig.dataFilePath = visFilePath.getText();
            if (!visConfig.dataFilePath.isEmpty()) {
                visConfig.xAxisLabel = xAxisLabel.getText();
                visConfig.yAxisLabel = yAxisLabel.getText();
                visConfig.xAxisProperty = (String) xAxisCB.getSelectedItem();
                visConfig.yAxisProperty = (String) yAxisCB.getSelectedItem();
                visConfig.graphType = (GRAPH_TYPE) graphTypeCB.getSelectedItem();
                visConfig.facet1 = String.valueOf(groupByCB1.getSelectedItem());
                visConfig.facet2 = String.valueOf(groupByCB2.getSelectedItem());
                //converts colors to hex codes
                visConfig.colors = MyFilesUtils.convertAwtColorToHexColorCode(MyFilesUtils.asList((DefaultListModel) colorList.getModel()));
                visConfig.labelOrder = MyFilesUtils.asList((DefaultListModel) orderList.getModel());
                visConfig.resultFilePath = graphResultPath.getText();
                visConfig.caption = graphCaption.getText();
                visConfig.height = graphHeight.getText();
                visConfig.width = graphWidth.getText();
                visConfig.unit = "\""+(String)unitCB.getSelectedItem()+ "\"";
                visConfig.filter1Values = facet1Vals.getText();
                visConfig.filter2Values = facet2Vals.getText();
                visConfig.showLegend = showLegendCB.isSelected();
                //Validates informaiton and creates graph
                SwingUtilities.invokeLater(() -> VisualizationRunner.getInstance(MyToolWindow.this).generateGraph(visConfig));
            }else {
                printVisMessage("Choose data file");
            }
        } else if (e.getSource() == resetVisBtn){
            //Clear all fields and listener in visualization tab.
            // Listeners are cleared so that double listeners are not registered later on
            orderList.addListSelectionListener(null);
            colorList.addListSelectionListener(null);
            orderCB.addItemListener(null);
            xAxisCB.addItemListener(null);
            VisualizationRunner.getInstance(this).reset();
            for(Component control : visualizationOptions.getComponents()) {
                if(control instanceof JTextField) {
                    JTextField ctrl = (JTextField) control;
                    ctrl.setText("");
                } else if(control instanceof JComboBox) {
                    JComboBox ctrl = (JComboBox) control;
                    ctrl.setModel(new DefaultComboBoxModel());
                } else if(control instanceof JList) {
                    JList ctrl = (JList) control;
                    ctrl.setModel(new DefaultListModel<>());
                }
            }
            colorModel.set(new DefaultListModel<>());
            colorList.setModel(colorModel.get());
            orderModel.set(new DefaultListModel<>());
            orderList.setModel(orderModel.get());
            visterminal.setText("");
            filter1CB.setModel(new DefaultComboBoxModel<>());
            filter2CB.setModel(new DefaultComboBoxModel<>());
            filter1List.clear();
            filter2List.clear();
            facet1Vals.setText("");
            facet2Vals.setText("");
        } else if (e.getSource() == anaResetbtn){
            //reset data in analysis tab
            anaResultPath.setText("");
            anaDataFilePath.setText("");
            analysisTypeCb.setSelectedIndex(0);
            depPropCB.setModel(new DefaultComboBoxModel<>());
            indPropCB.setModel(new DefaultComboBoxModel<>());
            filter1PropCB.setModel(new DefaultComboBoxModel<>());
            groupValCB.setModel(new DefaultComboBoxModel<>());
            filter2PropCB.setModel(new DefaultComboBoxModel<>());
            group2ValCB.setModel(new DefaultComboBoxModel<>());
            anaTerminal.setText("");
            depVarList.clear();
            indVarList.clear();
            depTA.setText("");
            indTA.setText("");
        } else if (e.getSource() == anaReportBtn){
            AnalysisArgs args = new AnalysisArgs();
            args.dataFilePath = anaDataFilePath.getText();
            args.depVar = "depVars=\"c("+ depTA.getText() + ")\"";//creates a vector representation as it needs to be sent to R script
            args.indVar = "indVars=\"c("+ indTA.getText() + ")\"";//creates a vector representation as it needs to be sent to R script
            args.groupByVar = (String) filter1PropCB.getSelectedItem();
            args.groupByVal = (String) groupValCB.getSelectedItem();
            args.group2ByVar = (String) filter2PropCB.getSelectedItem();
            args.group2ByVal = (String) group2ValCB.getSelectedItem();
            args.resultPath = anaResultPath.getText();
            //Runs the analysis which valides data and then runs the script to generate report
            SwingUtilities.invokeLater(() -> {
                AnalysisRunner.getInstance(this).runAnalysis((ANALYSIS_TYPE) analysisTypeCb.getSelectedItem(), args);
            });

        }
    }

    @Override
    public void printVisMessage(String message) {
        visterminal.append(MyFilesUtils.getCurrentLocalDateTimeStamp() + "  " + message);
        visterminal.append("\n");
        visterminal.setCaretPosition(terminal.getDocument().getLength());
    }

    //Creates a Settings Config object and return ConfigStatus object that gives information about success/failure of valid object creation
    private ConfigStatus createSettingsConfig() {
        //populate data list with selected data types in a list
        ArrayList<DataType> dataList = new ArrayList<>();
        for (JCheckBox checkBox : checkBoxes){
            if (checkBox.isSelected()){
                switch (checkBox.getActionCommand()){
                    case "1":
                        dataList.add(DataType.ADB_LOGS);
                        break;
                    case "2":
                        dataList.add(DataType.CURRENT_VOLTAGE);
                        break;
                    case "3":
                        dataList.add(DataType.CPU);
                        break;
                    case "4":
                        dataList.add(DataType.MEMORY);
                        break;
                    case "5":
                        dataList.add(DataType.BATTERY);
                        break;
                    case "6":
                        dataList.add(DataType.NETWORK);
                        break;
                    case "7":
                        dataList.add(DataType.TRACE);
                        break;
                }
            }
        }
        //Validate field data
        if (noOfRuns.getText().isEmpty()){
            return new ConfigStatus(false, "Enter value for no of runs");
        }
        if (!StringUtils.isNumeric(noOfRuns.getText())){
            return new ConfigStatus(false, "You can only enter no of runs as an integer");
        }
        if (!StringUtils.isNumeric(stopTime.getText())){
            return new ConfigStatus(false, "You can only enter stop time as an integer");
        }
        if (!StringUtils.isNumeric(stopTime.getText())){
            return new ConfigStatus(false, "You can only enter delay as an integer");
        }
        if (dataList.isEmpty()){
            return new ConfigStatus(false, "Choose data type to be selected");
        }
        if (Integer.parseInt(noOfRuns.getText()) <= 0){
            return new ConfigStatus(false, "Number of runs should be greater than 0");
        }
        if (resultsPath.getText().isEmpty()){
            return new ConfigStatus(false, "Choose path where results will be saved");
        }
        if (stopTime.getText().isEmpty()){
            stopTime.setToolTipText("Enter value");
            return new ConfigStatus(false, "Enter value for stop time");
        }
        if (Long.parseLong(stopTime.getText()) <=0){
            return new ConfigStatus(false, "Choose total time greater than zero");
        }
        if (Long.parseLong(delayTime.getText()) <0){
            return new ConfigStatus(false, "Choose non-negative delay time");
        }
        //Make the checks on APKs and Test class only if application type is given.
        if (applicationExecutionRadioButton.isSelected() ){
            if (apkPath.getText().isEmpty()){
                return new ConfigStatus(false, "Choose APK Path");
            }
            if (testApkPath.getText().isEmpty()){
                printMessage("Note: No test apk provided. Application needs to be operated manually");
            }
            //Show warning but let the user push scripts.
            else{
                if (testClass.getText().isEmpty()){
                    return new ConfigStatus(false, "Test class is missing");
                }
                if (testRunner.getText().isEmpty()) {
                    return new ConfigStatus(false, "Test runner is missing");
                }
            }
        }
        //populate data in settingsConfig settings.model class
        settingsConfig.setExperimentType(experimentTypeButtonGroup.getSelection().getActionCommand().equals("1")?ExperimentType.HARDWARE : ExperimentType.SOFTWARE);
        settingsConfig.setExperimentStage(experimentStageButtonGroup.getSelection().getActionCommand().equals("1")? ExperimentStage.BASELINE: ExperimentStage.APP_EXECUTION);
        settingsConfig.setDataTypes(dataList);
        settingsConfig.setNumberOfRuns(Integer.parseInt(noOfRuns.getText()));
        settingsConfig.setTestClass(testClass.getText());
        settingsConfig.setTestRunner(testRunner.getText());
        settingsConfig.setRunConfig(reRunConfigButtonGroup.getSelection().getActionCommand().equals("1")? RunConfig.REINSTALL: RunConfig.CLEAN_DATA);
        settingsConfig.setPcResultsPath(resultsPath.getText());
        settingsConfig.setStopTime(Long.parseLong(stopTime.getText()));
        settingsConfig.setDelayTime(Long.parseLong(delayTime.getText()));
        settingsConfig.setDataPath(dataPath.getText().trim());
        System.out.println(settingsConfig.toString());
        return new ConfigStatus(true, "Configuration successful");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
//        if(path.isEmpty()) {
//            path = getClass().getResource("/scripts/").getPath();
//            path = path.replace("file:/", "");
//            Constants.RES_DIR_PATH = path;
//            printMessage(path);
//        }
        if(e.getSource() == testApkFileExpBtn){
            //get file path from file chooser into the text fields
            int status = fileChooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null) {
                    printMessage("No file found");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                testApkPath.setText(file.getAbsolutePath());
                settingsConfig.setTestApkFileName(file.getName());
                settingsConfig.setTestApkPath(file.getAbsolutePath());
            }
        } else if(e.getSource() == apkFileExpBtn){
            //get file path from file chooser into the text fields
            int status = fileChooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file == null) {
                    printMessage("No file found");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                apkPath.setText(file.getAbsolutePath());
//                String dataPathString = Constants.DATA_LOCAL_TMP_DIR;//+ APKReader.getInstance().getPackageName(file.getAbsolutePath());
                settingsConfig.setApkFileName(file.getName());
                settingsConfig.setApkPath(file.getAbsolutePath());
//                settingsConfig.setDeviceTmpPath(dataPathString);

            }
            System.out.println("Select App APK");
        }else if(e.getSource() == resultsFileExpBtn){
            //get folder path from file chooser into the text fields
            System.out.println("Select results folder");
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    printMessage("No folder selected");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                resultsPath.setText(file.getAbsolutePath());
            }
        } else if (e.getSource() == addRawBtn){
            System.out.println("Select raw files");
            //get files from file chooser into a jList
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File[] files = chooser.getSelectedFiles();
                if (files == null) {
                    printMessage("No files selected");
                    return;
                }
                CleanupRunner.getInstance(this).loadRawData(files, rawFileList);
            }
        } else if (e.getSource() == prepResultFolder || e.getSource() == prepFileChooser){
            //get folder path from file chooser into the text fields
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    printMessage("No folder selected");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                prepResultFolder.setText(file.getAbsolutePath());
            }
        } else if (e.getSource() == dataFileChooserBtn){
            //get folder path from file chooser into the text fields
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
            chooser.setFileFilter(filter);
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    printMessage("No folder selected");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                visFilePath.setText(file.getAbsolutePath());
                orderCB.addItemListener(null);
                orderCB.setModel(new DefaultComboBoxModel<>());
                colorList.setModel(colorModel.get());
                orderList.setModel(orderModel.get());
                VisualizationRunner.getInstance(this).setDataFilePath(visFilePath.getText());
                printVisMessage("Loading data from data file ...");
                SwingUtilities.invokeLater(() -> {
                    loadVisComboBoxData();
                    printVisMessage("Data loaded.");
                });
                depVarList.clear();
                indVarList.clear();
                depTA.setText("");
                indTA.setText("");
            }
        }  else if (e.getSource() == graphFileChooserBtn){
            //get folder path from file chooser into the text fields
            // parent component of the dialog
            JFrame parentFrame = new JFrame();
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Choose folder and type filename save file");
            FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Files", "png");
            fileChooser.setFileFilter(filter);
            int userSelection = fileChooser.showSaveDialog(parentFrame);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (FilenameUtils.getBaseName(fileToSave.getName()).isEmpty()){
                    printVisMessage("Choose a file name");
                    return;
                }
                if (!FilenameUtils.getExtension(fileToSave.getName()).equalsIgnoreCase("png")){
                    printVisMessage("You can only save png files");
                    return;
                }
                if (FilenameUtils.getExtension(fileToSave.getName()).isEmpty()){
                    fileToSave = new File(fileToSave+ ".png");
                }
                System.out.println("Save as file: " + fileToSave.getAbsolutePath());
                graphResultPath.setText(fileToSave.getAbsolutePath());
            }
        } else if (e.getSource() == anaFileExpBtn){
            //only csv files can be chosen
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            chooser.setFileFilter(new FileNameExtensionFilter("CSV Files", "csv"));
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    printMessage("No files selected");
                    return;
                }
                anaDataFilePath.setText(file.getAbsolutePath());
                DefaultComboBoxModel<String> depColNameModel = new DefaultComboBoxModel<String>();
                DefaultComboBoxModel<String> indColNameModel = new DefaultComboBoxModel<String>();
                DefaultComboBoxModel<String> grpColNameModel = new DefaultComboBoxModel<String>();
                DefaultComboBoxModel<String> grpCol2NameModel = new DefaultComboBoxModel<String>();
                //add none value as first value
                grpColNameModel.addElement("none");
                grpCol2NameModel.addElement("none");
                //get of columns and set them to models of respective drop downs
                for (String colName: AnalysisRunner.getInstance(this).getColNames(file.getAbsolutePath())){
                    depColNameModel.addElement(colName);
                    indColNameModel.addElement(colName);
                    grpColNameModel.addElement(colName);
                    grpCol2NameModel.addElement(colName);

                }
                //set models to their drop downs
                if (depColNameModel != null) {
                    depPropCB.setModel(depColNameModel);
                }
                if (indColNameModel != null) {
                    indPropCB.setModel(indColNameModel);
                }
                if (grpColNameModel != null) {
                    filter1PropCB.setModel(grpColNameModel);
                }
                if (grpCol2NameModel != null) {
                    filter2PropCB.setModel(grpCol2NameModel);
                }
                //Add selection listener to drop downs, upon selection add item to its respective text area
                depPropCB.addItemListener(itemEvent -> {
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                        String selectedItem = (String)depPropCB.getModel().getSelectedItem();
                        if(!depVarList.contains(selectedItem)){
                            depVarList.add(selectedItem);
                            depTA.setText(MyFilesUtils.listToString(depVarList));
                        }
                    }
                });
                //Add selection listener to drop downs, upon selection add item to its respective text area
                indPropCB.addItemListener(itemEvent -> {
                    if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                        String selectedItem = (String)indPropCB.getModel().getSelectedItem();
                        if(!indVarList.contains(selectedItem)){
                            indVarList.add(selectedItem);
                            indTA.setText(MyFilesUtils.listToString(indVarList));
                        }
                    }
                });

            }
        } else if (e.getSource() == anaResExpBtn){
            //Choose result path for analysis, only directoreis can be chosen
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int status = chooser.showOpenDialog(null);
            if (status == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                if (file == null) {
                    printMessage("No folder selected");
                    return;
                }
                System.out.println("File path: "+ file.getAbsolutePath());
                anaResultPath.setText(file.getAbsolutePath());
            }
        }
    }

    private void loadVisComboBoxData() {
        DefaultComboBoxModel<String> unitCBModel = new DefaultComboBoxModel();
        unitCBModel.addElement("in");
        unitCBModel.addElement("px");
        unitCBModel.addElement("cm");
        unitCBModel.addElement("mm");
        unitCB.setModel(unitCBModel);

        colorList.addListSelectionListener(null);
        orderCB.addItemListener(null);
        DefaultComboBoxModel<GRAPH_TYPE> comboBoxModel = new DefaultComboBoxModel<>();
        graphTypeCB.setModel(comboBoxModel);
        for(GRAPH_TYPE graph_type: VisualizationRunner.getInstance(this).getGraphTypes()){
            comboBoxModel.addElement(graph_type);
        }
        String[] xylabels = VisualizationRunner.getInstance(this).getLabels();
        xAxisCB.setModel(new DefaultComboBoxModel(xylabels));
        yAxisCB.setModel(new DefaultComboBoxModel(xylabels));
        groupByCB1.setModel(new DefaultComboBoxModel(xylabels));
        groupByCB2.setModel(new DefaultComboBoxModel(xylabels));
        //label list
        orderModel = new AtomicReference<>(new DefaultListModel<>());
        colorModel = new AtomicReference<>(new DefaultListModel<>());
        orderList.setModel(orderModel.get());
        colorList.setCellRenderer(new ColorListRenderer());
        colorList.setModel(colorModel.get());


        xAxisCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED){
                if (!((String)xAxisCB.getSelectedItem()).equals("none")) {
                    System.out.println("Clearing");
                    orderModel.set(new DefaultListModel<>());
                    orderList.setModel(orderModel.get());
                    colorModel.set(new DefaultListModel<>());
                    colorList.setModel(colorModel.get());
                    String[] labelsToOrder = VisualizationRunner.getInstance(MyToolWindow.this).getLabelsToOrder(visFilePath.getText(), (String) xAxisCB.getSelectedItem());
                    DefaultComboBoxModel<String> orderCbModel1 = new DefaultComboBoxModel<>(labelsToOrder);
                    orderCB.setModel(orderCbModel1);
                } else {
                    orderCB.setModel(new DefaultComboBoxModel<>());
                }
            }
        });

        orderCB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED){
                if (orderCB.getSelectedIndex() != 0) {
                    if (!orderModel.get().contains((String) orderCB.getSelectedItem())) {
                        orderModel.get().addElement((String) orderCB.getSelectedItem());
                        colorModel.get().addElement(MyFilesUtils.getRandomColor());
                        colorList.setModel(colorModel.get());
                        orderList.setModel(orderModel.get());
                        if (orderCB.getModel() != null) {
//                            ((DefaultComboBoxModel)orderCB.getModel()).removeElement((String) orderCB.getSelectedItem());
                        }
                    }
                }
            }
        });
        colorList.addListSelectionListener(this);

        groupByCB1.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                String selectedFacet = (String) groupByCB1.getModel().getSelectedItem();
                if (selectedFacet.equals("none")){
                    filter1CB.setModel(new DefaultComboBoxModel<>());
                    filter1List.clear();
                } else {
                    String[] values = VisualizationRunner.getInstance(MyToolWindow.this).getUniqueValsFromCol(visFilePath.getText(), selectedFacet);
                    if (values != null) {
                        DefaultComboBoxModel<String> comboBoxModel12 = new DefaultComboBoxModel<>();
                        for (String value : values) {
                            comboBoxModel12.addElement(value);
                        }
                        filter1CB.setModel(comboBoxModel12);
                    }
                }
                facet1Vals.setText("");
                filter1List.clear();
            }
        });
        groupByCB2.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                String selectedFacet = (String)groupByCB2.getModel().getSelectedItem();
                if (selectedFacet.equals("none")){
                    filter2CB.setModel(new DefaultComboBoxModel<>());
                    filter2List.clear();
                } else {
                    String[] values = VisualizationRunner.getInstance(MyToolWindow.this).getUniqueValsFromCol(visFilePath.getText(), selectedFacet);
                    if (values != null) {
                        DefaultComboBoxModel<String> comboBoxModel1 = new DefaultComboBoxModel<>();
                        for (String value : values) {
                            comboBoxModel1.addElement(value);
                        }
                        filter2CB.setModel(comboBoxModel1);
                    }
                }
                facet2Vals.setText("");
                filter2List.clear();
            }
        });
        filter1CB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                String selectedValue = (String)filter1CB.getSelectedItem();
                System.out.println("selected filter 1");
                if (selectedValue != null && !selectedValue.equals("none") && !filter1List.contains(selectedValue)) {
                    filter1List.add(selectedValue);
                    facet1Vals.setText(MyFilesUtils.listToString(filter1List));
                    System.out.println("setting value of filter 1 "+ MyFilesUtils.listToString(filter1List));
                    System.out.println("setting value of filter 1 "+ facet1Vals.getText());
                }
                if(selectedValue.equals("none")){
                    facet1Vals.setText("");
                    filter1List.clear();
                }
            }
        });
        filter2CB.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                String selectedValue = (String)filter2CB.getSelectedItem();
                if (selectedValue != null && !selectedValue.equals("none") && !filter2List.contains(selectedValue)) {
                    filter2List.add(selectedValue);
                    facet2Vals.setText(MyFilesUtils.listToString(filter2List));
                }
                if(selectedValue.equals("none")){
                    facet2Vals.setText("");
                    filter2List.clear();
                }
            }
        });
    }

    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    @Override
    public void printError(Exception exception) {
        //prints exception message in the plugin terminal for Experiment Runner on Settings screen

        terminal.append(MyFilesUtils.getCurrentLocalDateTimeStamp() + "  " + exception.getMessage());
        terminal.append("\n");
        terminal.setCaretPosition(terminal.getDocument().getLength());
    }

    @Override
    public void printMessage(String message) {
        //prints any message in the plugin terminal  for Experiment Runner on Settings screen
        terminal.append(MyFilesUtils.getCurrentLocalDateTimeStamp() + "  " + message);
        terminal.append("\n");
        terminal.setCaretPosition(terminal.getDocument().getLength());
    }

    public void showDialog(String title, String message) {
        Messages.showInfoMessage(message, title);
    }

    @Override
    public void updateExperimentStatus(int readingNumber, long stopTime, long delay) {
        printMessage("Reading # "+ readingNumber + "\n" + "Stop time: "+ stopTime + "\n" + "Delay: "+ delay);
    }

    @Override
    public void startStartDelayTimer(long delay) {
        if (startTimeTimer != null ) {
            if (startTimeTimer.isRunning()) {
                startTimeTimer.stop();
            }
        }
        final int[] currdelay = {(int) (delay)};
        startTimeTimer = new Timer(1000, e -> {
            currdelay[0] = currdelay[0] - 1;
            if (currdelay[0] == 0){
                startTimeTimer.stop();
//                printMessage("Collecting data: " + ExperimentRunner.getInstance(this).getDataTypes(settingsConfig));
//                printMessage("Experiment started.");
                startStopTimeTimer(settingsConfig.getStopTime());//once delay timer finishes, start stop time timer
            }
        });
        startTimeTimer.start();

    }

    @Override
    public void startStopTimeTimer(long delay) {
        if (stopTimeTimer != null ) {
            if (stopTimeTimer.isRunning()) {
                stopTimeTimer.stop();
            }
        }
        final int[] currdelay = {(int) ((delay))};
        stopTimeTimer = new Timer(1000, e -> {
            currdelay[0] = currdelay[0] - 1;
            if (currdelay[0] == 0){
                stopTimeTimer.stop();
                printMessage("Experiment ended. Click on 'Pull Data' button to pull data files to results folder");
            }
        });
        stopTimeTimer.start();
    }

    @Override
    public void stopTimers() {
        if (stopTimeTimer != null ) {
            if (stopTimeTimer.isRunning()) {
                stopTimeTimer.stop();
            }
        }
        if (startTimeTimer != null ) {
            if (startTimeTimer.isRunning()) {
                startTimeTimer.stop();
            }
        }
    }

    @Override
    public void printCleanupMessage(String message) {
        //print any message from AnalysisRunner in the analysis UI
        analysisTerminal.append(message);
        analysisTerminal.append("\n");
        analysisTerminal.setCaretPosition(analysisTerminal.getDocument().getLength());
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        if (!listSelectionEvent.getValueIsAdjusting() && orderCB.getModel().getSize() != 0) {
            System.out.println("Setting color of "+ colorList.getSelectedIndex());
            if (colorList.getSelectedIndex() >=0) {
                Color color = JColorChooser.showDialog(null, "Choose color for graph", Color.BLACK);
                if (color != null) {
                    for (int i = 0; i < colorModel.get().size(); i++) {
                        if (i == colorList.getSelectedIndex()) {
                            colorModel.get().setElementAt(color, i);
                            colorList.clearSelection();
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void printAnaMessage(String message) {
        anaTerminal.append(message);
        anaTerminal.append("\n");
        anaTerminal.setCaretPosition(terminal.getDocument().getLength());
    }
}
