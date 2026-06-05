package com.nisovin.magicspells.spells.instant;

import java.util.Map;
import java.util.List;
import java.util.HashMap;

import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EntityEquipment;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.handlers.EnchantmentHandler;

public class EnchantSpell extends InstantSpell {

	private final Map<Enchantment, Integer> enchantments = new HashMap<>();

	private final ConfigData<Boolean> safeEnchants;
	private final ConfigData<Boolean> requireSupportedItem;

	public EnchantSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		safeEnchants = getConfigDataBoolean("safe-enchants", true);
		requireSupportedItem = getConfigDataBoolean("require-supported-item", true);

		List<String> enchantList = getConfigStringList("enchantments", List.of());
		if (enchantList.isEmpty()) {
			MagicSpells.error("EnchantSpell '" + internalName + "' has no 'enchantments' defined!");
			return;
		}

		for (int i = 0; i < enchantList.size(); i++) {
			String[] splits = enchantList.get(i).split(" ", 2);
			Enchantment enchant = EnchantmentHandler.getEnchantment(splits[0]);
			if (enchant == null) {
				MagicSpells.error("EnchantSpell '" + internalName + "' has an invalid enchantment key '" + splits[0] + "' on element '#" + i + "'");
				continue;
			}

			int level = enchant.getStartLevel();
			if (splits.length > 1) {
				try {
					level = Integer.parseInt(splits[1]);
				} catch (NumberFormatException _) {
					MagicSpells.error("EnchantSpell '" + internalName + "' has an invalid enchantment level '" + splits[1] + "' on element '#" + i + "'");
					continue;
				}
			}

			enchantments.put(enchant, level);
		}
	}

	@Override
	public CastResult cast(SpellData data) {
		EntityEquipment eq = data.caster().getEquipment();
		if (eq == null) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		ItemStack item = eq.getItemInMainHand();
		if (item.isEmpty()) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		boolean safeEnchants = this.safeEnchants.get(data);
		boolean requireSupportedItem = this.requireSupportedItem.get(data);

		for (Enchantment e : enchantments.keySet())
			enchant(item, safeEnchants, requireSupportedItem, e, enchantments.get(e));

		playSpellEffects(data);

		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	private void enchant(ItemStack item, boolean safeEnchants, boolean requireSupportedItem, Enchantment enchant, int level) {
		if (level <= 0) {
			item.removeEnchantment(enchant);
			return;
		}

		if (requireSupportedItem && !enchant.canEnchantItem(item)) return;
		if (safeEnchants) level = Math.clamp(level, enchant.getStartLevel(), enchant.getMaxLevel());

		item.addUnsafeEnchantment(enchant, level);
	}

	public Map<Enchantment, Integer> getEnchantments() {
		return enchantments;
	}

}
