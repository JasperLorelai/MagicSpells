package com.nisovin.magicspells.spells.targeted;

import java.util.*;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.RayTraceResult;
import org.bukkit.block.data.BlockData;

import com.nisovin.magicspells.util.*;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.compat.EventUtil;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.events.MagicSpellsBlockPlaceEvent;
import com.nisovin.magicspells.events.MagicSpellsBlockBreakEvent;

/**
 * Pattern feature was inspired by Shadoward12's Rune/Pattern-Tester spell.
 */
public class MaterializeSpell extends TargetedSpell implements TargetedLocationSpell {

	private final List<Block> blocks = new ArrayList<>();
	private final Set<Material> materials = new HashSet<>();

	private final boolean falling;
	private final boolean applyPhysics;
	private final boolean checkPlugins;
	private final boolean removeBlocks;
	private final boolean stretchPattern;
	private final boolean playBreakEffect;
	private final boolean randomizePattern;
	private final boolean restartPatternEachRow;

	private Material defaultMaterial;

	private final int resetDelay;
	private final ConfigData<Integer> height;

	private final ConfigData<Double> fallHeight;

	private final String strFailed;

	private Material[][] patterns;

	private int rowSize = 1;
	private int columnSize = 1;

	public MaterializeSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		falling = getConfigBoolean("falling", false);
		applyPhysics = getConfigBoolean("apply-physics", true);
		checkPlugins = getConfigBoolean("check-plugins", true);
		removeBlocks = getConfigBoolean("remove-blocks", true);
		stretchPattern = getConfigBoolean("stretch-pattern", false);
		playBreakEffect = getConfigBoolean("play-break-effect", true);
		randomizePattern = getConfigBoolean("randomize-pattern", false);
		restartPatternEachRow = getConfigBoolean("restart-pattern-each-row", false);

		String blockType = getConfigString("block-type", "stone");
		defaultMaterial = Material.matchMaterial(blockType);
		if (defaultMaterial == null || !defaultMaterial.isBlock()) {
			MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid 'block-type' defined! Falling back to 'stone'.");
			defaultMaterial = Material.STONE;
		}

		height = getConfigDataInt("height", 1);
		resetDelay = getConfigInt("reset-delay", 0);

		fallHeight = getConfigDataDouble("fall-height", 0.5);

		strFailed = getConfigString("str-failed", "");

		parsePatterns(getConfigStringList("patterns", List.of()));

