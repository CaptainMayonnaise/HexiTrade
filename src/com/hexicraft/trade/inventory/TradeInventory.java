package com.hexicraft.trade.inventory;

import com.hexicraft.trade.HexiTrade;
import com.hexicraft.trade.ItemListing;
import com.hexicraft.trade.ItemMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.material.MaterialData;

/**
 * @author Ollie
 * @version 1.0
 */
public class TradeInventory {

    private Inventory inventory;
    private InventoryTab tab;
    private InventoryPage page;
    private HexiTrade plugin;
    private ItemListing listing = null;


    public TradeInventory(HexiTrade plugin, ItemMap itemMap) {
        this.plugin = plugin;
        inventory = Bukkit.createInventory(null, 54, "HexiTrade");
        tab = InventoryTab.BUILDING;
        page = InventoryPage.firstPage();

        inventory.setItem(tab.slot, tab.currentTab());
        for (int i = 1; i < 10; i++) {
            inventory.setItem(InventoryTab.values()[i].slot, InventoryTab.values()[i].getItem());
        }
        setItems(itemMap);
    }

    @SuppressWarnings("deprecation")
    public TradeInventory(HexiTrade plugin, Inventory inventory, InventoryClickEvent event, ItemMap itemMap) {
        this.plugin = plugin;
        this.inventory = inventory;
        int slot = event.getRawSlot();
        if (slot < 54) {
            tab = InventoryTab.getOpenTab(inventory);
            page = InventoryPage.getOpenPage(inventory);
            if (slot < 5 || slot > 44) {
                inventory.setItem(tab.slot, tab.getItem());
                tab = InventoryTab.getFromSlot(slot);
                page = InventoryPage.firstPage();
                inventory.setItem(slot, tab.currentTab());
                setItems(itemMap);
            } else if (slot == 6) {
                page = page.getPrevious().getCurrent();
                setItems(itemMap);
            } else if (slot == 8) {
                page = page.getNext(itemMap.getTabs().get(tab).size()).getCurrent();
                setItems(itemMap);
            } else if (slot != 7) {
                listing = itemMap.getFromStack(event.getCurrentItem());
            }
        }
    }

    private void setItems(ItemMap itemMap) {
        inventory.setItem(6, page.getPrevious().getItem());
        inventory.setItem(7, page.getItem());
        inventory.setItem(8, page.getNext(itemMap.getTabs().get(tab).size()).getItem());
        int offset = ((page.getNumber() - 1) * 36) - 9;
        for (int i = 9; i < 45; i++) {
            if (offset + i < itemMap.getTabs().get(tab).size()) {
                inventory.setItem(i, itemMap.get(itemMap.getTabs().get(tab).get(offset + i)).getItem());
            } else {
                inventory.setItem(i, null);
            }
        }
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ItemListing getListing() {
        return listing;
    }
}
