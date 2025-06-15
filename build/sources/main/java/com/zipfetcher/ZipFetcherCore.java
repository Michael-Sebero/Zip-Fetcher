package com.zipfetcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipFetcherCore {
    private static Logger logger;
    private static final String CONFIG_FILE = "zipfetcher.properties";
    private static File configFile;
    private static File minecraftDir;
    private static File modsDir;

    public static void initialize(Logger log) {
        logger = log;
        minecraftDir = Minecraft.getMinecraft().mcDataDir;
        modsDir = new File(minecraftDir, "mods");
        configFile = new File(minecraftDir, CONFIG_FILE);

        // Start the process in a separate thread to avoid blocking the game
        new Thread(ZipFetcherCore::startFetchProcess).start();
    }

    private static void startFetchProcess() {
        try {
            // Check if config file exists
            if (!configFile.exists()) {
                createDefaultConfig();
                logger.info("Created default config file. Please edit zipfetcher.properties and restart the game.");
                return;
            }

            // Read config
            Properties config = new Properties();
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config.load(fis);
            }

            String downloadUrl = config.getProperty("download_url", "").trim();
            if (downloadUrl.isEmpty()) {
                logger.info("No download URL specified in config file.");
                return;
            }

            // Show progress GUI
            showProgressGUI();

            // Download and extract
            File tempFile = downloadFile(downloadUrl);
            if (tempFile != null) {
                extractArchive(tempFile);
                tempFile.delete();
                
                // Show success message
                showSuccessMessage();
                
                // Delete the mod file and shutdown
                deleteSelfAndShutdown();
            }

        } catch (Exception e) {
            logger.error("Error in zip fetcher process", e);
        }
    }

    private static void createDefaultConfig() {
        try {
            Properties config = new Properties();
            config.setProperty("download_url", "");
            config.setProperty("# Instructions", "Set download_url to your zip or 7z file URL");
            
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                config.store(fos, "Zip Fetcher Configuration");
            }
        } catch (IOException e) {
            logger.error("Failed to create config file", e);
        }
    }

    @SideOnly(Side.CLIENT)
    private static void showProgressGUI() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new ZipFetcherProgressGUI());
        });
    }

    @SideOnly(Side.CLIENT)
    private static void showSuccessMessage() {
        Minecraft.getMinecraft().addScheduledTask(() -> {
            Minecraft.getMinecraft().displayGuiScreen(new ZipFetcherSuccessGUI());
        });
    }

    private static File downloadFile(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
            if (!fileName.contains(".")) {
                fileName = "download.zip";
            }
            
            File tempFile = new File(minecraftDir, fileName);
            
            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            logger.info("Downloaded file: " + tempFile.getName());
            return tempFile;
            
        } catch (Exception e) {
            logger.error("Failed to download file from: " + urlString, e);
            return null;
        }
    }

    private static void extractArchive(File archiveFile) throws IOException {
        String fileName = archiveFile.getName().toLowerCase();
        
        if (fileName.endsWith(".7z")) {
            extract7z(archiveFile);
        } else {
            extractZip(archiveFile);
        }
    }

    private static void extractZip(File zipFile) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                File outputFile = new File(minecraftDir, entry.getName());
                File parentDir = outputFile.getParentFile();
                
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    IOUtils.copy(zis, fos);
                }
                
                logger.info("Extracted: " + entry.getName());
                zis.closeEntry();
            }
        }
    }

    private static void extract7z(File sevenZFile) throws IOException {
        try (SevenZFile archive = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            while ((entry = archive.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                
                File outputFile = new File(minecraftDir, entry.getName());
                File parentDir = outputFile.getParentFile();
                
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = archive.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
                
                logger.info("Extracted: " + entry.getName());
            }
        }
    }

    private static void deleteSelfAndShutdown() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3 seconds
                
                // Find and delete this mod file
                File[] modFiles = modsDir.listFiles((dir, name) -> 
                    name.toLowerCase().contains("zipfetcher") && name.endsWith(".jar"));
                
                if (modFiles != null) {
                    for (File modFile : modFiles) {
                        try {
                            // On Windows, we need to mark for deletion on exit
                            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                                modFile.deleteOnExit();
                            } else {
                                modFile.delete();
                            }
                            logger.info("Marked mod for deletion: " + modFile.getName());
                        } catch (Exception e) {
                            logger.error("Failed to delete mod file", e);
                        }
                    }
                }
                
                // Shutdown the game
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    Minecraft.getMinecraft().shutdown();
                });
                
            } catch (Exception e) {
                logger.error("Error during cleanup", e);
            }
        }).start();
    }
}
