package com.nisovin.magicspells.spells.targeted;

import java.util.Map;
import java.util.UUID;
import java.util.Iterator;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.util.MagicLocation;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.spells.instant.MarkSpell;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;

public class RemoveMarksSpell extends TargetedSpell implements TargetedLocationSpell {

	private float radius;

	private boolean pointBlank;

	private MarkSpell markSpell;
	private String markSpellName;

	public RemoveMarksSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		radius = getConfigFloat("radius", 10F);

		pointBlank = getConfigBoolean("point-blank", false);

		markSpellName = getConfigString("mark-spell", "");
	}

	@Override
	public void initialize() {
		super.initialize();

		Spell spell = MagicSpells.getSpellByInternalName(markSpellName);
		if (spell instanceof MarkSpell) {
			markSpell = (MarkSpell) spell;
			return;
		}

		MagicSpells.error("RemoveMarksSpell '" + internalName + "' has an invalid mark-spell defined!");
	}

	@Override
	public PostCastAction castSpell(Player player, SpellCastState state, float power, String[] args) {
		if (state == SpellCastState.NORMAL) {
			Location loc = null;
			if (pointBlank) loc = player.getLocation();
			else {
				Block b = getTargetedBlock(player, power);
				if (b != null && !BlockUtils.isAir(b.getType())) loc = b.getLocation();
			}
			if (loc == null) return noTarget(player);
			removeMarks(player, loc, power);
		}
		return PostCastAction.HANDLE_NORMALLY;
	}

	@Override
	public boolean castAtLocation(Player caster, Location target, float power) {
		removeMarks(caster, target, power);
		return true;
	}

	@Override
	public boolean castAtLocation(Location target, float power) {
		removeMarks(null, target, power);
		return true;
	}

	private void removeMarks(Player caster, Location loc, float power) {
		float rad = radius * power;
		float radSq = rad * rad;

		Map<UUID, MagicLocation> marks = markSpell.getMarks();
		Iterator<UUID> iter = marks.keySet().iterator();
		World locWorld = loc.getWorld();

		while (iter.hasNext()) {
			MagicLocation l = marks.get(iter.next());
			if (!l.getWorld().equals(locWorld.getName())) continue;
			if (l.getLocation().distanceSquared(loc) < radSq) iter.remove();
		}

		markSpell.setMarks(marks);
		playSpellEffects(EffectPosition.TARGET, loc);
		if (caster != null) playSpellEffects(EffectPosition.CASTER, caster);
	}
	
}
