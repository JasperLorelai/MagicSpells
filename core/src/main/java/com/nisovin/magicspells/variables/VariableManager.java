package com.nisovin.magicspells.variables;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;

import com.udojava.evalex.Expression;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.Util;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.DebugHandler;
import com.nisovin.magicspells.util.TimeUtil;
import com.nisovin.magicspells.Spell.PostCastAction;
import com.nisovin.magicspells.Spell.SpellCastState;
import com.nisovin.magicspells.util.PlayerNameUtils;
import com.nisovin.magicspells.events.SpellCastEvent;
import com.nisovin.magicspells.events.SpellCastedEvent;
import com.nisovin.magicspells.events.SpellTargetEvent;

public class VariableManager implements Listener {

	private Map<String, Variable> variables = new HashMap<>();
	private Set<String> dirtyPlayerVars = new HashSet<>();
	private boolean dirtyGlobalVars = false;
	private File folder;

	// DEBUG INFO: level 2, loaded variable (name)
	// DEBUG INFO: level 1, # variables loaded
	public VariableManager(MagicSpells plugin, ConfigurationSection section) {
		if (section != null) {
			for (String var : section.getKeys(false)) {
				ConfigurationSection varSection = section.getConfigurationSection(var);
				String type = section.getString(var + ".type", "global");
				double def = section.getDouble(var + ".default", 0);
				double min = section.getDouble(var + ".min", 0);
				double max = section.getDouble(var + ".max", Double.MAX_VALUE);
				boolean perm = section.getBoolean(var + ".permanent", true);

				Variable variable = VariableType.getType(type).newInstance();

				String scoreName = section.getString(var + ".scoreboard-title", null);
				String scorePos = section.getString(var + ".scoreboard-position", null);
				Objective objective = null;
				if (scoreName != null && scorePos != null) {
					String objName = "MSV_" + var;
					if (objName.length() > 16) objName = objName.substring(0, 16);
					objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective(objName);
					if (objective != null) {
						objective.unregister();
						objective = null;
					}
					objective = Bukkit.getScoreboardManager().getMainScoreboard().registerNewObjective(objName, objName, objName);
					objective.setDisplayName(Util.colorize(scoreName));
					if (scorePos.equalsIgnoreCase("nameplate")) objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
					else if (scorePos.equalsIgnoreCase("playerlist")) objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
					else objective.setDisplaySlot(DisplaySlot.SIDEBAR);
				}

				boolean expBar = section.getBoolean(var + ".exp-bar", false);

				String bossbarTitle = null;
				BarStyle bossbarStyle = null;
				BarColor bossbarColor = null;
				String bossBarNamespace = null;
				// Reserve preceded handling.
				if (section.isString(var + ".boss-bar")) bossbarTitle = section.getString(var + ".boss-bar");
				else {
					ConfigurationSection bossBar = section.getConfigurationSection(var + ".boss-bar");
					if (bossBar != null) {
						bossbarTitle = bossBar.getString("title");
						String style = bossBar.getString("style");
						String color = bossBar.getString("color");
						if (style != null) {
							try {
								bossbarStyle = BarStyle.valueOf(style.toUpperCase());
							}
							catch (IllegalArgumentException ignored) {
								MagicSpells.error("Variable '" + var + "' has an invalid bossbar style defined: '" + style + "'");
							}
						}
						if (color != null) {
							try {
								bossbarColor = BarColor.valueOf(color.toUpperCase());
							}
							catch (IllegalArgumentException ignored) {
								MagicSpells.error("Variable '" + var + "' has an invalid bossbar color defined: '" + color + "'");
							}
						}
						bossBarNamespace = bossBar.getString("namespace");
						if (!MagicSpells.getBossBarManager().isNameSpace(bossBarNamespace)) {
							MagicSpells.error("Variable '" + var + "' has an invalid bossbar namespace defined: '" + bossBarNamespace + "'");
						}
					}
				}
				if (bossbarStyle == null) bossbarStyle = BarStyle.SOLID;
				if (bossbarColor == null) bossbarColor = BarColor.PURPLE;
				if (bossBarNamespace == null || bossBarNamespace.isEmpty()) bossBarNamespace = MagicSpells.getBossBarManager().getNamespaceVariable();

				variable.init(var, def, min, max, perm, objective, expBar, bossbarTitle, bossbarStyle, bossbarColor, bossBarNamespace);
				variable.loadExtraData(varSection);
				variables.put(var, variable);
				MagicSpells.debug(2, "Loaded variable " + var);
			}
			MagicSpells.debug(1, variables.size() + " variables loaded!");
		}
		variables.putAll(SpecialVariables.getSpecialVariables());

		if (!variables.isEmpty()) MagicSpells.registerEvents(this);

		// Load vars
		folder = new File(plugin.getDataFolder(), "vars");
		if (!folder.exists()) folder.mkdir();
		loadGlobalVars();
		for (Player player : Bukkit.getOnlinePlayers()) {
			loadPlayerVars(player.getName(), Util.getUniqueId(player));
			loadBossBars(player);
			loadExpBar(player);
		}

		// Start save task
		MagicSpells.scheduleRepeatingTask(() -> {
			if (dirtyGlobalVars) saveGlobalVars();
			if (!dirtyPlayerVars.isEmpty()) saveAllPlayerVars();
		}, TimeUtil.TICKS_PER_MINUTE, TimeUtil.TICKS_PER_MINUTE);
	}

