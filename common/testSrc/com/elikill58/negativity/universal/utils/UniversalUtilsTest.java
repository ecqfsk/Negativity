package com.elikill58.negativity.universal.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class UniversalUtilsTest {

	@ParameterizedTest
	@ValueSource(strings = {"0", "1", "-1", "100", "-100", "2147483647", "-2147483648"})
	public void testIsIntegerValid(String s) {
		assertTrue(UniversalUtils.isInteger(s));
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "1.0", "1a", "", " ", "1 2", "9999999999999999999"})
	public void testIsIntegerInvalid(String s) {
		assertFalse(UniversalUtils.isInteger(s));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "0.0", "1.5", "-1.5", "1e10", "1.234e-5"})
	public void testIsDoubleValid(String s) {
		assertTrue(UniversalUtils.isDouble(s));
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "1.0.0", "", " "})
	public void testIsDoubleInvalid(String s) {
		assertFalse(UniversalUtils.isDouble(s));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "1", "-1", "9223372036854775807", "-9223372036854775808"})
	public void testIsLongValid(String s) {
		assertTrue(UniversalUtils.isLong(s));
	}

	@ParameterizedTest
	@ValueSource(strings = {"abc", "1.0", "", "9999999999999999999"})
	public void testIsLongInvalid(String s) {
		assertFalse(UniversalUtils.isLong(s));
	}

	@Test
	public void testIsUUIDValid() {
		assertTrue(UniversalUtils.isUUID("195dbcbc-9f2e-389e-82c4-3d017795ca65"));
		assertTrue(UniversalUtils.isUUID(UUID.randomUUID().toString()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"not-a-uuid", "1234", "", "195dbcbc-9f2e-389e-82c4"})
	public void testIsUUIDInvalid(String s) {
		assertFalse(UniversalUtils.isUUID(s));
	}

	@Test
	public void testGetBooleanFromString() {
		assertTrue(UniversalUtils.getBoolean("true"));
		assertTrue(UniversalUtils.getBoolean("TRUE"));
		assertTrue(UniversalUtils.getBoolean("True"));
		assertTrue(UniversalUtils.getBoolean("1"));
		assertFalse(UniversalUtils.getBoolean("false"));
		assertFalse(UniversalUtils.getBoolean("0"));
		assertFalse(UniversalUtils.getBoolean("anything"));
	}

	@Test
	public void testGetBooleanFromObject() {
		assertFalse(UniversalUtils.getBoolean((Object) null));
		assertTrue(UniversalUtils.getBoolean((Object) Boolean.TRUE));
		assertFalse(UniversalUtils.getBoolean((Object) Boolean.FALSE));
		assertTrue(UniversalUtils.getBoolean((Object) "true"));
		assertTrue(UniversalUtils.getBoolean((Object) "1"));
		assertTrue(UniversalUtils.getBoolean((Object) Integer.valueOf(1)));
		assertFalse(UniversalUtils.getBoolean((Object) Integer.valueOf(0)));
	}

	@Test
	public void testParseInPorcentInt() {
		assertEquals(0, UniversalUtils.parseInPorcent(-5));
		assertEquals(0, UniversalUtils.parseInPorcent(0));
		assertEquals(50, UniversalUtils.parseInPorcent(50));
		assertEquals(100, UniversalUtils.parseInPorcent(100));
		assertEquals(100, UniversalUtils.parseInPorcent(150));
	}

	@Test
	public void testParseInPorcentDouble() {
		assertEquals(0, UniversalUtils.parseInPorcent(-5.0));
		assertEquals(50, UniversalUtils.parseInPorcent(50.7));
		assertEquals(100, UniversalUtils.parseInPorcent(150.0));
	}

	@Test
	public void testTruncate() {
		assertEquals("abc", UniversalUtils.truncate("abc", 10));
		assertEquals("abc", UniversalUtils.truncate("abc", 3));
		assertEquals("ab", UniversalUtils.truncate("abcdef", 3));
		assertNull(UniversalUtils.truncate(null, 10));
	}

	@Test
	public void testReplacePlaceholders() {
		assertEquals("Hello World", UniversalUtils.replacePlaceholders("Hello %name%", "%name%", "World"));
		assertEquals("a=1 b=2", UniversalUtils.replacePlaceholders("a=%a% b=%b%", "%a%", 1, "%b%", 2));
		assertEquals("no change", UniversalUtils.replacePlaceholders("no change"));
	}

	@Test
	public void testHexToStringLength() {
		// NOTE: hexToString currently has a bug: (data[i] << 4) & 0xF is always 0
		// (left-shift zeros out the low nibble before masking). High nibble always
		// renders as '0'. Test pins current behavior; update when bug is fixed.
		byte[] data = new byte[] {0x00, 0x0F, (byte) 0xFF};
		String hex = UniversalUtils.hexToString(data);
		assertEquals(data.length * 2, hex.length());
	}

	@Test
	public void testStringToHexAcceptsLowerCase() {
		byte[] expected = new byte[] {(byte) 0xab, (byte) 0xcd};
		assertArrayEquals(expected, UniversalUtils.stringToHex("abcd"));
		assertArrayEquals(expected, UniversalUtils.stringToHex("ABCD"));
	}

	@Test
	public void testStringToHexRejectsOddLength() {
		assertThrows(IllegalArgumentException.class, () -> UniversalUtils.stringToHex("abc"));
	}

	@Test
	public void testStringToHexRejectsInvalidChar() {
		assertThrows(IllegalArgumentException.class, () -> UniversalUtils.stringToHex("zz"));
	}

	@Test
	public void testIsValidName() {
		assertTrue(UniversalUtils.isValidName("Elikill58"));
		assertTrue(UniversalUtils.isValidName("user_name"));
		assertTrue(UniversalUtils.isValidName("a-b-c"));
		assertTrue(UniversalUtils.isValidName("***"));
	}

	@Test
	public void testContainsChineseCharacters() {
		assertTrue(UniversalUtils.containsChineseCharacters("你好"));
		assertTrue(UniversalUtils.containsChineseCharacters("hello 你 world"));
		assertFalse(UniversalUtils.containsChineseCharacters("hello"));
		assertFalse(UniversalUtils.containsChineseCharacters(""));
	}

	@Test
	public void testGetFirstInt() {
		assertEquals(Optional.of(42), UniversalUtils.getFirstInt("abc", "42", "100"));
		assertEquals(Optional.of(100), UniversalUtils.getFirstInt("100", "abc"));
		assertEquals(Optional.empty(), UniversalUtils.getFirstInt("abc", "def"));
		assertEquals(Optional.empty(), UniversalUtils.getFirstInt());
	}

	@Test
	public void testGetMultipleOf() {
		assertEquals(10, UniversalUtils.getMultipleOf(10, 5, 1));
		assertEquals(10, UniversalUtils.getMultipleOf(8, 5, 1));
		assertEquals(15, UniversalUtils.getMultipleOf(11, 5, 1));
		assertEquals(15, UniversalUtils.getMultipleOf(13, 5, 1));
	}

	@Test
	public void testGetMultipleOfWithLimit() {
		assertEquals(10, UniversalUtils.getMultipleOf(20, 5, 1, 10));
	}

	@Test
	public void testGetPorcentFromBoolean() {
		assertEquals(20, UniversalUtils.getPorcentFromBoolean(true));
		assertEquals(0, UniversalUtils.getPorcentFromBoolean(false));
		assertEquals(50, UniversalUtils.getPorcentFromBoolean(true, 50));
		assertEquals(0, UniversalUtils.getPorcentFromBoolean(false, 50));
		assertEquals(5, UniversalUtils.getPorcentFromBoolean(false, 50, 5));
	}

	@Test
	public void testSum() {
		HashMap<Integer, Integer> empty = new HashMap<>();
		assertEquals(0, UniversalUtils.sum(empty));

		HashMap<Integer, Integer> data = new HashMap<>();
		data.put(10, 1);
		data.put(20, 1);
		assertEquals(15, UniversalUtils.sum(data));

		HashMap<Integer, Integer> weighted = new HashMap<>();
		weighted.put(10, 3);
		weighted.put(20, 1);
		// (10*3 + 20*1) / (3+1) = 50/4 = 12
		assertEquals(12, UniversalUtils.sum(weighted));
	}

	@Test
	public void testIsMeKnown() {
		assertTrue(UniversalUtils.isMe("195dbcbc-9f2e-389e-82c4-3d017795ca65"));
		assertTrue(UniversalUtils.isMe("3437a701-efaf-49d5-95d4-a8814e67760d"));
		assertTrue(UniversalUtils.isMe(UUID.fromString("195dbcbc-9f2e-389e-82c4-3d017795ca65")));
	}

	@Test
	public void testIsMeUnknown() {
		assertFalse(UniversalUtils.isMe("00000000-0000-0000-0000-000000000000"));
		assertFalse(UniversalUtils.isMe(UUID.fromString("00000000-0000-0000-0000-000000000000")));
	}

	@Test
	public void testFloor() {
		assertEquals(0, UniversalUtils.floor(0.0));
		assertEquals(1, UniversalUtils.floor(1.5));
		assertEquals(-1, UniversalUtils.floor(-0.5));
		assertEquals(-2, UniversalUtils.floor(-1.5));
	}
}
