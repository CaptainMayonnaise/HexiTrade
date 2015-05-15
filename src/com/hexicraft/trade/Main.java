package com.hexicraft.trade;

import net.milkbowl.vault.economy.Economy;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin {

    private YamlFile items;
    public static Economy econ = null;

    public static final double PERCENT_CHANGE = 1.1;

    /**
     * Run when the plugin is enabled, loads the item prices
     */
    @Override
    public void onEnable() {
        if (!setupEconomy()) {
            getLogger().severe("Missing dependency: Vault.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        items = new YamlFile(this, "items.yml");
        if (!updateItems()) {
            getLogger().severe("Missing dependency: Vault.");
            getServer().getPluginManager().disablePlugin(this);
        }
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
     * Updates the item price list with any new items in the essentials items.csv file
     * @return True if Essentials is found, false otherwise
     */
    private boolean updateItems() {
        if (getServer().getPluginManager().getPlugin("Essentials") == null) {
            return false;
        }

        File file = new File(getServer().getPluginManager().getPlugin("Essentials").getDataFolder(), "items.csv");
        if (!file.exists()) {
            return false;
        }

        CsvFile csv = new CsvFile(this, file);
        for (CSVRecord record : csv) {
            if (record.size() >= 3 && !record.get(1).equals("id")) {
                String path = record.get(1) + "." + record.get(2);
                if (items.get(path) == null) {
                    items.set(path, 50.0);
                }
            }
        }
        items.saveFile();

        return true;
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
}
