package com.hexicraft.trade.inventory;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;

/**
 * @author Ollie
 * @version 1.0
 */
public class InventoryPage {

    private int number;
    private ItemStack item;

    private enum Type {
        PREVIOUS(ChatColor.GOLD + "Previous page"),
        CURRENT(ChatColor.GOLD + "Current page"),
        NEXT(ChatColor.GOLD + "Next page");

        String description;

        Type(String description) {
            this.description = description;
        }
    }

    public InventoryPage() {
        this.number = -1;
        this.item = null;
    }

    private InventoryPage(int number, Type type) {
        this.number = number;
        item = new MaterialData(Material.PAPER).toItemStack(1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + "Page " + number);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(type.description);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    public InventoryPage getPrevious() {
        if (number > 1) {
            return new InventoryPage(number - 1, Type.PREVIOUS);
        } else {
            return new InventoryPage();
        }
    }

    public InventoryPage getNext(int totalSize) {
        if (number < totalSize / 36 || (number == totalSize / 36 && totalSize % 36 != 0)) {
            return new InventoryPage(number + 1, Type.NEXT);
        } else {
            return new InventoryPage();
        }
    }

    public InventoryPage getCurrent() {
        if (number != -1) {
            return new InventoryPage(number, Type.CURRENT);
        } else {
            return new InventoryPage();
        }
    }

    public static InventoryPage firstPage() {
        return new InventoryPage(1, Type.CURRENT);
    }

    public static InventoryPage getOpenPage(Inventory inventory) {
        try {
            return new InventoryPage(
                    Integer.parseInt(inventory.getItem(7).getItemMeta().getDisplayName().split("\\s+")[1]),
                    Type.CURRENT);
        } catch (NullPointerException | ArrayIndexOutOfBoundsException | NumberFormatException e) {
            return new InventoryPage();
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public int getNumber() {
        return number;
    }
}
