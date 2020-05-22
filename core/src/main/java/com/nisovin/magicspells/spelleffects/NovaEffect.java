package com.nisovin.magicspells.spelleffects;

import java.util.Set;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.BlockUtils;
import com.nisovin.magicspells.util.SpellAnimation;

public class NovaEffect extends SpellEffect {

	private Random random;

	private List<Material> materials;

	private Material material;
	private String materialName;

	private double range;

	private int radius;
	private int startRadius;
	private int heightPerTick;
	private int expandInterval;
	private int expandingRadiusChange;

	private boolean circleShape;
	private boolean removePreviousBlocks;

	@Override
	public void loadFromString(String string) {
		super.loadFromString(string);
	}

	@Override
	public void loadFromConfig(ConfigurationSection config) {

		List<String> materialList = config.getStringList("types");
		if (!materialList.isEmpty()) {
			materials = new ArrayList<>();
			Material material;

			for (String str : materialList) {
				material = Material.getMaterial(str.toUpperCase());
				if (material == null || !material.isBlock()) {
					MagicSpells.error("Wrong nova type defined! '" + str + "'");
					continue;
				}
				materials.add(material);
			}
		}

		materialName = config.getString("type", "fire").toUpperCase();
		material = Material.getMaterial(materialName);

		if (material == null || !material.isBlock()) {
			material = null;
			MagicSpells.error("Wrong nova type defined! '" + materialName + "'");
		}

		range = Math.max(config.getDouble("range", 20), 1);

		radius = config.getInt("radius", 3);
		startRadius = config.getInt("start-radius", 0);
		heightPerTick = config.getInt("height-per-tick", 0);
		expandInterval = config.getInt("expand-interval", 5);
		expandingRadiusChange = config.getInt("expanding-radius-change", 1);
		if (expandingRadiusChange < 1) expandingRadiusChange = 1;

		circleShape = config.getBoolean("circle-shape", false);
		removePreviousBlocks = config.getBoolean("remove-previous-blocks", true);

		random = new Random();

	}

	@Override
	public Runnable playEffectLocation(Location location) {
		if (material == null) return null;

		// Get nearby players
		Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, range, range, range);
		List<Player> nearby = new ArrayList<>();
		for (Entity e : nearbyEntities) {
			if (!(e instanceof Player)) continue;
			nearby.add((Player) e);
		}

