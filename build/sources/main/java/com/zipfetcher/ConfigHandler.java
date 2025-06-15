package com.zipfetcher;

import java.io.*;
import java.util.Properties;

public class ConfigHandler {
    private static final String CONFIG_COMMENT = "Zip Fetcher Configuration\nSet the download_url to the direct link of your zip or 7z file";
    private static final String URL_KEY = "download_url";
    private static final String DEFAULT_URL = "https://example.com/your-file.zip";
    
    public static void initializeConfig(File configFile) {
        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                Properties props = new Properties();
                props.setProperty(URL_KEY, DEFAULT_URL);
                
                try (FileOutputStream fos = new FileOutputStream(configFile)) {
                    props.store(fos, CONFIG_COMMENT);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public static String getDownloadUrl(File configFile) {
        if (!configFile.exists()) {
            return null;
        }
        
        try {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            }
            
            String url = props.getProperty(URL_KEY, "").trim();
            if (url.equals(DEFAULT_URL) || url.isEmpty()) {
                return null; // Don't download if using default URL
            }
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
