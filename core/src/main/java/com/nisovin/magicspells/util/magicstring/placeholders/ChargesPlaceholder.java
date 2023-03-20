package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class ChargesPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <charges:[caster,target,name,uuid]:(spell):[consumed]>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'charges' placeholder.");
		String ownerName = "caster";
		String spellName = args.get(0);
		boolean isConsumed = argAtIndexEquals(args, 1, "consumed");

		Spell spell = MagicSpells.getSpellByInternalName(spellName);
		if (spell == null) {
			if (args.size() < 2) throw new MagicException("Spell specified in 'charges' does not exist: " + spellName);
			ownerName = spellName;
			spellName = args.get(1);
			isConsumed = argAtIndexEquals(args, 2, "consumed");
		}
		spell = MagicSpells.getSpellByInternalName(spellName);
		if (spell == null) throw new MagicException("Spell specified in 'charges' does not exist: " + spellName);
		Player owner = getPlayer(ownerName, caster, target);

		int charges = 0;
		if (!isConsumed) charges = spell.getCharges();
		else if (owner != null) charges = spell.getCharges(owner);
		return charges + "";
	}

}