	/**
	 * Adds a variable with the provided name to the list of variables.
	 * This will replace existing variables if the same name is used.
	 * @param name the name of the variable
	 * @param variable the variable to add
	 * @return Returns true if an existing variable was overwritten
	 */
	public boolean addVariable(String name, Variable variable) {
		return variables.put(name, variable) != null;
	}

	public int count() {
		return variables.size();
	}

	public void set(String variable, Player player, double amount) {
		set(variable, player.getName(), amount);
	}

	public void set(String variable, String player, double amount) {
		Variable var = variables.get(variable);
		if (var == null) return;
		var.set(player, amount);
		updateBossBar(var, player);
		updateExpBar(var, player);
		if (!var.permanent) return;
		if (var instanceof PlayerVariable) dirtyPlayerVars.add(player);
		else if (var instanceof GlobalVariable) dirtyGlobalVars = true;
	}

	public void set(String variable, Player player, String amount) {
		set(variable, player.getName(), amount);
	}

	public void set(String variable, String player, String amount) {
		Variable var = variables.get(variable);
		if (var == null) return;
		var.parseAndSet(player, amount);
		updateBossBar(var, player);
		updateExpBar(var, player);
		if (!var.permanent) return;
		if (var instanceof PlayerVariable) dirtyPlayerVars.add(player);
		else if (var instanceof GlobalVariable) dirtyGlobalVars = true;
	}

	public double getValue(String variable, Player player) {
		Variable var = variables.get(variable);
		if (var != null) return var.getValue(player);
		return 0D;
	}

	public String getStringValue(String variable, Player player) {
		Variable var = variables.get(variable);
		if (var != null) return var.getStringValue(player);
		return 0D + "";
	}

	public double getValue(String variable, String player) {
		Variable var = variables.get(variable);
		if (var != null) return var.getValue(player);
		return 0;
	}

	public String getStringValue(String variable, String player) {
		Variable var = variables.get(variable);
		if (var != null) return var.getStringValue(player);
		return 0D + "";
	}

	public Variable getVariable(String name) {
		return variables.get(name);
	}

	public void reset(String variable, Player player) {
		Variable var = variables.get(variable);
		if (var == null) return;
		var.reset(player);
		updateBossBar(var, player != null ? player.getName() : "");
		updateExpBar(var, player != null ? player.getName() : "");
		if (!var.permanent) return;
		if (var instanceof PlayerVariable) dirtyPlayerVars.add(player != null ? player.getName() : "");
		else if (var instanceof GlobalVariable) dirtyGlobalVars = true;
	}

	private void updateBossBar(Variable var, String player) {
		if (var.bossbarTitle == null) return;
		if (var instanceof GlobalVariable) {
			double pct = var.getValue("") / var.maxValue;
			Util.forEachPlayerOnline(p -> MagicSpells.getBossBarManager().getBar(p, var.bossbarNamespace).set(var.bossbarTitle, pct, var.bossbarStyle, var.bossbarColor));
		} else if (var instanceof PlayerVariable) {
			Player p = PlayerNameUtils.getPlayerExact(player);
			if (p != null) MagicSpells.getBossBarManager().getBar(p, var.bossbarNamespace).set(var.bossbarTitle, var.getValue(p) / var.maxValue, var.bossbarStyle, var.bossbarColor);
		}
	}

	private void updateExpBar(Variable var, String player) {
		if (!var.expBar) return;
		if (var instanceof GlobalVariable) {
			double pct = var.getValue("") / var.maxValue;
			Util.forEachPlayerOnline(p -> MagicSpells.getVolatileCodeHandler().setExperienceBar(p, (int) var.getValue(""), (float) pct));
		} else if (var instanceof PlayerVariable) {
			Player p = PlayerNameUtils.getPlayerExact(player);
			if (p != null) MagicSpells.getVolatileCodeHandler().setExperienceBar(p, (int) var.getValue(p), (float) (var.getValue(p) / var.maxValue));
		}
	}

