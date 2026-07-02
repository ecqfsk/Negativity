package com.elikill58.negativity.common.protocols;

import com.elikill58.negativity.api.NegativityPlayer;
import com.elikill58.negativity.api.entity.EntityType;
import com.elikill58.negativity.api.entity.Player;
import com.elikill58.negativity.api.events.EventListener;
import com.elikill58.negativity.api.events.Listeners;
import com.elikill58.negativity.api.events.entity.ProjectileHitEvent;
import com.elikill58.negativity.api.events.player.PlayerDeathEvent;
import com.elikill58.negativity.api.events.player.PlayerRegainHealthEvent;
import com.elikill58.negativity.api.item.Materials;
import com.elikill58.negativity.api.location.Location;
import com.elikill58.negativity.api.potion.PotionEffect;
import com.elikill58.negativity.api.potion.PotionEffectType;
import com.elikill58.negativity.common.protocols.data.EmptyData;
import com.elikill58.negativity.universal.Adapter;
import com.elikill58.negativity.universal.FlyingReason;
import com.elikill58.negativity.universal.detections.Cheat;
import com.elikill58.negativity.universal.detections.keys.CheatKeys;

public class AntiPotion extends Cheat implements Listeners {

	public AntiPotion() {
		super(CheatKeys.ANTI_POTION, CheatCategory.COMBAT, Materials.POTION, EmptyData::new, CheatDescription.HEALTH);
	}

	@EventListener
	public void onRegen(PlayerRegainHealthEvent e) {
		Player p = e.getPlayer();
		NegativityPlayer np = NegativityPlayer.getNegativityPlayer(p);
		boolean hasPotion = false;
		for (PotionEffect pe : p.getActivePotionEffect()) {
			if (pe.getType().equals(PotionEffectType.POISON) || pe.getType().equals(PotionEffectType.BLINDNESS)
					|| pe.getType().equals(PotionEffectType.WITHER)
					|| pe.getType().equals(PotionEffectType.SLOW_MINING)
					|| pe.getType().equals(PotionEffectType.WEAKNESS)
					|| pe.getType().equals(PotionEffectType.NAUSEA)
					|| pe.getType().equals(PotionEffectType.HUNGER)) {
				hasPotion = true;
				// keep at most one entry per effect type (regen fires every 0.5-4s while the
				// effect runs: adding blindly duplicates it dozens of times per potion), and
				// keep the freshest snapshot so a restore applies the current remaining duration
				np.potionEffects.removeIf(other -> other.getType().equals(pe.getType()));
				np.potionEffects.add(pe);
			}
		}
		if (hasPotion)
			np.flyingReason = FlyingReason.POTION;
		else
			np.flyingReason = FlyingReason.REGEN;
	}

	@EventListener
	public void onDeath(PlayerDeathEvent e) {
		// death clears every effect in vanilla: the remembered ones are obsolete
		// (also the only purge point — a legit player never triggers the set-back restore)
		NegativityPlayer.getNegativityPlayer(e.getPlayer()).potionEffects.clear();
	}
	
	@EventListener
	public void onProjectileHit(ProjectileHitEvent e) {
		if(!e.getEntity().getType().equals(EntityType.SPLASH_POTION))
			return;
		Location loc = e.getEntity().getLocation();
		for(Player p : Adapter.getAdapter().getOnlinePlayers()){
			if(loc.getWorld().equals(p.getLocation().getWorld()))
				if(loc.distance(p.getLocation()) < 9)
					NegativityPlayer.getNegativityPlayer(p).flyingReason = FlyingReason.POTION;
		}
	}
}
