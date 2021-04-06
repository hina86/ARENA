package cleanup.models;

public class PID {
    public String PID = "";
    public String readingNo = "";
    public String versionNo = "";

    public PID(String PID, String readingNo, String versionNo) {
        this.PID = PID;
        this.readingNo = readingNo;
        this.versionNo = versionNo;
    }

    public PID(){}
}
