package org.gtreimagined.gtlib.mui;

import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.UITexture;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;

public class GTGuiTextures {
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gt_standard_bg";

        public static final String STANDARD_SLOT = "gt_standard_slot";

        public static final String STANDARD_FLUID_SLOT = "gt_standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gregtech_standard_button";
    }

    public static final UITexture GT_LOGO = UITexture.fullImage(Ref.ID,"textures/gui/icons/gt_logo.png");

    public static final UITexture BATTERY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/battery.png").colorType(null).build();
    public static final UITexture CELL_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell.png").defaultColorType().build();
    public static final UITexture CELL_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell_in.png").defaultColorType().build();
    public static final UITexture CELL_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell_out.png").defaultColorType().build();
    public static final UITexture ENERGY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/energy.png").defaultColorType().build();
    public static final UITexture FLUID_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/fluid_in.png").colorType(null).build();
    public static final UITexture FLUID_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/fluid_out.png").colorType(null).build();
    public static final UITexture ITEM_SLOT = UITexture.builder().location(Ref.ID, "textures/gui/slots/item.png").defaultColorType().build();
    public static final UITexture FLUID_SLOT = UITexture.builder().location(Ref.ID, "textures/gui/slots/fluid.png").defaultColorType().build();


    public static final UITexture MACHINE_STATE = UITexture.builder().imageSize(20, 11).location(Ref.ID, "textures/gui/widgets/machine_state.png").build();
    public static final UITexture IO_BUTTON = UITexture.fullImage(Ref.ID, "textures/gui/button/io.png");
    public static final UITexture DEFAULT_PROGRESS = UITexture.builder().location(new ResourceLocation(Ref.ID, "textures/gui/progress_bars/default.png")).imageSize(20, 36).build();

}
