package com.hexicraft.trade;

import com.hexicraft.trade.logger.FileLogger;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ollie
 * @version 1.0
 */
public class ItemListing {

    private String key;
    private ItemStack item;
    private double price;
    private List<String> aliases;
    private HexiTrade plugin;
    private ItemMap itemMap;

    /**
     * Constructs an ItemListing
     * @param key The Item's key in the ItemMap
     * @param item The ItemStack the listing represents
     * @param price The price of the item
     * @param aliases List of aliases for the item
     * @param plugin The HexiTrade object
     */
    ItemListing(String key, ItemStack item, double price, List<String> aliases, HexiTrade plugin, ItemMap itemMap) {
        this.key = key;
        this.item = item;
        this.price = price;
        this.aliases = aliases;
        this.plugin = plugin;
        this.itemMap = itemMap;
        updateItemPrice(false);
    }

    /**
     * Should be called when price is changed, updates the item's meta and the saved price in the items file
     */
    private void updateItemPrice(boolean save) {
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Price: " + plugin.getEcon().format(price * plugin.getPercentChange()));
        lore.add(ChatColor.GOLD + "<click to buy>");
        lore.add(ChatColor.GOLD + "<shift-click to buy stack>");
        meta.setLore(lore);
        item.setItemMeta(meta);
        if (save) {
            itemMap.getItems().set(key + ".price", price);
        } else {
            itemMap.getItems().setNoSave(key + ".price", price);
        }
    }

    /**
     * Sells an amount of items for the player and adjusts the price accordingly
     * @param amount The amount of sales to make.
     * @param player The player buying the items.
     */
    public synchronized double sell(int amount, Player player, FileLogger fileLogger) {
        double profit = 0;
        for (int i = 0; i < amount; i++) {
            profit += price;
            price *= 1 / plugin.getPercentChange();
        }
        profit = profit / plugin.getSellTax();
        plugin.getEcon().depositPlayer(player, profit);
        updateItemPrice(true);
        return profit;
    }

    public void sellPrice(int amount, Player player) {
        double price = this.price;
        double profit = 0;
        for (int i = 0; i < amount; i++) {
            profit += price;
            price *= 1 / plugin.getPercentChange();
        }
        profit = profit / plugin.getSellTax();
        player.sendMessage(ChatColor.GOLD + "Value of " + amount + " " + aliases.get(0) + ": " +
                ChatColor.WHITE + plugin.getEcon().format(profit));
    }

    public void buyPrice(int amount, Player player) {
        double price = this.price;
        double cost = 0;
        for (int i = 0; i < amount; i++) {
            price *= plugin.getPercentChange();
            cost += price;
        }
        player.sendMessage(ChatColor.GOLD + "Price of " + amount + " " + aliases.get(0) + ": " +
                ChatColor.WHITE + plugin.getEcon().format(cost));
    }

    /**
     * Buys an amount of items for the player and adjusts the price accordingly
     * @param amount The amount of purchases to make.
     * @param player The player buying the items.
     * @return The purchased items.
     */
    public synchronized ItemStack buy(int amount, Player player, FileLogger fileLogger) {
        double cost = 0;
        double oldPrice = price;
        for (int i = 0; i < amount; i++) {
            price *= plugin.getPercentChange();
            cost += price;
        }

        if (plugin.getEcon().has(player, cost)) { // Check player can afford item
            plugin.getEcon().withdrawPlayer(player, cost);

            ItemStack item = this.item.clone();
            item.setAmount(amount);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(new ArrayList<String>());
            item.setItemMeta(meta);

            updateItemPrice(true);
            player.sendMessage(ChatColor.GOLD + "Bought " + amount + " " + aliases.get(0) + " for " +
                    ChatColor.WHITE + plugin.getEcon().format(cost) + ChatColor.GOLD + ".");
            fileLogger.log(player.getName() + " bought " + amount + " " + aliases.get(0) + " for " +
                    plugin.getEcon().format(cost));
            return item;
        } else {
            price = oldPrice;
            updateItemPrice(true);
            player.sendMessage(ChatColor.RED + "You can't afford this item.");
            return null;
        }
    }

    public void setPrice(double price, Player player, FileLogger logger) {
        this.price = price;
        updateItemPrice(true);
        player.sendMessage(ChatColor.GOLD + "Setting price of " + aliases.get(0) + " to " + ChatColor.WHITE +
                plugin.getEcon().format(price) + ChatColor.GOLD + ".");
        logger.log(player.getName() + " set the price of " + aliases.get(0) + " to " + plugin.getEcon().format(price));
    }

    public List<String> getAliases() {
        return aliases;
    }

    public ItemStack getItem() {
        return item;
    }
}
