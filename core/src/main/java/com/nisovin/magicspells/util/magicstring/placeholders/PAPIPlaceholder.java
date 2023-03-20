package com.nisovin.magicspells.util.magicstring.placeholders;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import me.clip.placeholderapi.PlaceholderAPI;

import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

import java.util.List;

public class PAPIPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <papi:[caster,target,name,uuid]:(placeholder)>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'papi' placeholder.");
		String ownerName = "caster";
		String placeholder = args.get(0);
		if (args.size() > 1) {
			ownerName = placeholder;
			placeholder = args.get(1);
		}
		Player owner = getPlayer(ownerName, caster, target);
		String result = PlaceholderAPI.setBracketPlaceholders(owner, placeholder);
		result = PlaceholderAPI.setPlaceholders(owner, result);
		return result;
	}

}
