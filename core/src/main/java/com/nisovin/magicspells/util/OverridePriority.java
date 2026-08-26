package com.nisovin.magicspells.util;

import org.bukkit.event.EventHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on {@link EventHandler} annotated methods implemented by
 * PassiveSpell triggers to allow for configurable override of their priority.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OverridePriority {

}
