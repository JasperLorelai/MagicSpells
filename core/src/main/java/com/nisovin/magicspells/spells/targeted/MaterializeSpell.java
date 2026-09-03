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

	private final ConfigData<Boolean> falling;
	private final ConfigData<Boolean> applyPhysics;
	private final ConfigData<Boolean> checkPlugins;
	private final ConfigData<Boolean> removeBlocks;
	private final ConfigData<Boolean> stretchPattern;
	private final ConfigData<Boolean> playBreakEffect;
	private final ConfigData<Boolean> checkBlockAbove;
	private final ConfigData<Boolean> randomizePattern;
	private final ConfigData<Boolean> fallbackToOriginal;
	private final ConfigData<Boolean> restartPatternEachRow;

	private Material defaultMaterial;

	private final ConfigData<Integer> height;
	private final ConfigData<Integer> resetDelay;

	private final ConfigData<Double> fallHeight;

	private final String strFailed;

	private Material[][] patterns;

	private int rowSize = 1;
	private int columnSize = 1;

	public MaterializeSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		falling = getConfigDataBoolean("falling", false);
		applyPhysics = getConfigDataBoolean("apply-physics", true);
		checkPlugins = getConfigDataBoolean("check-plugins", true);
		removeBlocks = getConfigDataBoolean("remove-blocks", true);
		stretchPattern = getConfigDataBoolean("stretch-pattern", false);
		playBreakEffect = getConfigDataBoolean("play-break-effect", true);
		checkBlockAbove = getConfigDataBoolean("check-block-above", true);
		randomizePattern = getConfigDataBoolean("randomize-pattern", false);
		fallbackToOriginal = getConfigDataBoolean("fallback-to-original", false);
		restartPatternEachRow = getConfigDataBoolean("restart-pattern-each-row", false);

		String blockType = getConfigString("block-type", "stone");
		defaultMaterial = Material.matchMaterial(blockType);
		if (defaultMaterial == null || !defaultMaterial.isBlock()) {
			MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid 'block-type' defined! Falling back to 'stone'.");
			defaultMaterial = Material.STONE;
		}

		height = getConfigDataInt("height", 1);
		resetDelay = getConfigDataInt("reset-delay", 0);

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
		if (block.getType().isAir() || !checkBlockAbove.get(data))
			return castMaterialize(caster, block, data);

		Block blockUp = block.getRelative(BlockFace.UP);
		SpellData dataUp = data.location(blockUp.getLocation());

		SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, dataUp, blockUp.getLocation());
		if (event.callEvent()) {
			dataUp = event.getSpellData();
			blockUp = event.getTargetLocation().getBlock();

			if (blockUp.getType().isAir())
				return castMaterialize(caster, blockUp, dataUp);
		}

		return fallbackToOriginal.get(data) ?
			castMaterialize(caster, block, data) :
			noTarget(strFailed, event);
	}

	private CastResult castMaterialize(Player caster, Block block, SpellData data) {
		return materializeArea(caster, block, data) ?
			new CastResult(PostCastAction.HANDLE_NORMALLY, data) :
			noTarget(strFailed, data);
	}

	private boolean materializeArea(Player player, Block block, SpellData data) {
		// Unfortunately, shape array placement is world relative, will fix later. This is the top-left (NW) edge.
		Location patternStart = block.getLocation().subtract(rowSize >> 1, 0, columnSize >> 1);

		int rowPosition = 0;

		boolean falling = this.falling.get(data);
		boolean stretchPattern = this.stretchPattern.get(data);
		boolean randomizePattern = this.randomizePattern.get(data);
		boolean restartPatternEachRow = this.restartPatternEachRow.get(data);

		MaterializeOptions options = new MaterializeOptions(
			applyPhysics.get(data),
			checkPlugins.get(data),
			playBreakEffect.get(data),
			removeBlocks.get(data),
			resetDelay.get(data)
		);

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

					if (falling) {
						spawnFallingBlock(player, spawnBlock, material, data);
						continue;
					}

					boolean done = materializeBlock(player, spawnBlock, material, data.location(spawnLoc), options);
					if (!done) return false;
				}

				patternPosition++;
			}
		}

		return true;
	}

	private record MaterializeOptions(
		boolean applyPhysics,
		boolean checkPlugins,
		boolean playBreakEffect,
		boolean removeBlocks,
		int resetDelay
	) {}

	private boolean materializeBlock(Player player, Block block, Material material, SpellData data, MaterializeOptions options) {
		BlockState blockState = block.getState();
		block.setType(material, options.applyPhysics);

		if (options.checkPlugins && player != null) {
			MagicSpellsBlockPlaceEvent event = new MagicSpellsBlockPlaceEvent(block, blockState, block.getRelative(BlockFace.DOWN), player.getEquipment().getItemInMainHand(), player, true);
			if (!event.callEvent()) {
				blockState.update(true);
				return false;
			}
		}

		playSpellEffects(EffectPosition.TARGET, block.getLocation(), data);
		if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player, data);
			playSpellEffectsTrail(player.getLocation(), block.getLocation(), data);
		}

		if (options.playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, blockState.getBlockData());
		if (options.removeBlocks) blocks.add(block);

		if (options.resetDelay <= 0) return true;
		MagicSpells.scheduleDelayedTask(() -> {
			if (materials.contains(block.getType())) {
				blocks.remove(block);

				playSpellEffects(EffectPosition.DELAYED, block.getLocation(), data);

				if (options.checkPlugins && player != null) {
					MagicSpellsBlockBreakEvent event = new MagicSpellsBlockBreakEvent(block, player);
					if (!event.callEvent()) return;
				}

				BlockData blockData = block.getBlockData();
				block.setType(Material.AIR);

				playSpellEffects(EffectPosition.BLOCK_DESTRUCTION, block.getLocation(), data);
				if (options.playBreakEffect) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, blockData);
			}
		}, options.resetDelay);

		return true;
	}

	private void spawnFallingBlock(Player player, Block block, Material material, SpellData data) {
		Location location = block.getLocation().add(0.5, fallHeight.get(data), 0.5);
		block.getWorld().spawn(location, FallingBlock.class, fb -> fb.setBlockData(material.createBlockData()));

		playSpellEffects(EffectPosition.TARGET, block.getLocation(), data);
		if (player != null) {
			playSpellEffects(EffectPosition.CASTER, player, data);
			playSpellEffectsTrail(player.getLocation(), block.getLocation(), data);
		}
	}

}
