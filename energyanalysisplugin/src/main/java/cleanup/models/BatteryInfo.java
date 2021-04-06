package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class BatteryInfo {
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
    @CsvBindByName(column = "system_start(count)")
    public String systemStarts;
    @CsvBindByName(column = "on_battery")
    public String currentlyOnBattery;
    @CsvBindByName(column = "time_on_battery(ms)")
    public long timeOnBattery;
    @CsvBindByName(column = "total_runtime(ms)")
    public long totalRunTime;
    @CsvBindByName(column = "total_screen_on_time(ms)")
    public long screenOn;
    @CsvBindByName(column = "mobile_total_received(B)")
    public String mobileTotalReceived;
    @CsvBindByName(column = "wifi_total_received(B)")
    public String wiFiTotalReceived;
    @CsvBindByName(column = "wifi_total_sent(B)")
    public String wiFiTotalSent;
    @CsvBindByName(column = "total_full_wakelock_time(ms)")
    public long totalFullWakelockTime;
    @CsvBindByName(column = "signal_scanning_time(ms)")
    public long signalScanningTime;
    @CsvBindByName(column = "wifi_on_time(ms)")
    public long wifiOn;
    @CsvBindByName(column = "wifi_running_time(ms)")
    public long wifiRunning;
    @CsvBindByName(column = "radio_data_up_time(ms)")
    public long radioDataUptimeWhenUnplugged;
    @CsvBindByName(column = "bluetooth_on_time(ms)")
    public long bluetoothOn;
    @CsvBindByName(column = "start_clock_time(yyyy-mm-dd-hh-mm-ss)")
    public String startClockTime;
    @CsvBindByName(column = "mobile_radio_active_time(ms)")
    public long mobileRadioActiveTime;
    @CsvBindByName(column = "wifi_recieve_time(ms)")
    public long wifiRxTime;
    @CsvBindByName(column = "wifi_send_time(ms)")
    public long wifiTxTime;
    @CsvBindByName(column = "bluetooth_receive_time(ms)")
    public long bluetoothRxTime;
    @CsvBindByName(column = "bluetooth_send_time(ms)")
    public long bluetoothTxTime;
    @CsvBindByName(column = "radio_receive_time(ms)")
    public long radioRxTime;
    @CsvBindByName(column = "radio_send_time(ms)")
    public long radioTxTime;
    @CsvBindByName(column = "bluetooth_power_drain(mAh)")
    public String bluetoothPowerDrain = "0";
    @CsvBindByName(column = "wifi_power_drain(mAh)")
    public String wifiPowerDrain = "0";
    @CsvBindByName(column = "radio_power_drain(mAh)")
    public String radioPowerDrain = "0";
    @CsvBindByName(column = "estimated_battery_capacity(mAh)")
    public String estimatedBatteryCapacity;
    @CsvBindByName(column = "discharge(mAh)")
    public String discharge = "0";
    @CsvBindByName(column = "screen_off_discharge(mAh)")
    public String screenOffDischarge = "0";
    @CsvBindByName(column = "screen_on_discharge(mAh)")
    public String screenOnDischarge = "0";
    @CsvBindByName(column = "screen_doze_discharge(mAh)")
    public String screenDozeDischarge = "0";
    @CsvBindByName(column = "light_doze_discharge(mAh)")
    public String lightDozeDischarge = "0";
    @CsvBindByName(column = "deep_doze_discharge(mAh)")
    public String deepDozeDischarge = "0";
    @CsvBindByName(column = "cell_power_drain(mAh)")
    public String cellPowerDrain = "0";
    @CsvBindByName(column = "screen_power_drain(mAh)")
    public String screenPowerDrain = "0";//added for all uids
    @CsvBindByName(column = "*uid_power_drain(mAh)")
    public float uidPowerDrain = 0;
    @CsvBindByName(column = "computed_drain(mAh)")
    public String computedDrain = "0";
    @CsvBindByName(column = "actual_drain(mAh)")
    public String actualDrain = "0";
    @CsvBindByName(column = "voltage(V)")
    public float voltage = 4.2f;//default value

    @Override
    public String toString() {
        return "BatteryInfo{" +
                "systemStarts='" + systemStarts + '\'' + "\n" +
                ", currentlyOnBattery='" + currentlyOnBattery + '\'' + "\n" +
                ", timeOnBattery='" + timeOnBattery + '\'' + "\n" +
                ", totalRunTime='" + totalRunTime + '\'' + "\n" +
                ", screenOn='" + screenOn + '\'' + "\n" +
                ", mobileTotalReceived='" + mobileTotalReceived + '\'' + "\n" +
                ", wiFiTotalReceived='" + wiFiTotalReceived + '\'' + "\n" +
                ", totalFullWakelockTime='" + totalFullWakelockTime + '\'' + "\n" +
                ", signalScanningTime='" + signalScanningTime + '\'' + "\n" +
                ", wifiOn='" + wifiOn + '\'' + "\n" +
                ", wifiRunning='" + wifiRunning + '\'' + "\n" +
                ", radioDataUptimeWhenUnplugged='" + radioDataUptimeWhenUnplugged + '\'' + "\n" +
                ", bluetoothOn='" + bluetoothOn + '\'' + "\n" +
                ", startClockTime='" + startClockTime + '\'' + "\n" +
                ", mobileRadioActiveTime='" + mobileRadioActiveTime + '\'' + "\n" +
                ", wifiRxTime='" + wifiRxTime + '\'' + "\n" +
                ", wifiTxTime='" + wifiTxTime + '\'' + "\n" +
                ", bluetoothRxTime='" + bluetoothRxTime + '\'' + "\n" +
                ", bluetoothTxTime='" + bluetoothTxTime + '\'' + "\n" +
                ", radioRxTime='" + radioRxTime + '\'' + "\n" +
                ", radioTxTime='" + radioTxTime + '\'' + "\n" +
                ", bluetoothPowerDrain='" + bluetoothPowerDrain + '\'' + "\n" +
                ", wifiPowerDrain='" + wifiPowerDrain + '\'' + "\n" +
                ", radioPowerDrain='" + radioPowerDrain + '\'' + "\n" +
                ", estimatedBatteryCapacity='" + estimatedBatteryCapacity + '\'' + "\n" +
                ", discharge='" + discharge + '\'' + "\n" +
                ", screenOffDischarge='" + screenOffDischarge + '\'' + "\n" +
                ", screenOnDischarge='" + screenOnDischarge + '\'' + "\n" +
                ", screenDozeDischarge='" + screenDozeDischarge + '\'' + "\n" +
                ", lightDozeDischarge='" + lightDozeDischarge + '\'' + "\n" +
                ", deepDozeDischarge='" + deepDozeDischarge + '\'' + "\n" +
                ", cellPowerDrain='" + cellPowerDrain + '\'' + "\n" +
                ", screenPowerDrain='" + screenPowerDrain + '\'' + "\n" +
                ", uidPowerDrain='" + uidPowerDrain + '\'' + "\n" +
                ", computerDrain='" + computedDrain + '\'' + "\n" +
                ", actualDrain='" + actualDrain + '\'' + "\n" +
                '}';
    }
}
