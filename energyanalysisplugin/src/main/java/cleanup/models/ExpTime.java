package cleanup.models;

public class ExpTime {
    public long startTime;
    public long endTime;

    public ExpTime(long startTime, long endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "ExpTime{" +
                "startTime='" + startTime + '\'' + "\n" +
                ", endTime='" + endTime + '\'' + "\n" +
                '}';
    }
}
