package org.gtreimagined.gtlib.material;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.Arrays;

public class MaterialTypeFluid<T> extends MaterialType<T> {

    public MaterialTypeFluid(String id, int layers, boolean visible, long unitValue) {
        super(id, layers, visible, unitValue);
        AntimatterAPI.register(MaterialTypeFluid.class, this);
    }

    public static FluidStack getEmptyFluidAndLog(MaterialType<?> type, IGTObject... objects) {
        Utils.onInvalidData("Tried to create " + type.getId() + " for objects: " + Arrays.toString(Arrays.stream(objects).map(IGTObject::getId).toArray(String[]::new)));
        return new FluidStack(Fluids.WATER, 1);
    }

    @Override
    protected TagKey<?> tagFromString(String name) {
        return TagUtils.getForgelikeFluidTag(name);
    }

    public interface IFluidGetter {
        FluidStack get(Material m, int amount);
    }
}
