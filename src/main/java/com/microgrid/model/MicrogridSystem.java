package com.microgrid.model;

import com.microgrid.service.AlertManager;
import com.microgrid.service.EnergyManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class MicrogridSystem {

    private final SolarSource solarSource;
    private final Load load;
    private final Battery battery;
    private final PublicGrid grid;

    private final EnergyManager energyManager;
    private final AlertManager alertManager;

    private final List<MicrogridSnapshot> history;

    private int simulatedHour = 5;

    public MicrogridSystem() {
        this.solarSource = new SolarSource();
        this.load = new Load();
        this.battery = new Battery(20, 60);
        this.grid = new PublicGrid();

        this.energyManager = new EnergyManager();
        this.alertManager = new AlertManager();

        this.history = new ArrayList<>();
    }

    public synchronized MicrogridSnapshot simulateStep() {
        simulatedHour++;

        if (simulatedHour > 23) {
            simulatedHour = 0;
        }

        solarSource.update(simulatedHour);
        load.update(simulatedHour);

        double previousSoc = battery.getStateOfCharge();

        List<String> alerts = new ArrayList<>();

        String status = energyManager.manage(
                solarSource.getPower(),
                load.getDemand(),
                battery,
                grid,
                alerts
        );

        alerts.addAll(alertManager.evaluate(
                solarSource.getPower(),
                load.getDemand(),
                battery.getStateOfCharge(),
                grid.getImportedPower(),
                simulatedHour
        ));

        double currentSoc = battery.getStateOfCharge();

        double batteryPower =
                ((previousSoc - currentSoc) / 100.0) * battery.getCapacity();

        double gridExchange = grid.getImportedPower() - grid.getExportedPower();

        double integratedEnergy =
                solarSource.getPower()
                        + batteryPower
                        + grid.getImportedPower()
                        - grid.getExportedPower();

        MicrogridSnapshot snapshot = new MicrogridSnapshot(
                LocalDateTime.now(),
                simulatedHour,
                solarSource.getPower(),
                load.getDemand(),
                battery.getStateOfCharge(),
                batteryPower,
                grid.getImportedPower(),
                grid.getExportedPower(),
                gridExchange,
                integratedEnergy,
                status,
                alerts
        );

        history.add(snapshot);
        return snapshot;
    }

    public synchronized MicrogridSnapshot getLatestSnapshot() {
        if (history.isEmpty()) {
            return simulateStep();
        }

        return history.get(history.size() - 1);
    }

    public synchronized List<MicrogridSnapshot> getHistory() {
        return new ArrayList<>(history);
    }

    public synchronized void resetHistory() {
        history.clear();
    }

    public synchronized AverageStats getAverageStats(int steps) {
        if (history.isEmpty()) {
            return new AverageStats();
        }

        int startIndex = Math.max(0, history.size() - steps);
        int count = history.size() - startIndex;

        double solarSum = 0;
        double loadSum = 0;
        double socSum = 0;
        double integratedSum = 0;
        double gridExchangeSum = 0;

        for (int i = startIndex; i < history.size(); i++) {
            MicrogridSnapshot s = history.get(i);
            solarSum += s.getSolarProduction();
            loadSum += s.getLoadConsumption();
            socSum += s.getBatterySoc();
            integratedSum += s.getIntegratedEnergy();
            gridExchangeSum += s.getGridExchange();
        }

        AverageStats stats = new AverageStats();
        stats.setSolarAverage(solarSum / count);
        stats.setLoadAverage(loadSum / count);
        stats.setSocAverage(socSum / count);
        stats.setIntegratedAverage(integratedSum / count);
        stats.setGridExchangeAverage(gridExchangeSum / count);

        return stats;
    }

    public synchronized DashboardData getDashboardData() {
        MicrogridSnapshot latest = getLatestSnapshot();

        DashboardData data = new DashboardData();
        data.setSnapshot(latest);
        data.setDay(getAverageStats(24));
        data.setWeek(getAverageStats(24 * 7));
        data.setMonth(getAverageStats(24 * 30));
        data.setYear(getAverageStats(24 * 365));
        data.setHistory(getHistory());

        return data;
    }

    public static class AverageStats {
        private double solarAverage;
        private double loadAverage;
        private double socAverage;
        private double integratedAverage;
        private double gridExchangeAverage;

        public double getSolarAverage() {
            return solarAverage;
        }

        public void setSolarAverage(double solarAverage) {
            this.solarAverage = solarAverage;
        }

        public double getLoadAverage() {
            return loadAverage;
        }

        public void setLoadAverage(double loadAverage) {
            this.loadAverage = loadAverage;
        }

        public double getSocAverage() {
            return socAverage;
        }

        public void setSocAverage(double socAverage) {
            this.socAverage = socAverage;
        }

        public double getIntegratedAverage() {
            return integratedAverage;
        }

        public void setIntegratedAverage(double integratedAverage) {
            this.integratedAverage = integratedAverage;
        }

        public double getGridExchangeAverage() {
            return gridExchangeAverage;
        }

        public void setGridExchangeAverage(double gridExchangeAverage) {
            this.gridExchangeAverage = gridExchangeAverage;
        }
    }

    public static class DashboardData {
        private MicrogridSnapshot snapshot;
        private AverageStats day;
        private AverageStats week;
        private AverageStats month;
        private AverageStats year;
        private List<MicrogridSnapshot> history;

        public MicrogridSnapshot getSnapshot() {
            return snapshot;
        }

        public void setSnapshot(MicrogridSnapshot snapshot) {
            this.snapshot = snapshot;
        }

        public AverageStats getDay() {
            return day;
        }

        public void setDay(AverageStats day) {
            this.day = day;
        }

        public AverageStats getWeek() {
            return week;
        }

        public void setWeek(AverageStats week) {
            this.week = week;
        }

        public AverageStats getMonth() {
            return month;
        }

        public void setMonth(AverageStats month) {
            this.month = month;
        }

        public AverageStats getYear() {
            return year;
        }

        public void setYear(AverageStats year) {
            this.year = year;
        }

        public List<MicrogridSnapshot> getHistory() {
            return history;
        }

        public void setHistory(List<MicrogridSnapshot> history) {
            this.history = history;
        }
    }
}
