package org.gtreimagined.gtlib.recipe;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;

@FunctionalInterface
public interface IRecipeValidator {
    boolean validate(IRecipe recipe, BlockEntityMachine<?> machine);

    default boolean tick(IRecipe recipe, BlockEntityMachine<?> machine) {
        return true;
    }
}
