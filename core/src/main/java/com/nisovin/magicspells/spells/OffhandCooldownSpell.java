package com.nisovin.magicspells.spells;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.magicitems.MagicItems;

public class OffhandCooldownSpell extends InstantSpell {

	private final List<Player> players = new ArrayList<>();

	private ItemStack item;

	private Spell spellToCheck;
	private String spellToCheckName;

	public OffhandCooldownSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		if (isConfigString("item")) {
			item = MagicItems.getItemFromString(getConfigString("item", "stone"));
		} else if (isConfigSection("item")) {
			item = MagicItems.getItemFromSection(getConfigSection("item"));
		}

		spellToCheckName = getConfigString("spell", "");
	}
	
	@Override
	public void initialize() {
		super.initialize();

		spellToCheck = MagicSpells.getSpellByInternalName(spellToCheckName);
		spellToCheckName = null;

		if (spellToCheck == null || item == null) return;
		
		MagicSpells.scheduleRepeatingTask(() -> {
			Iterator<Player> iter = players.iterator();
			while (iter.hasNext()) {
				Player pl = iter.next();
				if (!pl.isValid()) {
					iter.remove();
					continue;
				}
				float cd = spellToCheck.getCooldown(pl);
				int amt = 1;
				if (cd > 0) amt = (int) Math.ceil(cd);

				PlayerInventory inventory = pl.getInventory();
				ItemStack off = inventory.getItemInOffHand();

				if (!off.isSimilar(item)) {
					off = item.clone();
					inventory.setItemInOffHand(off);
				}

				off.setAmount(amt);
			}
		}, TimeUtil.TICKS_PER_SECOND, TimeUtil.TICKS_PER_SECOND);
	}

	@Override
	public CastResult cast(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		players.add(caster);
		playSpellEffects(data);

		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
