package com.nisovin.magicspells.castmodifiers.conditions;

import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.EntityEquipment;

import org.jetbrains.annotations.NotNull;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.util.InventoryUtil;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;

@Name("hasitemamount")
public class HasItemAmountCondition extends OperatorCondition {

	private MagicItemData itemData;
	private int amount;
	
	@Override
	public boolean initialize(@NotNull String var) {
		String[] args = var.split(";");
		if (args.length < 2) return false;

		if (!super.initialize(var)) return false;

		try {
			amount = Integer.parseInt(args[0].substring(1));
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}

		itemData = MagicItems.getMagicItemDataFromString(args[1]);
		return itemData != null;
	}

	@Override
	public boolean check(LivingEntity caster) {
		return check(caster, caster);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		if (target == null) return false;
		if (target instanceof InventoryHolder holder) return hasItem(holder.getInventory().getContents());
		EntityEquipment equipment = target.getEquipment();
		return equipment != null && hasItem(InventoryUtil.getEquipmentItems(equipment));
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		BlockState targetState = location.getBlock().getState();
		return targetState instanceof InventoryHolder holder && hasItem(holder.getInventory().getContents());
	}

	private boolean hasItem(ItemStack[] items) {
		int counted = 0;
		for (ItemStack item : items) {
			if (item == null) continue;
			if (!itemData.matches(item)) continue;

			counted += item.getAmount();

			if (moreThan && counted > amount) return true;
			if (lessThan && counted >= amount) return false;
		}
		return compare(counted, amount);
	}

}
