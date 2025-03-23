package com.nisovin.magicspells.util.itemreader;

import java.util.List;
import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.potion.PotionType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.magicitems.MagicItemData;
import static com.nisovin.magicspells.util.magicitems.MagicItemData.MagicItemAttribute.*;

public class PotionHandler {

	public static final String POTION_EFFECT_CONFIG_NAME = POTION_EFFECTS.toString();
	public static final String POTION_TYPE_CONFIG_NAME = POTION_TYPE.toString();
	public static final String COLOR_CONFIG_NAME = COLOR.toString();

	public static void process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof PotionMeta potionMeta)) return;

		if (config.isList(POTION_EFFECT_CONFIG_NAME)) {
			potionMeta.clearCustomEffects();
			List<String> potionEffectStrings = config.getStringList(POTION_EFFECT_CONFIG_NAME);
			List<PotionEffect> potionEffects = new ArrayList<>();

			for (String potionEffect : potionEffectStrings) {
				PotionEffect eff = Util.buildPotionEffect(potionEffect, false);
				if (eff == null) continue;

				potionMeta.addCustomEffect(eff, true);
				potionEffects.add(eff);
			}

			if (!potionEffects.isEmpty()) data.setAttribute(POTION_EFFECTS, potionEffects);
		}

		if (config.isString(COLOR_CONFIG_NAME) || config.isString("potion-color")) {
			try {
				int color = Integer.parseInt(config.getString(COLOR_CONFIG_NAME, config.getString("potion-color", "")).replace("#", ""), 16);
				Color c = Color.fromRGB(color);

				potionMeta.setColor(c);
				data.setAttribute(COLOR, c);
			} catch (NumberFormatException e) {
				DebugHandler.debugNumberFormat(e);
			}
		}

		if (config.isString(POTION_TYPE_CONFIG_NAME) || config.isString("potion-data")) {
			String potionTypeString = config.getString(POTION_TYPE_CONFIG_NAME, config.getString("potion-data", ""));

			PotionType potionType = getPotionType(potionTypeString);
			if (potionType == null) {
				MagicSpells.error("Invalid potion type '" + potionTypeString + "' found while parsing magic item.");
				return;
			}

			potionMeta.setBasePotionType(potionType);
			data.setAttribute(POTION_TYPE, potionType);
		}
	}

	public static void processItemMeta(ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof PotionMeta potionMeta)) return;

		if (data.hasAttribute(POTION_EFFECTS)) {
			potionMeta.clearCustomEffects();
			((List<PotionEffect>) data.getAttribute(POTION_EFFECTS)).forEach(potionEffect -> potionMeta.addCustomEffect(potionEffect, true));
		}
		if (data.hasAttribute(COLOR)) potionMeta.setColor((Color) data.getAttribute(COLOR));
		if (data.hasAttribute(POTION_TYPE)) potionMeta.setBasePotionType((PotionType) data.getAttribute(POTION_TYPE));
	}

	public static void processMagicItemData(ItemMeta meta, MagicItemData data) {
		if (!(meta instanceof PotionMeta potionMeta)) return;

		data.setAttribute(POTION_TYPE, potionMeta.getBasePotionType());
		if (potionMeta.hasCustomEffects()) {
			List<PotionEffect> effects = potionMeta.getCustomEffects();
			if (!effects.isEmpty()) data.setAttribute(POTION_EFFECTS, effects);
		}
		if (potionMeta.hasColor()) data.setAttribute(COLOR, potionMeta.getColor());
	}

	public static PotionType getPotionType(ItemMeta meta) {
		return meta instanceof PotionMeta potionMeta ? potionMeta.getBasePotionType() : null;
	}

	public static PotionType getPotionType(String potionTypeString) {
		try {
			return PotionType.valueOf(potionTypeString.toUpperCase());
		} catch (IllegalArgumentException ignored) {
		}

		for (PotionType type : PotionType.values())
			if (type.getKey().getKey().equalsIgnoreCase(potionTypeString))
				return type;

		// Legacy support for potion data format

		String[] potionData = potionTypeString.split(" ", 2);
		if (potionData.length != 2) return null;

		String prefix;
		if (potionData[1].equalsIgnoreCase("extended")) prefix = "LONG_";
		else if (potionData[1].equalsIgnoreCase("upgraded")) prefix = "STRONG_";
		else return null;

		try {
			return PotionType.valueOf(prefix + potionData[0].toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
