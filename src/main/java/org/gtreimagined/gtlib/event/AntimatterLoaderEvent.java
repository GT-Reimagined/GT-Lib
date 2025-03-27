package org.gtreimagined.gtlib.event;

import org.gtreimagined.gtlib.recipe.loader.IRecipeRegistrate;
import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;

public class AntimatterLoaderEvent extends AntimatterEvent {

    public final IRecipeRegistrate registrat;

    public AntimatterLoaderEvent(IAntimatterRegistrar registrar, IRecipeRegistrate reg) {
        super(registrar);
        this.registrat = reg;
    }
}
