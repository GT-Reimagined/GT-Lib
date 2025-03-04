package muramasa.antimatter.cover;

import muramasa.antimatter.blockentity.BlockEntityBase;
import muramasa.antimatter.blockentity.BlockEntityFakeBlock;
import muramasa.antimatter.blockentity.BlockEntityMachine;
import muramasa.antimatter.capability.ICoverHandler;
import muramasa.antimatter.capability.IGuiHandler;
import muramasa.antimatter.data.AntimatterDefaultTools;
import muramasa.antimatter.gui.event.GuiEvents;
import muramasa.antimatter.gui.event.IGuiEvent;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.tool.AntimatterToolType;
import muramasa.antimatter.util.Utils;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
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
    public InteractionResult onInteract(Player player, InteractionHand hand, Direction side, @org.jetbrains.annotations.Nullable AntimatterToolType type) {
        if (type != null && type.getTag() == AntimatterDefaultTools.SCREWDRIVER.getTag()){
            allowInput = !allowInput;
            String suffix = allowInput ? "allow" : "no";
            player.sendMessage(Utils.translatable("antimatter.tooltip.cover.output." + suffix + "_input"), player.getUUID());
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
        adjTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.side.getOpposite())
                .ifPresent(adjHandler -> {
                    handler.getTile().getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, this.side).ifPresent(h -> Utils.transferItems(h, adjHandler, false, i -> {
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
        if (processing > 0) return;
        processing++;
        adjTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.side.getOpposite()).ifPresent(adjHandler -> {
            handler.getTile().getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.side).ifPresent(h -> {
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
            ejectItems = !ejectItems;
            if (ejectItems) processItemOutput();
            Utils.markTileForNBTSync(handler.getTile());
        }
        if (event.getFactory() == GuiEvents.FLUID_EJECT) {
            ejectFluids = !ejectFluids;
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
