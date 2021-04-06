package cleanup.models;

public class ComputedPMResults {
    public double power;
    public double energy;
    public int rows;

    public ComputedPMResults(double power, double energy, int rows) {
        this.power = power;
        this.energy = energy;
        this.rows = rows;
    }
}
