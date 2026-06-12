package com.nisovin.magicspells.spells.targeted;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Objective;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class ScoreboardDataSpell extends TargetedSpell implements TargetedEntitySpell {

	private final ConfigData<String> variableName;
	private final ConfigData<String> objectiveName;

	public ScoreboardDataSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		variableName = getConfigDataString("variable-name", "");
		objectiveName = getConfigDataString("objective-name", "");
	}

	@Override
	public CastResult cast(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		TargetInfo<LivingEntity> info = getTargetedEntity(data);
		if (info.noTarget()) return noTarget(info);
		data = info.spellData();

		return setScore(caster, data);
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);
		return setScore(caster, data);
	}

	private CastResult setScore(Player caster, SpellData data) {
		Variable variable = MagicSpells.getVariableManager().getVariable(variableName.get(data));
		if (variable == null) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objectiveName.get(data));
		if (objective == null) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		int score = objective.getScoreFor(data.target()).getScore();
		MagicSpells.getVariableManager().set(variable, caster, String.valueOf(score));

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

}
