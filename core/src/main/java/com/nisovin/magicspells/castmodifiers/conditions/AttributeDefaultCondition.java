package com.nisovin.magicspells.castmodifiers.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.attribute.AttributeInstance;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.util.AttributeUtil;
import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;

@Name("attributedefault")
public class AttributeDefaultCondition extends OperatorCondition {

	private static final Pattern ATTRIBUTE_NAME_PATTERN = Pattern.compile("[-.\\w]+");

	private Attribute attribute;
	private double value;

	@Override
	public boolean initialize(@NotNull String var) {
		Matcher matcher = ATTRIBUTE_NAME_PATTERN.matcher(var);
		if (!matcher.find()) return false;

		String attributeName = matcher.group();

		attribute = AttributeUtil.getAttribute(attributeName);
		if (attribute == null) return false;

		String number = var.substring(attributeName.length());
		if (number.length() < 2 || !super.initialize(number)) return false;

		try {
			value = Double.parseDouble(number.substring(1));
		} catch (NumberFormatException e) {
			DebugHandler.debugNumberFormat(e);
			return false;
		}

		return true;
	}

	@Override
	public boolean check(LivingEntity caster) {
		EntityType entityType = caster.getType();
		if (!entityType.hasDefaultAttributes()) return false;

		AttributeInstance instance = entityType.getDefaultAttributes().getAttribute(attribute);
		if (instance == null) return false;

		return compare(instance.getBaseValue(), value);
	}

	@Override
	public boolean check(LivingEntity caster, LivingEntity target) {
		return check(target);
	}

	@Override
	public boolean check(LivingEntity caster, Location location) {
		return false;
	}

}
