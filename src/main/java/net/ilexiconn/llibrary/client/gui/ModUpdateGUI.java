package net.ilexiconn.llibrary.client.gui;

import net.ilexiconn.llibrary.server.update.UpdateContainer;
import net.ilexiconn.llibrary.server.update.UpdateHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;

@SideOnly(Side.CLIENT)
public class ModUpdateGUI extends GuiScreen {
    private GuiScreen mainMenu;
    private ModUpdateListGUI modList;
    private ModUpdateEntryGUI modInfo;
    private int selected = -1;
    private GuiButton buttonUpdate;
    private GuiButton buttonDone;

    public ModUpdateGUI(GuiScreen mainMenu) {
        this.mainMenu = mainMenu;
    }

    public ModUpdateListGUI getModList() {
        return modList;
    }

    public ModUpdateEntryGUI getModInfo() {
        return modInfo;
    }

    @Override
    public void initGui() {
        int listWidth = 0;
        for (UpdateContainer mod : UpdateHandler.INSTANCE.getOutdatedModList()) {
            listWidth = Math.max(listWidth, fontRendererObj.getStringWidth(mod.getModContainer().getName()) + 47);
            listWidth = Math.max(listWidth, fontRendererObj.getStringWidth(mod.getModContainer().getVersion()) + 47);
        }
        listWidth = Math.min(listWidth, 150);
        this.modList = new ModUpdateListGUI(this, listWidth);

        this.buttonList.add(buttonDone = new GuiButton(6, ((modList.getRight() + this.width) / 2) - 100, this.height - 38, I18n.translateToLocal("gui.done")));
        this.buttonList.add(buttonUpdate = new GuiButton(20, 10, this.height - 38, this.modList.getWidth(), 20, I18n.translateToLocal("gui.llibrary.update")));

        this.updateCache();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            switch (button.id) {
                case 6: {
                    this.mc.displayGuiScreen(this.mainMenu);
                    return;
                }
                case 20: {
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(UpdateHandler.INSTANCE.getOutdatedModList().get(selected).getUpdateURL()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (UpdateHandler.INSTANCE.getOutdatedModList().isEmpty()) {
            this.drawDefaultBackground();
            int i = this.width / 2;
            int j = this.height / 2;
            this.buttonDone.xPosition = width / 2 - 100;
            this.buttonDone.yPosition = height - 38;
            this.buttonList.clear();
            this.buttonList.add(buttonDone);
            this.drawScaledString(I18n.translateToLocal("gui.llibrary.updated.1"), i, j - 40, 0xFFFFFF, 2.0F);
            this.drawScaledString(I18n.translateToLocal("gui.llibrary.updated.2"), i, j - 20, 0xFFFFFF, 1.0F);
        } else {
            this.modList.drawScreen(mouseX, mouseY, partialTicks);
            if (this.modInfo != null) {
                this.modInfo.drawScreen(mouseX, mouseY, partialTicks);
            }

            int left = ((this.width - this.modList.getWidth() - 38) / 2) + this.modList.getWidth() + 30;
            this.drawCenteredString(this.fontRendererObj, I18n.translateToLocal("gui.llibrary.update.title"), left, 16, 0xFFFFFF);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void selectModIndex(int index) {
        if (this.selected != index) {
            this.selected = index;
            this.updateCache();
        }
    }

    public boolean modIndexSelected(int index) {
        return this.selected == index;
    }

    private void updateCache() {
        buttonUpdate.visible = false;
        modInfo = null;

        if (selected == -1) {
            return;
        }

        List<String> textList = new ArrayList<>();

        buttonUpdate.visible = true;
        buttonUpdate.enabled = true;
        buttonUpdate.displayString = I18n.translateToLocal("gui.llibrary.update");

        UpdateContainer updateContainer = UpdateHandler.INSTANCE.getOutdatedModList().get(selected);
        textList.add(updateContainer.getModContainer().getName());
        textList.add(I18n.translateToLocal("gui.llibrary.currentVersion") + String.format(": %s", updateContainer.getModContainer().getVersion()));
        textList.add(I18n.translateToLocal("gui.llibrary.latestVersion") + String.format(": %s", updateContainer.getLatestVersion().getVersionString()));
        textList.add(null);
        Collections.addAll(textList, UpdateHandler.INSTANCE.getChangelog(updateContainer, updateContainer.getLatestVersion()));

        modInfo = new ModUpdateEntryGUI(this, this.width - this.modList.getWidth() - 30, textList);
    }

    public void drawScaledString(String text, int x, int y, int color, float scale) {
        GL11.glPushMatrix();
        GL11.glScalef(scale, scale, scale);
        drawCenteredString(fontRendererObj, text, (int) (x / scale), (int) (y / scale), color);
        GL11.glPopMatrix();
    }

    @Override
    public boolean handleComponentClick(ITextComponent component) {
        return super.handleComponentClick(component);
    }
}