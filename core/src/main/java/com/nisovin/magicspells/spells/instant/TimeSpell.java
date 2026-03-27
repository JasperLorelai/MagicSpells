package com.nisovin.magicspells.spells.instant;

import org.bukkit.World;
import org.bukkit.GameRules;
import org.bukkit.entity.Player;

import net.kyori.adventure.util.TriState;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;

public class TimeSpell extends InstantSpell implements TargetedEntitySpell, TargetedLocationSpell {

	private final ConfigData<Integer> time;

	private final ConfigData<Boolean> addTime;
	private final ConfigData<Boolean> setPlayerTime;

	private final ConfigData<TriState> advance;

	private String strAnnounce;

	public TimeSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		time = getConfigDataInt("time", getConfigDataInt("time-to-set", 0));

		addTime = getConfigDataBoolean("add-time", false);
		setPlayerTime = getConfigDataBoolean("set-player-time", false);

		advance = getConfigDataEnum("advance", TriState.class, TriState.NOT_SET);

		strAnnounce = getConfigString("str-announce", "");
	}

	@Override
	public CastResult cast(SpellData data) {
		return setTime(data.caster().getWorld(), data.target(data.caster()));
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		return setTime(data.target().getWorld(), data);
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		return setTime(data.location().getWorld(), data);
	}

	private CastResult setTime(World world, SpellData data) {
		long time = this.time.get(data);
		if (addTime.get(data)) time += world.getTime();

		if (setPlayerTime.get(data)) {
			if (!(data.target() instanceof Player player)) return noTarget(data);
			player.setPlayerTime(time, true); // Reset with "time: 0".
		} else world.setTime(time);

		Boolean advance = this.advance.get(data).toBoolean();
		if (advance != null) world.setGameRule(GameRules.ADVANCE_TIME, advance);

		sendMessageNear(strAnnounce, data);
		playSpellEffects(data);

		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	public String getStrAnnounce() {
		return strAnnounce;
	}

	public void setStrAnnounce(String strAnnounce) {
		this.strAnnounce = strAnnounce;
	}

}
