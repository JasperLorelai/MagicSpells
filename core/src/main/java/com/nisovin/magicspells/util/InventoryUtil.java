package com.nisovin.magicspells.util;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.inventory.InventoryType;

import com.nisovin.magicspells.util.magicitems.MagicItemData;

public class InventoryUtil {

	private static final String SERIALIZATION_KEY_SIZE = "size";
	private static final String SERIALIZATION_KEY_TYPE = "type";
	private static final String SERIALIZATION_KEY_TITLE = "title";
	private static final String SERIALIZATION_KEY_CONTENTS = "contents";

	/*
	 * type: INVENTORY_TYPE/string
	 * size: integer
	 * title: string
	 * contents:
	 *     slot number: serialized itemstack
	 *     slot number: serialized itemstack
	 */
	public static Map<Object, Object> serializeInventoryContents(Inventory inv, InventoryView view) {
		Map<Object, Object> ret = new HashMap<>();
		ItemStack[] contents = inv.getContents();
		String inventoryType = inv.getType().name();
		int size = inv.getSize();
		String title = Util.getStringFromComponent(view.title());
		
		// A map of slot to itemstack
		Map<Object, Object> serializedContents = createContentsMap(contents);
		
		ret.put(SERIALIZATION_KEY_SIZE, size);
		ret.put(SERIALIZATION_KEY_TYPE, inventoryType);
		ret.put(SERIALIZATION_KEY_TITLE, title);
		ret.put(SERIALIZATION_KEY_CONTENTS, serializedContents);

		return ret;
	}
	
	private static Map<Object, Object> createContentsMap(ItemStack[] items) {
		Map<Object, Object> serialized = new HashMap<>();
		int maxSlot = items.length - 1;
		for (int currentSlot = 0; currentSlot <= maxSlot; currentSlot++) {
			ItemStack currentItem = items[currentSlot];
			if (currentItem == null) continue;
			serialized.put(currentSlot, currentItem.serialize());
		}
		return serialized;
	}
	
	public static Inventory deserializeInventory(Map<Object, Object> serialized) {
		String strInventoryType = (String) serialized.get(SERIALIZATION_KEY_TYPE);
		int inventorySize = (Integer) serialized.get(SERIALIZATION_KEY_SIZE);
		String title = (String) serialized.get(SERIALIZATION_KEY_TITLE);
		Inventory ret;
		if (strInventoryType.equals(InventoryType.CHEST.name())) ret = Bukkit.createInventory(null, inventorySize, Util.getMiniMessage(title));
		else ret = Bukkit.createInventory(null, InventoryType.valueOf(strInventoryType), Util.getMiniMessage(title));

		// Handle the item contents
		Map<Object, Object> serializedItems = (Map<Object, Object>) serialized.get(SERIALIZATION_KEY_CONTENTS);
		ret.setContents(deserializeContentsMap(serializedItems, inventorySize));
		
		return ret;
	}
	
	private static ItemStack[] deserializeContentsMap(Map<Object, Object> contents, int size) {
		ItemStack[] ret = new ItemStack[size];
		
		// Can we exit early?
		if (contents == null) return ret;
		
		for (int i = 0; i < size; i++) {
			Map<String, Object> serializedStack = (Map<String, Object>) contents.get(i);
			if (serializedStack == null) continue;
			ret[i] = ItemStack.deserialize(serializedStack);
		}
		
		return ret;
	}

	private static boolean itemsContains(ItemStack[] items, SpellReagents.ReagentItem item) {
		int count = 0;
		for (ItemStack inside : items) {
			if (inside.isEmpty()) continue;
			if (item.getMagicItemData().matches(inside)) count += inside.getAmount();
			if (count >= item.getAmount()) return true;
		}
		return false;
	}

	public static boolean inventoryContains(LivingEntity entity, SpellReagents.ReagentItem item) {
		if (entity instanceof InventoryHolder holder)
			return itemsContains(holder.getInventory().getContents(), item);

		EntityEquipment equipment = entity.getEquipment();
		return equipment != null && itemsContains(getEquipmentItems(equipment), item);
	}

	private static ItemStack[] removeFromItems(ItemStack[] items, MagicItemData data, int amount) {
		for (int i = 0; i < items.length; i++) {
			if (items[i] == null) continue;
			if (!data.matches(items[i])) continue;

			int invAmt = items[i].getAmount();
			if (invAmt >= amount) {
				items[i].setAmount(invAmt - amount);
				return items;
			}

			amount -= invAmt;
			items[i] = null;
		}

		return amount <= 0 ? items : null;
	}

	public static void removeFromInventory(LivingEntity entity, SpellReagents.ReagentItem item) {
		if (entity instanceof InventoryHolder holder) {
			Inventory inventory = holder.getInventory();
			ItemStack[] newContent = removeFromItems(inventory.getContents(), item.getMagicItemData(), item.getAmount());
			if (newContent != null) inventory.setContents(newContent);
			return;
		}

		EntityEquipment equipment = entity.getEquipment();
		if (equipment == null) return;
		ItemStack[] newEquipment = removeFromItems(getEquipmentItems(equipment), item.getMagicItemData(), item.getAmount());
		if (newEquipment == null) return;
		EquipmentSlot[] slots = EquipmentSlot.values();
		for (int i = 0; i < slots.length; i++) {
			equipment.setItem(slots[i], newEquipment[i]);
		}
	}

	public static ItemStack[] getEquipmentItems(EntityEquipment equipment) {
		EquipmentSlot[] slots = EquipmentSlot.values();
		ItemStack[] items = new ItemStack[slots.length];
		for (int i = 0; i < slots.length; i++) {
			items[i] = equipment.getItem(slots[i]);
		}
		return items;
	}
	
}
