package com.nisovin.magicspells.util.magicstring;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.exception.MagicException;

public abstract class PlaceholderFunction {

    abstract public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException;

    protected LivingEntity getEntity(String input, LivingEntity caster, LivingEntity target) {
        return switch (input) {
            case "caster" -> caster;
            case "target" -> target;
            default -> {
                try {
                    Entity entity = Bukkit.getEntity(UUID.fromString(input));
                    if (entity instanceof LivingEntity livingEntity) yield livingEntity;
                } catch (IllegalArgumentException ignored) {}
                yield Bukkit.getPlayer(input);
            }
        };
    }

    protected Player getPlayer(String input, LivingEntity caster, LivingEntity target) {
        LivingEntity entity = getEntity(input, caster, target);
        return entity instanceof Player player ? player : null;
    }

    protected static String setPrecision(double value, int precision) {
        if (precision < 0) return value + "";
        return BigDecimal.valueOf(value).setScale(precision, RoundingMode.HALF_UP).toString();
    }

    protected static String argAt(List<String> args, int index) {
        if (args == null) return null;
        try {
            return args.get(index);
        } catch (IndexOutOfBoundsException ignored) {}
        return null;
    }

    protected static int parsePrecisionAtArg(List<String> args, int index) {
        String precision = argAt(args, index);
        if (precision == null || !precision.matches("^\\d+$")) return -1;
        try {
            return Integer.parseInt(precision);
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    protected static boolean argAtIndexEquals(List<String> args, int index, String value) {
        return value.equals(argAt(args, index));
    }

}
