package com.elikill58.negativity.universal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class VersionTest {

	@Test
	public void testGetName() {
		assertEquals("1.8.8", Version.V1_8.getName());
		assertEquals("1.20", Version.V1_20.getName());
		assertEquals("1.21", Version.V1_21.getName());
		assertEquals("lower", Version.LOWER.getName());
		assertEquals("higher", Version.HIGHER.getName());
	}

	@Test
	public void testGetPower() {
		assertEquals(8.0, Version.V1_8.getPower());
		assertEquals(20.0, Version.V1_20.getPower());
		assertEquals(0.0, Version.LOWER.getPower());
		assertEquals(42.0, Version.HIGHER.getPower());
	}

	@Test
	public void testProtocolNumbers() {
		assertEquals(47, Version.V1_8.getLastProtocolNumber());
		assertEquals(6, Version.V1_8.getFirstProtocolNumber());
		assertEquals(767, Version.V1_21.getFirstProtocolNumber());
	}

	@Test
	public void testIsNewerThan() {
		assertTrue(Version.V1_20.isNewerThan(Version.V1_19));
		assertTrue(Version.V1_21.isNewerThan(Version.V1_8));
		assertFalse(Version.V1_8.isNewerThan(Version.V1_20));
		assertFalse(Version.V1_20.isNewerThan(Version.V1_20));
		assertTrue(Version.HIGHER.isNewerThan(Version.LOWER));
	}

	@Test
	public void testIsNewerOrEquals() {
		assertTrue(Version.V1_20.isNewerOrEquals(Version.V1_20));
		assertTrue(Version.V1_20.isNewerOrEquals(Version.V1_19));
		assertFalse(Version.V1_8.isNewerOrEquals(Version.V1_20));
	}

	@Test
	public void testHasProtocolNumber() {
		assertTrue(Version.V1_8.hasProtocolNumber(47));
		assertTrue(Version.V1_8.hasProtocolNumber(6));
		assertTrue(Version.V1_8.hasProtocolNumber(20));
		assertFalse(Version.V1_8.hasProtocolNumber(48));
		assertFalse(Version.V1_8.hasProtocolNumber(5));
	}

	@Test
	public void testGetVersionByName() {
		assertEquals(Version.V1_8, Version.getVersionByName("1.8.8"));
		assertEquals(Version.V1_20, Version.getVersionByName("1.20"));
		assertEquals(Version.V1_21, Version.getVersionByName("1.21"));
	}

	@Test
	public void testGetVersionByNameFallbackHigher() {
		assertEquals(Version.HIGHER, Version.getVersionByName("99.99.99"));
	}

	@Test
	public void testGetVersionByProtocolID() {
		assertEquals(Version.V1_8, Version.getVersionByProtocolID(47));
		assertEquals(Version.V1_9, Version.getVersionByProtocolID(48));
		assertEquals(Version.V1_21, Version.getVersionByProtocolID(767));
	}

	@Test
	public void testGetVersionByProtocolIDBounds() {
		assertEquals(Version.LOWER, Version.getVersionByProtocolID(0));
		assertEquals(Version.LOWER, Version.getVersionByProtocolID(5));
		assertEquals(Version.HIGHER, Version.getVersionByProtocolID(9999));
	}
}
