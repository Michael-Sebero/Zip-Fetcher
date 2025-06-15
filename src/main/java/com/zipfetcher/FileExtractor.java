package com.zipfetcher;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.*;
import java.util.Enumeration;

public class FileExtractor {
    
    public boolean extractArchive(File archiveFile, File destinationDir, ProgressDialog dialog) {
        String fileName = archiveFile.getName().toLowerCase();
        
        try {
            if (fileName.endsWith(".zip")) {
                return extractZip(archiveFile, destinationDir, dialog);
            } else if (fileName.endsWith(".7z")) {
                return extract7z(archiveFile, destinationDir, dialog);
            } else {
                // Try zip first, then 7z
                try {
                    return extractZip(archiveFile, destinationDir, dialog);
                } catch (Exception e) {
                    return extract7z(archiveFile, destinationDir, dialog);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean extractZip(File zipFile, File destinationDir, ProgressDialog dialog) {
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<ZipArchiveEntry> entries = zip.getEntries();
            int totalEntries = 0;
            int processedEntries = 0;
            
            // Count total entries
            while (entries.hasMoreElements()) {
                entries.nextElement();
                totalEntries++;
            }
            
            // Reset enumeration
            entries = zip.getEntries();
            
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                
                if (entry.isDirectory()) {
                    processedEntries++;
                    continue;
                }
                
                File outputFile = new File(destinationDir, entry.getName());
                
                // Security check - prevent path traversal
                if (!isValidPath(outputFile, destinationDir)) {
                    continue;
                }
                
                // Create parent directories
                outputFile.getParentFile().mkdirs();
                
                // Extract file
                try (InputStream inputStream = zip.getInputStream(entry);
                     FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                    
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                }
                
                processedEntries++;
                int progress = (processedEntries * 100) / totalEntries;
                dialog.setProgress(progress);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean extract7z(File sevenZFile, File destinationDir, ProgressDialog dialog) {
        try (SevenZFile sevenZ = new SevenZFile(sevenZFile)) {
            SevenZArchiveEntry entry;
            int processedEntries = 0;
            int totalEntries = 0;
            
            // First pass: count total entries
            while ((entry = sevenZ.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    totalEntries++;
                }
            }
            
            // Close and reopen to reset position
            sevenZ.close();
            
            try (SevenZFile sevenZReset = new SevenZFile(sevenZFile)) {
                while ((entry = sevenZReset.getNextEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    
                    File outputFile = new File(destinationDir, entry.getName());
                    
                    // Security check - prevent path traversal
                    if (!isValidPath(outputFile, destinationDir)) {
                        continue;
                    }
                    
                    // Create parent directories
                    outputFile.getParentFile().mkdirs();
                    
                    // Extract file
                    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = sevenZReset.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                    }
                    
                    processedEntries++;
                    int progress = totalEntries > 0 ? (processedEntries * 100) / totalEntries : 100;
                    dialog.setProgress(progress);
                }
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean isValidPath(File outputFile, File destinationDir) {
        try {
            String destPath = destinationDir.getCanonicalPath();
            String outputPath = outputFile.getCanonicalPath();
            return outputPath.startsWith(destPath + File.separator) || outputPath.equals(destPath);
        } catch (IOException e) {
            return false;
        }
    }
}
