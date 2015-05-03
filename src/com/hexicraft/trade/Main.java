package com.hexicraft.trade;

import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin {

    private YamlFile items;

    public static final double PERCENT_CHANGE = 0.9;

    /**
     * Run when the plugin is enabled, loads the item prices
     */
    @Override
    public void onEnable() {
        // TODO: This should only be loaded each time it is needed (seems to be how other people do it)
        items = new YamlFile(this, "items.yml");
        updateItems();
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

    private ReturnCode trade(Player player) {
        player.sendMessage("Well done on typing this command :).");
        return ReturnCode.SUCCESS;
    }

    @SuppressWarnings("deprecation") // FU Mojang
    private ReturnCode sell(Player player) {
        MaterialData data = player.getItemInHand().getData();
        String path = data.getItemType().getId() + "." + data.getData();
        double price = items.getDouble(path);
        price = price * PERCENT_CHANGE;
        items.set(path, price);
        return ReturnCode.SUCCESS;
    }

    @SuppressWarnings("deprecation") // FU Mojang
    private ReturnCode price(Player player) {
        MaterialData data = player.getItemInHand().getData();
        String path = data.getItemType().getId() + "." + data.getData();
        player.sendMessage(String.valueOf(items.getDouble(path)));
        return ReturnCode.SUCCESS;
    }

    private void updateItems() {
        try {
            CsvFile csv = new CsvFile(this,
                    Bukkit.getServer().getPluginManager().getPlugin("Essentials").getDataFolder(), "items.csv");
            for (CSVRecord record : csv) {
                if (record.size() >= 3 && !record.get(1).equals("id")) {
                    String path = record.get(1) + "." + record.get(2);
                    if (items.get(path) == null) {
                        items.set(path, 50.0);
                    }
                }
            }
            items.saveFile();
        } catch (NullPointerException e) {
            getLogger().warning("Missing dependency: Essentials.\n" + e.getMessage());
        }
    }
}
