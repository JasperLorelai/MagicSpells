package com.nisovin.magicspells.variables.meta;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.SpellUtil;
import com.nisovin.magicspells.variables.variabletypes.MetaVariable;

public class FoodLevelVariable extends MetaVariable {

	@Override
	public double getValue(String player) {
		Player p = Bukkit.getPlayerExact(player);
		if (p != null) return p.getFoodLevel();
		return 0;
	}
	
	@Override
	public void set(String player, double amount) {
		Player p = Bukkit.getPlayerExact(player);
		if (p == null) return;

		p.setFoodLevel((int) amount);
		SpellUtil.updateManaBar(p);
	}

}
