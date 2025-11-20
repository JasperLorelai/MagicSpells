package com.nisovin.magicspells.castmodifiers.conditions;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

@Name("usingitem")
public class UsingItemCondition extends Condition {

	private MagicItemData itemData;

	@Override
	public boolean initialize(@NotNull String var) {
		if (var.isEmpty()) return true;
		itemData = MagicItems.getMagicItemDataFromString(var);
		return itemData != null;
	}

	@Override
	public boolean check(LivingEntity caster) {
		return checkUsing(caster);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return checkUsing(target);
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

	private boolean checkUsing(LivingEntity target) {
		if (!target.hasActiveItem()) return false;
		return itemData.matches(target.getActiveItem());
	}

}
