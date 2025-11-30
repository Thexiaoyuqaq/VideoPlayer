package com.github.NGoedix.videoplayer.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.awt.*;
import java.util.List;

public class ScrollingStringList extends ScrollingList<ScrollingStringList.PlayerSlot> {
    private static final int SLOT_HEIGHT = 30;

    public interface PlayerSlotClickListener {
        void onClick(String text);
    }

    private PlayerSlotClickListener playerSlotClickListener;

    public ScrollingStringList(int x, int y, int width, int height, List<String> text) {
        super(x, y, width, height, SLOT_HEIGHT);
        this.updateEntries(text);
    }

    public void setPlayerSlotClickListener(PlayerSlotClickListener playerSlotClickListener) {
        this.playerSlotClickListener = playerSlotClickListener;
    }

    public String getSelectedText() {
        if (this.getSelected() != null) {
            return this.getSelected().getText();
        }
        return "";
    }

    public void setSelected(String entry) {
        for (int i = 0; i < this.children().size(); i++) {
            PlayerSlot slot = (PlayerSlot) this.children().get(i);
            if (slot.getText().equals(entry)) {
                this.setSelected(slot);
                break;
            }
        }
    }

    @Override
    public void setSelected(PlayerSlot entry) {
        super.setSelected(entry);
        if (entry != null && this.playerSlotClickListener != null) {
            this.playerSlotClickListener.onClick(entry.getText());
        }
    }

    public void updateEntries(List<String> texts) {
        this.clearEntries();
        texts.forEach(text -> this.addEntry(new PlayerSlot(text, this)));
    }

    @Override
    protected void renderListBackground(GuiGraphics pGuiGraphics) {
        // 渲染列表背景
        int i = this.getRowLeft();
        int j = this.getRowTop(this.getItemCount());
        int k = this.getRowTop(0);

        // 渲染容器背景颜色灰色
        pGuiGraphics.fillGradient(i, k - 4, i + this.getRowWidth(), j + 4, -1072689136, -804253680);

        // 渲染容器边框颜色黑色
        pGuiGraphics.fillGradient(i, k - 4, i + 1, j + 4, -804253680, -804253680);
    }

    public class PlayerSlot extends Entry<PlayerSlot> {

        private final String text;
        private final ScrollingStringList parent;

        PlayerSlot(String text, ScrollingStringList parent) {
            this.text = text;
            this.parent = parent;
        }

        public String getText() {
            return text;
        }

        @Override
        public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
            this.parent.setSelected(this);
            return false;
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            Font font = this.parent.minecraft.font;

            pGuiGraphics.fillGradient(pLeft, pTop, pLeft + pWidth, pTop + pHeight, -435154928, -435154928);

            // 如果鼠标悬停在槽位上，渲染背景
            if (pMouseX >= parent.getRowLeft() && pMouseX <= parent.getRowRight() && pMouseY >= pTop && pMouseY <= pTop + pHeight) {
                pGuiGraphics.fillGradient(pLeft, pTop, pLeft + pWidth, pTop + pHeight, -1072689136, -804253680);
            }

            pGuiGraphics.drawString(font, this.text, pLeft + 65, pTop + 10, Color.WHITE.getRGB());
        }
    }
}