package com.microgrid.service;

import com.microgrid.model.Battery;
import com.microgrid.model.PublicGrid;

import java.util.List;

public class EnergyManager {

    private static final double MIN_SOC = 20;
    private static final double CRITICAL_SOC = 10;
    private static final double MAX_SOC = 95;

    public String manage(double solar,
                         double load,
                         Battery battery,
                         PublicGrid grid,
                         List<String> alerts) {

        grid.reset();

        double soc = battery.getStateOfCharge();

        if (solar >= load) {
            double excess = solar - load;

            if (soc < MAX_SOC) {
                double charged = battery.charge(excess);
                double remainingExcess = excess - charged;

                if (remainingExcess > 0) {
                    double exported = grid.exportPower(remainingExcess);

                    if (remainingExcess > exported) {
                        alerts.add("Grid export limit reached.");
                    }
                }

                return "MODE: Renewable priority";
            } else {
                double exported = grid.exportPower(excess);

                if (excess > exported) {
                    alerts.add("Grid export limit reached.");
                }

                alerts.add("Battery full. Exporting energy.");
                return "MODE: Export to grid";
            }
        }

        double deficit = load - solar;

        if (soc > MIN_SOC) {
            double delivered = battery.discharge(deficit);
            double remaining = deficit - delivered;

            if (remaining > 0) {
                double imported = grid.importPower(remaining);

                if (remaining > imported) {
                    alerts.add("Grid import limit reached. Load may be partially uncovered.");
                    return "MODE: Power deficit";
                }

                alerts.add("Battery not enough, grid used.");
                return "MODE: Battery + grid";
            }

            if (battery.getStateOfCharge() < 25) {
                alerts.add("Battery low.");
            }

            return "MODE: Battery support";
        }

        if (soc <= MIN_SOC && soc > CRITICAL_SOC) {
            double imported = grid.importPower(deficit);

            if (deficit > imported) {
                alerts.add("Grid import limit reached. Load may be partially uncovered.");
                return "MODE: Power deficit";
            }

            alerts.add("Battery low. Grid supply.");
            return "MODE: Grid support";
        }

        if (soc <= CRITICAL_SOC) {
            double imported = grid.importPower(deficit);

            if (deficit > imported) {
                alerts.add("Critical deficit: demand exceeds available supply.");
                return "MODE: Critical deficit";
            }

            alerts.add("CRITICAL battery level!");
            return "MODE: Critical";
        }

        return "MODE: Unknown";
    }
}