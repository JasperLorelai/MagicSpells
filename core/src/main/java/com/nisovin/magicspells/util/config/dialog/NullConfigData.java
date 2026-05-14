package com.nisovin.magicspells.util.config.dialog;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;

public interface NullConfigData<T> extends ConfigData<T> {

	@Override
	@Nullable
	T get(@NotNull SpellData data);

}
