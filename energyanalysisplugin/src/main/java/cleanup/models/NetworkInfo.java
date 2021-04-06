package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class NetworkInfo {

    @CsvBindByName(column = "package_name", required = true)
    public String packageName;
    @CsvBindByName(column = "device_api", required = true)
    public int deviceApiVersion;
    @CsvBindByName(column = "pid", required = true)
    public String pid;
    @CsvBindByName(column = "uid", required = true)
    public String uid;
    @CsvBindByName(column = "version_no", required = true)
    public String versionNo;
    @CsvBindByName(column = "reading_no", required = true)
    public String readingNo;
    @CsvBindByName(column = "connection_type", required = false)
    public String type;
    @CsvBindByName(column = "connection_subtype", required = false)
    public String subtype;
    @CsvBindByName(column = "network_id", required = false)
    public String networkId;
    @CsvBindByName(column = "is_metered", required = false)
    public boolean metered;
    @CsvBindByName(column = "default_network", required = false)
    public String defaultNetwork;
    @CsvBindByName(column = "received_bytes(B)", required = false)
    public long receivedBytes;
    @CsvBindByName(column = "received_packets(count)", required = false)
    public long receivedPackets;
    @CsvBindByName(column = "sent_bytes(B)", required = false)
    public long sentBytes;
    @CsvBindByName(column = "sent_packets(count)", required = false)
    public long sentPackets;
    @CsvBindByName(column = "usage_type", required = false)
    public String set;

    @Override
    public String toString() {
        return "NetworkInfo{" +
                "packageName='" + packageName + '\'' + "\n" +
                ", deviceApiVersion=" + deviceApiVersion +
                ", pid='" + pid + '\'' + "\n" +
                ", uid='" + uid + '\'' + "\n" +
                ", versionNo='" + versionNo + '\'' + "\n" +
                ", readingNo='" + readingNo + '\'' + "\n" +
                ", type='" + type + '\'' + "\n" +
                ", subtype='" + subtype + '\'' + "\n" +
                ", networkId='" + networkId + '\'' + "\n" +
                ", metered=" + metered +
                ", defaultNetwork='" + defaultNetwork + '\'' + "\n" +
                ", receivedBytes=" + receivedBytes +
                ", receivedPackets=" + receivedPackets +
                ", sentBytes=" + sentBytes +
                ", sentPackets=" + sentPackets +
                ", set='" + set + '\'' + "\n" +
                '}';
    }
}
