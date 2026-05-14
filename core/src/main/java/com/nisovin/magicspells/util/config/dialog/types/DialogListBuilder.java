package com.nisovin.magicspells.util.config.dialog.types;

import java.util.List;
import java.util.ArrayList;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Registry;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.TagKey;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.set.RegistrySet;
import io.papermc.paper.registry.set.RegistryKeySet;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.type.DialogType;

import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import com.nisovin.magicspells.util.config.dialog.Debugger;
import com.nisovin.magicspells.util.config.dialog.BuildContext;
import com.nisovin.magicspells.util.config.dialog.NullConfigData;
import com.nisovin.magicspells.util.config.dialog.action.ActionButtonBuilder;

@SuppressWarnings("UnstableApiUsage")
public class DialogListBuilder extends BuildContext<DialogType> {

	private final ConfigurationSection config;
	private final String basePath;

	protected static NullConfigData<DialogType> create(Debugger debugger, ConfigurationSection config, String basePath) {
		return new DialogListBuilder(debugger, config, basePath).build();
	}

	private DialogListBuilder(Debugger debugger, ConfigurationSection config, String basePath) {
		super(debugger);
		this.config = config;
		this.basePath = basePath;
	}

	@Override
	protected @NotNull NullConfigData<DialogType> build() {
		RegistryKeySet<Dialog> dialogs = getDialogKeySet();

		ConfigurationSection exitSection = config.getConfigurationSection("exit");
		NullConfigData<ActionButton> exit = ActionButtonBuilder.createOptional(debugger, exitSection, basePath, "exit");

		ConfigData<Integer> columns = ConfigDataUtil.getInteger(config, "columns", 2);
		ConfigData<Integer> buttonWidth = ConfigDataUtil.getInteger(config, "button-width", 150);

		return data -> {
			try {
				return DialogType.dialogList(dialogs, exit.get(data), columns.get(data), buttonWidth.get(data));
			} catch (IllegalArgumentException e) {
				debugger.cast("Invalid '" + basePath + "': " + e.getMessage());
				return null;
			}
		};
	}

	private RegistryKeySet<Dialog> getDialogKeySet() {
		List<TypedKey<Dialog>> keys = new ArrayList<>();
		Registry<Dialog> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.DIALOG);

		List<String> strings = config.getStringList("dialogs");
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			String subPath = "dialogs[" + i + "]";

			if (!string.startsWith("#")) {
				NamespacedKey key = NamespacedKey.fromString(string);
				if (key == null || registry.get(key) == null) {
					debugger.config("Invalid '%s' registry entry at '%s': '%s'".formatted(subPath, basePath, string));
					continue;
				}

				keys.add(TypedKey.create(RegistryKey.DIALOG, key));
				continue;
			}

			NamespacedKey key = NamespacedKey.fromString(string.substring(1));
			if (key == null) {
				debugger.config("Invalid '%s' key at '%s': '%s'".formatted(subPath, basePath, string));
				continue;
			}

			TagKey<Dialog> tagKey = TagKey.create(RegistryKey.DIALOG, key);
			if (!registry.hasTag(tagKey)) {
				debugger.config("Invalid '%s' key at '%s': '%s'".formatted(subPath, basePath, string));
				continue;
			}

			keys.addAll(registry.getTag(tagKey).values());
		}

		return RegistrySet.keySet(RegistryKey.DIALOG, keys);
	}

}
