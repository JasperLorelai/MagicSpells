package com.nisovin.magicspells.spells.instant;

import org.bukkit.World;
import org.bukkit.GameRules;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;

import net.kyori.adventure.util.TriState;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;

public class WeatherSpell extends InstantSpell implements TargetedEntitySpell, TargetedLocationSpell {

	private final ConfigData<TriState> rain;
	private final ConfigData<TriState> thunder;
	private final ConfigData<TriState> advance;

	private final ConfigData<Integer> durationClear;
	private final ConfigData<Integer> durationWeather;
	private final ConfigData<Integer> durationThunder;

	private final ConfigData<PlayerWeather> playerWeather;

	public WeatherSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		rain = getConfigDataEnum("rain", TriState.class, TriState.NOT_SET);
		thunder = getConfigDataEnum("thunder", TriState.class, TriState.NOT_SET);
		advance = getConfigDataEnum("advance", TriState.class, TriState.NOT_SET);

		durationClear = getConfigDataInt("duration-clear", -1);
		durationWeather = getConfigDataInt("duration-weather", -1);
		durationThunder = getConfigDataInt("duration-thunder", -1);

		playerWeather = getConfigDataEnum("player-weather", PlayerWeather.class, null);
	}

	@Override
	public CastResult cast(SpellData data) {
		return weather(data.caster().getWorld(), data.target(data.caster()));
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		return weather(data.target().getWorld(), data);
	}

	@Override
	public CastResult castAtLocation(SpellData data) {
		return weather(data.location().getWorld(), data);
	}

	private CastResult weather(World world, SpellData data) {
		PlayerWeather playerWeather = this.playerWeather.get(data);

		if (playerWeather == null) {
			Boolean rain = this.rain.get(data).toBoolean();
			if (rain != null) world.setStorm(rain);

			Boolean thunder = this.thunder.get(data).toBoolean();
			if (thunder != null) world.setThundering(thunder);

			Boolean advance = this.advance.get(data).toBoolean();
			if (advance != null) world.setGameRule(GameRules.ADVANCE_WEATHER, advance);

			int durationWeather = this.durationWeather.get(data);
			if (durationWeather >= 0) world.setWeatherDuration(durationWeather);

			int durationThunder = this.durationThunder.get(data);
			if (durationThunder >= 0) world.setThunderDuration(durationThunder);

			int durationClear = this.durationClear.get(data);
			if (durationClear >= 0) world.setClearWeatherDuration(durationClear);
		} else {
			if (!(data.target() instanceof Player player)) return noTarget(data);

			switch (playerWeather) {
				case CLEAR -> player.setPlayerWeather(WeatherType.CLEAR);
				case DOWNFALL -> player.setPlayerWeather(WeatherType.DOWNFALL);
				case RESET -> player.resetPlayerWeather();
			}
		}

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	private enum PlayerWeather {
		CLEAR,
		DOWNFALL,
		RESET,
	}

}
