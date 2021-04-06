package settings.model;

/**
 * @author Iffat Fatima
 * @created on 26/10/2020
 */
public class ConfigStatus {
    boolean isValid;
    String message;
    public ConfigStatus(boolean isValid, String message) {
        this.isValid = isValid;
        this.message = message;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
