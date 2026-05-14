package com.nisovin.magicspells.spells.instant;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.util.config.dialog.DialogBuilder;

public class DialogSpell extends InstantSpell implements TargetedEntitySpell {

	private ConfigData<Dialog> dialog;

	public DialogSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}

	@Override
	protected void initializeModifiers() {
		super.initializeModifiers();

		String error = "DialogSpell '" + internalName + "' reports - ";
		dialog = DialogBuilder.create(
			getConfigSection(""),
			extra -> MagicSpells.error(error + extra),
			extra -> MagicSpells.debug("    " + error + extra)
		);
	}

	@Override
	public CastResult cast(SpellData data) {
		return castAtEntity(data.target(data.caster()));
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		if (!(data.target() instanceof Player player)) return noTarget(data);

		Dialog dialog = this.dialog.get(data);
		if (dialog == null) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		player.showDialog(dialog);

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
