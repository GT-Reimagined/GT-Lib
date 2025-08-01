package org.gtreimagined.gtlib.material;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;

public class MaterialTypeItem<T> extends MaterialType<T> {

    public interface ItemSupplier {
        void createItems(String domain, MaterialType<?> type, Material material);
    }

    private final ItemSupplier itemSupplier;

    public MaterialTypeItem(String id, int layers, boolean visible, long unitValue) {
        super(id, layers, visible, unitValue);
        GTAPI.register(MaterialTypeItem.class, this);
        this.itemSupplier = MaterialItem::new;
    }

    public MaterialTypeItem(String id, int layers, boolean visible, long unitValue, ItemSupplier itemSupplier) {
        super(id, layers, visible, unitValue);
        GTAPI.register(MaterialTypeItem.class, this);
        this.itemSupplier = itemSupplier;
    }

    @Override
    public MaterialTypeItem<T> unSplitName() {
        return (MaterialTypeItem<T>) super.unSplitName();
    }

    public boolean allowItemGen(Material material) {
        return !replacements.containsKey(material) && allowGen(material) && !blockType;
    }

    public Item get(Material material) {
        Item replacement = GTAPI.getReplacement(this, material);
        if (replacement == null) {
            if (!allowItemGen(material))
                Utils.onInvalidData(String.join("", "GET ERROR - DOES NOT GENERATE: T(", id, ") M(", material.getId(), ")"));
            else return GTAPI.get(MaterialItem.class, idGetter.apply(material));
        }
        return replacement;
    }

    public ItemSupplier getSupplier() {
        return itemSupplier;
    }

    public ItemStack get(Material material, int count) {
        if (count < 1)
            Utils.onInvalidData(String.join("", "GET ERROR - MAT STACK EMPTY: T(", id, ") M(", material.getId(), ")"));
        return new ItemStack(get(material), count);
    }

    public RecipeIngredient getIngredient(Material material, int count) {
        if (count < 1)
            Utils.onInvalidData(String.join("", "GET ERROR - MAT STACK EMPTY: T(", id, ") M(", material.getId(), ")"));
        return RecipeIngredient.of(getMaterialTag(material), count);
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        super.onRegistryBuild(registry);
        if (registry != Keys.ITEMS) return;
        if (doRegister()) {
            for (Material material : this.materials) {
                if (!material.enabled) continue;
                if (allowItemGen(material)) getSupplier().createItems(material.materialDomain(), this, material);
            }
        }
    }
}
