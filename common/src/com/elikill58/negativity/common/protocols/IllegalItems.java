package com.elikill58.negativity.common.protocols;

import static com.elikill58.negativity.universal.detections.keys.CheatKeys.ILLEGAL_ITEMS;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.player.PlayerInteractEvent;
import com.elikill58.negativity.api.item.Enchantment;
import com.elikill58.negativity.api.item.ItemStack;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.protocols.Check;
import com.elikill58.negativity.common.protocols.data.EmptyData;
import com.elikill58.negativity.universal.Negativity;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.report.ReportType;

/**
 * IllegalItems detector.
 *
 * <p>Flags items that cannot exist in survival: over-enchanted gear (enchant level above
 * the configured vanilla maximum) and over-stacked items (amount above 64). Scans the
 * held item and worn armour whenever the player interacts.
 */
public class IllegalItems extends Cheat {

	public IllegalItems() {
		super(ILLEGAL_ITEMS, CheatCategory.PLAYER, Materials.BOOK, EmptyData::new);
	}

	@Check(name = "illegal", description = "Over-enchanted or over-stacked items")
	public void onInteract(PlayerInteractEvent e, NegativityPlayer np) {
		Player p = e.getPlayer();
		int maxLevel = getConfig().getInt("max_enchant_level", 5);
		int maxAmount = getConfig().getInt("max_stack_amount", 64);

		if (check(p, p.getItemInHand(), maxLevel, maxAmount, "hand"))
			return;
		ItemStack[] armor = p.getInventory().getArmorContent();
		if (armor != null)
			for (ItemStack piece : armor)
				if (check(p, piece, maxLevel, maxAmount, "armor"))
					return;
	}

	private boolean check(Player p, ItemStack item, int maxLevel, int maxAmount, String where) {
		if (item == null)
			return false;
		if (item.getAmount() > maxAmount) {
			Negativity.alertMod(ReportType.WARNING, p, this, 100, "illegal",
					"Over-stacked item (" + where + "): " + item.getType().getId() + " x" + item.getAmount());
			return true;
		}
		for (Enchantment ench : Enchantment.values()) {
			int level = item.getEnchantLevel(ench);
			if (level > maxLevel) {
				Negativity.alertMod(ReportType.WARNING, p, this, 100, "illegal",
						"Over-enchanted item (" + where + "): " + item.getType().getId() + " " + ench + " " + level + " > " + maxLevel);
				return true;
			}
		}
		return false;
	}
}
