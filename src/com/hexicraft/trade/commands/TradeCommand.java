package com.hexicraft.trade.commands;

import com.hexicraft.trade.HexiTrade;
import com.hexicraft.trade.ItemListing;
import com.hexicraft.trade.ItemMap;
import com.hexicraft.trade.ReturnCode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * @author Ollie
 * @version 1.0
 */
public class TradeCommand implements CommandExecutor{

    private HexiTrade plugin;

    /**
     * Constructs a TradeCommand
     * @param plugin The HexiTrade plugin
     */
    public TradeCommand(HexiTrade plugin) {
        this.plugin = plugin;
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

        if (args.length == 0 || !sender.hasPermission("hexitrade.admin")) {
            code = sendTradeHelp(sender);
        } else {
            code = trade(sender, args);
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
        }
        return true;
    }

    /**
     * Sends the HexiTrade help to the CommandSender
     * @param sender Source of the command
     * @return Success
     */
    private ReturnCode sendTradeHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.WHITE + " HexiTrade " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.DARK_GRAY + " - - -");
        sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/buy" + ChatColor.WHITE +
                " - Opens the buy interface");
        sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/buy <item> [amount]" + ChatColor.WHITE +
                " - Buys an item");
        sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/sell [item] [amount]" + ChatColor.WHITE +
                " - Sells an item");
        sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/price [item] [amount]" + ChatColor.WHITE +
                " - Gives the buy price of an item");
        sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/worth [item] [amount]" + ChatColor.WHITE +
                " - Gives the sell worth of an item");
        if (sender.hasPermission("hexitrade.admin")) {
            sender.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/trade admin" + ChatColor.WHITE +
                    " - Lists HexiTrade admin commands");
        }
        return ReturnCode.SUCCESS;
    }

    /**
     * Processes the first argument given and performs the corresponding action
     * @param sender Source of the command
     * @param args Passed command arguments
     * @return Corresponding ReturnCode
     */
    private ReturnCode trade(CommandSender sender, String[] args) {


        switch (args[0]) {
            case "reload":
                plugin.reload();
                sender.sendMessage(ChatColor.GOLD + "HexiTrade has been reloaded.");
                return ReturnCode.SUCCESS;
            case "admin":
                return sendAdminHelp(sender);
            case "setprice":
                if (!(sender instanceof Player)) {
                    return ReturnCode.NOT_PLAYER;
                } else if (!plugin.isActive()) {
                    return ReturnCode.NOT_ACTIVE;
                } else {
                    Player player = (Player) sender;
                    if (args.length > 2) {
                        return setPrice(player, args[1], args[2]);
                    } else {
                        return ReturnCode.TOO_FEW_ARGUMENTS;
                    }
                }
            default:
                return ReturnCode.INVALID_ARGUMENT;
        }
    }

    /**
     * Sends the HexiTrade admin help to the CommandSender
     * @param sender The sender of the command
     * @return Success
     */
    private ReturnCode sendAdminHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.WHITE + " HexiTrade Admin " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.DARK_GRAY + " - - -");
        sender.sendMessage(ChatColor.GOLD + "/trade setprice <item> <price>" + ChatColor.WHITE +
                " - Sets the price of the item.");
        sender.sendMessage(ChatColor.GOLD + "/trade reload" + ChatColor.WHITE +
                " - Reloads the plugin.");
        return ReturnCode.SUCCESS;
    }

    /**
     * Sets the price of an ItemListing
     * @param player Source of the command
     * @param alias Alias of the item to be set
     * @param priceStr String version of the new price
     * @return Corresponding ReturnCode
     */
    private ReturnCode setPrice(Player player, String alias, String priceStr) {
        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
        ItemMap map = plugin.getWorlds().get(player.getLocation().getWorld().getName());
        ItemListing itemListing = map.getFromAlias(alias);
        if (itemListing == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else {
            itemListing.setPrice(price, player, plugin.getFileLogger());
            return ReturnCode.SUCCESS;
        }
    }
}
