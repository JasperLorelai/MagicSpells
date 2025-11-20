package com.nisovin.magicspells.util;

import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;

import org.jetbrains.annotations.NotNull;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.meta.Damageable;

import com.nisovin.magicspells.Perm;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.handlers.MoneyHandler;
import com.nisovin.magicspells.mana.ManaChangeReason;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import com.nisovin.magicspells.util.managers.VariableManager;

public class SpellReagents {

	private Set<ReagentItem> items;
	private int mana;
	private int hunger;
	private int experience;
	private int levels;
	private int durability;
	private float money;
	private double health;
	private Map<String, Double> variables;

	public SpellReagents() {
	}

	public SpellReagents(SpellReagents other) {
		if (other.items != null) {
			items = new HashSet<>();
			other.items.forEach(item -> items.add(item.clone()));
		}
		mana = other.mana;
		health = other.health;
		hunger = other.hunger;
		experience = other.experience;
		levels = other.levels;
		durability = other.durability;
		money = other.money;
		if (other.variables != null) {
			variables = new HashMap<>();
			variables.putAll(other.variables);
		}
	}

	public Set<ReagentItem> getItems() {
		return items;
	}

	public ReagentItem[] getItemsAsArray() {
		if (items == null || items.isEmpty()) return null;
		ReagentItem[] arr = new ReagentItem[items.size()];
		arr = items.toArray(arr);
		return arr;
	}

	public void setItems(Collection<ReagentItem> newItems) {
		if (newItems == null || newItems.isEmpty()) items = null;
		else items = new HashSet<>(newItems);
	}

	public void setItems(ReagentItem[] newItems) {
		if (newItems == null || newItems.length == 0) items = null;
		else items = new HashSet<>(Arrays.asList(newItems));
	}

	public void addItem(MagicItemData data, int amount) {
		addItem(new ReagentItem(data, amount));
	}

	public void addItem(ReagentItem item) {
		if (items == null) items = new HashSet<>();
		items.add(item);
	}

	public int getMana() {
		return mana;
	}

	public void setMana(int newMana) {
		mana = newMana;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double newHealth) {
		health = newHealth;
	}

	public int getHunger() {
		return hunger;
	}

	public void setHunger(int newHunger) {
		hunger = newHunger;
	}

	public int getExperience() {
		return experience;
	}

	public void setExperience(int newExperience) {
		experience = newExperience;
	}

	public int getLevels() {
		return levels;
	}

	public void setLevels(int newLevels) {
		levels = newLevels;
	}

	public int getDurability() {
		return durability;
	}

	public void setDurability(int newDurability) {
		durability = newDurability;
	}

	public float getMoney() {
		return money;
	}

	public void setMoney(float newMoney) {
		money = newMoney;
	}

	public Map<String, Double> getVariables() {
		return variables;
	}

	public void addVariable(String var, double val) {
		if (variables == null) variables = new HashMap<>();
		variables.put(var, val);
	}

	public void setVariables(Map<String, Double> newVariables) {
		if (newVariables == null || newVariables.isEmpty()) variables = null;
		else {
			variables = new HashMap<>();
			variables.putAll(newVariables);
		}
	}

	@Override
	public SpellReagents clone() {
		SpellReagents other = new SpellReagents();
		if (items != null) {
			other.items = new HashSet<>();
			for (ReagentItem item : items) {
				other.items.add(item.clone());
			}
		}
		other.mana = mana;
		other.health = health;
		other.hunger = hunger;
		other.experience = experience;
		other.levels = levels;
		other.durability = durability;
		other.money = money;
		if (variables != null) {
			other.variables = new HashMap<>();
			other.variables.putAll(variables);
		}
		return other;
	}

	public SpellReagents multiply(float x) {
		SpellReagents other = new SpellReagents();
		if (items != null) {
			other.items = new HashSet<>();
			for (ReagentItem item : items) {
				ReagentItem i = item.clone();
				i.setAmount(Math.round(i.getAmount() * x));
				other.items.add(i);
			}
		}
		other.mana = Math.round(mana * x);
		other.health = health * x;
		other.hunger = Math.round(hunger * x);
		other.experience = Math.round(experience * x);
		other.levels = Math.round(levels * x);
		other.durability = Math.round(durability * x);
		other.money = money * x;
		if (variables != null) {
			other.variables = new HashMap<>();
			for (Map.Entry<String, Double> entry : variables.entrySet()) {
				other.variables.put(entry.getKey(), entry.getValue() * x);
			}
		}
		return other;
	}

