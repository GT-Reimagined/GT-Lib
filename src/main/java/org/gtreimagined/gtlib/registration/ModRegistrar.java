package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider;
import org.gtreimagined.gtlib.recipe.loader.IRecipeRegistrate;

/**
 * ModRegistrar is the base class for registering mod registrars.
 */
public abstract class ModRegistrar implements IGTObject {

    public ModRegistrar() {
        GTAPI.register(ModRegistrar.class, this);
    }

    /**
     * List of modids that need to be loaded to run this registrar.
     *
     * @return list of modids.
     */
    public abstract String[] modIds();

    /**
     * Called when GT maps are initiated. Either add loaders to the registrate
     * or simply use recipe maps manually.
     *
     * @param registrate recipe adder.
     */
    public abstract void antimatterRecipes(IRecipeRegistrate registrate);

    /**
     * Crafting recipes, or regular recipe provider.
     *
     * @param provider the AM provider.
     */
    public abstract void craftingRecipes(GTRecipeProvider provider);
}
