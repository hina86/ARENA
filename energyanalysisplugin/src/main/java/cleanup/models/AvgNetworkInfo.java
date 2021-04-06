package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class AvgNetworkInfo {
    @CsvBindByName(column = "package_name")
    public String packageName;
    @CsvBindByName(column = "version_no")
    public String versionNo;
    @CsvBindByName(column = "avg_sent_packets(Count)")
    long avgSentPcks = 0;
    @CsvBindByName(column = "avg_received_packets(Count)")
    long avgRecvPcks = 0;
    @CsvBindByName(column = "avg_sent_bytes(Bytes)")
    long avgSentBytes = 0;
    @CsvBindByName(column = "avg_sent_bytes(Bytes)")
    long avgRecvBytes = 0;
    @CsvBindByName(column = "max_sent_packets(Count)")
    long maxSentPcks = 0;
    @CsvBindByName(column = "max_received_packets(Count)")
    long maxRecvPcks = 0;
    @CsvBindByName(column = "max_sent_bytes(Bytes)")
    long maxSentBytes = 0;
    @CsvBindByName(column = "max_sent_bytes(Bytes)")
    long maxRecvBytes = 0;
    @CsvBindByName(column = "min_sent_packets(Count)")
    long minSentPcks = 0;
    @CsvBindByName(column = "min_received_packets(Count)")
    long minRecvPcks = 0;
    @CsvBindByName(column = "min_sent_bytes(Bytes)")
    long minSentBytes = 0;
    @CsvBindByName(column = "min_sent_bytes(Bytes)")
    long minRecvBytes = 0;
    @CsvBindByName(column = "standard_deviation_sent_packets(Count)")
    double sdSentPcks = 0;
    @CsvBindByName(column = "standard_deviation_received_packets(Count)")
    double sdRecvPcks = 0;
    @CsvBindByName(column = "standard_deviation_sent_bytes")
    double sdSentBytes = 0;
    @CsvBindByName(column = "standard_deviation_sent_bytes")
    double sdRecvBytes = 0;

    public AvgNetworkInfo(String packageName, String versionNo, long avgSentPcks, long avgRecvPcks, long avgSentBytes, long avgRecvBytes, long maxSentPcks, long maxRecvPcks, long maxSentBytes, long maxRecvBytes, long minSentPcks, long minRecvPcks, long minSentBytes, long minRecvBytes, double sdSentPcks, double sdRecvPcks, double sdSentBytes, double sdRecvBytes) {
        this.packageName = packageName;
        this.versionNo = versionNo;
        this.avgSentPcks = avgSentPcks;
        this.avgRecvPcks = avgRecvPcks;
        this.avgSentBytes = avgSentBytes;
        this.avgRecvBytes = avgRecvBytes;
        this.maxSentPcks = maxSentPcks;
        this.maxRecvPcks = maxRecvPcks;
        this.maxSentBytes = maxSentBytes;
        this.maxRecvBytes = maxRecvBytes;
        this.minSentPcks = minSentPcks;
        this.minRecvPcks = minRecvPcks;
        this.minSentBytes = minSentBytes;
        this.minRecvBytes = minRecvBytes;
        this.sdSentPcks = sdSentPcks;
        this.sdRecvPcks = sdRecvPcks;
        this.sdSentBytes = sdSentBytes;
        this.sdRecvBytes = sdRecvBytes;
    }
}
