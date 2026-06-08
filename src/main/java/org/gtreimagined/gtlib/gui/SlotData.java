package org.gtreimagined.gtlib.gui;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.widgets.slot.ModularSlot;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

@Builder
public class SlotData<T extends ModularSlot> {

    @Getter
    private SlotType<T> type;
    @Getter
    private int x;
    @Getter
    private int y;
    @Getter
    private int jeiX;
    @Getter
    private int jeiY;
    @Getter
    private UITexture baseTexture;
    @Getter
    private UITexture overlayTexture;
    @Getter
    @Default
    private int data = -1;

    public SlotData(SlotType<T> type, int x, int y, int jeiX, int jeiY, UITexture baseTexture, UITexture overlayTexture, int data){
        this.type = type;
        this.x = x;
        this.y = y;
        this.jeiX = jeiX;
        this.jeiY = jeiY;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
        this.data = data;
    }

    public SlotData(SlotType<T> type, int x, int y, UITexture baseTexture, UITexture overlayTexture) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.baseTexture = baseTexture;
        this.overlayTexture = overlayTexture;
    }

    public static <T extends ModularSlot> SlotDataBuilder<T> builder(){
        return new CustomSlotDataBuilder<>();
    }

    public static class CustomSlotDataBuilder<T extends ModularSlot> extends SlotDataBuilder<T> {
        boolean xSet = false, ySet = false, jeiXSet = false, jeiYSet = false, baseTextureSet = false, overlayTextureSet = false;

        @Override
        public SlotDataBuilder<T> x(int x) {
            xSet = true;
            return super.x(x);
        }
        @Override
        public SlotDataBuilder<T> y(int x) {
            ySet = true;
            return super.y(x);
        }

        @Override
        public SlotDataBuilder<T> jeiX(int jeiX) {
            jeiXSet = true;
            return super.jeiX(jeiX);
        }

        @Override
        public SlotDataBuilder<T> jeiY(int jeiY) {
            jeiYSet = true;
            return super.jeiY(jeiY);
        }

        @Override
        public SlotDataBuilder<T> baseTexture(UITexture baseTexture) {
            baseTextureSet = true;
            return super.baseTexture(baseTexture);
        }

        @Override
        public SlotDataBuilder<T> overlayTexture(UITexture overlayTexture) {
            overlayTextureSet = true;
            return super.overlayTexture(overlayTexture);
        }

        @Override
        public SlotData<T> build() {
            SlotData<T> slotData = super.build();
            if (slotData.getType() == null){
                throw new IllegalStateException("Slot Data must call type!");
            }
            if (!xSet){
                throw new IllegalStateException("X must be set");
            }
            if (!ySet){
                throw new IllegalStateException("Y must be set");
            }
            if (!jeiXSet) slotData.jeiX = slotData.x;
            if (!jeiYSet) slotData.jeiY = slotData.y;
            if (!baseTextureSet) slotData.baseTexture = slotData.type.getTexture();
            if (!overlayTextureSet) slotData.overlayTexture = slotData.type.getOverlay();
            return slotData;
        }
    }
}
