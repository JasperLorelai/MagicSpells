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

	private Material material;

	private final int resetDelay;
	private final ConfigData<Integer> height;

	private final ConfigData<Double> fallHeight;

	private final String area;
	private final String strFailed;

	private final List<String> patterns;

	private Material[][] rowPatterns;

	private int rowSize;
	private int columnSize;
	private boolean hasMiddle;

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
		material = Util.getMaterial(blockType);
		if (material == null || !material.isBlock()) MagicSpells.error("MaterializeSpell '" + internalName + "' has an invalid block-type defined!");

		height = getConfigDataInt("height", 1);
		resetDelay = getConfigInt("reset-delay", 0);

		fallHeight = getConfigDataDouble("fall-height", 0.5);

		area = getConfigString("area", "1x1");
		strFailed = getConfigString("str-failed", "");

		patterns = getConfigStringList("patterns", null);
	}

	@Override
	public void initialize() {
		super.initialize();

		String[] areaParts = area.split("x", 2);
		rowSize = Integer.parseInt(areaParts[0]);
		columnSize = Integer.parseInt(areaParts[1]);

		/*For this to work smoothly, we need to see if the shape array has a middle;
		It becomes very complicated when working with shape arrays without a block as a geometrical middle
		So unfortunately. Shape arrays without a block as its geometrical center cannot be accepted.
		3x2, 9x8. Basically, if the product of the length and width is even. Don't use it. */
		hasMiddle = ((rowSize * columnSize) % 2) == 1;

		if (!hasMiddle && patterns != null) {
			MagicSpells.error("MaterializeSpell " + internalName + " is using a shape array without a geometrical center! A single block will spawn instead.");
		}

		if (patterns == null) {
			rowPatterns = new Material[1][1];
			rowPatterns[0][0] = material;
			materials.add(material);
		} else parseBlocks();
	}

	private void parseBlocks() {
		rowPatterns = new Material[patterns.size()][];

		for (int i = 0; i < patterns.size(); i++) {
			String[] split = patterns.get(i).split(",");
			rowPatterns[i] = new Material[split.length];

			for (int j = 0; j < split.length; j++) {
				Material mat = Util.getMaterial(split[j]);
				if (mat == null) mat = Material.STONE;

				materials.add(mat);
				rowPatterns[i][j] = mat;
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
		if (!(data.caster() instanceof Player caster)) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

		RayTraceResult result = rayTraceBlocks(data);
		if (result == null) return noTarget(data);

		Block against = result.getHitBlock();
		Block block = against.getRelative(result.getHitBlockFace());

		SpellTargetLocationEvent event = new SpellTargetLocationEvent(this, data, block.getLocation());
		if (!event.callEvent()) return noTarget(strFailed, event);

		data = event.getSpellData();
		block = event.getTargetLocation().getBlock();

		if (!hasMiddle) {
			boolean done = materialize(caster, block, against, data);
			if (!done) return noTarget(strFailed, data);
			return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
		}

		// Unfortunately, shape array placement is world relative, will fix later. This is the top-left (NW) edge.
		Location patternStart = against.getLocation();

		patternStart.setX(against.getX() - Math.ceil(rowSize / 2F));
		patternStart.setZ(against.getZ() - Math.ceil(columnSize / 2F));

		int rowPosition = 0;

		for (int y = 0; y < Math.max(height.get(data), 1); y++) {
			int patternPosition = 0;

			for (int z = 0; z < columnSize; z++) {
				if (patterns != null && patternPosition >= patterns.size()) patternPosition = 0;

				int rowLength = rowPatterns[patternPosition].length;

				if (restartPatternEachRow) rowPosition = 0;

				for (int x = 0; x < rowSize; x++) {
					Block ground = patternStart.clone().add(x, y, z).getBlock();
					Block air = ground.getRelative(BlockFace.UP);

					if (rowPosition >= rowLength) rowPosition = 0;

					if (!stretchPattern || y < 1)
						material = blockGenerator(randomizePattern, patternPosition, rowPosition);
					else material = ground.getType();

					rowPosition++;

					boolean done = materialize(caster, air, ground, data.location(block.getLocation()));
					if (!done) return noTarget(strFailed, data);
				}

				patternPosition++;
			}
		}

		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		Player caster = data.caster() instanceof Player p ? p : null;

		Block block = data.location().getBlock();
		if (!block.getType().isAir()) {
			block = block.getRelative(BlockFace.UP);
			data = data.location(block.getLocation());
			if (!block.getType().isAir()) return noTarget(strFailed, data);
		}

		boolean done = materialize(caster, block, block.getRelative(BlockFace.DOWN), data);
		return done ? new CastResult(PostCastAction.HANDLE_NORMALLY, data) : noTarget(strFailed, data);
	}

	private Material blockGenerator(boolean randomize, int patternPosition, int rowPosition) {
		int index = randomize ? random.nextInt(rowPatterns[patternPosition].length) : rowPosition;
		return rowPatterns[patternPosition][index];
	}

	private boolean materialize(Player player, Block block, Block against, SpellData data) {
		BlockState blockState = block.getState();

		if (checkPlugins && player != null) {
			block.setType(material, false);
			MagicSpellsBlockPlaceEvent event = new MagicSpellsBlockPlaceEvent(block, blockState, against, player.getEquipment().getItemInMainHand(), player, true);
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
