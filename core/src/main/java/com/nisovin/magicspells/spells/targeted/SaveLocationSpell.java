package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.util.managers.VariableManager;

public class SaveLocationSpell extends TargetedSpell implements TargetedLocationSpell {

	private final ConfigData<String> variableWorld;

	private final ConfigData<String> variableX;
	private final ConfigData<String> variableY;
	private final ConfigData<String> variableZ;

	private final ConfigData<String> variableYaw;
	private final ConfigData<String> variablePitch;

	public SaveLocationSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		variableWorld = getConfigDataString("variable-world", null);

		variableX = getConfigDataString("variable-x", null);
		variableY = getConfigDataString("variable-y", null);
		variableZ = getConfigDataString("variable-z", null);

		variableYaw = getConfigDataString("variable-yaw", null);
		variablePitch = getConfigDataString("variable-pitch", null);
	}

	@Override
	public CastResult cast(SpellData data) {
		TargetInfo<Location> info = getTargetedBlockLocation(data);
		if (info.noTarget()) return noTarget(info);

		return castAtLocation(info.spellData());
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		VariableManager manager = MagicSpells.getVariableManager();
		Location loc = data.location();

		manager.set(variableWorld.get(data), caster, loc.getWorld().getName());

		manager.set(variableX.get(data), caster, loc.x());
		manager.set(variableY.get(data), caster, loc.y());
		manager.set(variableZ.get(data), caster, loc.z());

		manager.set(variableYaw.get(data), caster, loc.getYaw());
		manager.set(variablePitch.get(data), caster, loc.getPitch());

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
