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
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin implements Listener {

    private ItemsFile items;
    private Economy econ = null;
    private LinkedHashMap<String, ItemStack> itemMap = new LinkedHashMap<>();

    public static final double PERCENT_CHANGE = 1.01;

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

        setupItemMap();
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

    @SuppressWarnings("deprecation") // FU Mojang (/ Bukkit?)
    private void setupItemMap() {
        Set<String> itemSet = items.getKeys(false);

        for (String element : itemSet) {
            String[] split = element.split("-");
            ItemStack item = new MaterialData(Integer.parseInt(split[0]),
                    (byte) Integer.parseInt(split[1])).toItemStack(1);
            setItemPrice(item, items.getDouble(element + ".price"));
            itemMap.put(element, item);
        }
    }

    private void setItemPrice(ItemStack itemStack, double price) {
        ItemMeta meta = itemStack.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.RESET + "Price: " + econ.format(price));
        lore.add(ChatColor.GOLD + "<click to buy>");
        lore.add(ChatColor.GOLD + "<shift-click to buy stack>");
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
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
                    code = sell(player, args);
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
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
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
    private ReturnCode sell(Player player, String[] args) {
        try {
            int amount;
            if (args.length > 0) {
                amount = Integer.parseInt(args[0]);
            } else {
                amount = 1;
            }

            ItemStack itemInHand = player.getItemInHand();

            if (itemInHand.getAmount() < amount) {
                return ReturnCode.TOO_FEW_ITEMS;
            }

            MaterialData data = itemInHand.getData(); // Data of item in hand
            String element = data.getItemType().getId() + "-" + data.getData() + ".price"; // YAML path of item price
            econ.depositPlayer(player, makeSale(element, amount)); // Give the player the money

            if (itemInHand.getAmount() <= amount) {
                player.setItemInHand(null); // Take the item
            } else {
                itemInHand.setAmount(itemInHand.getAmount() - amount);
            }
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
        return ReturnCode.SUCCESS;
    }

    private synchronized double makeSale(String element, int numOfSales) {
        double price = items.getDouble(element);
        double profit = 0;
        for (int i = 0; i < numOfSales; i++) {
            profit = profit + price;
            price = price / PERCENT_CHANGE;
        }
        items.set(element, price);
        items.saveFile();
        setItemPrice(itemMap.get(element), items.getDouble(element));
        return profit;
    }

    private synchronized double makePurchase(String element, int numOfPurchases) {
        double price = items.getDouble(element);
        double cost = 0;
        for (int i = 0; i < numOfPurchases; i++) {
            cost = cost + price;
            price = price * PERCENT_CHANGE;
        }
        items.set(element, price);
        items.saveFile();
        return cost;
    }

    /**
     * Reports the price of the item in hand
     * @param player Player who asked for price
     * @return Success!
     */
    @SuppressWarnings("deprecation") // FU Mojang (/ Bukkit?)
    private ReturnCode price(Player player) {
        MaterialData data = player.getItemInHand().getData();
        String element = data.getItemType().getId() + "-" + data.getData() + ".price";
        player.sendMessage(String.valueOf(items.getDouble(element)));
        return ReturnCode.SUCCESS;
    }

    /**
     * Opens the first inventory for the player
     * @param player The player that typed the command
     * @return Success!
     */
    private ReturnCode buy(Player player) {
        player.openInventory(generateInventory(1));
        return ReturnCode.SUCCESS;
    }

    private Inventory generateInventory(int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, "HexiTrade");
        int count = 45; // First page is 1 not 0
        for (ItemStack item : itemMap.values()) {
            int currentPage = count / 45;
            if (currentPage == page) {
                inventory.setItem((count % 45) + 9, item); // Put the item in the inventory at the current slot
            } else if (currentPage > page) {
                if (count % 45 == 0) { // If there is an item after the last in the inv (and thus there is a next page)
                    inventory.setItem(7, generatePage("Page " + (currentPage), "Next page"));
                }
                break;
            }
            count++;
        }
        if (page != 1) { // If there are previous pages
            inventory.setItem(1, generatePage("Page " + (page - 1), "Previous page"));
        }
        inventory.setItem(4, generatePage("Page " + page, "Current page"));
        return inventory;
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
     * Detects whether or not the player tried to change page and stops them from picking up the items
     * @param event The inventory click event
     */
    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("HexiTrade") &&
                event.getCurrentItem() != null &&
                event.getCurrentItem().getData().getItemType() != Material.AIR) {
            event.setCancelled(true);
            int slot = event.getSlot();
            int inv = Integer.parseInt(
                    event.getInventory().getItem(4).getItemMeta().getDisplayName().split("\\s+")[1]
            );
            if (slot == 1) {
                event.getWhoClicked().openInventory(generateInventory(inv - 1));
            } else if (slot == 7) {
                event.getWhoClicked().openInventory(generateInventory(inv + 1));
            }
        }
    }
}
