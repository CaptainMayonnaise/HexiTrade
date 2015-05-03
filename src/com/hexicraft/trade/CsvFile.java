package com.hexicraft.trade;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Ollie
 * @version 1.0
 */
public class CsvFile implements Iterable<CSVRecord> {

    CSVParser parser;

    CsvFile(JavaPlugin plugin, File dataFolder, String fileName) {
        File file = new File(dataFolder, fileName);
        try {
            parser = new CSVParser(new FileReader(file), CSVFormat.DEFAULT);
        } catch (IOException e) {
            plugin.getLogger().warning("Error loading CSV file.\n" + e.getMessage());
        }
    }

    /**
     * The iterator of the CSVParser
     * @return The iterator
     */
    public Iterator<CSVRecord> iterator() {
        return parser.iterator();
    }
}
