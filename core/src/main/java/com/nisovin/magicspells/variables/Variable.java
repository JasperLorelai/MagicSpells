package com.nisovin.magicspells.variables;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.configuration.ConfigurationSection;

public abstract class Variable {

	protected String name;
	protected double defaultValue = 0;
	protected String defaultStringValue = 0D + "";
	protected double maxValue = Double.MAX_VALUE;
	protected double minValue = 0;
	protected boolean permanent;
	protected Objective objective;
	protected boolean expBar;
	protected String bossbarTitle;
	protected BarStyle bossbarStyle;
	protected BarColor bossbarColor;
	protected String bossbarNamespace;

	public Variable() {
		// No op
	}
	
	public final void init(String name, double defaultValue, double minValue, double maxValue, boolean permanent, Objective objective, boolean expBar, String bossbarTitle, BarStyle bossbarStyle, BarColor bossbarColor, String bossbarNamespace) {
		this.name = name;
		this.defaultValue = defaultValue;
		this.defaultStringValue = defaultValue + "";
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.permanent = permanent;
		this.objective = objective;
		this.expBar = expBar;
		this.bossbarTitle = bossbarTitle;
		this.bossbarStyle = bossbarStyle;
		this.bossbarColor = bossbarColor;
		this.bossbarNamespace = bossbarNamespace;
		init();
	}
	
	protected void init() {
		// No op
	}
	
	public final void set(Player player, double amount) {
		set(player.getName(), amount);
	}
	
	public void parseAndSet(Player player, String textValue) {
		parseAndSet(player.getName(), textValue);
	}
	
	public abstract void set(String player, double amount);
	
	public void parseAndSet(String player, String textValue) {
		set(player, Double.parseDouble(textValue));
	}
	
	public double getValue(Player player) {
		if (player == null) return getValue("");
		return getValue(player.getName());
	}
	
	public abstract double getValue(String player);
	
	public final void reset(Player player) {
		reset(player.getName());
	}
	
	public abstract void reset(String player);
	
	public void loadExtraData(ConfigurationSection section) {
		// No op from here, but subclasses may add behavior
	}
	
	public String getStringValue(Player player) {
		return getStringValue(player.getName());
	}
	
	public String getStringValue(String player) {
		return getValue(player) + "";
	}

	public double getMaxValue() {
		return maxValue;
	}

	public double getMinValue() {
		return minValue;
	}
}
