package settings;

/**
 * @author Iffat Fatima
 * @created on 26/10/2020
 */
public interface ExperimentEventListener {
    void printError(Exception exception);

    void printMessage(String message);

    void showDialog(String title, String message);

    void updateExperimentStatus (int readingNumber, long stopTime, long delay);

    void startStartDelayTimer(long delay);

    void startStopTimeTimer(long delay);

    void stopTimers();
}
