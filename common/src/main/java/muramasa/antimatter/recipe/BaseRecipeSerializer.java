package muramasa.antimatter.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T> {
    private ResourceLocation registryName = null;

    public RecipeSerializer<?> setRegistryName(ResourceLocation arg) {
        if (getRegistryName() != null)
            throw new IllegalStateException("Attempted to set registry name with existing registry name! New: " + arg.toString() + " Old: " + getRegistryName());
        this.registryName = arg;
        return this;
    }

    @Nullable
    public ResourceLocation getRegistryName() {
        return registryName;
    }

    public Class getRegistryType() {
        return this.getClass();
    }
}
