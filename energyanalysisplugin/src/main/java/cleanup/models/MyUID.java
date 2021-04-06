package cleanup.models;

public class MyUID {
    public String UID = "";
    public String readingNo = "";
    public String versionNo = "";

    public MyUID(String UID, String readingNo, String versionNo) {
        this.UID = UID;
        this.readingNo = readingNo;
        this.versionNo = versionNo;
    }
    public MyUID(){}
}
