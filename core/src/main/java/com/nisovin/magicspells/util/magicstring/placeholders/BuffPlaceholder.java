package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class BuffPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <buff:[caster,target,name,uuid]:(spell):[now]:[precision]>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'buff' placeholder.");
		String ownerName = "caster";
		String spellName = args.get(0);
		boolean isNow = argAtIndexEquals(args, 1, "now");
		int precision = parsePrecisionAtArg(args, 2);
		// Check if precision was set previously.
		if (!isNow && precision < 0) precision = parsePrecisionAtArg(args, 1);

		BuffSpell buffSpell = resolveBuffSpell(spellName);
		if (buffSpell == null) {
			if (args.size() < 2) {
				throw new MagicException("Spell specified in 'buff' does not exist or is not a buff spell: " + spellName);
			}
			ownerName = spellName;
			spellName = args.get(1);
			isNow = argAtIndexEquals(args, 2, "now");

			buffSpell = resolveBuffSpell(spellName);
			if (buffSpell == null) {
				throw new MagicException("Spell specified in 'buff' does not exist or is not a buff spell: " + spellName);
			}
			precision = parsePrecisionAtArg(args, 3);
			// Check if precision was set previously.
			if (!isNow && precision < 0) precision = parsePrecisionAtArg(args, 2);
		}
		Player owner = getPlayer(ownerName, caster, target);

		double duration = 0;
		if (!isNow) duration = buffSpell.getDuration();
		else if (owner != null) duration = buffSpell.getDuration(owner);
		return setPrecision(duration, precision);
	}

	private static BuffSpell resolveBuffSpell(String spellName) {
		return MagicSpells.getSpellByInternalName(spellName) instanceof BuffSpell buffSpell ? buffSpell : null;
	}

}
