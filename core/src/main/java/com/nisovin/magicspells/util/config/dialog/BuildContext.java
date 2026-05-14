package com.nisovin.magicspells.util.config.dialog;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;

import org.apache.commons.lang3.function.TriFunction;

import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.ConfigReaderUtil;
import com.nisovin.magicspells.util.config.ConfigData;

public abstract class BuildContext<T> {

	protected final Debugger debugger;

	protected BuildContext(Debugger debugger) {
		this.debugger = debugger;
	}

	@NotNull
	protected abstract NullConfigData<T> build();

	@NotNull
	protected <E> NullConfigData<E> required(
		TriFunction<ConfigurationSection, String, E, ConfigData<E>> configDataFun,
		ConfigurationSection config,
		String basePath,
		String subPath
	) {
		String error = "Missing '%s' at '%s'.".formatted(subPath, basePath);

		ConfigData<E> configData = configDataFun.apply(config, subPath, null);
		if (configData.isNull()) {
			debugger.config(error);
			return _ -> null;
		}

		return data -> {
			E object = configData.get(data);
			if (object == null) debugger.cast(error);
			return object;
		};
	}

	protected static <E> ConfigData<List<E>> configDataList(
		ConfigurationSection config,
		String basePath,
		String subPath,
		BiFunction<ConfigurationSection, String, ConfigData<? extends E>> configDataFun
	) {
		List<ConfigData<? extends E>> list = new ArrayList<>();
		List<?> configList = config.getList(subPath, List.of());
		for (int i = 0; i < configList.size(); i++) {
			if (!(configList.get(i) instanceof Map<?, ?> map)) continue;
			ConfigurationSection section = ConfigReaderUtil.mapToSection(map);

			String path = "%s.%s[%d]".formatted(basePath, subPath, i);

			list.add(configDataFun.apply(section, path));
		}

		return data -> {
			List<E> result = new ArrayList<>();

			for (ConfigData<? extends E> element : list) {
				E item = element.get(data);
				if (item == null) continue;
				result.add(item);
			}

			return result;
		};
	}

}
