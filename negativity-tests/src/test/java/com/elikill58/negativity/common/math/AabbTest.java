package com.elikill58.negativity.common.math;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AabbTest {

    @Test
    void intersectingZero() {
        Aabb a = new Aabb(0, 0, 0, 1, 2, 1);
        Aabb b = new Aabb(0.5, 0.5, 0.5, 1.5, 2.5, 1.5);
        assertEquals(0.0, Aabb.distance(a, b), 0.0001);
    }

    @Test
    void separatedOnX() {
        Aabb a = new Aabb(0, 0, 0, 1, 1, 1);
        Aabb b = new Aabb(3, 0, 0, 4, 1, 1);
        assertEquals(2.0, Aabb.distance(a, b), 0.0001);
    }
}
