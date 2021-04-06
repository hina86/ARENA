package analysis;

import util.Constants;
import util.RScriptResult;
import util.RScriptRunner;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AnalysisRunner {

    private static AnalysisRunner instance;
    private static AnalysisEventListener eventListener;

    public static AnalysisRunner getInstance(AnalysisEventListener el) {
        if (instance == null){
            instance = new AnalysisRunner();
            eventListener = el;
        }
        return instance;
    }

    private AnalysisRunner(){}

    /**
     * Gets column names from the csv file on @param path
     * @return string array of column names
     */
    public String [] getColNames(String path) {
        //bat file path
        String batFilePath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.GET_COL_NAMES;
        //creates a bat files that runs the bat script at script path, which contains the path of the R script to run and arguments
        RScriptRunner.createBatFile(batFilePath,
                Constants.RES_DIR_PATH + "getCsvColumnNames.R",
                new String[]{path}
        );
        String result = RScriptRunner.executeBatFile(batFilePath).result;
        String columnNames[] = result.split("\\r?\\n");
        return columnNames;
    }


    public URL getHelp(ANALYSIS_TYPE analysisType) {
        java.net.URL helpURL = getClass().getResource("/help/none.html");

        switch (analysisType){
            case SUMMARY:
                helpURL = getClass().getResource("/help/summary.html");
                break;
            case ANOVA:
                helpURL = getClass().getResource("/help/anova.html");
                break;
            case SPEARMAN:
                helpURL = getClass().getResource("/help/spearman.html");
                break;
            case KRUSKAL_WALLIS:
                helpURL = getClass().getResource("/help/kruskal_wallis.html");
                break;
            case PAIRWISE_T_TEST:
                helpURL = getClass().getResource("/help/pairwisettest.html");
                break;
            case PEARSON:
                helpURL = getClass().getResource("/help/pearson.html");
                break;
            case SHAPIRO_WILK_TEST:
                helpURL = getClass().getResource("/help/shapiro.html");
                break;
            case NONE:
                helpURL = getClass().getResource("/help/none.html");
                break;
        }
        return helpURL;
    }

    public static List<ANALYSIS_TYPE> getAnalysisTypes() {
        return Arrays.asList(ANALYSIS_TYPE.values());
    }


    public String [] getUniqueValsFromCol(String path, String selectedCol) {
        if (path.isEmpty()){
            eventListener.printAnaMessage("Select data file");
            return null;
        }
        if (selectedCol.isEmpty()){
            eventListener.printAnaMessage("Select group by property");
            return null;
        }
        String batFilePath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.GET_COL_VALUES;
        selectedCol = "\""+selectedCol+"\"";
        RScriptRunner.createBatFile(batFilePath,
                Constants.RES_DIR_PATH + "getColumnValues.R",
                new String[]{path, selectedCol}
        );
        String result = RScriptRunner.executeBatFile(batFilePath).result;
        String list[] = result.split("\\r?\\n");
        List<String> tmpList = new LinkedList<String>(Arrays.asList(list));
        tmpList.add(0, "none");
        list = tmpList.toArray(new String[tmpList.size()]);
        return list;
    }
    
    public void runAnalysis(ANALYSIS_TYPE analysisType, AnalysisArgs analysisArgs){
        String scriptName = "";
        String resultFileName = "";
        switch (analysisType){
            case SUMMARY:
               scriptName = "summary.R";
               resultFileName = "Summary Statistics.docx";
                break;
            case ANOVA:
                scriptName = "anova.R";
                resultFileName = "Anova.docx";
                break;
            case SPEARMAN:
                scriptName = "spearman.R";
                resultFileName = "Spearman Analysis.docx";
                break;
            case KRUSKAL_WALLIS:
                scriptName = "kruskal_wallis.R";
                resultFileName = "Kruskal Wallis.docx";
                break;
            case PAIRWISE_T_TEST:
                scriptName = "pairwise.R";
                resultFileName = "Pairwise.docx";
                break;
            case PEARSON:
                scriptName = "pearson.R";
                resultFileName = "Pearson.docx";
                break;
            case SHAPIRO_WILK_TEST:
                scriptName = "shapiro.R";
                resultFileName = "Shapiro.docx";
                break;
            case NONE:
                eventListener.printAnaMessage("Choose analysis type");
                return;
        }

        eventListener.printAnaMessage("Starting analysis ...");
        String batFilePath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.ANA_BAT;
        RScriptRunner.createBatFile(batFilePath,
                Constants.RES_DIR_PATH + scriptName,
                analysisArgs.getArgs()
        );
        RScriptResult scriptResult = RScriptRunner.executeBatFile(batFilePath);
        boolean isSuccessful = scriptResult.successful;
        if (isSuccessful) {
            eventListener.printAnaMessage("Analysis successful. Results are saved in file: "+ analysisArgs.resultPath + File.separator + resultFileName);
        } else {
            eventListener.printAnaMessage("Error performing analysis!\n"+ scriptResult.error);
        }
    }
}
