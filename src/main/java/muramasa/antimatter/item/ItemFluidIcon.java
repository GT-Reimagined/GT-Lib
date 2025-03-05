package muramasa.antimatter.item;

import lombok.Getter;
import muramasa.antimatter.Ref;
import muramasa.antimatter.client.AntimatterTextureStitcher;
import muramasa.antimatter.datagen.providers.AntimatterItemModelProvider;
import muramasa.antimatter.integration.jeirei.AntimatterJEIREIPlugin;
import muramasa.antimatter.util.FluidUtils;
import muramasa.antimatter.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class ItemFluidIcon extends ItemBasic<ItemFluidIcon> implements IFluidItem{

    @Getter
    private final int capacity;

    private final Fluid stack;

    /**
     * Tag name for fluid in a bucket
     */
    private static final String TAG_FLUID = "Fluid";

    public ItemFluidIcon() {
        super(Ref.ID, "fluid_icon");
        AntimatterTextureStitcher.addStitcher(t -> {
            t.accept(new ResourceLocation(domain, "item/mask/icon_fluid"));
        });
        this.capacity = 1;
        this.stack = Fluids.EMPTY;
    }

    /*@Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            //AntimatterMaterialTypes.LIQUID.all().forEach(m -> items.add(fill(m.getLiquid())));
            //AntimatterMaterialTypes.GAS.all().forEach(m -> items.add(fill(m.getGas())));
            //AntimatterMaterialTypes.PLASMA.all().forEach(m -> items.add(fill(m.getPlasma())));
        }
    }*/

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.remove(0);
        stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(x -> {
            FluidStack fluid = x.getFluidInTank(0);
            if (fluid.isEmpty()) return;
            List<Component> str = new ArrayList<>();
            str.add(FluidUtils.getFluidDisplayName(fluid));
            str.add(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(fluid.getFluid())).withStyle(ChatFormatting.RED));
            String liquid = !FluidUtils.isFluidGaseous(fluid.getFluid()) ? "liquid" : "gas";
            str.add(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN));
            AntimatterJEIREIPlugin.addModDescriptor(str, fluid);
            tooltip.addAll(str);
        });
    }

    public Fluid getFluid() {
        return this.stack;
    }

    /**
     * Returns whether a cell has fluid.
     */
    protected boolean hasFluid(ItemStack container) {
        return !getFluidStack(container).isEmpty();
    }

    @Override
    public boolean hasContainerItem(ItemStack stack) {
        return hasFluid(stack);
    }

    @Override
    public ItemStack getContainerItem(ItemStack itemStack) {
        return new ItemStack(this);
    }

    @Override
    public Predicate<FluidStack> getFilter() {
        return f -> true;
    }

    @Override
    public void onItemModelBuild(ItemLike item, AntimatterItemModelProvider prov) {
        prov.getAntimatterBuilder(item).bucketProperties(stack, true, false).parent(new ResourceLocation(Ref.ID + ":item/bucket")).tex((map) -> {
            map.put("base", getDomain() + ":block/empty");
            map.put("cover", getDomain() + ":block/empty");
            map.put("fluid", getDomain() + ":item/mask/icon_fluid");
        });
    }
}
