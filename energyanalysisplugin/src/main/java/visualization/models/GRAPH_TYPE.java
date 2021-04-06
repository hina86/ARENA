package visualization.models;

public enum GRAPH_TYPE {
    BOXPLOT("Box Plot"),
    DOTPLOT("Dot Plot"),
    SCATTERPLOT("Scatter Plot"),
    LINEPLOT("Line Plot"),
    VIOLINPLOT("Violin Plot"),
    BARPLOT("Bar Plot"),
    PIECHART("Pie Chart"),
    BUBBLEPLOT("Bubble Plot");

    private final String value;
    GRAPH_TYPE(String s) {
        this.value = s;
    }


}
