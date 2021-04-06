package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class AvgCPUMemoryInfo {
    @CsvBindByName(column = "package_name")
    public String packageName;
    @CsvBindByName(column = "version_no")
    public String analysisVersion;
    @CsvBindByName(column = "avg_vss(KB)")
    double avgVss;
    @CsvBindByName(column = "avg_rss(KB)")
    double avgRss;
    @CsvBindByName(column = "avg_cpu(%)")
    double avgCpuPercentage;
    @CsvBindByName(column = "avg_memory(%)")
    double avgMemoryPercentage;
    @CsvBindByName(column = "max_vss(KB)")
    double maxVss;
    @CsvBindByName(column = "max_rss(KB)")
    double maxRss;
    @CsvBindByName(column = "max_cpu(%)")
    double maxCpuPercentage;
    @CsvBindByName(column = "max_memory(%)")
    double maxMemoryPercentage;
    @CsvBindByName(column = "min_vss(KB)")
    double minVss;
    @CsvBindByName(column = "min_rss(KB)")
    double minRss;
    @CsvBindByName(column = "min_cpu(%))")
    double minCpuPercentage;
    @CsvBindByName(column = "min_memory(%)")
    double minMemoryPercentage;
    @CsvBindByName(column = "standard_deviation_vss")
    double sdVss;
    @CsvBindByName(column = "standard_deviation_rss")
    double sdRss;
    @CsvBindByName(column = "standard_deviation_cpu")
    double sdCpuPercentage;
    @CsvBindByName(column = "standard_deviation_memory")
    double sdMemoryPercentage;

    public AvgCPUMemoryInfo(String packageName, String analysisVersion, double avgVss, double avgRss, double avgCpuPercentage, double avgMemoryPercentage, double maxVss, double maxRss, double maxCpuPercentage, double maxMemoryPercentage, double minVss, double minRss, double minCpuPercentage, double minMemoryPercentage, double sdVss, double sdRss, double sdCpuPercentage, double sdMemoryPercentage) {
        this.packageName = packageName;
        this.analysisVersion = analysisVersion;
        this.avgVss = avgVss;
        this.avgRss = avgRss;
        this.avgCpuPercentage = avgCpuPercentage;
        this.avgMemoryPercentage = avgMemoryPercentage;
        this.maxVss = maxVss;
        this.maxRss = maxRss;
        this.maxCpuPercentage = maxCpuPercentage;
        this.maxMemoryPercentage = maxMemoryPercentage;
        this.minVss = minVss;
        this.minRss = minRss;
        this.minCpuPercentage = minCpuPercentage;
        this.minMemoryPercentage = minMemoryPercentage;
        this.sdVss = sdVss;
        this.sdRss = sdRss;
        this.sdCpuPercentage = sdCpuPercentage;
        this.sdMemoryPercentage = sdMemoryPercentage;
    }
}