		// Start animation
		if (circleShape) {
			if (materials != null && !materials.isEmpty()) new NovaAnimationCircle(nearby, location.getBlock(), materials, radius, expandInterval, expandingRadiusChange);
			else new NovaAnimationCircle(nearby, location.getBlock(), material, radius, expandInterval, expandingRadiusChange);
		} else {
			if (materials != null && !materials.isEmpty()) new NovaAnimationSquare(nearby, location.getBlock(), materials, radius, expandInterval, expandingRadiusChange);
			else new NovaAnimationSquare(nearby, location.getBlock(), material, radius, expandInterval, expandingRadiusChange);
		}
		return null;
	}

	private class NovaAnimationSquare extends SpellAnimation {

		List<Player> nearby;
		Set<Block> blocks;
		Block center;
		Material matNova;
		List<Material> materialList;
		int radiusNova;
		int radiusChange;

		public NovaAnimationSquare(List<Player> nearby, Block center, Material mat, int radius, int tickInterval, int activeRadiusChange) {
			super(tickInterval, true);
			this.nearby = nearby;
			this.center = center;
			this.matNova = mat;
			this.radiusNova = radius;
			this.blocks = new HashSet<>();
			this.radiusChange = activeRadiusChange;
		}

		public NovaAnimationSquare(List<Player> nearby, Block center, List<Material> materialList, int radius, int tickInterval, int activeRadiusChange) {
			super(tickInterval, true);
			this.nearby = nearby;
			this.center = center;
			this.materialList = materialList;
			this.radiusNova = radius;
			this.blocks = new HashSet<>();
			this.radiusChange = activeRadiusChange;
		}

		@Override
		protected void onTick(int tick) {
			tick += startRadius;
			tick *= radiusChange;

			// Remove old blocks
			if (removePreviousBlocks) {
				for (Block b : blocks) {
					for (Player p : nearby) p.sendBlockChange(b.getLocation(), b.getType().createBlockData());
				}
				blocks.clear();
			}

			if (tick > radiusNova + 1) {
				stop();
				return;
			} else if (tick > radiusNova) {
				return;
			}

			// Set next ring
			int bx = center.getX();
			int y = center.getY();
			int bz = center.getZ();
			y += tick * heightPerTick;

			for (int x = bx - tick; x <= bx + tick; x++) {
				for (int z = bz - tick; z <= bz + tick; z++) {
					if (Math.abs(x - bx) != tick && Math.abs(z - bz) != tick) continue;

					Block b = center.getWorld().getBlockAt(x, y, z);
					if (BlockUtils.isAir(b.getType()) || b.getType() == Material.TALL_GRASS) {
						Block under = b.getRelative(BlockFace.DOWN);
						if (BlockUtils.isAir(under.getType()) || under.getType() == Material.TALL_GRASS) b = under;
					} else if (BlockUtils.isAir(b.getRelative(BlockFace.UP).getType()) || b.getRelative(BlockFace.UP).getType() == Material.TALL_GRASS) {
						b = b.getRelative(BlockFace.UP);
					}

					if (!BlockUtils.isAir(b.getType()) && b.getType() != Material.TALL_GRASS) continue;

					if (blocks.contains(b)) continue;
					for (Player p : nearby) {
						if (materialList != null && !materialList.isEmpty()) p.sendBlockChange(b.getLocation(), materialList.get(random.nextInt(materialList.size())).createBlockData());
						else if (matNova != null) p.sendBlockChange(b.getLocation(), matNova.createBlockData());
					}
					blocks.add(b);
				}
			}
		}

		@Override
		protected void stop() {
			super.stop();

			for (Block b : blocks) {
				for (Player p : nearby) p.sendBlockChange(b.getLocation(), b.getType().createBlockData());
			}

			blocks.clear();
		}

	}

	private class NovaAnimationCircle extends SpellAnimation {

		List<Player> nearby;
		Set<Block> blocks;
		Block center;
		Material matNova;
		List<Material> materialList;
		int radiusNova;
		int radiusChange;

		public NovaAnimationCircle(List<Player> nearby, Block center, Material mat, int radius, int tickInterval, int activeRadiusChange) {
			super(tickInterval, true);
			this.nearby = nearby;
			this.center = center;
			this.matNova = mat;
			this.radiusNova = radius;
			this.blocks = new HashSet<>();
			this.radiusChange = activeRadiusChange;
		}

		public NovaAnimationCircle(List<Player> nearby, Block center, List<Material> materialList, int radius, int tickInterval, int activeRadiusChange) {
			super(tickInterval, true);
			this.nearby = nearby;
			this.center = center;
			this.materialList = materialList;
			this.radiusNova = radius;
			this.blocks = new HashSet<>();
			this.radiusChange = activeRadiusChange;
		}

		@Override
		protected void onTick(int tick) {
			tick += startRadius;
			tick *= radiusChange;

			// Remove old blocks
			if (removePreviousBlocks) {
				for (Block b : blocks) {
					for (Player p : nearby) p.sendBlockChange(b.getLocation(), b.getType().createBlockData());
				}
				blocks.clear();
			}

			if (tick > radiusNova + 1) {
				stop();
				return;
			} else if (tick > radiusNova) {
				return;
			}

			// Generate the bottom block
			Location centerLocation = center.getLocation().clone();
			centerLocation.add(0.5, tick * heightPerTick, 0.5);
			Block b;

			if (startRadius == 0 && tick == 0) {
				b = centerLocation.getWorld().getBlockAt(centerLocation);
				if (BlockUtils.isAir(b.getType()) || b.getType() == Material.TALL_GRASS) {
					Block under = b.getRelative(BlockFace.DOWN);
					if (BlockUtils.isAir(under.getType()) || under.getType() == Material.TALL_GRASS) b = under;
				} else if (BlockUtils.isAir(b.getRelative(BlockFace.UP).getType()) || b.getRelative(BlockFace.UP).getType() == Material.TALL_GRASS) {
					b = b.getRelative(BlockFace.UP);
				}

				if (!BlockUtils.isAir(b.getType()) && b.getType() != Material.TALL_GRASS) return;

				if (blocks.contains(b)) return;
				for (Player p : nearby) {
					if (materialList != null && !materialList.isEmpty()) p.sendBlockChange(b.getLocation(), materialList.get(random.nextInt(materialList.size())).createBlockData());
					else if (matNova != null) p.sendBlockChange(b.getLocation(), matNova.createBlockData());
				}
				blocks.add(b);
			}

			// Generate the circle
			Vector v;
			double angle, x, z;
			double amount = tick * 64;
			double inc = (2 * Math.PI) / amount;
			for (int i = 0; i < amount; i++) {
				angle = i * inc;
				x = tick * Math.cos(angle);
				z = tick * Math.sin(angle);
				v = new Vector(x, 0, z);
				b = center.getWorld().getBlockAt(centerLocation.add(v));
				centerLocation.subtract(v);

				if (BlockUtils.isAir(b.getType()) || b.getType() == Material.TALL_GRASS) {
					Block under = b.getRelative(BlockFace.DOWN);
					if (BlockUtils.isAir(under.getType()) || under.getType() == Material.TALL_GRASS) b = under;
				} else if (BlockUtils.isAir(b.getRelative(BlockFace.UP).getType()) || b.getRelative(BlockFace.UP).getType() == Material.TALL_GRASS) {
					b = b.getRelative(BlockFace.UP);
				}

				if (!BlockUtils.isAir(b.getType()) && b.getType() != Material.TALL_GRASS) continue;

				if (blocks.contains(b)) continue;
				for (Player p : nearby) {
					if (materialList != null && !materialList.isEmpty()) p.sendBlockChange(b.getLocation(), materialList.get(random.nextInt(materialList.size())).createBlockData());
					else if (matNova != null) p.sendBlockChange(b.getLocation(), matNova.createBlockData());
				}
				blocks.add(b);
			}

		}

		@Override
		protected void stop() {
			super.stop();

			for (Block b : blocks) {
				for (Player p : nearby) p.sendBlockChange(b.getLocation(), b.getType().createBlockData());
			}

			blocks.clear();
		}

	}

}
