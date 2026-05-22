package com.elikill58.negativity.universal.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ChatUtilsTest {

	@Test
	public void testParseDurationToSecondsSingleUnit() {
		assertEquals(30L, ChatUtils.parseDurationToSeconds("30s"));
		assertEquals(120L, ChatUtils.parseDurationToSeconds("2m"));
		assertEquals(3600L, ChatUtils.parseDurationToSeconds("1h"));
		assertEquals(86400L, ChatUtils.parseDurationToSeconds("1d"));
	}

	@Test
	public void testParseDurationToSecondsAliases() {
		assertEquals(60L, ChatUtils.parseDurationToSeconds("1min"));
		assertEquals(60L, ChatUtils.parseDurationToSeconds("1minutes"));
		assertEquals(30L, ChatUtils.parseDurationToSeconds("30sec"));
		assertEquals(30L, ChatUtils.parseDurationToSeconds("30seconds"));
		assertEquals(3600L, ChatUtils.parseDurationToSeconds("1hour"));
		assertEquals(86400L, ChatUtils.parseDurationToSeconds("1j"));
		assertEquals(ChatUtils.MONTHS, ChatUtils.parseDurationToSeconds("1mo"));
		assertEquals(ChatUtils.MONTHS, ChatUtils.parseDurationToSeconds("1mu"));
		assertEquals(ChatUtils.YEARS, ChatUtils.parseDurationToSeconds("1y"));
		assertEquals(ChatUtils.YEARS, ChatUtils.parseDurationToSeconds("1yo"));
	}

	@Test
	public void testParseDurationToSecondsCompound() {
		assertEquals(3600L + 1800L, ChatUtils.parseDurationToSeconds("1h30m"));
		assertEquals(86400L + 3600L + 60L + 1L, ChatUtils.parseDurationToSeconds("1d1h1m1s"));
	}

	@Test
	public void testParseDurationToSecondsNoMarker() {
		// Trailing integer with no marker is added directly
		assertEquals(100L, ChatUtils.parseDurationToSeconds("100"));
	}

	@Test
	public void testParseDurationToSecondsEmpty() {
		assertEquals(0L, ChatUtils.parseDurationToSeconds(""));
	}

	@Test
	public void testParseDurationToSecondsUnknownMarker() {
		assertThrows(IllegalArgumentException.class, () -> ChatUtils.parseDurationToSeconds("1xyz"));
	}

	@Test
	public void testCapitalizeSingleWord() {
		assertEquals("Hello", ChatUtils.capitalize("hello"));
		assertEquals("Hello", ChatUtils.capitalize("HELLO"));
		assertEquals("Hello", ChatUtils.capitalize("HeLLo"));
	}

	@Test
	public void testCapitalizeWithUnderscores() {
		assertEquals("HelloWorld", ChatUtils.capitalize("hello_world"));
		assertEquals("ABC", ChatUtils.capitalize("a_b_c"));
		assertEquals("FooBarBaz", ChatUtils.capitalize("FOO_BAR_BAZ"));
	}

	@Test
	public void testCapitalizeEmpty() {
		assertEquals("", ChatUtils.capitalize(""));
	}

	@Test
	public void testFormatTimeZero() {
		assertNull(ChatUtils.formatTime(0L));
		assertNull(ChatUtils.formatTime(-1L));
	}

	@Test
	public void testFormatTimePositive() {
		String result = ChatUtils.formatTime(1700000000000L);
		assertNotNull(result);
		// yyyy-MM-dd HH:mm:ss = 19 chars
		assertEquals(19, result.length());
	}

	@Test
	public void testGetFullTimeFromLongZero() {
		assertEquals("", ChatUtils.getFullTimeFromLong(0L));
		assertEquals("", ChatUtils.getFullTimeFromLong(-100L));
	}

	@Test
	public void testGetFullTimeFromLongSeconds() {
		// 5 seconds in ms
		assertEquals("5s", ChatUtils.getFullTimeFromLong(5000L));
	}

	@Test
	public void testGetFullTimeFromLongHoursMinutes() {
		// 1h 30m = 5400 seconds = 5400000 ms
		assertEquals("1h 30m", ChatUtils.getFullTimeFromLong(5400000L));
	}

	@Test
	public void testGetFullTimeFromLongDays() {
		// 2 days
		assertEquals("2d", ChatUtils.getFullTimeFromLong(2L * 86400L * 1000L));
	}

	@Test
	public void testGetTimeFromLongShortest() {
		// returns major value only with decimal
		String result = ChatUtils.getTimeFromLong(5000L);
		assertEquals("5.0s", result);
	}

	@Test
	public void testGetTimeFromLongHours() {
		// 2h in ms; > MINUTES branch picks first if not > HOURS strictly. 7200s > 3600 (HOURS), returns hours.
		String result = ChatUtils.getTimeFromLong(7200000L);
		assertEquals("2.0h ", result);
	}
}
