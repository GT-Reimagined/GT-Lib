package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class SlotData<T extends ModularSlot> {

    @Getter
    private final SlotType<T> type;
    @Getter
    private final int x;
    @Getter
    private final int y;
    @Getter
    private final UITexture baseTexture;
    @Getter
    private final UITexture overlayTexture;
    @Getter
    private int data = -1;

    public SlotData(SlotType<T> type, int x, int y) {
        this(type, x, y, type.getTexture(), type.getOverlay());
    }

    public SlotData(SlotType<T> type, int x, int y, UITexture baseTexture, UITexture overlayTexture) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
    }

    public SlotData(SlotType<T> type, int x, int y, int data) {
        this(type, x, y);
        this.data = data;
    }
}
