package com.nisovin.magicspells.util.magicitems;

import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

public record MagicItem(@NotNull ItemStack itemStack, @NotNull MagicItemData magicItemData) {

	@NotNull
	public ItemStack getItemStack() {
		return itemStack;
	}

	@NotNull
	public MagicItemData getMagicItemData() {
		return magicItemData;
	}

	@Override
	public MagicItem clone() {
		return new MagicItem(itemStack.clone(), magicItemData.clone());
	}

}
