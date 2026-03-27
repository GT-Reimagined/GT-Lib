package org.gtreimagined.gtlib.material;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;
import java.util.function.Supplier;

public class MaterialTypeFluid<T> extends MaterialType<T> {

    @Getter
    private final Object2ObjectMap<Material, Supplier<Fluid>> fluidReplacements = new Object2ObjectOpenHashMap<>();

    public MaterialTypeFluid(String id, int layers, boolean visible, long unitValue) {
        super(id, layers, visible, unitValue);
        GTAPI.register(MaterialTypeFluid.class, this);
    }

    public static FluidStack getEmptyFluidAndLog(MaterialType<?> type, IGTObject... objects) {
        Utils.onInvalidData("Tried to create " + type.getId() + " for objects: " + Arrays.toString(Arrays.stream(objects).map(IGTObject::getId).toArray(String[]::new)));
        return new FluidStack(Fluids.WATER, 1);
    }

    public void addReplacement(Material material, Supplier<Fluid> fluidSupplier){
        if (!material.enabled) return;
        fluidReplacements.put(material, fluidSupplier);
        this.add(material);
    }

    @Override
    public boolean hasReplacement(Material mat) {
        return fluidReplacements.containsKey(mat);
    }

    @Override
    public void replacement(Material mat, Supplier<Item> replacement) {
        //NOOP
    }

    public boolean allowGen(Material material) {
        return generating && materials.contains(material) && !hasReplacement(material);
    }

    @Override
    protected TagKey<?> tagFromString(String name) {
        return TagUtils.getForgelikeFluidTag(name);
    }

    public interface IFluidGetter {
        FluidStack get(Material m, int amount);
    }
}
