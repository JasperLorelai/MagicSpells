package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class SelectedSpellPlaceholder extends PlaceholderFunction {

	/*
	 * (required) & [optional]
	 * <selected_spell:[caster,target,name,uuid]:[displayed]>
	 */
	@Override
	public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
		String ownerName = "caster";
		boolean isDisplayed = argAtIndexEquals(args, 0, "displayed");
		if (args != null && args.size() > 1) {
			ownerName = args.get(0);
			isDisplayed = argAtIndexEquals(args, 1, "displayed");
		}
		Player owner = getPlayer(ownerName, caster, target);
		if (owner == null) return "";
		ItemStack heldItem = owner.getInventory().getItemInMainHand();
		Spell spell = MagicSpells.getSpellbook(owner).getActiveSpell(heldItem);
		if (spell == null) return "";
		return isDisplayed ? spell.getName() : spell.getInternalName();
	}

}
