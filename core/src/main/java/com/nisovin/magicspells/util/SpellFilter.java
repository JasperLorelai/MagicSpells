package com.nisovin.magicspells.util;

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.ArrayList;

import com.nisovin.magicspells.Spell;

import org.bukkit.configuration.ConfigurationSection;

public class SpellFilter {

	private Set<String> allowedSpells = null;
	private Set<String> blacklistedSpells = null;
	private Set<String> allowedTags = null;
	private Set<String> disallowedTags = null;
	
	private final boolean defaultReturn;
	private boolean emptyFilter = false;
	
	public SpellFilter(List<String> allowedSpells, List<String> blacklistedSpells, List<String> allowedTags, List<String> disallowedTags) {
		
		// Initialize the collections
		if (allowedSpells != null && !allowedSpells.isEmpty()) this.allowedSpells = new HashSet<>(allowedSpells);
		if (blacklistedSpells != null && !blacklistedSpells.isEmpty()) this.blacklistedSpells = new HashSet<>(blacklistedSpells);
		if (allowedTags != null && !allowedTags.isEmpty()) this.allowedTags = new HashSet<>(allowedTags);
		if (disallowedTags != null && !disallowedTags.isEmpty()) this.disallowedTags = new HashSet<>(disallowedTags);

		// Determine the default outcome if nothing catches
		defaultReturn = determineDefaultValue();
	}
	
	private boolean determineDefaultValue() {
		// This means there is a tag whitelist check
		if (allowedTags != null) return false;
		
		// If there is a spell whitelist check
		if (allowedSpells != null) return false;
		
		// This means there is a tag blacklist
		if (disallowedTags != null) return true;
		
		// If there is a spell blacklist
		if (blacklistedSpells != null) return true;
		
		// If all the collections are null, then there is no filter
		emptyFilter = true;
		return true;
	}
	
	public boolean check(Spell spell) {
		// Can't do anything if null anyway
		if (spell == null) return false;
		
		// Quick check to exit early if possible
		if (emptyFilter) return true;
		
		// Is it whitelisted explicitly?
		if (allowedSpells != null && allowedSpells.contains(spell.getInternalName())) return true;
		
		// Is it blacklisted?
		if (blacklistedSpells != null && blacklistedSpells.contains(spell.getInternalName())) return false;
		
		// Does it have a blacklisted tag?
		if (disallowedTags != null) {
			for (String tag : disallowedTags) {
				if (spell.hasTag(tag)) return false;
			}
		}
		
		// Does it have a whitelisted tag?
		if (allowedTags != null) {
			for (String tag : allowedTags) {
				if (spell.hasTag(tag)) return true;
			}
		}
		
		return defaultReturn;
	}

	/**
	 * Create a {@link SpellFilter} instance out of a configuration section.
	 * @param config Reads from the following keys: "spells", "denied-spells", "spell-tags", and "denied-spell-tags".
	 * @param path Path for the keys to be read from. If the path is set to something like "filter", the keys will
	 *             be read from the passed config section under the "filter" section.
	 */
	public static SpellFilter fromConfig(ConfigurationSection config, String path) {
		return fromConfigKeys(config, path, "", "", "", "");
	}

	/**
	 * Create a {@link SpellFilter} instance out of a legacy configuration section.
	 * @param config Reads from the normal keys as well as the following keys:
	 *               "allowed-spells", "disallowed-spells","allowed-spell-tags", and "disallowed-spell-tags".
	 * @param path Path for the keys to be read from. If the path is set to something like "filter", the keys will
	 *             be read from the passed config section under the "filter" section.
	 */
	public static SpellFilter fromLegacyConfig(ConfigurationSection config, String path) {
		return fromConfigKeys(config, path,
				"allowed-spells",
				"disallowed-spells",
				"allowed-spell-tags",
				"disallowed-spell-tags"
		);
	}

	private static SpellFilter fromConfigKeys(ConfigurationSection config, String path, String keySpells, String keyDeniedSpells, String keyTags, String deniedTags) {
		if (!path.isEmpty() && !path.endsWith(".")) path += ".";

		List<String> spells = merge(config, path, "spells", keySpells);
		List<String> deniedSpells = merge(config, path, "denied-spells", keyDeniedSpells);
		List<String> spellTags = merge(config, path, "spell-tags", keyTags);
		List<String> deniedSpellTags = merge(config, path, "denied-spell-tags", deniedTags);

		return new SpellFilter(spells, deniedSpells, spellTags, deniedSpellTags);
	}

	private static List<String> merge(ConfigurationSection config, String path, String key, String legacyKey) {
		List<String> list = config.getStringList(path + key);
		if (legacyKey == null || legacyKey.isEmpty()) return list;
		list.addAll(config.getStringList(path + legacyKey));
		return list;
	}

	/**
	 * Create a {@link SpellFilter} instance out of a formatted string.
	 * @param string Follows format: allowedSpell, !disallowedSpell, tag:allowedTag, !tag:disallowedTag
	 */
	public static SpellFilter fromString(String string) {
		List<String> spells = new ArrayList<>();
		List<String> deniedSpells = new ArrayList<>();
		List<String> spellTags = new ArrayList<>();
		List<String> deniedSpellTags = new ArrayList<>();

		String[] split = string.split(",");
		for (String s : split) {
			boolean denied = false;
			s = s.trim();

			if (s.startsWith("!")) {
				s = s.substring(1);
				denied = true;
			}

			if (s.toLowerCase().startsWith("tag:")) {
				s = s.substring(4);
				if (denied) deniedSpellTags.add(s);
				else spellTags.add(s);
			} else {
				if (denied) deniedSpells.add(s);
				else spells.add(s);
			}
		}

		return new SpellFilter(spells, deniedSpells, spellTags, deniedSpellTags);
	}

}
