package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class CooldownPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <cooldown:[caster,target,name,uuid]:(spell):[now]:[precision]>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'cooldown' placeholder.");
		String ownerName = "caster";
		String spellName = args.get(0);
		boolean isNow = argAtIndexEquals(args, 1, "now");
		int precision = parsePrecisionAtArg(args, 2);
		// Check if precision was set previously.
		if (!isNow && precision < 0) precision = parsePrecisionAtArg(args, 1);

		Spell spell = MagicSpells.getSpellByInternalName(spellName);
		// Check if the first arg might be owner.
		if (spell == null) {
			if (args.size() < 2) throw new MagicException("Spell specified in 'cooldown' does not exist: " + spellName);
			ownerName = spellName;
			spellName = args.get(1);

			spell = MagicSpells.getSpellByInternalName(spellName);
			if (spell == null) throw new MagicException("Spell specified in 'cooldown' does not exist: " + spellName);
			isNow = argAtIndexEquals(args, 2, "now");
			precision = parsePrecisionAtArg(args, 3);
			// Check if precision was set previously.
			if (!isNow && precision < 0) precision = parsePrecisionAtArg(args, 2);
		}
		Player owner = getPlayer(ownerName, caster, target);

		double cooldown = 0;
		if (!isNow) cooldown = spell.getCooldown();
		else if (owner != null) cooldown = spell.getCooldown(owner);
		return setPrecision(cooldown, precision);
	}

}
