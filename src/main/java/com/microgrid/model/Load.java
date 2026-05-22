package com.microgrid.model;

import java.util.Random;

public class Load {

    private final Random random = new Random();
    private double demand;

    public void update(int hour) {
        if ((hour >= 7 && hour <= 9) || (hour >= 18 && hour <= 23)) {
            demand = 6 + random.nextDouble() * 4;
        } else {
            demand = 2 + random.nextDouble() * 3;
        }
    }

    public double getDemand() {
        return demand;
    }
}