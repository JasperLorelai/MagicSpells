package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EntityEquipment;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

@Name("wearingprecise")
public class WearingPreciseCondition extends Condition {

	private MagicItemData itemData = null;
	
	@Override
	public boolean initialize(@NotNull String var) {
		itemData = MagicItems.getMagicItemDataFromString(var);
		return itemData != null;
	}
	
	@Override
	public boolean check(LivingEntity caster) {
		return checkInventory(caster);
	}
	
	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return checkInventory(target);
	}
	
	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

	private boolean checkInventory(LivingEntity target) {
		EntityEquipment eq = target.getEquipment();
		if (eq == null) return false;

		return checkItem(eq.getHelmet())
			|| checkItem(eq.getChestplate())
			|| checkItem(eq.getLeggings())
			|| checkItem(eq.getBoots());
	}

	private boolean checkItem(ItemStack item) {
		return item != null && itemData.matches(item);
	}
	
}
