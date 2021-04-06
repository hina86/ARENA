package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class ExperimentData {
    @CsvBindByName(column = "package_name")
    public String packageName = "";
    @CsvBindByName(column = "version_no")
    public String versionNo = "";
    @CsvBindByName(column = "reading_no")
    public int readingNo = 0;
    @CsvBindByName(column = "time")
    public float time = 0;//in seconds
    @CsvBindByName(column = "energy")
    public double energy = 0;
    @CsvBindByName(column = "power")
    public double power = 0;
    @CsvBindByName(column = "cpu")
    public double cpu = 0;
    @CsvBindByName(column = "memory")
    public double memory = 0;
    @CsvBindByName(column = "RSS")
    public double rss = 0;
    @CsvBindByName(column = "VSS")
    public double vss = 0;
    @CsvBindByName(column = "sent_packets(Count)")
    public long sentPcks = 0;
    @CsvBindByName(column = "received_packets(Count)")
    public long recvPcks = 0;
    @CsvBindByName(column = "sent_bytes(Bytes)")
    public long sentBytes = 0;
    @CsvBindByName(column = "sent_bytes(Bytes)")
    public long recvBytes = 0;
}