	private void loadGlobalVars() {
		File file = new File(folder, "GLOBAL.txt");
		if (file.exists()) {
			try {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty()) {
						String[] s = line.split("=", 2);
						Variable variable = variables.get(s[0]);
						if (variable instanceof GlobalVariable && variable.permanent) variable.parseAndSet("", s[1]);
					}
				}
				scanner.close();
			} catch (Exception e) {
				MagicSpells.error("ERROR LOADING GLOBAL VARIABLES");
				MagicSpells.handleException(e);
			}
		}

		dirtyGlobalVars = false;
	}

	private void saveGlobalVars() {
		File file = new File(folder, "GLOBAL.txt");
		if (file.exists()) file.delete();

		List<String> lines = new ArrayList<>();
		for (String variableName : variables.keySet()) {
			Variable variable = variables.get(variableName);
			if (variable instanceof GlobalVariable && variable.permanent) {
				String val = variable.getStringValue("");
				if (!val.equals(variable.defaultStringValue)) lines.add(variableName + '=' + Util.flattenLineBreaks(val));
			}
		}

		if (!lines.isEmpty()) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file, false));
				for (String line : lines) {
					writer.write(line);
					writer.newLine();
				}
				writer.flush();
			} catch (Exception e) {
				MagicSpells.error("ERROR SAVING GLOBAL VARIABLES");
				MagicSpells.handleException(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Exception e) {
						// No op
					}
				}
			}
		}
		dirtyGlobalVars = false;
	}

	private void loadPlayerVars(String player, String uniqueId) {
		File file = new File(folder, "PLAYER_" + uniqueId + ".txt");
		if (!file.exists()) {
			File file2 = new File(folder, "PLAYER_" + player + ".txt");
			if (file2.exists()) file2.renameTo(file);
		}
		if (file.exists()) {
			try {
				Scanner scanner = new Scanner(file);
				while (scanner.hasNext()) {
					String line = scanner.nextLine().trim();
					if (!line.isEmpty()) {
						String[] s = line.split("=", 2);
						Variable variable = variables.get(s[0]);
						if (variable instanceof PlayerVariable && variable.permanent) variable.parseAndSet(player, s[1]);
					}
				}
				scanner.close();
			} catch (Exception e) {
				MagicSpells.error("ERROR LOADING PLAYER VARIABLES FOR " + player);
				MagicSpells.handleException(e);
			}
		}

		dirtyPlayerVars.remove(player);
	}

	private void savePlayerVars(String player, String uniqueId) {
		File file = new File(folder, "PLAYER_" + player + ".txt");
		if (file.exists()) file.delete();
		file = new File(folder, "PLAYER_" + uniqueId + ".txt");
		if (file.exists()) file.delete();

		List<String> lines = new ArrayList<>();
		for (String variableName : variables.keySet()) {
			Variable variable = variables.get(variableName);
			if (variable instanceof PlayerVariable && variable.permanent) {
				String val = variable.getStringValue(player);
				if (!val.equals(variable.defaultStringValue)) lines.add(variableName + '=' + Util.flattenLineBreaks(val));
			}
		}

		if (!lines.isEmpty()) {
			BufferedWriter writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file, false));
				for (String line : lines) {
					writer.write(line);
					writer.newLine();
				}
				writer.flush();
			} catch (Exception e) {
				MagicSpells.error("ERROR SAVING PLAYER VARIABLES FOR " + player);
				MagicSpells.handleException(e);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch (Exception e) {
						DebugHandler.debugGeneral(e);
					}
				}
			}
		}

		dirtyPlayerVars.remove(player);
	}

	private void saveAllPlayerVars() {
		for (String playerName : new HashSet<>(dirtyPlayerVars)) {
			String uid = Util.getUniqueId(playerName);
			if (uid != null) savePlayerVars(playerName, uid);
		}
	}

	private void loadBossBars(Player player) {
		for (Variable var : variables.values()) {
			if (var.bossbarTitle == null) continue;
			MagicSpells.getBossBarManager().getBar(player, var.bossbarNamespace).set(var.bossbarTitle, var.getValue(player) / var.maxValue, var.bossbarStyle, var.bossbarColor);
		}
	}

	private void loadExpBar(Player player) {
		for (Variable var : variables.values()) {
			if (!var.expBar) continue;
			MagicSpells.getVolatileCodeHandler().setExperienceBar(player, (int) var.getValue(player), (float) (var.getValue(player) / var.maxValue));
			break;
		}
	}

	public void disable() {
		if (dirtyGlobalVars) saveGlobalVars();
		if (!dirtyPlayerVars.isEmpty()) saveAllPlayerVars();
		variables.clear();
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		loadPlayerVars(player.getName(), Util.getUniqueId(player));
		loadBossBars(player);
		MagicSpells.scheduleDelayedTask(() -> loadExpBar(player), 10);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		if (dirtyPlayerVars.contains(event.getPlayer().getName())) savePlayerVars(event.getPlayer().getName(), Util.getUniqueId(event.getPlayer()));
	}

	public void evalMath(Variable variable, String math, Player varToMod) {
		math = MagicSpells.doVariableReplacements(varToMod, math);
		if (variable instanceof PlayerStringVariable) {
			// Eventually some string operations could be supported.
			set(variable.name, varToMod, math);
		}
		else {
			double result = new Expression(math).eval().doubleValue();
			set(variable.name, varToMod, result);
		}
	}

	// DEBUG INFO: Debug log level 3, variable was modified for player by amount because of spell cast
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void variableModsCast(SpellCastEvent event) {
		if (event.getSpellCastState() != SpellCastState.NORMAL) return;
		List<String> varModCast = event.getSpell().getVariableModCast();
		if (varModCast == null || varModCast.isEmpty()) return;
		LivingEntity casterEntity = event.getCaster();
		if (!(casterEntity instanceof Player)) return;
		Player caster = (Player) casterEntity;
		for (String varMod : varModCast) {
			String[] splits = varMod.split("=");
			Variable variable = MagicSpells.getVariableManager().getVariable(splits[0].trim());
			if (variable == null) continue;
			String math = splits[1].trim();
			math = math.replaceAll("%a", caster.getName());
			evalMath(variable, math, caster);
			MagicSpells.debug(3, "Variable '" + variable.name + "' for player '" + caster.getName() + "' was modified to '" + variable.getStringValue(caster) + "' as a result of spell cast '" + event.getSpell().getName() + "'");
		}
	}

	// DEBUG INFO: Debug log level 3, variable was modified for player by amount because of spell casted
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void variableModsCasted(SpellCastedEvent event) {
		if (event.getSpellCastState() != SpellCastState.NORMAL || event.getPostCastAction() == PostCastAction.ALREADY_HANDLED) return;
		List<String> varModCasted = event.getSpell().getVariableModCasted();
		if (varModCasted == null || varModCasted.isEmpty()) return;
		LivingEntity casterEntity = event.getCaster();
		if (!(casterEntity instanceof Player)) return;
		Player caster = (Player) casterEntity;
		for (String varMod : varModCasted) {
			String[] splits = varMod.split("=");
			Variable variable = MagicSpells.getVariableManager().getVariable(splits[0].trim());
			if (variable == null) continue;
			String math = splits[1].trim();
			math = math.replaceAll("%a", caster.getName());
			evalMath(variable, math, caster);
			MagicSpells.debug(3, "Variable '" + variable.name + "' for player '" + caster.getName() + "' was modified to '" + variable.getStringValue(caster) + "' as a result of spell casted '" + event.getSpell().getName() + "'");
		}
	}

	// DEBUG INFO: Debug log level 3, variable was modified for player by amount because of spell target
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void variableModsTarget(SpellTargetEvent event) {
		List<String> varModTarget = event.getSpell().getVariableModTarget();
		if (varModTarget == null || varModTarget.isEmpty()) return;
		LivingEntity casterEntity = event.getCaster();
		if (!(casterEntity instanceof Player)) return;
		Player caster = (Player) casterEntity;
		Player target = event.getTarget() instanceof Player ? (Player) event.getTarget() : null;
		if (target == null) return;
		for (String varMod : varModTarget) {
			String[] splits = varMod.split("=");
			String varName = splits[0].trim();
			Variable variable = MagicSpells.getVariableManager().getVariable(varName.replaceFirst("target:", ""));
			if (variable == null) continue;
			Player playerToMod = varName.toLowerCase().startsWith("target:") ? target : caster;
			String math = splits[1].trim();
			math = math.replaceAll("%a", caster.getName());
			math = math.replaceAll("%t", target.getName());
			evalMath(variable, math, playerToMod);
			MagicSpells.debug(3, "Variable '" + variable.name + "' for player '" + playerToMod.getName() + "' was modified to '" + variable.getStringValue(playerToMod) + "' as a result of spell target '" + event.getSpell().getName() + "'");
		}
	}
}
