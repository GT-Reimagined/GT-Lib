package muramasa.antimatter.util;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import muramasa.antimatter.machine.types.BasicMultiMachine;
import muramasa.antimatter.structure.Pattern;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;


public interface AntimatterPlatformUtils {
    AntimatterPlatformUtils INSTANCE = ImplLoader.load(AntimatterPlatformUtils.class);

    default FluidHolder fromTag(CompoundTag tag){
        if (tag == null) {
            return FluidHooks.emptyFluid();
        }
        if (!tag.contains("FluidName", Tag.TAG_STRING)) {
            return FluidHooks.fluidFromCompound(tag);
        }

        ResourceLocation fluidName = new ResourceLocation(tag.getString("FluidName"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(fluidName);
        if (fluid == null) {
            return FluidHooks.emptyFluid();
        }
        FluidHolder stack = FluidHooks.newFluidHolder(fluid, tag.getInt("Amount"), null);
        if (tag.contains("Tag", Tag.TAG_COMPOUND)) {
            stack.setCompound(tag.getCompound("Tag"));
        }
        return stack;
    }

    void addMultiMachineInfo(BasicMultiMachine<?> machine, List<Pattern> patterns);
}
