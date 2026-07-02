package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.AUTO_ARMOR;

import java.util.Locale;
import java.util.Optional;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.inventory.InventoryAction;
import com.elikill58.negativity.api.events.inventory.InventoryClickEvent;
import com.elikill58.negativity.api.inventory.PlayerInventory;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.api.protocols.CheckConditions;
import com.elikill58.negativity.common.protocols.data.AutoArmorData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.Scheduler;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * AutoArmor detector.
 *
 * <p>Equipping several armour pieces requires distinct clicks separated by human reaction
 * time. AutoArmor clients equip a full set within a few milliseconds. Clicking an armour
 * item is NOT enough of a signal (shift-clicking armour inside a chest just moves it): one
 * tick after the click we verify the piece actually landed in its armour slot, and only
 * verified equips feed the timing streak. Back-to-back real equips faster than the
 * configured delay are inhuman.
 */
public class AutoArmor extends Cheat {

	public AutoArmor() {
		super(AUTO_ARMOR, CheatCategory.PLAYER, Materials.DIAMOND, AutoArmorData::new);
	}

	@Check(name = "fast-swap", description = "Equipping armour faster than humanly possible", conditions = { CheckConditions.SURVIVAL })
	public void onInventoryClick(InventoryClickEvent e, NegativityPlayer np, AutoArmorData data) {
		ItemStack item = e.getCurrentItem();
		if (item == null)
			return;
		String slot = armorSlot(item);
		if (slot == null)
			return;
		InventoryAction action = e.getAction();
		// only actions that can instantly move an item into an armour slot
		if (action != InventoryAction.LEFT_SHIFT && action != InventoryAction.RIGHT_SHIFT && action != InventoryAction.NUMBER)
			return;

		Player p = e.getPlayer();
		String clickedType = item.getType().getId();
		if (isWearing(p, slot, clickedType))
			return; // slot already occupied by this piece: the click cannot be an equip

		// verify next tick that the piece really landed in the armour slot
		// (kills the "shift-clicking armour in a chest" false positive)
		Scheduler.getInstance().runEntityDelayed(p, () -> {
			if (!p.isOnline() || !isWearing(p, slot, clickedType))
				return;
			long now = System.currentTimeMillis();
			long delay = now - data.lastEquipTime;
			data.lastEquipTime = now;
			long susDelay = getConfig().getLong("checks.fast-swap.suspicious-ms", 120);
			if (delay < susDelay) {
				data.equipStreak++;
				int alertAt = getConfig().getInt("streak_alert", 2);
				if (data.equipStreak >= alertAt) {
					int reliability = UniversalUtils.parseInPorcent(70 + (susDelay - delay) / 4);
					Negativity.alertMod(ReportType.WARNING, p, this, reliability, "fast-swap",
							"Armour piece (" + slot + ") equipped " + delay + "ms after previous piece (streak " + data.equipStreak + ")");
				}
			} else {
				data.equipStreak = 0;
			}
		}, 1);
	}

	private boolean isWearing(Player p, String slot, String typeId) {
		return armorPiece(p.getInventory(), slot).map(is -> is.getType().getId().equalsIgnoreCase(typeId)).orElse(false);
	}

	private Optional<ItemStack> armorPiece(PlayerInventory inv, String slot) {
		switch (slot) {
		case "helmet":
			return inv.getHelmet();
		case "chestplate":
			return inv.getChestplate();
		case "leggings":
			return inv.getLegging();
		case "boots":
			return inv.getBoots();
		default:
			return Optional.empty();
		}
	}

	private String armorSlot(ItemStack item) {
		String id = item.getType().getId().toLowerCase(Locale.ROOT);
		if (id.endsWith("helmet"))
			return "helmet";
		if (id.endsWith("chestplate"))
			return "chestplate";
		if (id.endsWith("leggings"))
			return "leggings";
		if (id.endsWith("boots"))
			return "boots";
		return null;
	}
}
