package com.elikill58.negativity.minestom.impl.entity;

import java.util.List;

import com.elikill58.negativity.api.entity.SplashPotion;
import com.elikill58.negativity.api.potion.PotionEffect;
import com.elikill58.negativity.api.potion.PotionEffectType;

import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.Entity;
import net.minestom.server.potion.CustomPotionEffect;

public class MinestomSplashPotion extends MinestomEntity<Entity> implements SplashPotion {
	
	public MinestomSplashPotion(Entity entity) {
		super(entity);
	}

	@Override
	public List<PotionEffect> getEffects() {
		return entity.get(DataComponents.POTION_CONTENTS).customEffects().stream().map(this::convert).toList();
	}
	
	private PotionEffect convert(CustomPotionEffect pe) {
		return new PotionEffect(PotionEffectType.forId(pe.id().name()), pe.duration(), pe.amplifier());
	}
}
