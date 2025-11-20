package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;

@Name("offhandprecise")
public class OffHandPreciseCondition extends Condition {

	private MagicItemData itemData = null;
	
	@Override
	public boolean initialize(@NotNull String var) {
		itemData = MagicItems.getMagicItemDataFromString(var);
		return itemData != null;
	}

	@Override
	public boolean check(LivingEntity caster) {
		return offHand(caster);
	}
	
	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return offHand(target);
	}
	
	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}
	
	private boolean offHand(LivingEntity target) {
		EntityEquipment equipment = target.getEquipment();
		if (equipment == null) return false;
		return itemData.matches(equipment.getItemInOffHand());
	}

}
