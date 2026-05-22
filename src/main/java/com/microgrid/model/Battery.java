package com.microgrid.model;

public class Battery {

    private final double capacity;
    private double stateOfCharge;

    private final double maxChargePower;
    private final double maxDischargePower;

    public Battery(double capacity, double initialSOC) {
        this.capacity = capacity;
        this.stateOfCharge = initialSOC;
        this.maxChargePower = 4.0;
        this.maxDischargePower = 5.0;
    }

    public double charge(double energy) {
        double acceptedEnergy = Math.min(energy, maxChargePower);

        stateOfCharge += (acceptedEnergy / capacity) * 100.0;

        if (stateOfCharge > 100) {
            stateOfCharge = 100;
        }

        return acceptedEnergy;
    }

    public double discharge(double energy) {
        double requestedEnergy = Math.min(energy, maxDischargePower);

        double availableEnergy = (stateOfCharge / 100.0) * capacity;
        double deliveredEnergy = Math.min(requestedEnergy, availableEnergy);

        stateOfCharge -= (deliveredEnergy / capacity) * 100.0;

        if (stateOfCharge < 0) {
            stateOfCharge = 0;
        }

        return deliveredEnergy;
    }

    public double getStateOfCharge() {
        return stateOfCharge;
    }

    public double getCapacity() {
        return capacity;
    }

    public double getMaxChargePower() {
        return maxChargePower;
    }

    public double getMaxDischargePower() {
        return maxDischargePower;
    }
}