package com.hexicraft.trade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
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
    private Economy econ;
    private YamlFile items;
    private double percentChange;

    /**
     * Sets up an ItemListing object
     * @param item An item stack of the item.
     * @param price The price of the item.
     * @param econ The Economy object.
     */
    ItemListing(String key, ItemStack item, double price, List<String> aliases, Economy econ, YamlFile items,
                double percentChange) {
        this.key = key;
        this.item = item;
        this.price = price;
        this.aliases = aliases;
        this.econ = econ;
        this.items = items;
        this.percentChange = percentChange;
        updateItemPrice(false);
    }

    /**
     * Should be called when price is changed, updates the item's meta and the saved price in the items file
     */
    private void updateItemPrice(boolean save) {
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Price: " + econ.format(price * percentChange));
        lore.add(ChatColor.GOLD + "<click to buy>");
        lore.add(ChatColor.GOLD + "<shift-click to buy stack>");
        meta.setLore(lore);
        item.setItemMeta(meta);
        if (save) {
            items.set(key + ".price", price);
        } else {
            items.setNoSave(key + ".price", price);
        }
    }

    /**
     * Sells an amount of items for the player and adjusts the price accordingly
     * @param amount The amount of sales to make.
     * @param player The player buying the items.
     */
    public synchronized void sell(int amount, Player player) {
        double profit = 0;
        for (int i = 0; i < amount; i++) {
            profit += price;
            price *= 1 / percentChange;
        }
        econ.depositPlayer(player, profit);
        updateItemPrice(true);

        player.sendMessage(ChatColor.GOLD + "Sold " + amount + " " + aliases.get(0) + " for " +
                ChatColor.WHITE + econ.format(profit) + ChatColor.GOLD + ".");
    }

    public void sellPrice(int amount, Player player) {
        double price = this.price;
        double profit = 0;
        for (int i = 0; i < amount; i++) {
            profit += price;
            price *= 1 / percentChange;
        }
        player.sendMessage(ChatColor.GOLD + "Value of " + amount + " " + aliases.get(0) + ": " +
                ChatColor.WHITE + econ.format(profit));
        if (amount != 1) {
            player.sendMessage(ChatColor.GOLD + "Value of 1 " + aliases.get(0) + ": " +
                    ChatColor.WHITE + econ.format(price));
        }
    }

    public void buyPrice(int amount, Player player) {
        double price = this.price;
        double cost = 0;
        for (int i = 0; i < amount; i++) {
            price *= percentChange;
            cost += price;
        }
        player.sendMessage(ChatColor.GOLD + "Price of " + amount + " " + aliases.get(0) + ": " +
                ChatColor.WHITE + econ.format(cost));
    }

    /**
     * Buys an amount of items for the player and adjusts the price accordingly
     * @param amount The amount of purchases to make.
     * @param player The player buying the items.
     * @return The purchased items.
     */
    public synchronized ItemStack buy(int amount, Player player) {
        double cost = 0;
        double oldPrice = price;
        for (int i = 0; i < amount; i++) {
            price *= percentChange;
            cost += price;
        }

        if (econ.has(player, cost)) { // Check player can afford item
            econ.withdrawPlayer(player, cost);

            ItemStack item = this.item.clone();
            item.setAmount(amount);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(new ArrayList<String>());
            item.setItemMeta(meta);

            updateItemPrice(true);
            player.sendMessage(ChatColor.GOLD + "Bought " + amount + " " + aliases.get(0) + " for " +
                    ChatColor.WHITE + econ.format(cost) + ChatColor.GOLD + ".");
            return item;
        } else {
            price = oldPrice;
            updateItemPrice(true);
            player.sendMessage(ChatColor.RED + "You can't afford this item.");
            return null;
        }
    }

    public void setPrice(double price, Player player) {
        this.price = price;
        updateItemPrice(true);
        player.sendMessage(ChatColor.GOLD + "Setting price of " + aliases.get(0) + " to " + ChatColor.WHITE +
                econ.format(price) + ChatColor.GOLD + ".");
    }

    public List<String> getAliases() {
        return aliases;
    }

    public ItemStack getItem() {
        return item;
    }
}
