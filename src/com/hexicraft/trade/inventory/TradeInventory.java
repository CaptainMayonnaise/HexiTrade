package com.hexicraft.trade.inventory;

import com.hexicraft.trade.ItemListing;
import com.hexicraft.trade.ItemMap;
import org.bukkit.Bukkit;
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
    private ItemMap itemMap;
    private ItemListing listing = null;


    public TradeInventory(ItemMap itemMap) {
        this.itemMap = itemMap;
        inventory = Bukkit.createInventory(null, 54, "HexiTrade");
        tab = InventoryTab.BUILDING;
        page = InventoryPage.firstPage();

        for (int i = 0; i < 10; i++) {
            inventory.setItem(InventoryTab.values()[i].slot, InventoryTab.values()[i].getItem());
        }
        setItems();
    }

    @SuppressWarnings("deprecation")
    public TradeInventory(ItemMap itemMap, Inventory inventory, InventoryClickEvent event) {
        this.itemMap = itemMap;
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
                setItems();
            } else if (slot == 6) {
                page = page.getPrevious().getCurrent();
                setItems();
            } else if (slot == 8) {
                page = page.getNext(tab.getItemKeys().size()).getCurrent();
                setItems();
            } else {
                MaterialData data = event.getCurrentItem().clone().getData();
                listing = itemMap.get(data.getItemTypeId() + "-" + data.getData());
            }
        }
    }

    private void setItems() {
        inventory.setItem(6, page.getPrevious().getItem());
        inventory.setItem(7, page.getItem());
        inventory.setItem(8, page.getNext(tab.getItemKeys().size()).getItem());
        int offset = ((page.getNumber() - 1) * 36) - 9;
        for (int i = 9; i < 45; i++) {
            if (offset + i < tab.getItemKeys().size()) {
                inventory.setItem(i, itemMap.get(tab.getItemKeys().get(offset + i)).getItem());
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
