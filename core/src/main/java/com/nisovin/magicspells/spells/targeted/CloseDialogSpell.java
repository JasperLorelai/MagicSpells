package com.nisovin.magicspells.spells.targeted;

import org.bukkit.entity.Player;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class CloseDialogSpell extends TargetedSpell implements TargetedEntitySpell {

	public CloseDialogSpell(MagicConfig config, String spellName) {
		super(config, spellName);
	}

	@Override
	public CastResult cast(SpellData data) {
		TargetInfo<Player> info = getTargetedPlayer(data);
		if (info.noTarget()) return noTarget(info);

		return close(info.target(), info.spellData());
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		if (!(data.target() instanceof Player target)) return noTarget(data);
		return close(target, data);
	}

	private CastResult close(Player target, SpellData data) {
		target.closeDialog();

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
