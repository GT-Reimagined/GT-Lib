package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.util.int2;
import org.gtreimagined.gtlib.util.int4;

import java.util.function.Supplier;

public class MachineStateWidget extends Widget<MachineStateWidget> {
    /* Location in most machine textures. */
    protected final int4 state = new int4(176, 56, 8, 8);
    protected int2 location;
    /* If the container contains recipe flag. */
    protected final boolean isRecipe;
    private final Supplier<MachineState> machineStateSupplier;
    private final IDrawable textureRegular, texturePowerLoss;
    @org.jetbrains.annotations.NotNull
    protected final Tier tier;

    public MachineStateWidget(Tier tier, boolean isRecipe, Supplier<MachineState> machineStateSupplier, UITexture texture) {
        this.tier = tier;
        this.isRecipe = isRecipe;
        this.machineStateSupplier = machineStateSupplier;
        this.textureRegular = texture.getSubArea(0,0,0.5f,1);
        this.texturePowerLoss = texture.getSubArea(0.5f, 0, 1, 1);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        MachineState machineState = machineStateSupplier.get();
        if (isRecipe) {
            if (machineState == MachineState.POWER_LOSS){
                texturePowerLoss.drawAtZero(context, this.getArea().getWidth(), this.getArea().getHeight(), widgetTheme.getTheme(false));
            } else {
                textureRegular.drawAtZero(context, this.getArea().getWidth(), this.getArea().getHeight(), widgetTheme.getTheme(false));
            }
        }
    }

    @Override
    public void drawForeground(ModularGuiContext context) {
        super.drawForeground(context);
        if (isHovering()){
            RichTooltip tooltip = new RichTooltip().add(machineStateSupplier.get().getDisplayName());
            tooltip.draw(context);
        }
    }
}
