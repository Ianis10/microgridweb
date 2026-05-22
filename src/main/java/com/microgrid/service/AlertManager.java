package com.microgrid.service;

import java.util.ArrayList;
import java.util.List;

public class AlertManager {

    public List<String> evaluate(double solar,
                                 double load,
                                 double batteryLevel,
                                 double gridImport,
                                 int hour) {

        List<String> alerts = new ArrayList<>();

        if (batteryLevel < 15) {
            alerts.add("Critical battery state detected.");
        }

        if (batteryLevel >= 15 && batteryLevel < 30) {
            alerts.add("Battery reserve is low.");
        }

        if (load > 9) {
            alerts.add("High consumption peak detected.");
        }

        if (solar < 1 && hour >= 9 && hour <= 16) {
            alerts.add("Low solar production detected during daylight period.");
        }

        if (gridImport > 5) {
            alerts.add("Heavy dependency on public grid.");
        }

        return alerts;
    }
}
