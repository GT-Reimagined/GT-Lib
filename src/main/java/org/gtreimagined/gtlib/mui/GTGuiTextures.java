package org.gtreimagined.gtlib.mui;

import brachy.modularui.drawable.UITexture;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;

public class GTGuiTextures {
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gt:standard_bg";

        public static final String STANDARD_SLOT = "gt:standard_slot";

        public static final String STANDARD_FLUID_SLOT = "gt:standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gt:standard_button";
        public static final String STANDARD_BUTTON_DISABLED = "gt:standard_button_disabled";
        public static final String STANDARD_BUTTON_PRESSED = "gt:standard_button_pressed";
        public static final String STANDARD_BUTTON_HOVER = "gt:standard_button_hover";
    }

    public static final UITexture GT_LOGO = UITexture.fullImage(Ref.ID,"gui/icons/gt_logo", null);

    public static final UITexture BATTERY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/battery").colorType(null).build();
    public static final UITexture CELL_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/cell").defaultColorType().build();
    public static final UITexture CELL_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/cell_in").defaultColorType().build();
    public static final UITexture CELL_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/cell_out").defaultColorType().build();
    public static final UITexture ENERGY_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/energy").defaultColorType().build();
    public static final UITexture FLUID_IN_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/fluid_in").colorType(null).build();
    public static final UITexture FLUID_OUT_SLOT_OVERLAY = UITexture.builder().location(Ref.ID, "gui/slots/overlays/fluid_out").colorType(null).build();
    public static final UITexture BLANK_SLOT = UITexture.fullImage(Ref.ID, "gui/slots/blank", null);
    public static final UITexture ITEM_SLOT = UITexture.builder().location(Ref.ID, "gui/slots/item").defaultColorType().build();
    public static final UITexture FLUID_SLOT = UITexture.builder().location(Ref.ID, "gui/slots/fluid").defaultColorType().build();


    public static final UITexture MACHINE_STATE = UITexture.builder().imageSize(20, 11).location(Ref.ID, "gui/widgets/machine_state").build();
    public static final UITexture STANDARD_BUTTON = UITexture.builder().name(IDs.STANDARD_BUTTON).location(Ref.ID, "gui/button/standard").build();
    public static final UITexture STANDARD_BUTTON_PRESSED = UITexture.builder().name(IDs.STANDARD_BUTTON_PRESSED).location(Ref.ID, "gui/button/standard_pressed").build();
    public static final UITexture STANDARD_BUTTON_DISABLED = UITexture.builder().name(IDs.STANDARD_BUTTON_DISABLED).location(Ref.ID, "gui/button/standard_disabled").build();
    public static final UITexture STANDARD_BUTTON_HOVER = UITexture.builder().name(IDs.STANDARD_BUTTON_HOVER).location(Ref.ID, "gui/button/standard_hover").build();
    public static final UITexture DEFAULT_PROGRESS = UITexture.builder().location(new ResourceLocation(Ref.ID, "gui/progress_bars/default")).imageSize(20, 36).build();
    public static final UITexture TANK_ICON = UITexture.fullImage(new ResourceLocation(Ref.ID, "gui/icons/tank"), null);
    public static final UITexture CREATIVE_GENERATOR_OVERLAY = UITexture.builder().location(Ref.ID, "gui/widgets/creative_generator_overlay").imageSize(158, 61).colorType(null).build();

    public static final UITexture IO_ITEM = UITexture.fullImage(Ref.ID, "gui/button/io_item", null);
    public static final UITexture IO_FLUID = UITexture.fullImage(Ref.ID, "gui/button/io_fluid", null);

    public static final UITexture STOP = UITexture.fullImage(Ref.ID,"gui/button/stop", null);
    public static final UITexture TORCH_OFF = UITexture.fullImage(Ref.ID,"gui/button/torch_off", null);
    public static final UITexture TORCH_ON = UITexture.fullImage(Ref.ID,"gui/button/torch_on", null);
    public static final UITexture REDSTONE = UITexture.fullImage(new ResourceLocation("item/redstone"), null);
    public static final UITexture EXPORT = UITexture.fullImage(Ref.ID,"gui/button/export", null);
    public static final UITexture IMPORT =  UITexture.fullImage(Ref.ID,"gui/button/import", null);
    public static final UITexture EXPORT_IMPORT =  UITexture.fullImage(Ref.ID,"gui/button/export_import", null);
    public static final UITexture IMPORT_EXPORT =  UITexture.fullImage(Ref.ID,"gui/button/import_export", null);
    public static final UITexture INPUT_OFF =  UITexture.fullImage(Ref.ID,"gui/button/input_off", null);

    public static final UITexture GREY_OFF =  UITexture.fullImage(Ref.ID,"gui/button/grey_off",  null);
    public static final UITexture GREY_ON =  UITexture.fullImage(Ref.ID,"gui/button/grey_on",  null);
    public static final UITexture BLUE_OFF =  UITexture.fullImage(Ref.ID,"gui/button/blue_off",  null);
    public static final UITexture BLUE_ON =  UITexture.fullImage(Ref.ID,"gui/button/blue_on",  null);
    public static final UITexture LESS =  UITexture.fullImage(Ref.ID,"gui/button/less", null);
    public static final UITexture EQUAL =  UITexture.fullImage(Ref.ID,"gui/button/equal", null);
    public static final UITexture MORE =  UITexture.fullImage(Ref.ID,"gui/button/more", null);
    public static final UITexture WHITELIST =  UITexture.fullImage(Ref.ID,"gui/button/whitelist", null);
    public static final UITexture BLACKLIST =  UITexture.fullImage(Ref.ID,"gui/button/blacklist", null);
    public static final UITexture MINUS =  UITexture.fullImage(Ref.ID,"gui/button/minus",  null);
    public static final UITexture PLUS =  UITexture.fullImage(Ref.ID,"gui/button/plus",  null);
    public static final UITexture DIVISION =  UITexture.fullImage(Ref.ID,"gui/button/division",  null);
    public static final UITexture MULT =  UITexture.fullImage(Ref.ID,"gui/button/mult",  null);
    public static final UITexture PERCENT =  UITexture.fullImage(Ref.ID,"gui/button/percent",  null);
    public static final UITexture ARROW_LEFT =  UITexture.fullImage(Ref.ID,"gui/button/arrow_left", null);
    public static final UITexture A_LEFT =  UITexture.fullImage(Ref.ID,"gui/button/a_left",  null);
    public static final UITexture A_RIGHT =  UITexture.fullImage(Ref.ID,"gui/button/a_right",  null);
    public static final UITexture ARROW_RIGHT =  UITexture.fullImage(Ref.ID,"gui/button/arrow_right",  null);
    public static final UITexture INPUT_OUTPUT =  UITexture.fullImage(Ref.ID,"gui/button/in_out",  null);
    public static final UITexture APAD_LEFT = UITexture.builder().location(Ref.ID, "gui/button/apad_left").imageSize(14, 28).colorType(null).build();
    public static final UITexture PAD_LEFT = UITexture.builder().location(Ref.ID, "gui/button/pad_left").imageSize(14, 28).colorType(null).build();
    public static final UITexture APAD_RIGHT = UITexture.builder().location(Ref.ID, "gui/button/apad_right").imageSize(14, 28).colorType(null).build();
    public static final UITexture PAD_RIGHT = UITexture.builder().location(Ref.ID, "gui/button/pad_right").imageSize(14, 28).colorType(null).build();

}
