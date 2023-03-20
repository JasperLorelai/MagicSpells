package com.nisovin.magicspells.util.magicstring.placeholders;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.exception.MagicException;
import com.nisovin.magicspells.util.magicstring.PlaceholderFunction;
import com.nisovin.magicspells.variables.variabletypes.PlayerStringVariable;
import com.nisovin.magicspells.variables.variabletypes.GlobalStringVariable;

public class VariablePlaceholder extends PlaceholderFunction {

    /*
     * (required) & [optional]
     * <var:[target,caster,name,uuid]:(varName):[max,min]:[precision]>
     */

    @Override
    public String apply(List<String> args, LivingEntity caster, LivingEntity target, String[] spellArgs) throws MagicException {
        if (args == null || args.size() == 0) throw new MagicException("No arguments passed to 'var' placeholder.");
        String ownerName = "caster";
        String varName = args.get(0);
        ValueType valueType = ValueType.CURRENT;
        int precision;
        try {
            String type = argAt(args, 1);
            if (type != null) valueType = ValueType.valueOf(type.toUpperCase());
            precision = parsePrecisionAtArg(args, 2);
        } catch (IllegalArgumentException ignored) {
            precision = parsePrecisionAtArg(args, 1);
        }

        Variable variable = MagicSpells.getVariableManager().getVariable(varName);
        // Check if the first argument might be variable owner.
        if (variable == null) {
            if (args.size() < 2) throw new MagicException("Variable specified in 'var' does not exist: " + varName);
            ownerName = varName;
            varName = args.get(1);

            variable = MagicSpells.getVariableManager().getVariable(varName);
            if (variable == null) throw new MagicException("Variable specified in 'var' does not exist: " + varName);
            try {
                String type = argAt(args, 2);
                if (type != null) valueType = ValueType.valueOf(type.toUpperCase());
                precision = parsePrecisionAtArg(args, 3);
            } catch (IllegalArgumentException ignored) {
                precision = parsePrecisionAtArg(args, 2);
            }
        }
        Player owner = getPlayer(ownerName, caster, target);

        // Get variable value.
        if (variable instanceof PlayerStringVariable || variable instanceof GlobalStringVariable) {
            return owner == null ? variable.getDefaultStringValue() : variable.getStringValue(owner);
        }
        double value = switch (valueType) {
            case CURRENT -> variable.getValue(owner);
            case MIN -> variable.getMinValue();
            case MAX -> variable.getMaxValue();
        };
        return setPrecision(value, precision);
    }

    private enum ValueType {
        CURRENT,
        MIN,
        MAX
    }

}
