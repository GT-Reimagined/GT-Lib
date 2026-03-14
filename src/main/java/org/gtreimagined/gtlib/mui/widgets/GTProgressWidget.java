package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.widgets.ProgressWidget;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.NotNull;

public class GTProgressWidget extends ProgressWidget implements Interactable {
    private final Machine<?> machine;
    private final Tier tier;

    public GTProgressWidget(Machine<?> machine, Tier tier) {
        this.machine = machine;
        this.tier = tier;
        this.tooltipBuilder(this::addTooltip);
    }

    private void addTooltip(RichTooltip tooltip){
        tooltip.addLine(Utils.translatable("gtlib.gui.show_recipes"));
    }

    @Override
    public @NotNull Result onMousePressed(double mouseX, double mouseY, int button) {
        if (machine != null && tier != null){
            GTLibXEIPlugin.showCategory(machine, tier);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Interactable.super.onMousePressed(mouseX, mouseY, button);
    }
}
