package com.microgrid.model;

public class PublicGrid {

    private double importedPower;
    private double exportedPower;

    private final double maxImportPower;
    private final double maxExportPower;

    public PublicGrid() {
        this.maxImportPower = 8.0;
        this.maxExportPower = 6.0;
    }

    public void reset() {
        importedPower = 0;
        exportedPower = 0;
    }

    public double importPower(double power) {
        double accepted = Math.min(power, maxImportPower);
        importedPower += accepted;
        return accepted;
    }

    public double exportPower(double power) {
        double accepted = Math.min(power, maxExportPower);
        exportedPower += accepted;
        return accepted;
    }

    public double getImportedPower() {
        return importedPower;
    }

    public double getExportedPower() {
        return exportedPower;
    }

    public double getMaxImportPower() {
        return maxImportPower;
    }

    public double getMaxExportPower() {
        return maxExportPower;
    }
}
