package com.zipfetcher;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ZipFetcherSuccessGUI extends GuiScreen {
    private long displayTime;

    @Override
    public void initGui() {
        super.initGui();
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height / 2 + 30, 150, 20, "Close"));
        displayTime = System.currentTimeMillis();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        // Draw success message
        this.drawCenteredString(this.fontRenderer, "Extraction Successful!", this.width / 2, this.height / 2 - 30, 0x00FF00);
        this.drawCenteredString(this.fontRenderer, "The game will close in a moment.", this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        this.drawCenteredString(this.fontRenderer, "Restart the game to use the updated files.", this.width / 2, this.height / 2 + 10, 0xAAAAAA);
        
        // Auto-close after 3 seconds
        if (System.currentTimeMillis() - displayTime > 3000) {
            this.mc.displayGuiScreen(null);
        }
        
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
