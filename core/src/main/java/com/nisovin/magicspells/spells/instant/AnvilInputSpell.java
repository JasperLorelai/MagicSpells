package com.nisovin.magicspells.spells.instant;

import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;

import net.kyori.adventure.text.Component;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MenuType;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.AnvilView;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.spells.InstantSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.variables.variabletypes.GlobalStringVariable;
import com.nisovin.magicspells.variables.variabletypes.PlayerStringVariable;

@SuppressWarnings("UnstableApiUsage")
public class AnvilInputSpell extends InstantSpell implements TargetedEntitySpell {

	private static final ItemStack PAPER = ItemStack.of(Material.PAPER);
	static {
		PAPER.editMeta(meta -> meta.customName(Component.empty()));
	}

	private final Map<Player, AnvilView> open = new HashMap<>();

	private final ConfigData<Component> title;

	private Variable variable;
	private Subspell spellOnClose;

	public AnvilInputSpell(MagicConfig config, String spellName) {
		super(config, spellName);

		title = getConfigDataComponent("title", null);
	}

	@Override
	public void initializeVariables() {
		super.initializeVariables();

		String variableName = getConfigString("variable-name", null);
		Variable variable = MagicSpells.getVariableManager().getVariable(variableName);
		if (variable instanceof PlayerStringVariable || variable instanceof GlobalStringVariable) {
			this.variable = variable;
			return;
		}
		MagicSpells.error("AnvilInputSpell '" + internalName + "' has an invalid 'variable-name' defined!");
	}

	@Override
	protected void initialize() {
		super.initialize();

		spellOnClose = initSubspell(
			getConfigString("spell-on-close", null),
			"AnvilInputSpell " + internalName + " has an invalid 'spell-on-close' defined!",
			true
		);
	}

	@Override
	public CastResult cast(SpellData data) {
		return castAtEntity(data.target(data.caster()));
	}

	@Override
	public CastResult castAtEntity(SpellData data) {
		if (!(data.target() instanceof Player player)) return noTarget(data);

		AnvilView anvil = MenuType.ANVIL.create(player, title.get(data));
		anvil.setMaximumRepairCost(0);
		anvil.setItem(0, PAPER);
		anvil.open();

		open.put(player, anvil);

		playSpellEffects(data);
		return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
	}

	@Override
	protected void turnOff() {
		for (AnvilView anvil : new HashSet<>(open.values())) anvil.close();
		open.clear();
	}

	@EventHandler
	private void onClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) return;

		AnvilView anvil = open.get(player);
		if (anvil == null) return;

		event.setCancelled(true);

		if (!event.getClick().isMouseClick()) return;
		if (event.getSlotType() != InventoryType.SlotType.RESULT) return;

		if (variable != null) variable.parseAndSet(player, anvil.getRenameText());

		anvil.close();
	}

	@EventHandler
	private void onClose(InventoryCloseEvent event) {
		if (!(event.getPlayer() instanceof Player player)) return;

		AnvilView anvil = open.get(player);
		if (anvil == null) return;

		anvil.setItem(0, null);

		if (spellOnClose != null) spellOnClose.subcast(new SpellData(player));

		open.remove(player);
	}

}
