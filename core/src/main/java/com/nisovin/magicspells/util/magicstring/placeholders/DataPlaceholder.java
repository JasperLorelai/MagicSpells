package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.data.DataLivingEntity;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class DataPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <data:[caster,target,name,uuid]:(dataElement)>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'data' placeholder.");
		String ownerName = "caster";
		String data = args.get(0);
		if (args.size() > 1) {
			ownerName = data;
			data = args.get(1);
		}
		LivingEntity owner = getEntity(ownerName, caster, target);
		if (owner == null) return "";
		Function<? super LivingEntity, String> dataFunction = DataLivingEntity.getDataFunction(data);
		return dataFunction == null ? "" : dataFunction.apply(owner);
	}

}
