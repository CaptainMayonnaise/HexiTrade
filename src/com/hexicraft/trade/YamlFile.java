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

    private String fileName;
    private JavaPlugin plugin;
    private String internalName;
    private File configFile;

    YamlFile(JavaPlugin plugin, String fileName) {
        this(plugin, fileName, fileName);
    }

    YamlFile(JavaPlugin plugin, String fileName, String internalName) {
        this.plugin = plugin;
        this.fileName = fileName;
        this.internalName = internalName;
        configFile = new File(plugin.getDataFolder(), fileName);
    }

    /**
     * Loads the file, if it doesn't exist then it creates a new one
     */
    public boolean loadFile() {
        try {
            if (!configFile.exists()) {
                load(new InputStreamReader(plugin.getResource(internalName)));
                saveFile();
            } else {
                load(configFile);
            }
            return true;
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().warning("Error loading YAML file.\n" + e.getMessage());
            return false;
        }
    }

    /**
     * Saves the file to disk
     */
    public void saveFile() {
        try {
            save(configFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Error saving YAML file.\n" + e.getMessage());
        }
    }

    /**
     * Sets values and then saves file
     * @param path Yaml path.
     * @param value Object to set.
     */
    @Override
    public void set(String path, Object value) {
        super.set(path, value);
        saveFile();
    }

    public void setNoSave(String path, Object value) {
        super.set(path, value);
    }

    public String getFileName() {
        return fileName;
    }
}
