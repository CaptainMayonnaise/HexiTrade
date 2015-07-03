package com.hexicraft.trade;

import com.hexicraft.trade.inventory.TradeInventory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author Ollie
 * @version 1.0
 */
public class Main extends JavaPlugin implements Listener {

    private Economy econ;
    private ItemMap itemMap;
    private boolean enabled;

    public static final double PERCENT_CHANGE = 1.001;

    @Override
    public void onLoad() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    /**
     * Run when the plugin is enabled, loads the item prices
     */
    @Override
    public void onEnable() {
        enabled = false;

        if (!setupEconomy()) {
            getLogger().severe("Missing dependency: Vault and/or compatible economy plugin.");
            return;
        }

        if (!setupItems()) {
            getLogger().severe("Could not load items.yml.");
            return;
        }
        enabled = true;
    }

    private boolean setupItems() {
        itemMap = null;
        YamlFile items = new YamlFile(this, "items.yml");
        if (!items.loadFile()) {
            return false;
        }

        itemMap = new ItemMap(this, econ, items);
        return true;
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
            String command = cmd.getName().toLowerCase();
            if (!enabled) {
                if ((Objects.equals(command, "trade") && args.length > 0 && Objects.equals(args[0], "reload"))) {
                    onEnable();
                    code = ReturnCode.SUCCESS;
                } else {
                    code = ReturnCode.NOT_ENABLED;
                }
            } else {
                switch (command) {
                    case "trade":
                        code = trade(player, args);
                        break;
                    case "sell":
                        if (args.length == 0) {
                            code = sell(player);
                        } else {
                            code = sell(player, args[0]);
                        }
                        break;
                    case "price":
                        if (args.length == 0) {
                            code = price(player);
                        } else if (args.length == 1) {
                            code = price(player, args[0]);
                        } else {
                            code = price(player, args[0], args[1]);
                        }
                        break;
                    case "buy":
                        if (args.length == 0) {
                            code = buy(player);
                        } else if (args.length == 1) {
                            code = buy(player, args[0]);
                        } else {
                            code = buy(player, args[0], args[1]);
                        }
                        break;
                    default:
                        code = ReturnCode.UNRECOGNISED_COMMAND;
                }
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
     * The help and admin command
     * @param player Player who needs help
     * @return Success!
     */
    private ReturnCode trade(Player player, String[] args) {
        if (args.length > 0 && player.hasPermission("hexitrade.admin")) {
            switch (args[0]) {
                case "admin":
                    return sendAdminHelp(player);
                case "setprice":
                    if (args.length > 3) {
                        return setPrice(player, args[2], args[3]);
                    } else {
                        return ReturnCode.TOO_FEW_ARGUMENTS;
                    }
                default:
                    return ReturnCode.INVALID_ARGUMENT;
            }
        } else {
            return sendHelp(player);
        }
    }

    private ReturnCode sendAdminHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.WHITE + " HexiTrade Admin " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.DARK_GRAY + " - - -");
        player.sendMessage(ChatColor.GOLD + "/trade setprice <item> <price>" + ChatColor.WHITE +
                " - Sets the price of the item.");
        player.sendMessage(ChatColor.GOLD + "/trade reload" + ChatColor.WHITE +
                " - Reloads the plugin.");
        return ReturnCode.SUCCESS;
    }

