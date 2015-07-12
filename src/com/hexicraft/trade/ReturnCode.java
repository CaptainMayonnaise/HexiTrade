package com.hexicraft.trade;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;

/**
 * @author Ollie
 * @version 1.0
 */
public enum ReturnCode {
    SUCCESS("", false),
    NOT_PLAYER("Only players can run this command.", false),
    INVALID_ARGUMENT("The arguments entered were invalid.", true),
    TOO_FEW_ARGUMENTS("You didn't enter enough arguments.", true),
    NONE_0F_ITEM("You don't have any of that item in your inventory.", false),
    INVALID_WORLD("HexiTrade is not active on this world.", false),
    ITEM_NOT_FOUND("Item wasn't found.", false),
    NOT_ACTIVE("The plugin hasn't been loaded properly.", false);

    private String message;
    private boolean sendUsage;

    ReturnCode(String message, boolean sendUsage) {
        this.message = message;
        this.sendUsage = sendUsage;
    }

    /**
     * Does the code have a message
     * @return true if has a message, false if empty
     */
    public boolean hasMessage() {
        return !(message.equals(""));
    }

    /**
     * Gets the return message, along with usage if required
     * @param cmd The command that was sent
     * @return The message
     */
    public String getMessage(Command cmd) {
        return message + (sendUsage ? ("\n" + ChatColor.GOLD + "Usage: " + ChatColor.RESET + cmd.getUsage()) : "");
    }
}
