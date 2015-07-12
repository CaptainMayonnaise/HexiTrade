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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

/**
 * @author Ollie
 * @version 1.0
 */
public class SellCommand implements CommandExecutor {

    private HexiTrade plugin;

    /**
     * Constructs a SellCommand
     * @param plugin The HexiTrade plugin
     */
    public SellCommand(HexiTrade plugin) {
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
            code = sell(player, parseItem(args, player), parseAmount(args));
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

    private ReturnCode sell(Player player, ItemListing item, int amount) {
        if (item == null) {
            return ReturnCode.ITEM_NOT_FOUND;
        } else if (amount < 1) {
            return ReturnCode.INVALID_ARGUMENT;
        } else {
            Inventory inventory = player.getInventory();
            ItemStack searchItem = item.getItem().clone();
            ItemMeta meta = searchItem.getItemMeta();
            meta.setLore(new ArrayList<String>());
            searchItem.setItemMeta(meta);

            double profit = 0;
            int startAmount = amount;
            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack currentItem = inventory.getItem(i);
                if (currentItem != null &&
                        currentItem.getItemMeta().equals(searchItem.getItemMeta()) &&
                        currentItem.getData().equals(searchItem.getData()) ) {
                    int currentAmount = currentItem.getAmount();
                    if (amount >= currentAmount) {
                        inventory.setItem(i, null);
                        profit += item.sell(currentAmount, player, plugin.getFileLogger());
                        amount -= currentAmount;
                    } else {
                        currentItem.setAmount(currentAmount - amount);
                        profit += item.sell(amount, player, plugin.getFileLogger());
                        amount = 0;
                    }
                    if (amount == 0) {
                        break;
                    }
                }
            }

            if (amount == startAmount) {
                return ReturnCode.NONE_0F_ITEM;
            } else {
                amount = startAmount - amount;
                player.sendMessage(ChatColor.GOLD + "Sold " + amount + " " + item.getAliases().get(0) + " for " +
                        ChatColor.WHITE + plugin.getEcon().format(profit) + ChatColor.GOLD + ".");
                plugin.getFileLogger().log(player.getName() + " sold " + amount + " " + item.getAliases().get(0) +
                        " for " + plugin.getEcon().format(profit));

                return ReturnCode.SUCCESS;
            }
        }
    }
}