    private ReturnCode sendHelp(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.WHITE + " HexiTrade " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.DARK_GRAY + " - - -");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/buy" + ChatColor.WHITE +
                " - Opens the buy interface");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/buy <item> <amount>" + ChatColor.WHITE +
                " - Buys an item");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/sell <amount>" + ChatColor.WHITE +
                " - Sells the item in your hand");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/price" + ChatColor.WHITE +
                " - Gives the sell price of the item in your hand");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/price <item> <amount>" + ChatColor.WHITE +
                " - Gives the buy price of an item");
        if (player.hasPermission("hexitrade.admin")) {
            player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/trade admin" + ChatColor.WHITE +
                    " - Lists HexiTrade admin commands");
        }
        return ReturnCode.SUCCESS;
    }

    private ReturnCode setPrice(Player player, String alias, String priceStr) {
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
        ItemListing itemListing = itemMap.getFromAlias(alias);
        if (itemListing == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else {
            itemListing.setPrice(price, player);
            return ReturnCode.SUCCESS;
        }
    }

    /**
     * Sells 1 of the item in player's hand
     * @param player Player who is selling their item.
     * @return Either success or some error.
     */
    private ReturnCode sell(Player player) {
        return sell(player, "1");
    }

    /**
     * Sells an amount of the item in the player's hand
     * @param player Player who is selling their item.
     * @param amountStr The amount of the item they wish to sell.
     * @return Either success or some error.
     */
    @SuppressWarnings("deprecation")
    private ReturnCode sell(Player player, String amountStr) {
        int amount;
        ItemStack itemInHand = player.getItemInHand();
        if (Objects.equals(amountStr, "all")) {
            amount = itemInHand.getAmount();
        } else {
            try {
                amount = Integer.parseInt(amountStr);
            } catch (NumberFormatException e) {
                return ReturnCode.INVALID_ARGUMENT;
            }
        }
        MaterialData data = itemInHand.getData();
        String key = data.getItemType().getId() + "-" + data.getData();
        if (itemInHand.getAmount() < amount) {
            return ReturnCode.TOO_FEW_ITEMS;
        } else if (!itemMap.containsKey(key)) {
            return ReturnCode.INVALID_ITEM;
        } else {
            itemMap.get(key).sell(amount, player);

            if (itemInHand.getAmount() <= amount) {
                player.setItemInHand(null); // Take the item
            } else {
                itemInHand.setAmount(itemInHand.getAmount() - amount);
            }
            return ReturnCode.SUCCESS;
        }
    }

    /**
     * Reports the price of the item in hand
     * @param player Player who asked for price
     * @return Success!
     */
    @SuppressWarnings("deprecation")
    private ReturnCode price(Player player) {
        MaterialData data = player.getItemInHand().getData();
        String key = data.getItemType().getId() + "-" + data.getData();
        if (!itemMap.containsKey(key)) {
            return ReturnCode.INVALID_ITEM;
        } else {
            itemMap.get(key).sellPrice(player.getItemInHand().getAmount(), player);
            return ReturnCode.SUCCESS;
        }
    }

    private ReturnCode price(Player player, String alias) {
        return price(player, alias, "1");
    }

    private ReturnCode price(Player player, String alias, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
        ItemListing itemListing = itemMap.getFromAlias(alias);
        if (itemListing == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else {
            itemListing.buyPrice(amount, player);
            return ReturnCode.SUCCESS;
        }
    }

    /**
     * Opens the buy inventory for the player
     * @param player The player that typed the command
     * @return Success!
     */
    private ReturnCode buy(Player player) {
        TradeInventory inventory = new TradeInventory(itemMap);
        player.openInventory(inventory.getInventory());
        return ReturnCode.SUCCESS;
    }

    private ReturnCode buy(Player player, String alias) {
        return buy(player, alias, "1");
    }

    private ReturnCode buy(Player player, String alias, String amountStr) {
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
        ItemListing listing = itemMap.getFromAlias(alias);
        if (listing == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else {
            ItemStack item = listing.buy(amount, player);
            addItem(item, player);
            return ReturnCode.SUCCESS;
        }
    }

    /**
     * Detects whether or not the player tried to change page and stops them from picking up the items
     * @param event The inventory click event
     */
    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getTitle().contains("HexiTrade") &&
                event.getCurrentItem() != null &&
                event.getCurrentItem().getData().getItemType() != Material.AIR &&
                event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            event.setCancelled(true);
            if (enabled) {
                TradeInventory tradeInventory = new TradeInventory(itemMap, event.getInventory(), event);
                ItemListing listing = tradeInventory.getListing();
                if (listing == null) {
                    player.openInventory(tradeInventory.getInventory());
                } else {
                    int amount = event.isShiftClick() ? listing.getItem().getMaxStackSize() : 1;
                    ItemStack item = listing.buy(amount, player);
                    event.getInventory().setItem(event.getRawSlot(), listing.getItem());
                    addItem(item, player);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void addItem(ItemStack item, Player player) {
        if (item != null) {
            System.out.println(item.getMaxStackSize());
            HashMap<Integer, ItemStack> dropItems = player.getInventory().addItem(item);
            if (dropItems.size() != 0) {
                Location location = player.getLocation();
                location.setY(location.getY() + 1);
                player.getWorld().dropItem(location, dropItems.get(0));
            }
        }
    }
}
