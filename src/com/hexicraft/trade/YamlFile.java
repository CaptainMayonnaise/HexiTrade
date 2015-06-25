package com.hexicraft.trade;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;

/**
 * @author Ollie
 * @version 1.0
 */
public class YamlFile extends YamlConfiguration {

    private JavaPlugin plugin;
    private String fileName;
    private File configFile;

    YamlFile(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
        configFile = new File(plugin.getDataFolder(), fileName);

        loadFile();
    }

    /**
     * Loads the file, if it doesn't exist then it creates a new one
     */
    public void loadFile() {
        try {
            if (!configFile.exists()) {
                load(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(fileName)));
                saveFile();
            } else {
                load(configFile);
            }
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
