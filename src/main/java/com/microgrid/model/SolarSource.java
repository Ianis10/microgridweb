package com.microgrid.model;

import java.util.Random;

public class SolarSource {

    private final Random random = new Random();
    private double power;

    public void update(int hour) {
        if (hour >= 6 && hour <= 18) {
            double peakFactor = 1 - Math.abs(12 - hour) / 6.0;
            power = peakFactor * 8 + random.nextDouble() * 2;
        } else {
            power = 0;
        }
    }

    public double getPower() {
        return power;
    }
}