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
    UNRECOGNISED_COMMAND("The command you entered was not recognised.", false),
    INVALID_ARGUMENT("The arguments entered were invalid.", true),
    TOO_FEW_ARGUMENTS("You didn't enter enough arguments.", true),
    TOO_FEW_ITEMS("You don't have enough items in your hand.", false),
    INVALID_ITEM("The item in your hand can't be sold.", false),
    ITEM_NOT_FOUND("Item wasn't found.", false),
    NOT_ENABLED("The plugin hasn't been loaded properly", false);

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
