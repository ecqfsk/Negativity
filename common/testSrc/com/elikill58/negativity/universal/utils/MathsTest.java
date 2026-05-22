package com.elikill58.negativity.universal.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MathsTest {

	@Test
	public void testFloorPositive() {
		assertEquals(0, Maths.floor(0.0));
		assertEquals(0, Maths.floor(0.5));
		assertEquals(1, Maths.floor(1.0));
		assertEquals(1, Maths.floor(1.9));
		assertEquals(10, Maths.floor(10.999));
	}

	@Test
	public void testFloorNegative() {
		assertEquals(-1, Maths.floor(-0.5));
		assertEquals(-1, Maths.floor(-1.0));
		assertEquals(-2, Maths.floor(-1.1));
		assertEquals(-11, Maths.floor(-10.5));
	}

	@Test
	public void testRound() {
		assertEquals(0, Maths.round(0.0));
		assertEquals(0, Maths.round(0.4));
		assertEquals(1, Maths.round(0.5));
		assertEquals(1, Maths.round(1.49));
		assertEquals(2, Maths.round(1.5));
		// round(-0.5) = floor(-0.5 + 0.5) = floor(0.0) = 0
		assertEquals(0, Maths.round(-0.5));
		assertEquals(-1, Maths.round(-1.0));
		// round(-1.4) = floor(-0.9) = -1
		assertEquals(-1, Maths.round(-1.4));
	}

	@Test
	public void testRoundLoc() {
		assertEquals(0, Maths.roundLoc(0.0));
		assertEquals(1, Maths.roundLoc(1.0));
		assertEquals(2, Maths.roundLoc(2.5));
		assertEquals(-1, Maths.roundLoc(-1.0));
	}

	@ParameterizedTest
	@CsvSource({
			"0.0, 0.0",
			"2.0, 4.0",
			"-3.0, 9.0",
			"1.5, 2.25",
			"0.5, 0.25"
	})
	public void testSquare(double input, double expected) {
		assertEquals(expected, Maths.square(input), 1e-9);
	}

	@Test
	public void testIsOnGroundTrue() {
		assertTrue(Maths.isOnGround(0.0));
		assertTrue(Maths.isOnGround(0.015625));
		assertTrue(Maths.isOnGround(0.03125));
		assertTrue(Maths.isOnGround(1.0));
		assertTrue(Maths.isOnGround(64.0));
	}

	@Test
	public void testIsOnGroundFalse() {
		assertFalse(Maths.isOnGround(0.01));
		assertFalse(Maths.isOnGround(0.1));
		assertFalse(Maths.isOnGround(1.234));
		assertFalse(Maths.isOnGround(0.42));
	}

	@Test
	public void testGetGcdSimple() {
		assertEquals(2.0, Maths.getGcd(4.0, 2.0), 1e-6);
		assertEquals(3.0, Maths.getGcd(9.0, 3.0), 1e-6);
		assertEquals(5.0, Maths.getGcd(10.0, 5.0), 1e-6);
	}

	@Test
	public void testGetGcdReverseOrder() {
		// a < b path - swaps
		assertEquals(2.0, Maths.getGcd(2.0, 4.0), 1e-6);
	}

	@Test
	public void testGetGcdTerminatesOnSmallB() {
		// b < 0.001 returns a
		assertEquals(4.0, Maths.getGcd(4.0, 0.0005), 1e-6);
	}

	@Test
	public void testGetGcdRecursionLimit() {
		// internalAmount <= 0 short-circuits returning a
		assertEquals(7.0, Maths.getGcd(7.0, 3.0, 0), 1e-6);
	}

	@Test
	public void testIsOutOfBoundsValid() {
		assertFalse(Maths.isOutOfBounds(0, 0, 0));
		assertFalse(Maths.isOutOfBounds(0, 5, 10));
		assertFalse(Maths.isOutOfBounds(5, 5, 10));
		assertFalse(Maths.isOutOfBounds(0, 10, 10));
	}

	@Test
	public void testIsOutOfBoundsInvalid() {
		assertTrue(Maths.isOutOfBounds(-1, 5, 10));
		assertTrue(Maths.isOutOfBounds(0, -1, 10));
		assertTrue(Maths.isOutOfBounds(0, 5, -1));
		assertTrue(Maths.isOutOfBounds(8, 5, 10));
		assertTrue(Maths.isOutOfBounds(0, 11, 10));
	}
}
