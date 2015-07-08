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
public enum InventoryTab {
    BUILDING("Building Blocks", new MaterialData(Material.BRICK).toItemStack(1), new ArrayList<String>(), 0),
    @SuppressWarnings("deprecation")DECORATION("Decoration Blocks", new MaterialData(Material.DOUBLE_PLANT,
            (byte) 5).toItemStack(1), new ArrayList<String>(), 1),
    REDSTONE("Redstone", new MaterialData(Material.REDSTONE).toItemStack(1), new ArrayList<String>(), 2),
    TRANSPORT("Transportation", new MaterialData(Material.POWERED_RAIL).toItemStack(1), new ArrayList<String>(), 3),
    MISC("Miscellaneous", new MaterialData(Material.LAVA_BUCKET).toItemStack(1), new ArrayList<String>(), 4),
    FOOD("Foodstuffs", new MaterialData(Material.APPLE).toItemStack(1), new ArrayList<String>(), 45),
    TOOLS("Tools", new MaterialData(Material.IRON_AXE).toItemStack(1), new ArrayList<String>(), 46),
    COMBAT("Combat", new MaterialData(Material.GOLD_SWORD).toItemStack(1), new ArrayList<String>(), 47),
    BREWING("Brewing", new MaterialData(Material.POTION).toItemStack(1), new ArrayList<String>(), 48),
    MATERIALS("Materials", new MaterialData(Material.STICK).toItemStack(1), new ArrayList<String>(), 49);

    private ItemStack item;
    private ArrayList<String> itemKeys;
    int slot;

    InventoryTab(String title, ItemStack item, ArrayList<String> itemKeys, int slot) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.WHITE + title);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Closed Tab");
        meta.setLore(lore);
        item.setItemMeta(meta);
        this.item = item;
        this.itemKeys = itemKeys;
        this.slot = slot;
    }

    public static InventoryTab getTab(int ordinal) {
        return values()[ordinal];
    }

    public ArrayList<String> getItemKeys() {
        return itemKeys;
    }

    public static void resetItemKeys() {
        for (InventoryTab tab : values()) {
            tab.itemKeys = new ArrayList<>();
        }
    }

    public ItemStack getItem() {
        return item;
    }

    public ItemStack currentTab() {
        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GOLD + "Open Tab");
        meta.setLore(lore);
        item.setItemMeta(meta);
        item.setAmount(0);
        return item;
    }

    public static InventoryTab getOpenTab(Inventory inventory) {
        for (int i = 0; i < 10; i++) {
            if (inventory.getItem(values()[i].slot).getAmount() == 0) {
                return values()[i];
            }
        }
        return InventoryTab.BUILDING;
    }

    public static InventoryTab getFromSlot(int slot) {
        for (int i = 0; i < 10; i++) {
            if (values()[i].slot == slot) {
                return values()[i];
            }
        }
        return InventoryTab.BUILDING;
    }
}
