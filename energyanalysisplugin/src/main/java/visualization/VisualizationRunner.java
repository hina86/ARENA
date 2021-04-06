package visualization;

import org.apache.commons.lang.StringUtils;
import util.Constants;
import util.RScriptResult;
import util.RScriptRunner;
import visualization.models.GRAPH_TYPE;
import visualization.models.GraphConfig;
import visualization.models.VisConfig;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class VisualizationRunner {
    private static VisualizationRunner instance;
    private static VisualizationEventListener eventListener;
    private VisConfig visConfig = new VisConfig();
    private String[] colNames;

    public static VisualizationRunner getInstance(VisualizationEventListener el) {
        if (instance == null){
            instance = new VisualizationRunner();
            eventListener = el;
        }
        return instance;
    }

    private VisualizationRunner(){
    }

    public void generateGraph(VisConfig visConfig) {
        //validate here
        if (visConfig.dataFilePath.isEmpty()){
            eventListener.printVisMessage("Choose data file");
            return;
        }if (visConfig.xAxisProperty.isEmpty() || visConfig.xAxisProperty.equals("none")){
            eventListener.printVisMessage("Choose x axis propery");
            return;
        }if (visConfig.yAxisProperty.isEmpty() || visConfig.yAxisProperty.equals("none")){
            eventListener.printVisMessage("Choose y axis propery");
            return;
        }if (visConfig.xAxisLabel.isEmpty() || visConfig.xAxisLabel.equals("none")){
            eventListener.printVisMessage("Choose x axis label");
            return;
        }if (visConfig.yAxisLabel.isEmpty() || visConfig.yAxisLabel.equals("none")){
            eventListener.printVisMessage("Choose y axis label");
            return;
        }if (visConfig.graphType == null ){
            eventListener.printVisMessage("Choose graph type");
            return;
        } if (visConfig.resultFilePath.isEmpty()){
            eventListener.printVisMessage("Choose result path");
            return;
        } if (!visConfig.facet1.equals("none") && visConfig.filter1Values.isEmpty()){
            eventListener.printVisMessage("Choose filter 1 values");
            return;
        } if (!visConfig.facet2.equals("none") && visConfig.filter2Values.isEmpty()){
            eventListener.printVisMessage("Choose filter 2 values");
            return;
        } if(!visConfig.width.isEmpty() && !StringUtils.isNumeric(visConfig.width)){
            eventListener.printVisMessage("Width can only be a whole number");
            return;
        } if(!visConfig.height.isEmpty() && !StringUtils.isNumeric(visConfig.height)){
            eventListener.printVisMessage("Height can only be a whole number");
            return;
        } if(!visConfig.width.isEmpty() && !visConfig.width.matches("[0-9]*")){
            eventListener.printVisMessage("Width can only be a whole number");
            return;
        } if(!visConfig.height.isEmpty() && !visConfig.height.matches("[0-9]*")){
            eventListener.printVisMessage("Height can only be a whole number");
            return;
        }

        //creates plots based on graph type
        this.visConfig = visConfig;
        System.out.println(visConfig.toString());
        GraphConfig graphConfig = GraphConfig.VisConfigtoGraphConfig(visConfig);
        switch (visConfig.graphType){
            case BOXPLOT:
                createPlot(graphConfig, "boxplot.R");
                break;
            case DOTPLOT:
                createPlot(graphConfig, "dotplot.R");
                break;
            case SCATTERPLOT:
                createPlot(graphConfig, "scatter.R");
                break;
            case LINEPLOT:
                createPlot(graphConfig, "lineplot.R");
                break;
            case VIOLINPLOT:
                createPlot(graphConfig, "violinplot.R");
                break;
            case BARPLOT:
                createPlot(graphConfig, "barplot.R");
                break;
            case PIECHART:
                createPlot(graphConfig, "pieplot.R");
                break;
            case BUBBLEPLOT:
                createPlot(graphConfig, "bubbleplot.R");
                break;
        }
    }

    private void createPlot(GraphConfig graphConfig, String plotScriptFileName) {
        eventListener.printVisMessage("Creating graph ...");
        //bat file path
        String scriptPath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.PLOT_BAT;
        //creates a bat files that runs the bat script at script path, which contains the path of the R script to run and arguments
        RScriptRunner.createBatFile(scriptPath,
                Constants.RES_DIR_PATH + plotScriptFileName,
                graphConfig.toArgs(graphConfig)
        );
        RScriptResult scriptResult = RScriptRunner.executeBatFile(scriptPath);
        //Display result based on result of the script
        boolean isSuccessful = scriptResult.successful;
        if (isSuccessful) {
            eventListener.printVisMessage("Graph saved successfully in file: "+ graphConfig.resultFilePath);
        } else {
            eventListener.printVisMessage("Error creating graph.\n"+ scriptResult.error);
        }
    }

    public void setDataFilePath(String path) {
        visConfig.dataFilePath = path;
        colNames = getColNames(path);
    }

    /**
     * Gets column names from the csv file on @param path
     * @return string array of column names
     */
    private String [] getColNames(String path) {
        //path of bat script
        String scriptPath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.GET_COL_NAMES;
        //creates a bat files that runs the bat script at script path, which contains the path of the R script to run and arguments
        RScriptRunner.createBatFile(scriptPath,
                Constants.RES_DIR_PATH + "getCsvColumnNames.R",
                new String[]{path}
        );
        //parse result to return array of column names
        String result = RScriptRunner.executeBatFile(scriptPath).result;
        String columnNames[] = result.split("\\r?\\n");
        return columnNames;
    }

    /**
     *
     * @return string array of column names with none added as first name
     */
    public String [] getLabels() {
        String[] list = colNames;
        List<String> tmpList = new LinkedList<String>(Arrays.asList(list));
        tmpList.add(0, "none");
        list = tmpList.toArray(new String[tmpList.size()]);
        return list;
    }

    /**
     *
     * Get the values of column name @param selectedCol
     * from the csv file  at @param path
     * @return string array of values
     */
    public String [] getLabelsToOrder(String path, String selectedCol) {
        //path to bat file
        String scriptPath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.GET_COL_VALUES;
        selectedCol = "\""+selectedCol+"\"";
        //creates a bat files that runs the bat script at script path, which contains the path of the R script to run and arguments
        RScriptRunner.createBatFile(scriptPath,
                Constants.RES_DIR_PATH + "getColumnValues.R",
                new String[]{path, selectedCol}
        );
        //get result of r script and parse it to get array of values
        String result = RScriptRunner.executeBatFile(scriptPath).result;
        String list[] = result.split("\\r?\\n");
        List<String> tmpList = new LinkedList<String>(Arrays.asList(list));
        tmpList.add(0, "none");
        list = tmpList.toArray(new String[tmpList.size()]);
        return list;
    }

    //gets graph type enums values list
    public List<GRAPH_TYPE> getGraphTypes() {
        return Arrays.asList(GRAPH_TYPE.values());
    }

    public void reset() {
        visConfig = new VisConfig();
        colNames = null;
    }

    //gets unique values under a column from the csv file with @param path
    public String [] getUniqueValsFromCol(String path, String selectedCol) {
        if (path.isEmpty()){
            eventListener.printVisMessage("Select data file");
            return null;
        }
        if (selectedCol.isEmpty()){
            eventListener.printVisMessage("Select group by property");
            return null;
        }
        //path to bat file that runs r script
        String scriptPath = Constants.SCRIPT_DIR_PATH + File.separator + Constants.GET_COL_VALUES;
        selectedCol = "\""+selectedCol+"\"";
        //creates a bat files that runs the bat script at script path, which contains the path of the R script to run and arguments
        RScriptRunner.createBatFile(scriptPath,
                Constants.RES_DIR_PATH + "getColumnValues.R",
                new String[]{path, selectedCol}
        );
        //get result of r script and parse it to get array of values
        String result = RScriptRunner.executeBatFile(scriptPath).result;
        String list[] = result.split("\\r?\\n");
        List<String> tmpList = new LinkedList<String>(Arrays.asList(list));
        tmpList.add(0, "none");
        list = tmpList.toArray(new String[tmpList.size()]);
        return list;
    }
}
