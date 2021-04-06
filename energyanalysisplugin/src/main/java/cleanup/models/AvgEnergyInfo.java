package cleanup.models;

import com.opencsv.bean.CsvBindByName;

public class AvgEnergyInfo {
    @CsvBindByName(column = "package_name")
    public String packageName;
    @CsvBindByName(column = "version_no")
    public String versionNo;
    @CsvBindByName(column = "total_avg_energy(Joule)")
    public double totalExeAvgEnergy;
    @CsvBindByName(column = "total_avg_power(Watt)")
    public double totalExeAvgPower;
    @CsvBindByName(column = "baseline_avg_energy(Joule)")
    public double baselineAvgEnergy;
    @CsvBindByName(column = "baseline_avg_power(Watt)")
    public double baselineAvgPower;
    @CsvBindByName(column = "app_avg_power(Watt)")
    public double appAvgPower;
    @CsvBindByName(column = "app_avg_energy(Joule)")
    public double appAvgEnergy;
    @CsvBindByName(column = "max_app_power(Watt)")
    public double maxPower;
    @CsvBindByName(column = "max_app_energy(Joule)")
    public double maxEnergy;
    @CsvBindByName(column = "min_app_power(Watt)")
    public double minPower;
    @CsvBindByName(column = "min_app_energy(Joule)")
    public double minEnergy;
    @CsvBindByName(column = "standard_deviation_app_power")
    public double sdPower;
    @CsvBindByName(column = "standard_deviation_app_energy")
    public double sdEnergy;
}
