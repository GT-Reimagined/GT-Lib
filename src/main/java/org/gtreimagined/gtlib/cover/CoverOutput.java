package org.gtreimagined.gtlib.cover;

import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.blockentity.BlockEntityFakeBlock;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.machine.event.MachineEvent;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class CoverOutput extends BaseCover {

    private boolean ejectItems = false;
    private boolean ejectFluids = false;

    private boolean allowInput = false;

    public CoverOutput(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
        if (source.getTile() instanceof BlockEntityFakeBlock){
            setEjects(true, true);
        }
    }

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        if (type.equals("pipe"))
            return PIPE_COVER_MODEL;
        return getBasicDepthModel();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (handler.getTile().getLevel().isClientSide) return;
        manualOutput();
    }

    @Override
    public void onRemove() {
        super.onRemove();
        // refresh(instance);
    }

    public void manualOutput() {
        if (shouldOutputFluids())
            processFluidOutput();
        if (shouldOutputItems())
            processItemOutput();
    }

    public boolean shouldOutputItems() {
        return this.ejectItems;
    }

    public boolean shouldOutputFluids() {
        return this.ejectFluids;
    }

    public void setEjects(boolean fluid, boolean item) {
        ejectItems = item;
        ejectFluids = fluid;
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        super.deserialize(nbt);
        this.ejectItems = nbt.getBoolean("ei");
        this.ejectFluids = nbt.getBoolean("ef");
        this.allowInput = nbt.getBoolean("ai");
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = super.serialize();
        nbt.putBoolean("ei", this.ejectItems);
        nbt.putBoolean("ef", this.ejectFluids);
        nbt.putBoolean("ai", allowInput);
        return nbt;
    }

    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, Direction side, @org.jetbrains.annotations.Nullable GTToolType type) {
        if (type != null && type.getTag() == GTTools.SCREWDRIVER.getTag()){
            allowInput = !allowInput;
            String suffix = allowInput ? "allow" : "no";
            player.displayClientMessage(Utils.translatable("gtlib.tooltip.cover.output." + suffix + "_input"), false);
            return InteractionResult.SUCCESS;
        }
        return super.onInteract(player, hand, side, type);
    }

    public boolean doesAllowInput() {
        return allowInput;
    }

    int processing = 0;
    protected void processItemOutput() {
        BlockEntity adjTile;
        if (handler.getTile() instanceof BlockEntityBase<?> base){
            adjTile = base.getCachedBlockEntity(this.side);
        } else {
            adjTile = handler.getTile().getLevel().getBlockEntity(handler.getTile().getBlockPos().relative(this.side));
        }
        if (adjTile == null)
            return;
        if (processing > 0) return;
        processing++;
        adjTile.getCapability(ForgeCapabilities.ITEM_HANDLER, this.side.getOpposite())
                .ifPresent(adjHandler -> {
                    handler.getTile().getCapability(ForgeCapabilities.ITEM_HANDLER, this.side).ifPresent(h -> Utils.transferItems(h, adjHandler, false, i -> {
                        return !(this.handler.getTile() instanceof BlockEntityMachine<?> machine) || machine.itemHandler.map(f -> f.canItemBeAutoOutput(i)).orElse(true);
                    }));
                });
        processing--;
    }

    protected void processFluidOutput() {
        BlockEntity adjTile;
        if (handler.getTile() instanceof BlockEntityBase<?> base){
            adjTile = base.getCachedBlockEntity(this.side);
        } else {
            adjTile = handler.getTile().getLevel().getBlockEntity(handler.getTile().getBlockPos().relative(this.side));
        }
        if (adjTile == null) return;
        if (processing > 0) return;
        processing++;
        adjTile.getCapability(ForgeCapabilities.FLUID_HANDLER, this.side.getOpposite()).ifPresent(adjHandler -> {
            handler.getTile().getCapability(ForgeCapabilities.FLUID_HANDLER, this.side).ifPresent(h -> {
                tryFluidTransfer(adjHandler, h, Integer.MAX_VALUE, true);
            });
        });
        processing--;
    }

    public void tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, int maxAmount, boolean doTransfer) {
        for (int i = 0; i < fluidSource.getTanks(); i++) {
            FluidStack fluid = fluidSource.getFluidInTank(i);
            if (fluid.isEmpty()) continue;
            if (this.handler.getTile() instanceof BlockEntityMachine<?> machine && machine.fluidHandler.map(f -> !f.canFluidBeAutoOutput(fluid)).orElse(false)){
                continue;
            }
            FluidUtil.tryFluidTransfer(fluidDestination, fluidSource, Utils.ca(Math.min(fluid.getAmount(), maxAmount), fluid), doTransfer);
        }
    }

    @Override
    public void onGuiEvent(IGuiEvent event, Player player) {
        if (event.getFactory() == GuiEvents.ITEM_EJECT) {
            if (ejectItems) processItemOutput();
            Utils.markTileForNBTSync(handler.getTile());
        }
        if (event.getFactory() == GuiEvents.FLUID_EJECT) {
            if (ejectFluids) processFluidOutput();
            Utils.markTileForNBTSync(handler.getTile());
        }
    }

    @Override
    public void onMachineEvent(IGuiHandler tile, IMachineEvent event, int... data) {
        if (event == MachineEvent.ITEMS_OUTPUTTED && ejectItems) {
            processItemOutput();
        } else if (event == MachineEvent.FLUIDS_OUTPUTTED && ejectFluids) {
            processFluidOutput();
        }
    }

    @Override
    public <T> boolean blocksInput(Class<T> cap, @org.jetbrains.annotations.Nullable Direction side) {
        return !allowInput;
    }
}
