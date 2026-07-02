package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.AUTO_ARMOR;

import java.util.Locale;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.inventory.InventoryAction;
import com.elikill58.negativity.api.events.inventory.InventoryClickEvent;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.common.protocols.data.AutoArmorData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;
import com.elikill58.negativity.universal.utils.UniversalUtils;

/**
 * AutoArmor detector.
 *
 * <p>Equipping several armour pieces requires distinct clicks separated by human reaction
 * time. AutoArmor clients equip a full set within a few milliseconds. We track the delay
 * between consecutive armour-equip clicks and alert when pieces are equipped back-to-back
 * faster than a human can click.
 */
public class AutoArmor extends Cheat {

	public AutoArmor() {
		super(AUTO_ARMOR, CheatCategory.PLAYER, Materials.DIAMOND, AutoArmorData::new);
	}

	@Check(name = "fast-swap", description = "Equipping armour faster than humanly possible")
	public void onInventoryClick(InventoryClickEvent e, NegativityPlayer np, AutoArmorData data) {
		ItemStack item = e.getCurrentItem();
		if (item == null || !isArmor(item))
			return;
		InventoryAction action = e.getAction();
		// only actions that instantly move an item to an armour slot
		if (action != InventoryAction.LEFT_SHIFT && action != InventoryAction.RIGHT_SHIFT && action != InventoryAction.NUMBER)
			return;

		Player p = e.getPlayer();
		long now = System.currentTimeMillis();
		long delay = now - data.lastEquipTime;
		data.lastEquipTime = now;
		long susDelay = getConfig().getLong("checks.fast-swap.suspicious-ms");
		if (delay < susDelay) {
			data.equipStreak++;
			int alertAt = getConfig().getInt("streak_alert", 2);
			if (data.equipStreak >= alertAt) {
				int reliability = UniversalUtils.parseInPorcent(70 + (susDelay - delay) / 4);
				Negativity.alertMod(ReportType.WARNING, p, this, reliability, "fast-swap",
						"Armour equipped " + delay + "ms after previous piece (streak " + data.equipStreak + ")");
			}
		} else {
			data.equipStreak = 0;
		}
	}

	private boolean isArmor(ItemStack item) {
		String id = item.getType().getId().toLowerCase(Locale.ROOT);
		return id.endsWith("helmet") || id.endsWith("chestplate") || id.endsWith("leggings") || id.endsWith("boots");
	}
}
