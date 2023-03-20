package com.nisovin.magicspells.util.magicstring.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaHandler;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

import java.util.List;

public class ManaPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <mana:[caster,target,name,uuid]:[max]>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		ManaHandler manaHandler = MagicSpells.getManaHandler();
		if (manaHandler == null) throw new MagicException("Cannot use 'mana' placeholder because Mana is disabled.");
		String ownerName = "caster";
		boolean isMax = argAtIndexEquals(args, 0, "max");
		if (args != null && args.size() > 1) {
			ownerName = args.get(0);
			isMax = argAtIndexEquals(args, 1, "max");
		}
		Player owner = getPlayer(ownerName, caster, target);
		if (owner == null) return "0";
		return (isMax ? manaHandler.getMaxMana(owner) : manaHandler.getMana(owner)) + "";
	}

}
