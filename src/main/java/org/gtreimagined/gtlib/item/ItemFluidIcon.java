package org.gtreimagined.gtlib.item;

import lombok.Getter;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.GTTextureStitcher;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibXEIPlugin;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;


public class ItemFluidIcon extends ItemBasic<ItemFluidIcon> implements IFluidItem, IColorHandler {

    @Getter
    private final int capacity;

    private final Fluid stack;

    /**
     * Tag name for fluid in a bucket
     */
    private static final String TAG_FLUID = "Fluid";

    public ItemFluidIcon() {
        super(Ref.ID, "fluid_icon");
        GTTextureStitcher.addStitcher(t -> {
            t.accept(new ResourceLocation(domain, "item/mask/icon_fluid"));
        });
        this.capacity = 1;
        this.stack = Fluids.EMPTY;
    }

    /*@Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            //GTMaterialTypes.LIQUID.all().forEach(m -> items.add(fill(m.getLiquid())));
            //GTMaterialTypes.GAS.all().forEach(m -> items.add(fill(m.getGas())));
            //GTMaterialTypes.PLASMA.all().forEach(m -> items.add(fill(m.getPlasma())));
        }
    }*/

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).ifPresent(x -> {
            FluidStack fluid = x.getFluidInTank(0);
            if (fluid.isEmpty()) return;
            tooltip.remove(0);
            List<Component> str = new ArrayList<>();
            str.add(FluidUtils.getFluidDisplayName(fluid));
            str.add(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(fluid.getFluid())).withStyle(ChatFormatting.RED));
            String liquid = !FluidUtils.isFluidGaseous(fluid.getFluid()) ? "liquid" : "gas";
            str.add(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN));
            GTLibXEIPlugin.addModDescriptor(str, fluid);
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
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return hasFluid(stack);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return new ItemStack(this);
    }

    @Override
    public Predicate<FluidStack> getFilter() {
        return f -> true;
    }

    @Override
    public void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        prov.getGTBuilder(item).bucketProperties(stack, true, false).parent(new ResourceLocation(Ref.ID + ":item/bucket")).tex((map) -> {
            map.put("base", getDomain() + ":block/empty");
            map.put("cover", getDomain() + ":block/empty");
            map.put("fluid", getDomain() + ":item/mask/icon_fluid");
        });
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return ModelUtils.ITEM_COLORS.getColor(stack, i);
    }

}
