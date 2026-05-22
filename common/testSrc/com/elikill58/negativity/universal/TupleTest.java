package com.elikill58.negativity.universal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class TupleTest {

	@Test
	public void testGetters() {
		Tuple<String, Integer> t = new Tuple<>("foo", 42);
		assertEquals("foo", t.getA());
		assertEquals(Integer.valueOf(42), t.getB());
	}

	@Test
	public void testNullValues() {
		Tuple<String, String> t = new Tuple<>(null, null);
		assertNull(t.getA());
		assertNull(t.getB());
	}

	@Test
	public void testHeterogeneousTypes() {
		Tuple<Integer, Boolean> t = new Tuple<>(1, true);
		assertEquals(Integer.valueOf(1), t.getA());
		assertEquals(Boolean.TRUE, t.getB());
	}

	@Test
	public void testToString() {
		Tuple<String, Integer> t = new Tuple<>("foo", 42);
		assertEquals("Tuple{foo,42}", t.toString());
	}

	@Test
	public void testToStringWithNull() {
		Tuple<String, Object> t = new Tuple<>(null, null);
		assertEquals("Tuple{null,null}", t.toString());
	}
}
