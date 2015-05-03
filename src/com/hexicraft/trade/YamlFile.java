package com.hexicraft.trade;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Ollie
 * @version 1.0
 */
public class YamlFile extends YamlConfiguration {

    private JavaPlugin plugin;
    private File configFile;

    YamlFile(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), fileName);

        loadFile();

        periodicSave(60);
    }

    /**
     * Saves the file every few seconds
     * @param seconds The period to save the file after
     */
    private void periodicSave(long seconds) {
        long milliseconds = seconds * 1000;
        Timer timer = new Timer();
        timer.schedule( new TimerTask() {
            public void run() {
                saveFile();
            }
        }, milliseconds, milliseconds);
    }

    /**
     * Loads the file, if it doesn't exist then it creates a new one
     */
    public void loadFile() {
        try {
            if (!configFile.exists()) {
                saveFile();
            }
            load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().warning("Error loading configuration file.\n" + e.getMessage());
        }
    }

    /**
     * Saves the file to disk
     */
    public void saveFile() {
        try {
            save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Error saving configuration file.\n" + e.getMessage());
        }
    }
}
