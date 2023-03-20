package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;

public class ArgumentPlaceholder extends PlaceholderFunction {

    /*
     * (required) & [optional]
     * <arg:(index):[defaultValue]>
     */
    @Override
    public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
        if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'arg' placeholder.");
        if (spellArgs == null) return "";
        String value = null;
        try {
            // <arg> starts with index 1.
            int index = Integer.parseInt(args.get(0)) - 1;
            value = spellArgs[index];
        } catch (NumberFormatException | IndexOutOfBoundsException ignored) {}
        if (value == null) value = "";
        // Try to return default value if empty.
        return value.isEmpty() && args.size() > 1 ? args.get(1) : value;
    }

}
