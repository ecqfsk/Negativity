package com.elikill58.negativity.universal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;

import org.junit.jupiter.api.Test;

import com.elikill58.negativity.universal.Minerate.MinerateType;

public class MinerateTest {

	@Test
	public void testDefaultConstructorInitsAllTypesToZero() {
		Minerate m = new Minerate();
		assertEquals(0, m.getFullMined());
		for (MinerateType type : MinerateType.values()) {
			assertEquals(Integer.valueOf(0), m.getMinerateType(type));
		}
	}

	@Test
	public void testSetMine() {
		Minerate m = new Minerate();
		m.setMine(MinerateType.DIAMOND, 5);
		assertEquals(Integer.valueOf(5), m.getMinerateType(MinerateType.DIAMOND));
		assertEquals(Integer.valueOf(0), m.getMinerateType(MinerateType.GOLD));
	}

	@Test
	public void testAddMineWithType() {
		Minerate m = new Minerate();
		m.addMine(MinerateType.DIAMOND, null);
		m.addMine(MinerateType.DIAMOND, null);
		m.addMine(MinerateType.GOLD, null);
		assertEquals(Integer.valueOf(2), m.getMinerateType(MinerateType.DIAMOND));
		assertEquals(Integer.valueOf(1), m.getMinerateType(MinerateType.GOLD));
		assertEquals(3, m.getFullMined());
	}

	@Test
	public void testAddMineNullTypeIncrementsOnlyFull() {
		Minerate m = new Minerate();
		m.addMine(null, null);
		m.addMine(null, null);
		assertEquals(2, m.getFullMined());
		// All types still 0
		for (MinerateType type : MinerateType.values()) {
			assertEquals(Integer.valueOf(0), m.getMinerateType(type));
		}
	}

	@Test
	public void testConstructorFromMap() {
		HashMap<MinerateType, Integer> data = new HashMap<>();
		data.put(MinerateType.DIAMOND, 7);
		Minerate m = new Minerate(data, 10);
		assertEquals(7, m.getMinerateType(MinerateType.DIAMOND));
		// Missing types filled with 0
		assertEquals(Integer.valueOf(0), m.getMinerateType(MinerateType.GOLD));
		assertEquals(Integer.valueOf(0), m.getMinerateType(MinerateType.IRON));
		assertEquals(10, m.getFullMined());
	}

	@Test
	public void testGetMinedMap() {
		Minerate m = new Minerate();
		HashMap<MinerateType, Integer> mined = m.getMined();
		assertNotNull(mined);
		assertEquals(MinerateType.values().length, mined.size());
	}

	@Test
	public void testMinerateTypeGetMinerateType() {
		assertEquals(MinerateType.DIAMOND, MinerateType.getMinerateType("DIAMOND"));
		assertEquals(MinerateType.DIAMOND, MinerateType.getMinerateType("diamond"));
		assertEquals(MinerateType.DIAMOND, MinerateType.getMinerateType("DIAMOND_ORE"));
		assertEquals(MinerateType.DIAMOND, MinerateType.getMinerateType("minecraft:diamond_ore"));
		assertEquals(MinerateType.GOLD, MinerateType.getMinerateType("GOLD_ORE"));
		assertEquals(MinerateType.ANCIENT_DEBRIS, MinerateType.getMinerateType("minecraft:ancient_debris"));
		assertNull(MinerateType.getMinerateType("unknown"));
		assertNull(MinerateType.getMinerateType(""));
	}

	@Test
	public void testMinerateTypeFromId() {
		assertEquals(MinerateType.DIAMOND, MinerateType.fromId("minecraft:diamond_ore"));
		assertEquals(MinerateType.IRON, MinerateType.fromId("minecraft:iron_ore"));
		assertEquals(MinerateType.COAL, MinerateType.fromId("MINECRAFT:COAL_ORE"));
		assertNull(MinerateType.fromId("minecraft:emerald_ore"));
		assertNull(MinerateType.fromId("DIAMOND"));
	}

	@Test
	public void testMinerateTypeGetters() {
		assertEquals("Diamond", MinerateType.DIAMOND.getName());
		assertEquals("DIAMOND_ORE", MinerateType.DIAMOND.getOreName());
		assertEquals("minecraft:diamond_ore", MinerateType.DIAMOND.getMcId());
	}

	@Test
	public void testToStringContainsAllMined() {
		Minerate m = new Minerate();
		m.setMine(MinerateType.DIAMOND, 3);
		String s = m.toString();
		assertNotNull(s);
		// String should mention each type by name
		for (MinerateType type : MinerateType.values()) {
			org.junit.jupiter.api.Assertions.assertTrue(s.contains(type.getName()),
					"toString should contain type name " + type.getName());
		}
	}
}
