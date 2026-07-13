package com.elikill58.negativity.common.math;

/**
 * Minimal AABB helpers for reach/combat math (no Bukkit dependency).
 */
public final class Aabb {

    public final double minX, minY, minZ, maxX, maxY, maxZ;

    public Aabb(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public static double distance(Aabb a, Aabb b) {
        double dx = gap(a.minX, a.maxX, b.minX, b.maxX);
        double dy = gap(a.minY, a.maxY, b.minY, b.maxY);
        double dz = gap(a.minZ, a.maxZ, b.minZ, b.maxZ);
        if (dx == 0 && dy == 0 && dz == 0) {
            return 0;
        }
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static double gap(double minA, double maxA, double minB, double maxB) {
        if (maxA < minB) {
            return minB - maxA;
        }
        if (maxB < minA) {
            return minA - maxB;
        }
        return 0;
    }
}
