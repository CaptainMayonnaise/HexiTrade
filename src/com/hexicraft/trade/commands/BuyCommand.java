package com.hexicraft.trade.commands;

import com.hexicraft.trade.HexiTrade;
import com.hexicraft.trade.ItemListing;
import com.hexicraft.trade.ReturnCode;
import com.hexicraft.trade.inventory.TradeInventory;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * @author Ollie
 * @version 1.0
 */
public class BuyCommand implements CommandExecutor {

    private HexiTrade plugin;

    /**
     * Constructs a BuyCommand
     * @param plugin The HexiTrade plugin
     */
    public BuyCommand(HexiTrade plugin) {
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

        if (!(sender instanceof Player)) {
            code = ReturnCode.NOT_PLAYER;
        } else if (!plugin.isActive()) {
            code = ReturnCode.NOT_ACTIVE;
        } else {
            Player player = (Player) sender;
            if (args.length == 0) {
                code = openInventory(player);
            } else {
                code = buy(player, parseItem(args), parseAmount(args));
            }
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
        }
        return true;
    }

    private int parseAmount(String[] args) {
        if (args.length <= 1) {
            return 1;
        } else {
            return HexiTrade.parseInt(args[1]);
        }
    }

    private ItemListing parseItem(String[] args) {
        return plugin.getItemMap().getFromAlias(args[0]);
    }

    private ReturnCode buy(Player player, ItemListing item, int amount) {
        if (item == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else if (amount < 1) {
            return ReturnCode.INVALID_ARGUMENT;
        } else {
            ItemStack itemStack = item.buy(amount, player, plugin.getFileLogger());
            HexiTrade.addItem(itemStack, player);
            return ReturnCode.SUCCESS;
        }
    }

    /**
     * Opens the buy inventory for the player
     * @param player The player that sent the command
     * @return Success
     */
    private ReturnCode openInventory(Player player) {
        TradeInventory inventory = new TradeInventory(plugin);
        player.openInventory(inventory.getInventory());
        return ReturnCode.SUCCESS;
    }
}
