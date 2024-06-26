package com.nisovin.magicspells.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Spell;

import org.jetbrains.annotations.NotNull;

public abstract class SpellEvent extends Event implements IMagicSpellsCompatEvent {

	protected static final HandlerList handlers = new HandlerList();

	protected Spell spell;

	protected LivingEntity caster;
	
	public SpellEvent(Spell spell, LivingEntity caster) {
		this.spell = spell;
		this.caster = caster;
	}
	
	/**
	 * Gets the spell involved in the event.
	 * @return the spell
	 */
	public Spell getSpell() {
		return spell;
	}
	
	/**
	 * Gets the player casting the spell.
	 * @return the casting player
	 */
	public LivingEntity getCaster() {
		return caster;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
}
