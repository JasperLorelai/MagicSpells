package com.nisovin.magicspells.util.itemreader;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import com.nisovin.magicspells.util.magicitems.MagicItemData;

import static com.nisovin.magicspells.util.magicitems.MagicItemData.MagicItemAttribute.ITEM_MODEL;

public class ItemModelHandler {

    private static final String CONFIG_NAME = ITEM_MODEL.toString();

    public static void process(ConfigurationSection config, ItemMeta meta, MagicItemData data) {
        if (!config.isString(CONFIG_NAME)) return;

        String itemModel = config.getString(CONFIG_NAME);

        meta.setItemModel(NamespacedKey.fromString(CONFIG_NAME));
        data.setAttribute(ITEM_MODEL, itemModel);
    }

    public static void processItemMeta(ItemMeta meta, MagicItemData data) {
        if (data.hasAttribute(ITEM_MODEL)) meta.setItemModel(NamespacedKey.fromString((String) data.getAttribute(ITEM_MODEL)));
    }

    public static void processMagicItemData(ItemMeta meta, MagicItemData data) {
        if (meta.hasItemModel()) data.setAttribute(ITEM_MODEL, meta.getItemModel());
    }

}
