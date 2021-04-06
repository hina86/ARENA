package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class AvgBatteryInfo {
    @CsvBindByName(column = "package_name")
    public String packageName;
    @CsvBindByName(column = "version_no")
    public String versionNo;
/*    @CsvBindByName(column = "avg_wifi_drain(mAh)")
    public double avgWifiDrain = 0;
    @CsvBindByName(column = "avg_uid_drain(mAh)")
    public double avgUidDrain = 0;
    @CsvBindByName(column = "avg_screen_drain(mAh)")
    public double avgScreenDrain = 0;
    @CsvBindByName(column = "avg_cell_power_drain(mAh)")
    public double avgCellPowerDrain = 0;
    @CsvBindByName(column = "avg_radio_power_drain(mAh)")
    public double avgRadioPowerDrain = 0;
    @CsvBindByName(column = "avg_bluetooth_power_drain(mAh)")
    public double avgBluetoothPowerDrain = 0;
    @CsvBindByName(column = "avg_actual_drain(mAh)")
    public double avgActualDrain = 0;
    @CsvBindByName(column = "avg_computed_drain(mAh)")
    public double avgComputedDrain = 0;
    @CsvBindByName(column = "avg_discharge(mAh)")
    public double avgDischarge = 0;
    @CsvBindByName(column = "avg_screen_on_discharge(mAh)")
    public double avgScreenOnDischarge = 0;
    @CsvBindByName(column = "avg_screen_off_discharge(mAh)")
    public double avgScreenOffDischarge = 0;
    @CsvBindByName(column = "avg_screen_doze_discharge(mAh)")
    public double avgScreenDozeDischarge = 0;
    @CsvBindByName(column = "avg_light_doze_discharge(mAh)")
    public double avgLightDozeDischarge = 0;
    @CsvBindByName(column = "avg_deep_doze_discharge(mAh)")
    public double avgDeepDozeDischarge = 0;
    @CsvBindByName(column = "capacity(mAh)")
    public int capacity = 0;*/
    @CsvBindByName(column = "computed_energy(Joule)")
    public double computedEnergy = 0;
    @CsvBindByName(column = "actual_energy(Joule)")
    public double actualEnergy = 0;
    @CsvBindByName(column = "min_computed_energy(Joule)")
    public double minComputedEnergy = 0;
    @CsvBindByName(column = "min_actual_energy(Joule)")
    public double minActualEnergy = 0;
    @CsvBindByName(column = "max_computed_energy(Joule)")
    public double maxComputedEnergy = 0;
    @CsvBindByName(column = "max_actual_energy(Joule)")
    public double maxActualEnergy = 0;
    @CsvBindByName(column = "standard_deviation_computed_energy")
    public double sdComputedEnergy = 0;
    @CsvBindByName(column = "standard_deviation_actual_energy")
    public double sdActualEnergy = 0;

/*    public AvgBatteryInfo(String packageName, String versionNo, double avgWifiDrain, double avgUidDrain, double avgScreenDrain, double avgCellPowerDrain, double avgRadioPowerDrain, double avgBluetoothPowerDrain, double avgActualDrain, double avgComputedDrain, double avgDischarge, double avgScreenOnDischarge, double avgScreenOffDischarge, double avgScreenDozeDischarge, double avgLightDozeDischarge, double avgDeepDozeDischarge, int capacity, double computedEnergy, double actualEnergy, double minComputedEnergy, double minActualEnergy, double maxComputedEnergy, double maxCctualEnergy, double sdComputedEnergy, double sdActualEnergy) {
        this.packageName = packageName;
        this.versionNo = versionNo;
        this.avgWifiDrain = avgWifiDrain;
        this.avgUidDrain = avgUidDrain;
        this.avgScreenDrain = avgScreenDrain;
        this.avgCellPowerDrain = avgCellPowerDrain;
        this.avgRadioPowerDrain = avgRadioPowerDrain;
        this.avgBluetoothPowerDrain = avgBluetoothPowerDrain;
        this.avgActualDrain = avgActualDrain;
        this.avgComputedDrain = avgComputedDrain;
        this.avgDischarge = avgDischarge;
        this.avgScreenOnDischarge = avgScreenOnDischarge;
        this.avgScreenOffDischarge = avgScreenOffDischarge;
        this.avgScreenDozeDischarge = avgScreenDozeDischarge;
        this.avgLightDozeDischarge = avgLightDozeDischarge;
        this.avgDeepDozeDischarge = avgDeepDozeDischarge;
        this.capacity = capacity;
        this.computedEnergy = computedEnergy;
        this.actualEnergy = actualEnergy;
        this.minComputedEnergy = minComputedEnergy;
        this.minActualEnergy = minActualEnergy;
        this.maxComputedEnergy = maxComputedEnergy;
        this.maxActualEnergy = maxCctualEnergy;
        this.sdComputedEnergy = sdComputedEnergy;
        this.sdActualEnergy = sdActualEnergy;
    }*/

    public AvgBatteryInfo() {

    }
}
