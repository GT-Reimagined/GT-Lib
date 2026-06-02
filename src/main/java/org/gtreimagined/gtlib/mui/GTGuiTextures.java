package org.gtreimagined.gtlib.mui;

import brachy.modularui.drawable.ColorType;
import brachy.modularui.drawable.UITexture;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.ButtonOverlay;

public class GTGuiTextures {
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gt:standard_bg";

        public static final String STANDARD_SLOT = "gt:standard_slot";

        public static final String STANDARD_FLUID_SLOT = "gt:standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gt:standard_button";
        public static final String STANDARD_BUTTON_DISABLED = "gt:standard_button_disabled";
        public static final String STANDARD_BUTTON_PRESSED = "gt:standard_button_pressed";
    }

    public static final UITexture GT_LOGO = UITexture.fullImage(Ref.ID,"textures/gui/icons/gt_logo.png", null);

    public static final UITexture BATTERY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/battery.png").colorType(null).build();
    public static final UITexture CELL_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell.png").defaultColorType().build();
    public static final UITexture CELL_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell_in.png").defaultColorType().build();
    public static final UITexture CELL_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/cell_out.png").defaultColorType().build();
    public static final UITexture ENERGY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/energy.png").defaultColorType().build();
    public static final UITexture FLUID_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/fluid_in.png").colorType(null).build();
    public static final UITexture FLUID_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/slots/overlays/fluid_out.png").colorType(null).build();
    public static final UITexture BLANK_SLOT = UITexture.fullImage(Ref.ID, "textures/gui/slots/blank.png", null);
    public static final UITexture ITEM_SLOT = UITexture.builder().location(Ref.ID, "textures/gui/slots/item.png").defaultColorType().build();
    public static final UITexture FLUID_SLOT = UITexture.builder().location(Ref.ID, "textures/gui/slots/fluid.png").defaultColorType().build();


    public static final UITexture MACHINE_STATE = UITexture.builder().imageSize(20, 11).location(Ref.ID, "textures/gui/widgets/machine_state.png").build();
    public static final UITexture IO_BUTTON = UITexture.fullImage(Ref.ID, "textures/gui/button/io.png");
    public static final UITexture STANDARD_BUTTON = UITexture.builder().name(IDs.STANDARD_BUTTON).location(Ref.ID, "textures/gui/button/standard.png").build();
    public static final UITexture STANDARD_BUTTON_PRESSED = UITexture.builder().name(IDs.STANDARD_BUTTON_PRESSED).location(Ref.ID, "textures/gui/button/standard_pressed.png").build();
    public static final UITexture STANDARD_BUTTON_DISABLED = UITexture.builder().name(IDs.STANDARD_BUTTON_DISABLED).location(Ref.ID, "textures/gui/button/standard_disabled.png").build();
    public static final UITexture DEFAULT_PROGRESS = UITexture.builder().location(new ResourceLocation(Ref.ID, "textures/gui/progress_bars/default.png")).imageSize(20, 36).build();
    public static final UITexture TANK_ICON = UITexture.fullImage(new ResourceLocation(Ref.ID, "textures/gui/button/tank.png"), null);
    public static final UITexture CREATIVE_GENERATOR_OVERLAY = UITexture.builder().location(Ref.ID, "textures/gui/widgets/creative_generator_overlay.png").imageSize(158, 61).colorType(null).build();

    public static UITexture STOP = UITexture.fullImage(Ref.ID,"textures/gui/button/stop", null);
    public static UITexture TORCH_OFF = UITexture.fullImage(Ref.ID,"textures/gui/button/torch_off", null);
    public static UITexture TORCH_ON = UITexture.fullImage(Ref.ID,"textures/gui/button/torch_on", null);
    public static UITexture REDSTONE = UITexture.fullImage(new ResourceLocation("textures/item/redstone.png"), null);
    public static UITexture EXPORT = UITexture.fullImage(Ref.ID,"textures/gui/button/export", null);
    public static UITexture IMPORT =  UITexture.fullImage(Ref.ID,"textures/gui/button/import", null);
    public static UITexture EXPORT_IMPORT =  UITexture.fullImage(Ref.ID,"textures/gui/button/export_import", null);
    public static UITexture IMPORT_EXPORT =  UITexture.fullImage(Ref.ID,"textures/gui/button/import_export", null);
    public static UITexture INPUT_OFF =  UITexture.fullImage(Ref.ID,"textures/gui/button/input_off", null);

    public static UITexture GREY_OFF =  UITexture.fullImage(Ref.ID,"textures/gui/button/grey_off.png",  null);
    public static UITexture GREY_ON =  UITexture.fullImage(Ref.ID,"textures/gui/button/grey_on.png",  null);
    public static UITexture BLUE_OFF =  UITexture.fullImage(Ref.ID,"textures/gui/button/blue_off.png",  null);
    public static UITexture BLUE_ON =  UITexture.fullImage(Ref.ID,"textures/gui/button/blue_on.png",  null);
    public static UITexture LESS =  UITexture.fullImage(Ref.ID,"textures/gui/button/less.png", null);
    public static UITexture EQUAL =  UITexture.fullImage(Ref.ID,"textures/gui/button/equal.png", null);
    public static UITexture MORE =  UITexture.fullImage(Ref.ID,"textures/gui/button/more.png", null);
    public static UITexture WHITELIST =  UITexture.fullImage(Ref.ID,"textures/gui/button/whitelist.png", null);
    public static UITexture BLACKLIST =  UITexture.fullImage(Ref.ID,"textures/gui/button/blacklist.png", null);
    public static UITexture MINUS =  UITexture.fullImage(Ref.ID,"textures/gui/button/minus.png",  null);
    public static UITexture PLUS =  UITexture.fullImage(Ref.ID,"textures/gui/button/plus.png",  null);
    public static UITexture DIVISION =  UITexture.fullImage(Ref.ID,"textures/gui/button/division.png",  null);
    public static UITexture MULT =  UITexture.fullImage(Ref.ID,"textures/gui/button/mult.png",  null);
    public static UITexture PERCENT =  UITexture.fullImage(Ref.ID,"textures/gui/button/percent.png",  null);
    public static UITexture ARROW_LEFT =  UITexture.fullImage(Ref.ID,"textures/gui/button/arrow_left.png", null);
    public static UITexture A_LEFT =  UITexture.fullImage(Ref.ID,"textures/gui/button/a_left.png",  null);
    public static UITexture A_RIGHT =  UITexture.fullImage(Ref.ID,"textures/gui/button/a_right.png",  null);
    public static UITexture ARROW_RIGHT =  UITexture.fullImage(Ref.ID,"textures/gui/button/arrow_right.png",  null);
    public static UITexture INPUT_OUTPUT =  UITexture.fullImage(Ref.ID,"textures/gui/button/in_out.png",  null);
    public static final UITexture APAD_LEFT = UITexture.builder().location(Ref.ID, "textures/gui/button/apad_left.png").imageSize(14, 28).colorType(null).build();
    public static final UITexture PAD_LEFT = UITexture.builder().location(Ref.ID, "textures/gui/button/pad_left.png").imageSize(14, 28).colorType(null).build();
    public static final UITexture APAD_RIGHT = UITexture.builder().location(Ref.ID, "textures/gui/button/apad_right.png").imageSize(14, 28).colorType(null).build();
    public static final UITexture PAD_RIGHT = UITexture.builder().location(Ref.ID, "textures/gui/button/pad_right.png").imageSize(14, 28).colorType(null).build();

}
