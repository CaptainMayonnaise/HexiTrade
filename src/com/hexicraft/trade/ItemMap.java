package com.hexicraft.trade;

import com.hexicraft.trade.inventory.InventoryTab;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ollie
 * @version 1.0
 */
public class ItemMap extends HashMap<String, ItemListing> {

    @SuppressWarnings("deprecation")
    ItemMap(JavaPlugin plugin, Economy econ, YamlFile items) {
        Set<String> keys = items.getKeys(false);

        for (String key : keys) {
            try {
                String[] matData = key.split("-");
                ItemStack item = new MaterialData(Integer.parseInt(matData[0]),
                        (byte) Integer.parseInt(matData[1])).toItemStack(1);
                put(key, new ItemListing(key, item, items.getDouble(key + ".price"),
                        items.getStringList(key + ".name"), econ, items));
                InventoryTab.getTab(items.getInt(key + ".tab")).getItemKeys().add(key);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Error in items.yml: " + key + " is not a valid item name.");
            }
        }
    }

    public ItemListing getFromAlias(String alias) {
        alias = alias.toLowerCase();
        for (ItemListing item : values()) {
            for (String itemAlias : item.getAliases()) {
                if (Objects.equals(alias, itemAlias)) {
                    return item;
                }
            }
        }
        return null;
    }
}
