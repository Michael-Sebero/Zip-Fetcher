package com.zipfetcher;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadManager {
    
    public File downloadFile(String urlString, ProgressDialog dialog) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            // Follow redirects
            connection.setInstanceFollowRedirects(true);
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP " + responseCode + ": " + connection.getResponseMessage());
            }
            
            long fileSize = connection.getContentLengthLong();
            String fileName = getFileNameFromUrl(urlString, connection);
            
            // Create temporary file
            File tempFile = File.createTempFile("zipfetcher_", fileName);
            tempFile.deleteOnExit();
            
            try (InputStream inputStream = connection.getInputStream();
                 FileOutputStream outputStream = new FileOutputStream(tempFile)) {
                
                byte[] buffer = new byte[8192];
                long totalBytesRead = 0;
                int bytesRead;
                
                dialog.setStatus("Downloading " + fileName + "...");
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                    
                    if (fileSize > 0) {
                        int progress = (int) ((totalBytesRead * 100) / fileSize);
                        dialog.setProgress(progress);
                    }
                }
            }
            
            return tempFile;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private String getFileNameFromUrl(String urlString, HttpURLConnection connection) {
        // Try to get filename from Content-Disposition header
        String contentDisposition = connection.getHeaderField("Content-Disposition");
        if (contentDisposition != null && contentDisposition.contains("filename=")) {
            String filename = contentDisposition.substring(contentDisposition.indexOf("filename=") + 9);
            if (filename.startsWith("\"")) {
                filename = filename.substring(1, filename.lastIndexOf("\""));
            }
            return filename;
        }
        
        // Fallback to URL path
        String path = urlString.substring(urlString.lastIndexOf('/') + 1);
        if (path.contains("?")) {
            path = path.substring(0, path.indexOf("?"));
        }
        
        // Default extension if none found
        if (!path.contains(".")) {
            path += ".zip";
        }
        
        return path;
    }
}
