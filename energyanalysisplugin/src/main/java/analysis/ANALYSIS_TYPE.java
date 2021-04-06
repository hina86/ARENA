package analysis;

public enum ANALYSIS_TYPE {
    NONE("none"),
    SUMMARY("Summary"),
    KRUSKAL_WALLIS("Kruskal Wallis"),
    SPEARMAN("Spearman Correlation"),
    PEARSON("Pearson Correlation"),
    ANOVA("Anova Test"),
    SHAPIRO_WILK_TEST("Shapiro Test"),
    PAIRWISE_T_TEST("Pairwise t-test");

    private final String value;
    ANALYSIS_TYPE(String s) {
        this.value = s;
    }

}
