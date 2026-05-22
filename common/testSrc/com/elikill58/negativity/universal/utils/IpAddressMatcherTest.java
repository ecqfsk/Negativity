package com.elikill58.negativity.universal.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class IpAddressMatcherTest {

	@Test
	public void testIPv4ExactMatch() {
		IpAddressMatcher m = new IpAddressMatcher("192.168.1.1");
		assertTrue(m.matches("192.168.1.1"));
		assertFalse(m.matches("192.168.1.2"));
	}

	@Test
	public void testIPv4Cidr24() {
		IpAddressMatcher m = new IpAddressMatcher("192.168.1.0/24");
		assertTrue(m.matches("192.168.1.0"));
		assertTrue(m.matches("192.168.1.100"));
		assertTrue(m.matches("192.168.1.255"));
		assertFalse(m.matches("192.168.2.0"));
		assertFalse(m.matches("10.0.0.1"));
	}

	@Test
	public void testIPv4Cidr16() {
		IpAddressMatcher m = new IpAddressMatcher("10.0.0.0/16");
		assertTrue(m.matches("10.0.0.1"));
		assertTrue(m.matches("10.0.255.255"));
		assertFalse(m.matches("10.1.0.0"));
	}

	@Test
	public void testIPv4Cidr8() {
		IpAddressMatcher m = new IpAddressMatcher("10.0.0.0/8");
		assertTrue(m.matches("10.0.0.1"));
		assertTrue(m.matches("10.255.255.255"));
		assertFalse(m.matches("11.0.0.0"));
	}

	@Test
	public void testIPv4Cidr32() {
		IpAddressMatcher m = new IpAddressMatcher("192.168.1.1/32");
		assertTrue(m.matches("192.168.1.1"));
		assertFalse(m.matches("192.168.1.2"));
	}

	@Test
	public void testIPv6ExactMatch() {
		IpAddressMatcher m = new IpAddressMatcher("::1");
		assertTrue(m.matches("::1"));
		assertFalse(m.matches("::2"));
	}

	@Test
	public void testIPv4VsIPv6MismatchReturnsFalse() {
		IpAddressMatcher m = new IpAddressMatcher("192.168.1.1");
		assertFalse(m.matches("::1"));
	}

	@Test
	public void testStaticMatchSubnetString() {
		assertTrue(IpAddressMatcher.match("192.168.1.50", "192.168.1.0/24"));
		assertFalse(IpAddressMatcher.match("192.168.2.50", "192.168.1.0/24"));
	}

	@Test
	public void testStaticMatchWithSeparateMask() {
		assertTrue(IpAddressMatcher.match("192.168.1.50", "192.168.1.0", 24));
		assertFalse(IpAddressMatcher.match("192.168.2.50", "192.168.1.0", 24));
	}

	@Test
	public void testInvalidAddressThrows() {
		assertThrows(IllegalArgumentException.class, () -> new IpAddressMatcher("not.an.ip.address.999"));
	}

	@Test
	public void testTwoArgConstructor() {
		IpAddressMatcher m = new IpAddressMatcher("192.168.1.0", 24);
		assertTrue(m.matches("192.168.1.42"));
		assertFalse(m.matches("192.168.2.42"));
	}
}
