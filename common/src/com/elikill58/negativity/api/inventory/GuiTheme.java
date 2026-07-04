package com.elikill58.negativity.api.inventory;

import java.util.ArrayList;
import java.util.List;

import com.elikill58.negativity.api.colors.ChatColor;
import com.elikill58.negativity.api.item.Enchantment;
import com.elikill58.negativity.api.item.ItemBuilder;
import com.elikill58.negativity.api.item.ItemFlag;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Material;
import com.elikill58.negativity.api.item.Materials;

/**
 * Shared visual theme for every Negativity inventory (MineEye-style framed look).
 *
 * <p>Instead of flooding the whole grid with filler, {@link #applyFrame(Inventory)} draws a
 * gray-glass border with white-glass corners and a glowing logo at the top center, leaving the
 * inner slots empty for content. Helper builders ({@link #card}, {@link #statusCard},
 * {@link #navigation}, {@link #glow}) give items the same "card" style.
 *
 * <p>Frame items carry a blank name so they never show a tooltip. The theme never changes the
 * material of a real (clickable) item, so inventory click handling that matches on material
 * keeps working.
 */
public final class GuiTheme {

	private GuiTheme() {
	}

	/** Draw the border + corners + logo, leaving inner slots free for content. */
	public static void applyFrame(Inventory inv) {
		applyFrame(inv, defaultLogo());
	}

	/**
	 * Draw the border + corners, and place {@code logo} at the top-center slot (4).
	 *
	 * @param inv  the inventory to frame
	 * @param logo the logo item, or {@code null} to leave the top-center slot empty
	 */
	public static void applyFrame(Inventory inv, ItemStack logo) {
		int size = inv.getSize();
		for (int slot = 0; slot < size; slot++)
			if (inv.get(slot) == null && isBorderSlot(size, slot))
				inv.set(slot, isCornerSlot(size, slot) ? corner() : border());
		if (logo != null && size > 4 && inv.get(4) == null)
			inv.set(4, logo);
	}

	public static boolean isBorderSlot(int size, int slot) {
		int col = slot % 9;
		return slot < 9 || slot >= size - 9 || col == 0 || col == 8;
	}

	private static boolean isCornerSlot(int size, int slot) {
		return slot == 0 || slot == 8 || slot == size - 9 || slot == size - 1;
	}

	/** The inner (non-border) slots, in order, usable to lay out content. */
	public static List<Integer> getContentSlots(int size) {
		List<Integer> slots = new ArrayList<>();
		for (int slot = 0; slot < size; slot++)
			if (!isBorderSlot(size, slot))
				slots.add(slot);
		return slots;
	}

	private static ItemStack border() {
		return ItemBuilder.Builder(Materials.GRAY_STAINED_GLASS_PANE).displayName(" ").itemFlag(ItemFlag.HIDE_ATTRIBUTES).build();
	}

	private static ItemStack corner() {
		return ItemBuilder.Builder(Materials.WHITE_STAINED_GLASS).displayName(" ").itemFlag(ItemFlag.HIDE_ATTRIBUTES).build();
	}

	public static ItemStack defaultLogo() {
		return createLogo(Materials.NETHER_STAR, "Negativity");
	}

	/** A glowing title item, aqua and bold, used as the header logo. */
	public static ItemStack createLogo(Material material, String name, String... lore) {
		return glow(card(material, ChatColor.AQUA + "" + ChatColor.BOLD + name, lore));
	}

	/** A plain item card with hidden attributes. */
	public static ItemStack card(Material material, String name, String... lore) {
		ItemBuilder builder = ItemBuilder.Builder(material).displayName(name).itemFlag(ItemFlag.HIDE_ATTRIBUTES);
		if (lore.length > 0)
			builder.lore(lore);
		return builder.build();
	}

	/** A yellow navigation card (next/previous/back...). */
	public static ItemStack navigation(Material material, String name, String... lore) {
		return card(material, ChatColor.YELLOW + name, lore);
	}

	/** A card whose material and lead line reflect an on/off state (emerald/redstone). */
	public static ItemStack statusCard(String name, boolean active, String... extraLore) {
		List<String> lore = new ArrayList<>();
		lore.add(active ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
		for (String line : extraLore)
			lore.add(line);
		return card(active ? Materials.EMERALD : Materials.REDSTONE_BLOCK, name, lore.toArray(new String[0]));
	}

	/** Add an enchantment glint without showing the enchantment tooltip. */
	public static ItemStack glow(ItemStack item) {
		return ItemBuilder.Builder(item).unsafeEnchant(Enchantment.UNBREAKING, 1).itemFlag(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES).build();
	}
}
