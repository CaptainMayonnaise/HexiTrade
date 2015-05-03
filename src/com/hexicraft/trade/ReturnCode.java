package com.hexicraft.trade;

/**
 * @author Ollie
 * @version 1.0
 */
public enum ReturnCode {
    SUCCESS(""),
    NOT_PLAYER("Only players can run this command."),
    UNRECOGNISED_COMMAND("The command you entered was not recognised.");

    String message;

    ReturnCode(String message) {
        this.message = message;
    }

    /**
     * Does the code have a message
     * @return true if has a message, false if empty
     */
    boolean hasMessage() {
        return !(message.equals(""));
    }
}
