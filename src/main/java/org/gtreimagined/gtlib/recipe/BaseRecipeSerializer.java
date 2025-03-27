package org.gtreimagined.gtlib.recipe;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.registration.IAntimatterObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T>, IAntimatterObject {
    private final String domain;
    private final String id;
    private ResourceLocation registryName = null;

    public BaseRecipeSerializer(String domain, String id) {
        this.domain = domain;
        this.id = id;
        AntimatterAPI.register(RecipeSerializer.class, this);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        return id;
    }

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
