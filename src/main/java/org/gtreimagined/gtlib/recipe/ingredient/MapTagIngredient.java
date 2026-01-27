package org.gtreimagined.gtlib.recipe.ingredient;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

public class MapTagIngredient extends AbstractMapIngredient {

    public TagKey<Item> loc;
    public TagKey<Fluid> floc;

    public MapTagIngredient(ResourceLocation tag, boolean insideMap) {
        super(insideMap);
        this.loc = new TagKey<>(Registries.ITEM, tag);
        this.floc = new TagKey<>(Registries.FLUID, tag);
    }

    public void setTag(ResourceLocation loc) {
        this.loc = new TagKey<>(Registries.ITEM, loc);
        this.floc = new TagKey<>(Registries.FLUID, loc);
        invalidate();
    }

    @Override
    protected int hash() {
        return loc.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MapTagIngredient) {
            return ((MapTagIngredient) o).loc.equals(loc);
        }
        if (o instanceof MapItemIngredient) {
            return ((MapItemIngredient) o).stack.builtInRegistryHolder().is(loc);
        }
        if (o instanceof MapFluidIngredient) {
            return ((MapFluidIngredient) o).stack.getFluid().builtInRegistryHolder().is(floc);
        }
        return false;
    }

    private static final boolean ENABLE_TAGS_LOOKUP = true;

  /*  public static Optional<ResourceLocation> findCommonTag(Ingredient ing, TagContainer tags) {
        if (!ENABLE_TAGS_LOOKUP || ing.getItems().length < 2) return Optional.empty();
        Optional<Set<ResourceLocation>> l = Arrays.stream(ing.getItems()).map(t -> (Set<ResourceLocation>) new ObjectOpenHashSet<>(t.getItem().getTags())).reduce((s, b) -> {
            s.retainAll(b);
            return s;
        });
        return l.map(t -> {
            for (ResourceLocation rl : l.get()) {
                if (tags.getOrEmpty(Registry.ITEM_REGISTRY).getAllTags().size() == ing.getItems().length) {
                    return rl;
                }
            }
            return null;
        });
    }*/

    @Override
    public String toString() {
        return loc.toString();
    }
}
