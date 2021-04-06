package settings.model;

/**
 * @author Iffat Fatima
 * @created on 29/10/2020
 */
public enum ExperimentStatus {
    IDLE,
    SCRIPT_PUSH_SUCCESS,
    SCRIPT_PUSH_ERROR,
    POWER_MONITOR_START_SUCCESS,
    POWER_MONITOR_ERROR,
    RERUN_SUCCESS,
    RERUN_ERROR,
    RESUME_SUCCESS,
    RESUME_ERROR,
    STOPPED,
    STOP_ERROR,
    PULL_DATA_SUCCESS,
    PULL_DATA_ERROR
}
