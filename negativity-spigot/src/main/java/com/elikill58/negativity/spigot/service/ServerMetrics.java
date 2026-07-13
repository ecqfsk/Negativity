package com.elikill58.negativity.spigot.service;

import org.bukkit.Bukkit;

/**
 * Lightweight TPS/MSPT sampling without NMS reflection.
 */
public final class ServerMetrics {

    private double tps = 20.0;
    private double mspt = 50.0;
    private long lastSampleNs = System.nanoTime();

    public void sample() {
        long now = System.nanoTime();
        long delta = now - lastSampleNs;
        lastSampleNs = now;
        if (delta <= 0) {
            return;
        }
        // 1 server tick expected = 50ms
        mspt = delta / 1_000_000.0;
        double instantTps = 1000.0 / Math.max(1.0, mspt);
        tps = clamp(instantTps, 0.0, 20.0);

        // Prefer Paper API when available
        try {
            double[] paperTps = Bukkit.getTPS();
            if (paperTps != null && paperTps.length > 0) {
                tps = clamp(paperTps[0], 0.0, 20.0);
            }
        } catch (NoSuchMethodError | Exception ignored) {
            // non-Paper fallback keeps tick-based estimate
        }
    }

    public double tps() {
        return tps;
    }

    public double mspt() {
        return mspt;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
