package com.hexicraft.trade.logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Ollie
 * @version 1.0
 */
public class HexiLogger {

    private File logFolder;
    private SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat time = new SimpleDateFormat("[hh:mm:ss]: ");

    public HexiLogger(File dataFolder) {
        this.logFolder = new File(dataFolder, "logs");
    }

    public void log(String data) {
        Date date = new Date();
        File file = new File(logFolder, day.format(date) + ".log");

        if (!logFolder.exists() && !logFolder.mkdirs()) {
            System.err.println("Failed to create log directory.");
        }

        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
            out.println(time.format(date) + data);
        } catch (IOException e) {
            System.err.println("Failed to save log: " + e.getMessage());
        }
    }
}
