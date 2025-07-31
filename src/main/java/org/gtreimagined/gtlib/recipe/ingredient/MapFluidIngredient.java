package org.gtreimagined.gtlib.recipe.ingredient;

import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraftforge.fluids.FluidStack;

public class MapFluidIngredient extends AbstractMapIngredient {

    public FluidStack stack;

    public MapFluidIngredient(FluidStack stack, boolean insideMap) {
        super(insideMap);
        this.stack = stack;
    }

    @Override
    protected int hash() {
        return RegistryUtils.getIdFromFluid(stack.getFluid()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (o instanceof MapFluidIngredient) {
            MapFluidIngredient fluid = (MapFluidIngredient) o;
            return this.hashCode() == fluid.hashCode() && stack.getFluid() == fluid.stack.getFluid(); //&& stack.isFluidEqual(fluid.stack);
        }
        if (o instanceof MapTagIngredient tag) {
            return stack.getFluid().builtInRegistryHolder().is(tag.floc);
        }
        return false;
    }

    @Override
    public String toString() {
        return RegistryUtils.getIdFromFluid(stack.getFluid()).toString();
    }
}
