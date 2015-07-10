package com.hexicraft.trade;

import com.hexicraft.trade.inventory.InventoryTab;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author Ollie
 * @version 1.0
 */
public class ItemMap extends HashMap<String, ItemListing> {

    @SuppressWarnings("deprecation")
    ItemMap(HexiTrade plugin, YamlFile items) {
        Set<String> keys = items.getKeys(false);

        for (String key : keys) {
            try {
                String[] matData = key.split("-");
                ItemStack item = new MaterialData(Integer.parseInt(matData[0]),
                        (byte) Integer.parseInt(matData[1])).toItemStack(1);

                double price = items.getDouble(key + ".price");
                List<String> aliases = items.getStringList(key + ".name");
                put(key, new ItemListing(key, item, price, aliases, plugin));

                InventoryTab tab = InventoryTab.getTab(items.getInt(key + ".tab"));
                plugin.getTabs().get(tab).add(key);
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

    @SuppressWarnings("deprecation")
    public ItemListing getFromStack(ItemStack stack) {
        MaterialData data = stack.getData();
        String key = data.getItemType().getId() + "-" + data.getData();
        return get(key);
    }
}
