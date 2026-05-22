package com.elikill58.negativity.universal.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ReportTypeTest {

	@Test
	public void testPower() {
		assertEquals(4, ReportType.VIOLATION.getPower());
		assertEquals(3, ReportType.WARNING.getPower());
		assertEquals(2, ReportType.INFO.getPower());
		assertEquals(1, ReportType.REPORT.getPower());
		assertEquals(0, ReportType.NONE.getPower());
	}

	@Test
	public void testName() {
		assertEquals("Violation", ReportType.VIOLATION.getName());
		assertEquals("Warning", ReportType.WARNING.getName());
		assertEquals("Info", ReportType.INFO.getName());
		assertEquals("Report", ReportType.REPORT.getName());
		assertEquals("None", ReportType.NONE.getName());
	}

	@Test
	public void testIsStronger() {
		assertTrue(ReportType.VIOLATION.isStronger(ReportType.WARNING));
		assertTrue(ReportType.WARNING.isStronger(ReportType.INFO));
		assertTrue(ReportType.INFO.isStronger(ReportType.REPORT));
		assertTrue(ReportType.REPORT.isStronger(ReportType.NONE));
	}

	@Test
	public void testIsStrongerFalse() {
		assertFalse(ReportType.NONE.isStronger(ReportType.REPORT));
		assertFalse(ReportType.INFO.isStronger(ReportType.WARNING));
		assertFalse(ReportType.VIOLATION.isStronger(ReportType.VIOLATION));
	}

	@Test
	public void testOrdering() {
		ReportType[] sorted = {ReportType.NONE, ReportType.REPORT, ReportType.INFO, ReportType.WARNING, ReportType.VIOLATION};
		for (int i = 1; i < sorted.length; i++) {
			assertTrue(sorted[i].isStronger(sorted[i - 1]),
					sorted[i] + " should be stronger than " + sorted[i - 1]);
		}
	}
}
