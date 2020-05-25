package com.nisovin.magicspells.castmodifiers;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.ManaChangeEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.events.MagicSpellsGenericPlayerEvent;

public enum ModifierType {
	
	REQUIRED(false, false, false, false, "required", "require") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!check) event.setNewAmount(event.getOldAmount());
			return check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!check) event.setCancelled(true);
			return check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!check) event.setCancelled(true);
			return check;
		}
		
	},
	
	DENIED(false, false, false, false, "denied", "deny") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setNewAmount(event.getOldAmount());
			return !check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCancelled(true);
			return !check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCancelled(true);
			return !check;
		}
		
	},
	
	POWER(false, true, false, false, "power", "empower", "multiply") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.increasePower(modifierVarFloat);
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				int gain = event.getNewAmount() - event.getOldAmount();
				gain = Math.round(gain * modifierVarFloat);
				int newAmt = event.getOldAmount() + gain;
				if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
				event.setNewAmount(newAmt);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.increasePower(modifierVarFloat);
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}
		
	},
	
	ADD_POWER(false, true, false, false, "addpower", "add") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setPower(event.getPower() + modifierVarFloat);
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				int newAmt = event.getNewAmount() + (int) modifierVarFloat;
				if (newAmt > event.getMaxMana()) newAmt = event.getMaxMana();
				if (newAmt < 0) newAmt = 0;
				event.setNewAmount(newAmt);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setPower(event.getPower() + modifierVarFloat);
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}
		
	},
	
	COOLDOWN(false, true, false, false, "cooldown") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCooldown(modifierVarFloat);
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}
		
	},
	
	REAGENTS(false, true, false, false, "reagents") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setReagents(event.getReagents().multiply(modifierVarFloat));
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}
		
	},
	
	CAST_TIME(false, false, true, false, "casttime") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) event.setCastTime(modifierVarInt);
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return true;
		}
		
	},
	
	STOP(false, false, false, false, "stop") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return !check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return !check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return !check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return !check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return !check;
		}
		
	},
	
	CONTINUE(false, false, false, false, "continue") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return check;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return check;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return check;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return check;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			return check;
		}
		
	},
	
	CAST(true, false, false, false, "cast") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getCaster(), event.getPower(), event.getSpellArgs());
			}
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getPlayer(), 1F, null);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getCaster(), 1F, null);
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell instanceof TargetedLocationSpell) ((TargetedLocationSpell) spell).castAtLocation(event.getCaster(), event.getTargetLocation(), 1F);
			}
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getPlayer(), 1F, null);
			}
			return true;
		}
		
	},
	
	CAST_INSTEAD(true, false, false, false, "castinstead") {
		
		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getCaster(), event.getPower(), event.getSpellArgs());
				event.setCancelled(true);
			}
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getPlayer(), 1F, null);

			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) {
					if (spell instanceof TargetedEntitySpell) ((TargetedEntitySpell) spell).castAtEntity(event.getCaster(), event.getTarget(), 1F);
					else spell.castSpell(event.getCaster(), SpellCastState.NORMAL, 1F, null);
				}
			}
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell instanceof TargetedLocationSpell) {
					((TargetedLocationSpell) spell).castAtLocation(event.getCaster(), event.getTargetLocation(), 1F);
					event.setCancelled(true);
				}
			}
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) {
				Spell spell = MagicSpells.getSpellByInternalName(modifierVar);
				if (spell != null) spell.cast(event.getPlayer(), 1F, null);
			}
			return true;
		}
		
	},

	VARIABLE_MODIFY(false, false, false, true, "variable") {

		class CustomData {
			public boolean targeted;
			public Variable variable;
			public String operations;
		}

		private void setVariable(CustomData customData, Player caster, Player target) {
			String math = customData.operations.replaceFirst("=", "");
			math = math.replaceAll("%a", caster.getName());
			if (target != null) math = math.replaceAll("%t", target.getName());
			Player playerToMod = customData.targeted && target != null ? target : caster;
			MagicSpells.getVariableManager().evalMath(customData.variable, math, playerToMod);
		}

		@Override
		public boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!(event.getCaster() instanceof Player)) return false;
			if (check) setVariable((CustomData) customData, (Player) event.getCaster(), null);
			return true;
		}

		@Override
		public boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) setVariable((CustomData) customData, event.getPlayer(), null);
			return true;
		}

		@Override
		public boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!(event.getCaster() instanceof Player)) return false;
			Player target = null;
			if (event.getTarget() instanceof Player) target = (Player) event.getTarget();
			if (check) setVariable((CustomData) customData, (Player) event.getCaster(), target);
			return true;
		}

		@Override
		public boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (!(event.getCaster() instanceof Player)) return false;
			if (check) setVariable((CustomData) customData, (Player) event.getCaster(), null);
			return true;
		}

		@Override
		public boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData) {
			if (check) setVariable((CustomData) customData, event.getPlayer(), null);
			return true;
		}

		@Override
		public Object buildCustomActionData(String text) {
			if (text == null || text.trim().isEmpty() || !text.contains(" ")) throw new IllegalArgumentException("action \"string\" requires arguments.");

			String[] splits = text.split(" ", 2);
			Variable variable = MagicSpells.getVariableManager().getVariable(splits[0].replaceFirst("target:", ""));
			if (variable == null) throw new IllegalArgumentException(splits[0] + " is not a defined variable!");

			CustomData ret = new CustomData();
			ret.targeted = splits[0].toLowerCase().startsWith("target:");
			ret.variable = variable;
			ret.operations = splits[1].replaceFirst("=", "").trim();
			return ret;
		}
	}

	;
	
	private String[] keys;
	private static boolean initialized = false;
	
	private boolean usesCustomData;
	private boolean usesModifierVar;
	private boolean usesModifierVarFloat;
	private boolean usesModifierVarInt;
	
	ModifierType(boolean usesModVarString, boolean usesModVarFloat, boolean usesModVarInt, boolean usesCustomData, String... keys) {
		this.keys = keys;
		this.usesCustomData = usesCustomData;
		this.usesModifierVar = usesModVarString;
		this.usesModifierVarFloat = usesModVarFloat;
		this.usesModifierVarInt = usesModVarInt;
	}
	
	public boolean usesCustomData() {
		return usesCustomData;
	}
	
	public boolean usesModifierString() {
		return usesModifierVar;
	}
	
	public boolean usesModifierFloat() {
		return usesModifierVarFloat;
	}
	
	public boolean usesModifierInt() {
		return usesModifierVarInt;
	}
	
	public abstract boolean apply(SpellCastEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData);
	public abstract boolean apply(ManaChangeEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData);
	public abstract boolean apply(SpellTargetEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData);
	public abstract boolean apply(SpellTargetLocationEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData);
	public abstract boolean apply(MagicSpellsGenericPlayerEvent event, boolean check, String modifierVar, float modifierVarFloat, int modifierVarInt, Object customData);
	
	public Object buildCustomActionData(String text) {
		return null;
	}
	
	static Map<String, ModifierType> nameMap;
	
	static void initialize() {
		nameMap = new HashMap<>();
		for (ModifierType type : ModifierType.values()) {
			for (String key : type.keys) {
				nameMap.put(key.toLowerCase(), type);
			}
		}
		initialized = true;
	}
	
	public static ModifierType getModifierTypeByName(String name) {
		if (!initialized) initialize();
		return nameMap.get(name.toLowerCase());
	}
	
}
