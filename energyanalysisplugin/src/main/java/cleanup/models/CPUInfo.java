package cleanup.models;

import com.opencsv.bean.CsvBindByName;

import java.util.ArrayList;
import java.util.List;

public class CPUInfo {
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
    @CsvBindByName(column = "*avg_cpu_usage_percentage")
    public double cpuUsage = 0;
    @CsvBindByName(column = "*avg_vss(KB)")
    public double vss = 0;
    @CsvBindByName(column = "*avg_rss(KB)")
    public double rss = 0;
    @CsvBindByName(column = "*avg_memory_usage_percentage")
    public double memory = 0;
    @CsvBindByName(column = "cores")
    public int cores = 1;
    private ArrayList<Float> cpuUsageList = new ArrayList<>();
    private ArrayList<Float> VSSList = new ArrayList<>();
    private ArrayList<Float> RSSList = new ArrayList<>();
    private ArrayList<Float> memoryUsageList = new ArrayList<>();

    public ArrayList<Float> getCpuUsageList() {
        return cpuUsageList;
    }

    public void setCpuUsageList(ArrayList<Float> cpuUsageList) {
        this.cpuUsageList = cpuUsageList;
    }

    public ArrayList<Float> getVSSList() {
        return VSSList;
    }

    public void setVSSList(ArrayList<Float> VSSList) {
        this.VSSList = VSSList;
    }

    public ArrayList<Float> getRSSList() {
        return RSSList;
    }

    public void setRSSList(ArrayList<Float> RSSList) {
        this.RSSList = RSSList;
    }

    public ArrayList<Float> getMemoryUsageList() {
        return memoryUsageList;
    }

    public void setMemoryUsageList(ArrayList<Float> memoryUsageList) {
        this.memoryUsageList = memoryUsageList;
    }

    @Override
    public String toString() {
        return "CPUInfo{" +
                "packageName='" + packageName + '\'' + "\n" +
                ", deviceApiVersion=" + deviceApiVersion +
                ", pid='" + pid + '\'' + "\n" +
                ", uid='" + uid + '\'' + "\n" +
                ", versionNo='" + versionNo + '\'' + "\n" +
                ", readingNo='" + readingNo + '\'' + "\n" +
                ", cpuUsage=" + cpuUsage +
                ", vss=" + vss +
                ", rss=" + rss +
                ", memory=" + memory +
                ", cores=" + cores +
                ", cpuUsageList=" + cpuUsageList +
                ", VSSList=" + VSSList +
                ", RSSList=" + RSSList +
                ", memoryUsageList=" + memoryUsageList +
                '}';
    }

    //calculation methods
    public double computeVss() {
        return calculateAverage(VSSList);
    }
    public double computeRss() {
        return calculateAverage(RSSList);
    }
    public double computeMem() {
        return calculateAverage(memoryUsageList);
    }
    public double computeCpu() {
        return calculateAverage(cpuUsageList);
    }
    private double calculateAverage(List<Float> arr) {
        double sum = 0;
        if (!arr.isEmpty()) {
            for (float val : arr) {
                sum += val;
            }
            return sum / arr.size();
        }
        return sum;
    }
}
