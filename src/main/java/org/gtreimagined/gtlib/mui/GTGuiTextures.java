package org.gtreimagined.gtlib.mui;

import brachy.modularui.drawable.UITexture;
import org.gtreimagined.gtlib.Ref;

public class GTGuiTextures {
    public static class IDs {

        public static final String STANDARD_BACKGROUND = "gt_standard_bg";

        public static final String STANDARD_SLOT = "gt_standard_slot";

        public static final String STANDARD_FLUID_SLOT = "gt_standard_fluid_slot";

        public static final String STANDARD_BUTTON = "gregtech_standard_button";
    }

    public static final UITexture GT_LOGO = UITexture.fullImage(Ref.ID,"textures/gui/icons/gt_logo.png");

    public static final UITexture MACHINE_STATE = UITexture.builder().imageSize(20, 11).location(Ref.ID, "textures/gui/widgets/machine_state.png").build();
}
