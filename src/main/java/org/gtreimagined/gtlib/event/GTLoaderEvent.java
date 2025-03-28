package org.gtreimagined.gtlib.event;

import org.gtreimagined.gtlib.recipe.loader.IRecipeRegistrate;
import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;

public class GTLoaderEvent extends GTEvent {

    public final IRecipeRegistrate registrat;

    public GTLoaderEvent(IAntimatterRegistrar registrar, IRecipeRegistrate reg) {
        super(registrar);
        this.registrat = reg;
    }
}