		String area = getConfigString("area", "1x1");
		if (!parseArea(area)) MagicSpells.error("MaterializeSpell " + internalName + " has an invalid 'area' defined: '" + area + "'. Falling back to 1x1.");
	}

	private boolean parseArea(String area) {
		String[] splits = area.split("x", 2);
		if (splits.length != 2) return false;

		try {
			int row = Integer.parseInt(splits[0]);
			int column = Integer.parseInt(splits[1]);
			if (row <= 0 || column <= 0) return false;

			rowSize = row;
			columnSize = column;

			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public void parsePatterns(List<String> patternList) {
		if (patternList.isEmpty()) {
			patterns = new Material[0][0];
			materials.add(defaultMaterial);
			return;
		}

		patterns = new Material[patternList.size()][];

		for (int i = 0; i < patternList.size(); i++) {
			String[] split = patternList.get(i).split(",");
			patterns[i] = new Material[split.length];

			for (int j = 0; j < split.length; j++) {
				String matName = split[j];
				Material mat = Material.matchMaterial(matName);
				if (mat == null || !mat.isBlock()) {
					MagicSpells.error("MaterializeSpell " + internalName + " has an invalid 'patterns[" + i + "][" + j + "]' defined: '" + matName + "'. Falling back to 'stone'.");
					mat = Material.STONE;
				}

				materials.add(mat);
				patterns[i][j] = mat;
			}
		}
	}

	@Override
	public void turnOff() {
		for (Block b : blocks) {
			b.setType(Material.AIR);
		}

		blocks.clear();
	}

	@Override
	public CastResult cast(SpellData data) {
		Player caster = data.caster() instanceof Player p ? p : null;

		RayTraceResult result = rayTraceBlocks(data);
		if (result == null) return noTarget(data);

		Block block = result.getHitBlock().getRelative(result.getHitBlockFace());

		SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, data, block.getLocation());
		if (!event.callEvent()) return noTarget(strFailed, event);

		data = event.getSpellData();
		block = event.getTargetLocation().getBlock();

		boolean done = materializeArea(caster, block, data);
		return done ? new CastResult(PostCastAction.HANDLE_NORMALLY, data) : noTarget(strFailed, data);
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		Player caster = data.caster() instanceof Player p ? p : null;

		Block block = data.location().getBlock();
		if (!block.getType().isAir()) {
			block = block.getRelative(BlockFace.UP);
			data = data.location(block.getLocation());

			SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, data, block.getLocation());
			if (!event.callEvent()) return noTarget(strFailed, event);
			data = event.getSpellData();
			block = event.getTargetLocation().getBlock();

			if (!block.getType().isAir()) return noTarget(strFailed, data);
		}

		boolean done = materializeArea(caster, block, data);
		return done ? new CastResult(PostCastAction.HANDLE_NORMALLY, data) : noTarget(strFailed, data);
	}

	private boolean materializeArea(Player player, Block block, SpellData data) {
		// Unfortunately, shape array placement is world relative, will fix later. This is the top-left (NW) edge.
		Location patternStart = block.getLocation().subtract(rowSize >> 1, 0, columnSize >> 1);

		int rowPosition = 0;

		for (int y = 0; y < Math.max(height.get(data), 1); y++) {
			int patternPosition = 0;

			for (int z = 0; z < columnSize; z++) {
				if (patternPosition >= patterns.length) patternPosition = 0;

				int rowLength = 0;
				if (patterns.length > 0) rowLength = patterns[patternPosition].length;

				if (restartPatternEachRow) rowPosition = 0;

				for (int x = 0; x < rowSize; x++) {
					Location spawnLoc = patternStart.clone().add(x, y, z);
					Block spawnBlock = spawnLoc.getBlock();

					if (rowPosition >= rowLength) rowPosition = 0;

					Material material;
					if (stretchPattern && y >= 1) material = spawnBlock.getRelative(BlockFace.DOWN).getType();
					else {
						if (patterns.length == 0 || rowLength == 0) material = this.defaultMaterial;
						else {
							int index = randomizePattern ? random.nextInt(rowLength) : rowPosition;
							material = patterns[patternPosition][index];
						}
					}

					rowPosition++;

					boolean done = materializeBlock(player, spawnBlock, material, data.location(spawnLoc));
					if (!done) return false;
				}

				patternPosition++;
			}
		}

		return true;
	}

	private boolean materializeBlock(Player player, Block block, Material material, SpellData data) {
		BlockState blockState = block.getState();

		if (checkPlugins && player != null) {
			block.setType(material, false);
			MagicSpellsBlockPlaceEvent event = new MagicSpellsBlockPlaceEvent(block, blockState, block.getRelative(BlockFace.DOWN), player.getEquipment().getItemInMainHand(), player, true);
			EventUtil.call(event);
			blockState.update(true);
			if (event.isCancelled()) return false;
		}

		if (falling) {
			Location location = block.getLocation().add(0.5, fallHeight.get(data), 0.5);
			block.getWorld().spawn(location, FallingBlock.class, fb -> fb.setBlockData(material.createBlockData()));
		}
		else block.setType(material, applyPhysics);

		playSpellEffects(EffectPosition.TARGET, block.getLocation(), data);
		if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player, data);
			playSpellEffectsTrail(player.getLocation(), block.getLocation(), data);
		}

		if (falling) return true;

		if (playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, blockState.getBlockData());
		if (removeBlocks) blocks.add(block);

		if (resetDelay <= 0) return true;
		MagicSpells.scheduleDelayedTask(() -> {
			if (materials.contains(block.getType())) {
				blocks.remove(block);

				playSpellEffects(EffectPosition.DELAYED, block.getLocation(), data);

				if (checkPlugins && player != null) {
					MagicSpellsBlockBreakEvent event = new MagicSpellsBlockBreakEvent(block, player);
					EventUtil.call(event);
					if (event.isCancelled()) return;
				}

				BlockData blockData = block.getBlockData();
				block.setType(Material.AIR);

				playSpellEffects(EffectPosition.BLOCK_DESTRUCTION, block.getLocation(), data);
				if (playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, blockData);
			}
		}, resetDelay);

		return true;
	}

}
