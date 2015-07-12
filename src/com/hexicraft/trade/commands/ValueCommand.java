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

import java.util.Objects;

/**
 * @author Ollie
 * @version 1.0
 */
public class ValueCommand implements CommandExecutor {

    private HexiTrade plugin;

    /**
     * Constructs a ValueCommand
     * @param plugin The HexiTrade plugin
     */
    public ValueCommand(HexiTrade plugin) {
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
            code = price(player, parseItem(args, player), parseAmount(args), cmd);
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
        }
        return true;
    }

    private int parseAmount(String[] args) {
        if (args.length >= 2) {
            return HexiTrade.parseInt(args[1]);
        } else if (args.length == 1 && HexiTrade.isInteger(args[0])) {
            return HexiTrade.parseInt(args[0]);
        } else {
            return 1;
        }
    }

    private ItemListing parseItem(String[] args, Player player) {
        ItemMap map = plugin.getWorlds().get(player.getLocation().getWorld().getName());
        if (map == null) {
            return null;
        } else if (args.length > 0 && !HexiTrade.isInteger(args[0])) {
            return map.getFromAlias(args[0]);
        } else {
            return map.getFromStack(player.getItemInHand());
        }
    }

    private ReturnCode price(Player player, ItemListing item, int amount, Command cmd) {
        if (item == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else if (amount < 1) {
            return ReturnCode.INVALID_ARGUMENT;
        } else {
            if (Objects.equals(cmd.getName(), "worth")) {
                item.sellPrice(amount, player);
            } else {
                item.buyPrice(amount, player);
            }
            return ReturnCode.SUCCESS;
        }
    }
}
