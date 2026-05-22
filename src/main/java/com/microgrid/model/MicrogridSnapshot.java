package com.microgrid.model;

import java.time.LocalDateTime;
import java.util.List;

public class MicrogridSnapshot {

    private LocalDateTime timestamp;
    private int simulatedHour;

    private double solarProduction;
    private double loadConsumption;
    private double batterySoc;
    private double batteryPower;

    private double gridImport;
    private double gridExport;
    private double gridExchange;

    private double integratedEnergy;

    private String systemStatus;
    private List<String> alerts;

    public MicrogridSnapshot() {
    }

    public MicrogridSnapshot(LocalDateTime timestamp,
                             int simulatedHour,
                             double solarProduction,
                             double loadConsumption,
                             double batterySoc,
                             double batteryPower,
                             double gridImport,
                             double gridExport,
                             double gridExchange,
                             double integratedEnergy,
                             String systemStatus,
                             List<String> alerts) {
        this.timestamp = timestamp;
        this.simulatedHour = simulatedHour;
        this.solarProduction = solarProduction;
        this.loadConsumption = loadConsumption;
        this.batterySoc = batterySoc;
        this.batteryPower = batteryPower;
        this.gridImport = gridImport;
        this.gridExport = gridExport;
        this.gridExchange = gridExchange;
        this.integratedEnergy = integratedEnergy;
        this.systemStatus = systemStatus;
        this.alerts = alerts;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getSimulatedHour() {
        return simulatedHour;
    }

    public double getSolarProduction() {
        return solarProduction;
    }

    public double getLoadConsumption() {
        return loadConsumption;
    }

    public double getBatterySoc() {
        return batterySoc;
    }

    public double getBatteryPower() {
        return batteryPower;
    }

    public double getGridImport() {
        return gridImport;
    }

    public double getGridExport() {
        return gridExport;
    }

    public double getGridExchange() {
        return gridExchange;
    }

    public double getIntegratedEnergy() {
        return integratedEnergy;
    }

    public String getSystemStatus() {
        return systemStatus;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setSimulatedHour(int simulatedHour) {
        this.simulatedHour = simulatedHour;
    }

    public void setSolarProduction(double solarProduction) {
        this.solarProduction = solarProduction;
    }

    public void setLoadConsumption(double loadConsumption) {
        this.loadConsumption = loadConsumption;
    }

    public void setBatterySoc(double batterySoc) {
        this.batterySoc = batterySoc;
    }

    public void setBatteryPower(double batteryPower) {
        this.batteryPower = batteryPower;
    }

    public void setGridImport(double gridImport) {
        this.gridImport = gridImport;
    }

    public void setGridExport(double gridExport) {
        this.gridExport = gridExport;
    }

    public void setGridExchange(double gridExchange) {
        this.gridExchange = gridExchange;
    }

    public void setIntegratedEnergy(double integratedEnergy) {
        this.integratedEnergy = integratedEnergy;
    }

    public void setSystemStatus(String systemStatus) {
        this.systemStatus = systemStatus;
    }

    public void setAlerts(List<String> alerts) {
        this.alerts = alerts;
    }
}
