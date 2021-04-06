package visualization.models;


import java.util.List;

public class VisConfig {
    public String dataFilePath;
    public GRAPH_TYPE graphType;
    public String facet1 = "none";
    public String facet2 = "none";
    public String xAxisProperty;
    public String yAxisProperty;
    public String xAxisLabel;
    public String yAxisLabel;
    public List<Object> labelOrder;
    public List<String> colors;//of hex colors
    public String resultFilePath;
    public String caption = "";
    public String height = "8";
    public String width = "8";
    public String unit = "in";
    public String filter1Values;
    public String filter2Values;
    public boolean showLegend = true;
    @Override
    public String toString() {
        return "VisConfig{" +
                "dataFilePath='" + dataFilePath + '\'' + "\n" +
                ", graphType=" + graphType +
                ", groupByProperty='" + facet1 + '\'' + "\n" +
                ", xAxisProperty='" + xAxisProperty + '\'' + "\n" +
                ", yAxisProperty='" + yAxisProperty + '\'' + "\n" +
                ", xAxisLabel='" + xAxisLabel + '\'' + "\n" +
                ", yAxisLabel='" + yAxisLabel + '\'' + "\n" +
                ", labelOrder=" + labelOrder +
                ", colors=" + colors +
                ", resultFilePath='" + resultFilePath + '\'' + "\n" +
                '}';
    }
}
