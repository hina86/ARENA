package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class MemoryInfo {

    @CsvBindByName(column = "package_name")
    public String packageName;
    @CsvBindByName(column = "device_api")
    public int deviceApiVersion;
    @CsvBindByName(column = "pid")
    public String pid;
    @CsvBindByName(column = "uid")
    public String uid;
    @CsvBindByName(column = "version_no")
    public String versionNo;
    @CsvBindByName(column = "reading_no")
    public String readingNo;
    @CsvBindByName(column = "total_time_percentage")
    public float totalPercentage;
    @CsvBindByName(column = "top_time_percentage")
    public float top;
    @CsvBindByName(column = "imp_fg_time_percentage")
    public float impFg;
    @CsvBindByName(column = "reading_start_time(yyyy-mm-dd hh:mm:ss tt)")
    public String startTime;//format: yyyy-mm-dd hh:mm:ss tt
    @CsvBindByName(column = "elapsed_time_since_collection(ms)")
    public long elapsedTime;//ms
    @CsvBindByName(column = "memory_usage(B)")
    public long memoryUsage;//bytes

    @Override
    public String toString() {
        return "MemoryInfo{" +
                "packageName='" + packageName + '\'' + "\n" +
                ", deviceApiVersion=" + deviceApiVersion +
                ", pid='" + pid + '\'' + "\n" +
                ", uid='" + uid + '\'' + "\n" +
                ", versionNo='" + versionNo + '\'' + "\n" +
                ", readingNo='" + readingNo + '\'' + "\n" +
                ", totalPercentage=" + totalPercentage +
                ", top=" + top +
                ", ImpFg=" + impFg +
                ", startTime='" + startTime + '\'' + "\n" +
                ", elapsedTime=" + elapsedTime +
                ", memoryUsage=" + memoryUsage +
                '}';
    }
}
