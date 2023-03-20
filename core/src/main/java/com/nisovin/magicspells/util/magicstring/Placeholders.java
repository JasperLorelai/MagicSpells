package com.nisovin.magicspells.util.magicstring;

import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.util.magicstring.placeholders.*;

public enum Placeholders {

    ARG(ArgumentPlaceholder.class),
    BUFF(BuffPlaceholder.class),
    CHARGES(ChargesPlaceholder.class),
    COOLDOWN(CooldownPlaceholder.class),
    DATA(DataPlaceholder.class),
    MANA(ManaPlaceholder.class),
    PAPI(PAPIPlaceholder.class),
    SELECTED_SPELL(SelectedSpellPlaceholder.class),
    VAR(VariablePlaceholder.class),
    ;

    private final Class<? extends PlaceholderFunction> clazz;

    Placeholders(Class<? extends PlaceholderFunction> clazz) {
        this.clazz = clazz;
    }

    public PlaceholderFunction getInstance() {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            DebugHandler.debugGeneral(e);
            return null;
        }
    }

}
