package com.zipfetcher;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ZipFetcherProgressGUI extends GuiScreen {
    private int progress = 0;
    private String statusText = "Downloading and extracting files...";

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        // Draw title
        this.drawCenteredString(this.fontRenderer, "Zip Fetcher", this.width / 2, 70, 0xFFFFFF);
        
        // Draw status text
        this.drawCenteredString(this.fontRenderer, statusText, this.width / 2, 100, 0xAAAAAA);
        
        // Draw progress bar background
        int barWidth = 200;
        int barHeight = 20;
        int barX = (this.width - barWidth) / 2;
        int barY = 120;
        
        drawRect(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
        
        // Draw progress bar fill
        progress = (progress + 1) % 100; // Animated progress
        int fillWidth = (barWidth * progress) / 100;
        drawRect(barX, barY, barX + fillWidth, barY + barHeight, 0xFF00AA00);
        
        // Draw progress percentage
        String progressText = progress + "%";
        this.drawCenteredString(this.fontRenderer, progressText, this.width / 2, barY + 25, 0xFFFFFF);
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
