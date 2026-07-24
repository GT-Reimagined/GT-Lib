package org.gtreimagined.gtlib.mui.widgets;

import brachy.modularui.api.widget.Interactable;
import brachy.modularui.widgets.ProgressWidget;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibXEIPlugin;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.jetbrains.annotations.NotNull;

public class GTProgressWidget extends ProgressWidget implements Interactable {
    private final Machine<?> machine;
    private final Tier tier;

    public GTProgressWidget(Machine<?> machine, Tier tier) {
        this.machine = machine;
        this.tier = tier;
    }

    @Override
    public @NotNull Result onMousePressed(int button) {
        if (machine != null && tier != null){
            GTLibXEIPlugin.showCategory(machine, tier);
            Interactable.playButtonClickSound();
            return Result.SUCCESS;
        }
        return Interactable.super.onMousePressed(button);
    }
}
