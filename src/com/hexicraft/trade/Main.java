package com.hexicraft.trade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin implements Listener {

    private ItemsFile items;
    private Economy econ = null;
    private ArrayList<Inventory> inventories = new ArrayList<>();

    public static final double PERCENT_CHANGE = 1.1;

    /**
     * Run when the plugin is enabled, loads the item prices
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);


        if (!setupEconomy()) {
            getLogger().severe("Missing dependency: Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        items = new ItemsFile(this, "items.yml");

        setupInventories();
    }

    /**
     * Sets the economy plugin
     * @return True if Vault is found, false otherwise
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        econ = rsp.getProvider();
        return econ != null;
    }

    /**
     * Generates the inventories of items that users will use to buy items
     */
    @SuppressWarnings("deprecation")  // FU Mojang (/ Bukkit?)
    private void setupInventories() {
        Set<String> itemSet = items.getKeys(true);

        // Remove the root elements
        Set<String> removeSet = new HashSet<>();
        for (String element : itemSet) {
            if (!element.contains(".")) {
                removeSet.add(element);
            }
        }
        itemSet.removeAll(removeSet);

        // Initialise variables for loop
        Inventory inventory = Bukkit.createInventory(null, 54, "HexiTrade");
        inventory.setItem(4, generatePage("Page 1", "Current page"));
        int count = 0;

        // Add each item to an inventory
        for (String element : itemSet) {
            int slot = count % 45; // Each inventory can have 45 items
            String[] split = element.split("\\.");
            inventory.setItem(
                    slot + 9,
                    new MaterialData(
                            Integer.parseInt(split[0]),
                            (byte) Integer.parseInt(split[1])
                    ).toItemStack(1)
            );

            if (slot == 44) { // If the last slot was filled
                inventory.setItem(6, generatePage("Page " + (count / 45 + 2), "Next page"));
                inventories.add(inventory);
                inventory = Bukkit.createInventory(null, 54, "HexiTrade");
                inventory.setItem(2, generatePage("Page " + (count / 45 + 1), "Previous page"));
                count++;
                inventory.setItem(4, generatePage("Page " + (count / 45 + 1), "Current page"));
            } else {
                count++;
            }
        }
        inventories.add(inventory); // Add the last incomplete inventory
    }

    /**
     * Generates a page item for the buy inventory
     * @param title The display name of the page
     * @param desc The description of the page
     * @return The generated page
     */
    private ItemStack generatePage(String title, String desc) {
        ItemStack paper = new MaterialData(Material.PAPER).toItemStack(1);

        ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(ChatColor.RESET + title);

        ArrayList<String> descList = new ArrayList<>();
        descList.add(ChatColor.GOLD + desc);
        meta.setLore(descList);

        paper.setItemMeta(meta);
        return paper;
    }

    /**
     * Run when the plugin is disabled
     */
    @Override
    public void onDisable() {
        items.saveFile();
    }

    /**
     * Executes the given command, returning its success
     * @param sender Source of the command
     * @param cmd Command which was executed
     * @param label Alias of the command which was used
     * @param args Passed command arguments
     * @return Always returns true, error messages are handled internally
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        ReturnCode code;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (cmd.getName().toLowerCase()) {
                case "trade":
                    code = trade(player);
                    break;
                case "sell":
                    code = sell(player);
                    break;
                case "price":
                    code = price(player);
                    break;
                case "buy":
                    code = buy(player);
                    break;
                default:
                    code = ReturnCode.UNRECOGNISED_COMMAND;
            }
        } else {
            code = ReturnCode.NOT_PLAYER;
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(code.message);
        }
        return true;
    }

    /**
     * Will probably be a help command later
     * @param player Player who needs help
     * @return Success!
     */
    private ReturnCode trade(Player player) {
        player.sendMessage("Well done on typing this command :).");
        return ReturnCode.SUCCESS;
    }

    /**
     * 'Sells' the item in player's hand
     * @param player Player who is selling their item
     * @return Success!
     */
    @SuppressWarnings("deprecation") // FU Mojang (/ Bukkit?)
    private ReturnCode sell(Player player) {
        MaterialData data = player.getItemInHand().getData(); // Data of item in hand
        String path = data.getItemType().getId() + "." + data.getData(); // YAML path of item
        double price = items.getDouble(path) / PERCENT_CHANGE; // Set price as old price / %change

        items.set(path, price); // Set new price of item
        items.saveFile();

        econ.depositPlayer(player, price); // Give the player the money
        player.getInventory().setItemInHand(null); // Take the item

        return ReturnCode.SUCCESS;
    }

    /**
     * Reports the price of the item in hand
     * @param player Player who asked for price
     * @return Success!
     */
    @SuppressWarnings("deprecation") // FU Mojang (/ Bukkit?)
    private ReturnCode price(Player player) {
        MaterialData data = player.getItemInHand().getData();
        String path = data.getItemType().getId() + "." + data.getData();
        player.sendMessage(String.valueOf(items.getDouble(path)));
        return ReturnCode.SUCCESS;
    }

    /**
     * Opens the first inventory for the player
     * @param player The player that typed the command
     * @return Success!
     */
    private ReturnCode buy(Player player) {
        player.openInventory(inventories.get(0));
        return ReturnCode.SUCCESS;
    }

    /**
     * Detects whether or not the player tried to change page and stops them from picking up the items
     * @param event The inventory click event
     */
    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && event.getInventory().getTitle().contains("HexiTrade")) {
            event.setCancelled(true);
            int slot = event.getSlot();
            int inv = Integer.parseInt(
                    event.getInventory().getItem(4).getItemMeta().getDisplayName().split("\\s+")[1]
            ) - 1; // Subtract 1 since page numbering starts at 1
            if (event.getCurrentItem().getData().getItemType() == Material.PAPER) {
                if (slot == 2) {
                    event.getWhoClicked().openInventory(inventories.get(inv - 1));
                } else if (slot == 6) {
                    event.getWhoClicked().openInventory(inventories.get(inv + 1));
                }
            }
        }
    }
}
