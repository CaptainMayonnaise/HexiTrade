package com.hexicraft.trade;

import com.hexicraft.trade.commands.*;
import com.hexicraft.trade.inventory.TradeInventory;
import com.hexicraft.trade.logger.FileLogger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

/**
 * @author Ollie
 * @version 1.0
 */
public class HexiTrade extends JavaPlugin implements Listener {

    private Economy econ;
    private HashMap<String, ItemMap> worlds;
    private boolean active = false;
    private double percentChange;
    private double sellTax;
    private FileLogger fileLogger = new FileLogger(getDataFolder());

    /**
     * Run when the plugin is enabled, loads the item prices
     */
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("trade").setExecutor(new TradeCommand(this));
        getCommand("sell").setExecutor(new SellCommand(this));
        getCommand("buy").setExecutor(new BuyCommand(this));

        ValueCommand valueCommand = new ValueCommand(this);
        getCommand("worth").setExecutor(valueCommand);
        getCommand("price").setExecutor(valueCommand);

        reload();
    }

    public void reload() {
        active = false;

        if (!setupEconomy()) {
            getLogger().severe("Missing dependency: Vault and/or compatible economy plugin.");
            return;
        }

        if (!setupConfig()) {
            getLogger().severe("Could not load config.yml.");
            return;
        }

        active = true;
    }

    /**
     * Sets the economy plugin
     * @return True if Vault is found, false otherwise
     */
    private boolean setupEconomy() {
        econ = null;
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

    private boolean setupConfig() {
        YamlFile config = new YamlFile(this, "config.yml");
        if (!config.loadFile()) {
            return false;
        }

        percentChange = config.getDouble("percent-change");
        if (percentChange == 0) {
            percentChange = 1.001;
            config.set("percent-change", 1.001);
        }
        sellTax = config.getDouble("sell-tax");
        if (sellTax == 0) {
            sellTax = 1.15;
            config.set("sell-tax", 1.15);
        }
        worlds = new HashMap<>();
        ConfigurationSection section = config.getConfigurationSection("groups");
        for (String key : section.getKeys(false)) {
            ItemMap itemMap = new ItemMap(this, new YamlFile(this, key + ".yml", "items.yml"));
            for (String world : config.getStringList("groups." + key)) {
                worlds.put(world, itemMap);
            }
        }
        config.set("groups", section);
        return true;
    }

    /**
     * Detects whether or not the player tried to change page and stops them from picking up the items
     * @param event The inventory click event
     */
    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("HexiTrade")) {
            event.setCancelled(true);
            if (active && event.getWhoClicked() instanceof Player &&
                    (event.getAction() == InventoryAction.PICKUP_ALL ||
                            event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                Player player = (Player) event.getWhoClicked();
                ItemMap itemMap = getWorlds().get(event.getWhoClicked().getLocation().getWorld().getName());
                if (itemMap == null) {
                    player.sendMessage(ChatColor.RED + ReturnCode.INVALID_WORLD.getMessage(getCommand("buy")));
                } else {
                    TradeInventory tradeInventory = new TradeInventory(this, event.getInventory(), event, itemMap);
                    ItemListing listing = tradeInventory.getListing();
                    if (listing == null) {
                        player.openInventory(tradeInventory.getInventory());
                    } else {
                        int amount = event.isShiftClick() ? listing.getItem().getMaxStackSize() : 1;
                        ItemStack item = listing.buy(amount, player, fileLogger);
                        event.getInventory().setItem(event.getRawSlot(), listing.getItem());
                        addItem(item, player);
                    }
                }
            }
        }
    }

    public static void addItem(ItemStack item, Player player) {
        if (item != null) {
            HashMap<Integer, ItemStack> dropItems = player.getInventory().addItem(item);
            if (dropItems.size() != 0) {
                Location location = player.getLocation();
                location.setY(location.getY() + 1);
                player.getWorld().dropItem(location, dropItems.get(0));
            }
        }
    }

    /**
     * Source: http://stackoverflow.com/questions/237159/whats-the-best-way-to-check-to-see-if-a-string-represents-an-integer-in-java
     * @param str String to be tested.
     * @return Whether or not the string is an integer.
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length == 0) {
            return false;
        }
        int i = 0;
        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }
            i = 1;
        }
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public boolean isActive() {
        return active;
    }

    public FileLogger getFileLogger() {
        return fileLogger;
    }

    public Economy getEcon() {
        return econ;
    }

    public double getPercentChange() {
        return percentChange;
    }

    public double getSellTax() {
        return sellTax;
    }

    public HashMap<String, ItemMap> getWorlds() {
        return worlds;
    }
}

