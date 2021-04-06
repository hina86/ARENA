package analysis ;

public class AnalysisArgs {
    public String dataFilePath = "";
    public String depVar = "";
    public String indVar = "";
    public String groupByVar = "";
    public String groupByVal = "";
    public String group2ByVar = "";
    public String group2ByVal = "";
    public String resultPath = "";

    public String[] getArgs() {
        return new String[]{ dataFilePath ,
                depVar ,
                indVar ,
                groupByVar ,
                groupByVal ,
                resultPath,
                group2ByVar ,
                group2ByVal};
    }
}
