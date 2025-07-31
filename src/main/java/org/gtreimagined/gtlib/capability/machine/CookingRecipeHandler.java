package org.gtreimagined.gtlib.capability.machine;

import lombok.Getter;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.recipe.ingredient.impl.Ingredients;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeHooks;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CookingRecipeHandler<T extends BlockEntityMachine<T>> extends MachineRecipeHandler<T> {

    @Getter
    protected int burnDuration = 0, maxBurn;
    private final float burnMultiplier;

    public CookingRecipeHandler(T tile, float burnMultiplier) {
        super(tile);
        this.burnMultiplier = burnMultiplier;
    }

    private boolean consume(boolean simulate) {
        List<ItemStack> stack = tile.itemHandler.map(t -> t.consumeInputs(Collections.singletonList(this.getBurnable()), simulate)).orElse(Collections.emptyList());
        if (!stack.isEmpty() && !simulate){
            maxBurn = (int) (ForgeHooks.getBurnTime(stack.get(0), null) * burnMultiplier);
            burnDuration += maxBurn;
        }
        return !stack.isEmpty();
    }

    protected Ingredient getBurnable() {
        return Ingredients.BURNABLES;
    }

    @Override
    public void onServerUpdate() {
        if (activeRecipe == null && burnDuration > 0) {
            burnDuration --;
            if (burnDuration > 0) {
                tile.setMachineState(MachineState.ACTIVE);
                return;
            } else {
                maxBurn = 0;
                tile.setMachineState(MachineState.IDLE);
            }
        }
        super.onServerUpdate();
    }

    @Override
    public boolean consumePower(boolean simulate) {
        if (burnDuration == 0) {
            if (!consume(simulate)) return false;
        } else {
            boolean hasFuel = burnDuration > 0;
            if (!simulate) {
                burnDuration--;
            }
            return hasFuel;
        }
        return burnDuration > 0 || simulate;
    }

    @Override
    protected void recipeFailure() {
        maxBurn = 0;
    }

    @Override
    protected void playInterruptSound() {

    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putInt("burn", burnDuration);
        nbt.putInt("maxBurn", maxBurn);
        return nbt;
    }

    @Override
    public void getInfo(List<String> builder) {
        super.getInfo(builder);
        if (burnDuration > 0) builder.add("Current burn time left: " + burnDuration);
    }

    @Override
    public boolean accepts(ItemStack stack) {
        return super.accepts(stack) || getBurnable().test(stack);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        this.burnDuration = nbt.getInt("burn");
        this.maxBurn = nbt.getInt("maxBurn");
    }
}
