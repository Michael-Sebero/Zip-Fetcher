package com.zipfetcher;

import javax.swing.*;
import java.awt.*;

public class ProgressDialog extends JFrame {
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JButton closeButton;
    
    public ProgressDialog() {
        initializeComponents();
        setupLayout();
        setupWindow();
    }
    
    private void initializeComponents() {
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Initializing...");
        
        statusLabel = new JLabel("Preparing download...", SwingConstants.CENTER);
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));
        
        closeButton = new JButton("Close");
        closeButton.setEnabled(false);
        closeButton.addActionListener(e -> dispose());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        centerPanel.add(statusLabel);
        centerPanel.add(progressBar);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);
        
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void setupWindow() {
        setTitle("Zip Fetcher");
        setSize(400, 150);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Always on top
        setAlwaysOnTop(true);
    }
    
    public void setProgress(int progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            progressBar.setString(progress + "%");
        });
    }
    
    public void setStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            if (status.toLowerCase().contains("successful") || status.toLowerCase().contains("failed") || status.toLowerCase().contains("error")) {
                closeButton.setEnabled(true);
                progressBar.setValue(100);
                progressBar.setString("Complete");
            }
        });
    }
}
