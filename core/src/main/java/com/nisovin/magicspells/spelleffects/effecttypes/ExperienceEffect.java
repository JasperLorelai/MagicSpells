package com.nisovin.magicspells.spelleffects.effecttypes;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.SpellEffect;
import com.nisovin.magicspells.util.config.ConfigDataUtil;

@Name("experience")
public class ExperienceEffect extends SpellEffect {

	private ConfigData<Integer> level;
	private ConfigData<Integer> progress;

	private ConfigData<Boolean> reset;

	@Override
	protected void loadFromConfig(ConfigurationSection config) {
		level = ConfigDataUtil.getInteger(config, "level");
		progress = ConfigDataUtil.getInteger(config, "progress");

		reset = ConfigDataUtil.getBoolean(config, "reset", false);
	}

	@Override
	protected Runnable playEffectEntity(Entity entity, SpellData data) {
		if (!(entity instanceof Player player)) return null;

		boolean reset = this.reset.get(data);

		Integer l = this.level.get(data);
		int level = l == null || reset ? player.getLevel() : l;

		Integer p = this.progress.get(data);
		float progress = p == null || reset ? player.getExp() : p / 100f;

		try {
			player.sendExperienceChange(progress, level);
		} catch (IllegalArgumentException e) {
			// debug
		}

		return null;
	}
}

