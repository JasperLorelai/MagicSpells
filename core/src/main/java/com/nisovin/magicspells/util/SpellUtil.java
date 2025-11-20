package com.nisovin.magicspells.util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.function.Predicate;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaSystem;

public class SpellUtil {
	
	// Currently will only work with direct permission nodes, doesn't handle child nodes yet
	// NOTE: allSpells should be a thread safe collection for read access
	public static Collection<Spell> getSpellsByPermissionNames(final Collection<Spell> allSpells, final Set<String> names) {
		Predicate<Spell> predicate = spell -> names.contains(spell.getPermissionName());
		return getSpellsByX(allSpells, predicate);
	}
	
	// NOTE: allSpells should be a thread safe collection for read access
	// NOTE: streams do work for making the collection thread safe
	public static Collection<Spell> getSpellsByX(final Collection<Spell> allSpells, final Predicate<Spell> predicate) {
		return allSpells
			.parallelStream()
			.filter(predicate)
			.collect(Collectors.toCollection(ArrayList::new));
	}

	private static SpellReagents createReagents(SpellReagents.ReagentItem[] items, double health, int mana, int hunger, int experience, int levels, int durability, float money, Map<String, Double> variables) {
		SpellReagents reagents = new SpellReagents();

		Arrays.stream(items).forEach(reagents::addItem);
		reagents.setHealth(health);
		reagents.setMana(mana);
		reagents.setHunger(hunger);
		reagents.setExperience(experience);
		reagents.setLevels(levels);
		reagents.setDurability(durability);
		reagents.setMoney(money);
		variables.forEach(reagents::addVariable);

		return reagents;
	}

	/**
	 * Checks if an entity has the specified reagents
	 * @param entity the living entity to check
	 * @param reagents the reagents to check for
	 * @return true if the entity has all the reagents
	 * @deprecated Use {@link SpellReagents#hasReagents}
	 */
	@Deprecated(forRemoval = true)
	public static boolean hasReagents(LivingEntity entity, SpellReagents reagents) {
		return reagents.hasReagents(entity);
	}

	/**
	 * Checks if an entity has the specified reagents
	 * @param entity the living entity to check
	 * @return true if the entity has all the reagents
	 * @deprecated Use {@link SpellReagents#hasReagents}
	 */
	@Deprecated(forRemoval = true)
	public static boolean hasReagents(LivingEntity entity, SpellReagents.ReagentItem[] items, double health, int mana, int hunger, int experience, int levels, int durability, float money, Map<String, Double> variables) {
		return hasReagents(entity, createReagents(items, health, mana, hunger, experience, levels, durability, money, variables));
	}

	/**
	 * @deprecated Use {@link SpellReagents#removeReagents}
	 */
	@Deprecated(forRemoval = true)
	public static void removeReagents(LivingEntity entity, SpellReagents reagents) {
		reagents.removeReagents(entity);
	}

	/**
	 * Removes the specified reagents from the entity's inventory.
	 * This does not check if the entity has the reagents, use {@link SpellReagents#hasReagents} for that.
	 *
	 * @deprecated Use {@link SpellReagents#removeReagents}
	 */
	@Deprecated(forRemoval = true)
	public static void removeReagents(LivingEntity entity, SpellReagents.ReagentItem[] items, double health, int mana, int hunger, int experience, int levels, int durability, float money, Map<String, Double> variables) {
		removeReagents(entity, createReagents(items, health, mana, hunger, experience, levels, durability, money, variables));
	}

	public static void updateManaBar(Player player) {
		if (!(MagicSpells.getManaHandler() instanceof ManaSystem system)) return;
		if (!system.usingHungerBar()) return;
		MagicSpells.scheduleDelayedTask(() -> system.showMana(player), 1);
	}
	
}