	/**
	 * Checks if an entity has the specified reagents.
	 * @param livingEntity the living entity to check
	 * @return true if the entity has all the reagents
	 */
	public boolean hasReagents(LivingEntity livingEntity) {
		if (Perm.NO_REAGENTS.has(livingEntity)) return true;

		if (livingEntity instanceof Player player) {
			if (mana > 0 && (MagicSpells.getManaHandler() == null || !MagicSpells.getManaHandler().hasMana(player, mana))) return false;

			if (hunger > 0 && player.getFoodLevel() < hunger) return false;

			if (experience > 0 && experience > player.calculateTotalExperiencePoints()) return false;

			if (levels > 0 && player.getLevel() < levels) return false;

			if (money > 0) {
				MoneyHandler handler = MagicSpells.getMoneyHandler();
				if (handler == null || !handler.hasMoney(player, money)) {
					return false;
				}
			}

			if (variables != null) {
				VariableManager manager = MagicSpells.getVariableManager();
				if (manager == null) return false;
				for (Map.Entry<String, Double> var : variables.entrySet()) {
					double val = var.getValue();
					if (val > 0 && manager.getValue(var.getKey(), player) < val) return false;
				}
			}
		}

		if (health > 0 && livingEntity.getHealth() <= health) return false;

		if (durability > 0) {
			EntityEquipment equipment = livingEntity.getEquipment();
			if (equipment == null) return false;
			ItemStack inHand = equipment.getItemInMainHand();
			if (!(inHand.getItemMeta() instanceof Damageable damageable)) return false;
			if (damageable.getDamage() >= inHand.getType().getMaxDurability()) return false;
		}

		if (items != null) {
			for (ReagentItem item : items) {
				if (item == null) continue;
				if (InventoryUtil.inventoryContains(livingEntity, item)) continue;
				return false;
			}
		}

		return true;
	}

	/**
	 * Removes the specified reagents from the entity's inventory.
	 * This does not check if the entity has the reagents, use {@link #hasReagents} for that.
	 */
	public void removeReagents(LivingEntity livingEntity) {
		if (Perm.NO_REAGENTS.has(livingEntity)) return;

		if (items != null) {
			for (ReagentItem item : items) {
				if (item == null) continue;
				InventoryUtil.removeFromInventory(livingEntity, item);
			}
		}

		if (livingEntity instanceof Player player) {
			if (mana != 0) MagicSpells.getManaHandler().addMana(player, -mana, ManaChangeReason.SPELL_COST);

			if (hunger != 0) player.setFoodLevel(Math.clamp(player.getFoodLevel() - hunger, 0, 20));

			if (experience != 0) Util.addExperience(player, -experience);

			if (money != 0) {
				MoneyHandler handler = MagicSpells.getMoneyHandler();
				if (handler != null) {
					if (money > 0) handler.removeMoney(player, money);
					else handler.addMoney(player, -money);
				}
			}

			if (levels != 0) player.setLevel(Math.max(player.getLevel() - levels, 0));

			if (variables != null) {
				VariableManager manager = MagicSpells.getVariableManager();
				if (manager != null) {
					for (Map.Entry<String, Double> var : variables.entrySet()) {
						manager.set(var.getKey(), player, manager.getValue(var.getKey(), player) - var.getValue());
					}
				}
			}
		}

		if (health != 0) livingEntity.setHealth(Math.clamp(livingEntity.getHealth() - health, 0, Util.getMaxHealth(livingEntity)));

		if (durability != 0) {
			EntityEquipment eq = livingEntity.getEquipment();

			if (eq != null) {
				ItemStack item = eq.getItemInMainHand();
				ItemMeta meta = item.getItemMeta();

				int maxDurability = item.getType().getMaxDurability();
				if (maxDurability > 0 && meta instanceof Damageable damageable) {
					damageable.setDamage(Math.clamp(damageable.getDamage() + durability, 0, maxDurability));
					item.setItemMeta(meta);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "SpellReagents:["
			+ "items=" + items
			+ ",mana=" + mana
			+ ",health=" + health
			+ ",hunger=" + hunger
			+ ",experience=" + experience
			+ ",levels=" + levels
			+ ",durability=" + durability
			+ ",money=" + money
			+ ",variables=" + variables
			+ ']';
	}

	public static class ReagentItem {

		@NotNull
		private MagicItemData magicItemData;

		private int amount;

		public ReagentItem(@NotNull MagicItemData magicItemData, int amount) {
			this.magicItemData = magicItemData.withIgnoredAmount();
			this.amount = amount;
		}

		@NotNull
		public MagicItemData getMagicItemData() {
			return magicItemData;
		}

		public int getAmount() {
			return amount;
		}

		public void setItemData(@NotNull MagicItemData magicItemData) {
			this.magicItemData = magicItemData;
		}

		public void setAmount(int amount) {
			this.amount = amount;
		}

		@Override
		public ReagentItem clone() {
			return new ReagentItem(magicItemData.clone(), amount);
		}

	}

}
