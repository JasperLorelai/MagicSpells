package com.nisovin.magicspells.util;

import java.util.Map;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.inventory.*;
import org.bukkit.event.inventory.InventoryType;

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

	public static boolean inventoryContains(EntityEquipment equipment, SpellReagents.ReagentItem item) {
		if (equipment == null) return false;
		int count = 0;
		for (ItemStack itemInside : getEquipmentItems(equipment)) {
			if (itemInside.isEmpty()) continue;
			if (item.getMagicItemData().matches(itemInside)) count += itemInside.getAmount();
			if (count >= item.getAmount()) return true;
		}
		return false;
	}

	public static boolean inventoryContains(Inventory inventory, SpellReagents.ReagentItem item) {
		if (inventory == null) return false;
		int count = 0;
		ItemStack[] items = inventory.getContents();
		for (ItemStack itemStack : items) {
			if (itemStack == null) continue;
			if (item.getMagicItemData().matches(itemStack)) count += itemStack.getAmount();
			if (count >= item.getAmount()) return true;
		}
		return false;
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
