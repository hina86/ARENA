package visualization.models;

public class GraphConfig {

    public String dataFilePath;
    public String graphType;
    public String groupByProperty = "";
    public String xAxisProperty;
    public String yAxisProperty;
    public String xAxisLabel;
    public String yAxisLabel;
    public String labelOrder = "";
    public String colors = "";
    public String resultFilePath;
    public String caption = "";
    public String height = "8";
    public String width = "8";
    public String unit = "in";
    public String facet1;
    public String facet2;
    public String filter1Vals;
    public String filter2Vals;
    public String showLegend;

    public static GraphConfig VisConfigtoGraphConfig(VisConfig visConfig){
        GraphConfig graphConfig = new GraphConfig();
        graphConfig.dataFilePath = "\""+ visConfig.dataFilePath + "\"";
        graphConfig.graphType = "\""+ visConfig.graphType.name() + "\"";
        graphConfig.xAxisProperty = "\""+ visConfig.xAxisProperty + "\"";
        graphConfig.yAxisProperty = "\""+ visConfig.yAxisProperty + "\"";
        graphConfig.xAxisLabel = "\""+ visConfig.xAxisLabel + "\"";
        graphConfig.yAxisLabel = "\"" + visConfig.yAxisLabel + "\"";
        graphConfig.resultFilePath = "\"" + visConfig.resultFilePath + "\"";
        graphConfig.caption = "\"" + visConfig.caption + "\"";
        if(visConfig.width.isEmpty()){
            graphConfig.width = "12";
        } else {
            graphConfig.width = visConfig.width;
        }
        if (visConfig.height.isEmpty()){
            graphConfig.height = "8";
        } else {
            graphConfig.height = visConfig.height;
        }
        graphConfig.unit =  visConfig.unit;
        graphConfig.showLegend =  visConfig.showLegend? "TRUE": "FALSE";
        for (Object name: visConfig.labelOrder){
            if (!name.equals("none")) {
                graphConfig.labelOrder += "'" + (String) name + "',";
            }
        }
        if (!graphConfig.labelOrder.isEmpty()) {
            graphConfig.labelOrder = graphConfig.labelOrder.substring(0, graphConfig.labelOrder.length() - 1);
        }
        graphConfig.labelOrder = "orderArr=\"c("+graphConfig.labelOrder + ")\"";
        for (String name: visConfig.colors){
            if (!name.equals("none")) {
                graphConfig.colors += "'" + name + "',";
            }
        }
        if (!graphConfig.colors.isEmpty()) {
            graphConfig.colors = graphConfig.colors.substring(0, graphConfig.colors.length() - 1);
        }
        graphConfig.colors = "colorArr=\"c("+graphConfig.colors + ")\"";

        String grpString = "";
        if (!visConfig.facet1.isEmpty()){
            if (!visConfig.facet1.equals("none")) {
                grpString += "'" + visConfig.facet1 + "'";
            }
            if (!visConfig.facet2.isEmpty()) {
                if (!visConfig.facet2.equals("none")) {
                    grpString += ",'" + visConfig.facet2 + "'";
                }
            }
        } else if (!visConfig.facet2.isEmpty()) {
            if (!visConfig.facet2.equals("none")) {
                grpString += "'" + visConfig.facet2 + "'";
            }
        }
        graphConfig.groupByProperty = "facets=\"c("+ grpString + ")\"";
        graphConfig.filter1Vals =  "filters1=\"c("+ visConfig.filter1Values + ")\"";
        graphConfig.filter2Vals =  "filters2=\"c("+ visConfig.filter2Values + ")\"";
        graphConfig.facet1 = "\""+ visConfig.facet1 + "\"";
        graphConfig.facet2 = "\""+ visConfig.facet2 + "\"";
        return graphConfig;
    }

    public String[] toArgs(GraphConfig graphConfig){
        return new String[]{graphConfig.xAxisProperty,
                graphConfig.yAxisProperty,
                graphConfig.groupByProperty,
                graphConfig.labelOrder,
                graphConfig.colors,
                graphConfig.xAxisLabel,
                graphConfig.yAxisLabel,
                graphConfig.dataFilePath,
                graphConfig.resultFilePath,
                graphConfig.caption,
                graphConfig.width,
                graphConfig.height,
                graphConfig.unit,
                graphConfig.showLegend,
                graphConfig.filter1Vals,
                graphConfig.filter2Vals,
                graphConfig.facet1,
                graphConfig.facet2
        };
    }
}
