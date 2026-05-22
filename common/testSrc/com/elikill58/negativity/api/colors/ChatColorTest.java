package com.elikill58.negativity.api.colors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

public class ChatColorTest {

	@Test
	public void testGetName() {
		assertEquals("&0", ChatColor.BLACK.getName());
		assertEquals("&6", ChatColor.GOLD.getName());
		assertEquals("&f", ChatColor.WHITE.getName());
		assertEquals("&r", ChatColor.RESET.getName());
	}

	@Test
	public void testColorTranslatesAmpersand() {
		assertEquals("§6Hello", ChatColor.color("&6Hello"));
		assertEquals("§aGreen §cRed", ChatColor.color("&aGreen &cRed"));
	}

	@Test
	public void testColorIgnoresInvalidCodes() {
		// &z is not a valid color code, should be left as-is
		assertEquals("&zfoo", ChatColor.color("&zfoo"));
	}

	@Test
	public void testColorLowercasesCode() {
		// Upper-case codes should be normalized to lowercase
		assertEquals("§ftext", ChatColor.color("&Ftext"));
	}

	@Test
	public void testColorNullReturnsNull() {
		assertNull(ChatColor.color(null));
	}

	@Test
	public void testColorEmpty() {
		assertEquals("", ChatColor.color(""));
	}

	@Test
	public void testColorTrailingAmpersandLeftAlone() {
		// Single trailing & cannot be a code, must stay literal
		assertEquals("hello&", ChatColor.color("hello&"));
	}

	@Test
	public void testTranslateAlternateColorCodes() {
		assertEquals("§6Gold", ChatColor.translateAlternateColorCodes('&', "&6Gold"));
		assertEquals("§6Gold", ChatColor.translateAlternateColorCodes('%', "%6Gold"));
		assertEquals("&6Untouched", ChatColor.translateAlternateColorCodes('%', "&6Untouched"));
	}

	@Test
	public void testStripColor() {
		assertEquals("Hello", ChatColor.stripColor("§6Hello"));
		assertEquals("Hello World", ChatColor.stripColor("§6Hello §cWorld"));
		assertEquals("plain", ChatColor.stripColor("plain"));
	}

	@Test
	public void testStripColorNull() {
		assertNull(ChatColor.stripColor(null));
	}

	@Test
	public void testStripColorEmpty() {
		assertEquals("", ChatColor.stripColor(""));
	}

	@Test
	public void testToStringReturnsColoredCode() {
		assertEquals("§6", ChatColor.GOLD.toString());
		assertEquals("§c", ChatColor.RED.toString());
	}

	@Test
	public void testFormatCodes() {
		assertEquals("&k", ChatColor.MAGIC.getName());
		assertEquals("&l", ChatColor.BOLD.getName());
		assertEquals("&m", ChatColor.STRIKETHROUGH.getName());
		assertEquals("&n", ChatColor.UNDERLINE.getName());
		assertEquals("&o", ChatColor.ITALIC.getName());
	}
}
