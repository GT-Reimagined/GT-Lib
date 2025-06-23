package org.gtreimagined.gtlib.blockentity;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.GTLibProperties;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityCable;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.capability.GTLibCaps;
import org.gtreimagined.gtlib.capability.CoverHandler;
import org.gtreimagined.gtlib.capability.EnergyHandler;
import org.gtreimagined.gtlib.capability.Holder;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.ICoverHandlerProvider;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.IMachineHandler;
import org.gtreimagined.gtlib.capability.machine.DefaultHeatHandler;
import org.gtreimagined.gtlib.capability.machine.MachineCoverHandler;
import org.gtreimagined.gtlib.capability.machine.MachineEnergyHandler;
import org.gtreimagined.gtlib.capability.machine.MachineFEHandler;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.client.SoundHelper;
import org.gtreimagined.gtlib.client.dynamic.DynamicTexturer;
import org.gtreimagined.gtlib.client.dynamic.DynamicTexturers;
import org.gtreimagined.gtlib.client.tesr.Caches;
import org.gtreimagined.gtlib.client.tesr.MachineTESR;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.gui.GuiData;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.gui.event.SlotClickEvent;
import org.gtreimagined.gtlib.gui.widget.FluidSlotWidget;
import org.gtreimagined.gtlib.gui.widget.SlotWidget;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.machine.types.BasicMultiMachine;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.network.packets.AbstractGuiEventPacket;
import org.gtreimagined.gtlib.network.packets.TileGuiEventPacket;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.structure.StructureCache;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.util.Cache;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tesseract.api.fe.IExtendedEnergyStorage;
import tesseract.api.fe.IFENode;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.gt.GTFactoryGrid;
import tesseract.api.gt.GTFactoryNetwork;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.gt.IGTCable;
import tesseract.api.gt.IGTNode;
import tesseract.api.heat.IHeatHandler;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import static org.gtreimagined.gtlib.gui.event.GuiEvents.FLUID_EJECT;
import static org.gtreimagined.gtlib.gui.event.GuiEvents.ITEM_EJECT;
import static org.gtreimagined.gtlib.machine.MachineFlag.*;
import static net.minecraft.world.level.block.Blocks.AIR;

public class BlockEntityMachine<T extends BlockEntityMachine<T>> extends BlockEntityTickable<T> implements MenuProvider, IMachineHandler, IGuiHandler, ICoverHandlerProvider<T>, IGTNode {

    /**
     * Open container. Allows for better syncing
     **/
    protected final Set<ContainerMachine<T>> openContainers = new ObjectOpenHashSet<>();

    /**
     * Machine Data
     **/
    protected Machine<?> type;
    protected Tier tier;
    @Getter
    protected MachineState machineState;

    protected MachineState disabledState;

    protected long lastSoundTime;

    @Getter
    protected boolean muffled = false;

    @OnlyIn(Dist.CLIENT)
    public SoundInstance playingSound;

    /**
     * Handlers
     **/
   /* public LazyOptional<MachineItemHandler<?>> itemHandler;
    public LazyOptional<MachineFluidHandler<?>> fluidHandler;
    public LazyOptional<MachineEnergyHandler<U>> energyHandler;
    public LazyOptional<MachineRecipeHandler<U>> recipeHandler;
    public LazyOptional<MachineCoverHandler<BlockEntityMachine>> coverHandler;*/

    public Holder<IItemHandler, MachineItemHandler<T>> itemHandler = new Holder<>(IItemHandler.class, dispatch);
    public Holder<IFluidHandler, MachineFluidHandler<T>> fluidHandler = new Holder<>(IFluidHandler.class, dispatch);
    public Holder<ICoverHandler<?>, MachineCoverHandler<T>> coverHandler = new Holder<>(ICoverHandler.class, dispatch, null);
    public Holder<IEnergyHandler, MachineEnergyHandler<T>> energyHandler = new Holder<>(IEnergyHandler.class, dispatch);
    public Holder<IHeatHandler, DefaultHeatHandler> heatHandler = new Holder<>(IHeatHandler.class, dispatch);
    public Holder<IExtendedEnergyStorage, MachineFEHandler<T>> feHandler = new Holder<>(IExtendedEnergyStorage.class, dispatch);
    public Holder<MachineRecipeHandler<?>, MachineRecipeHandler<T>> recipeHandler = new Holder<>(MachineRecipeHandler.class, dispatch, null);

    GTFactoryNetwork network;

    /**
     * Client related fields.
     **/
    public LazyLoadedValue<DynamicTexturer<Machine<?>, DynamicKey>> multiTexturer;
    public Cache<List<Caches.LiquidCache>> liquidCache;

    public BlockEntityMachine(Machine<?> type, BlockPos pos, BlockState state) {
        super(type.getTileType(), pos, state);
        this.tier = ((BlockMachine) state.getBlock()).getTier();
        this.type = type;
        this.machineState = getDefaultMachineState();
        if (type.has(ITEM) || type.has(CELL)) {
            itemHandler.set(() -> new MachineItemHandler<>((T) this));
        }
        if (type.has(FLUID)) {
            fluidHandler.set(() -> new MachineFluidHandler<>((T) this));
        }
        if (type.has(EU)) {
            energyHandler.set(() -> new MachineEnergyHandler<>((T) this, type.getAmps(), type.has(GENERATOR)));
        }
        if (type.has(FE)){
            feHandler.set(() -> new MachineFEHandler<>((T)this, (int) (this.getMachineTier().getVoltage() * 100), type.has(MachineFlag.GENERATOR)));
        }
        if (type.has(HEAT)){
            heatHandler.set(() -> new DefaultHeatHandler(this, (int) (this.getMachineTier().getVoltage() * 4), type.has(GENERATOR) ? 0 : (int) this.getMachineTier().getVoltage(), type.has(GENERATOR) ? (int) this.getMachineTier().getVoltage() : 0));
        }
        if (type.has(RECIPE)) {
            recipeHandler.set(() -> new MachineRecipeHandler<>((T) this));
        }
        if (type.has(COVERABLE)) {
            coverHandler.set(() -> new MachineCoverHandler<>((T) this));
        }
        multiTexturer = new LazyLoadedValue<>(() -> new DynamicTexturer<>(DynamicTexturers.TILE_DYNAMIC_TEXTURER));
    }

    public void addOpenContainer(ContainerMachine<T> c, Player player) {
        this.openContainers.add(c);
    }

    public void onContainerClose(ContainerMachine<T> c, Player player) {
        this.openContainers.remove(c);
    }

    @Override
    public void onFirstTickServer(Level level, BlockPos pos, BlockState state) {
        super.onFirstTickServer(level, pos, state);
        this.itemHandler.ifPresent(MachineItemHandler::init);
        this.fluidHandler.ifPresent(MachineFluidHandler::init);
        this.energyHandler.ifPresent(MachineEnergyHandler::init);
        this.feHandler.ifPresent(MachineFEHandler::init);
        this.recipeHandler.ifPresent(MachineRecipeHandler::init);
        this.coverHandler.ifPresent(CoverHandler::onFirstTick);
        GTFactoryGrid.INSTANCE.addElement(this);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.level.isClientSide) {
            liquidCache = new Cache<>(() -> MachineTESR.buildLiquids(this));
        }
    }

    protected void cacheInvalidate() {
        if (this.liquidCache != null) liquidCache.invalidate();
    }
    public String getDomain() {
        return getMachineType().getDomain();
    }

    @Override
    public boolean isRemote() {
        return level.isClientSide;
    }

    @Override
    public void addWidgets(GuiInstance instance, IGuiElement parent) {
        int index = 0;
        for (SlotData<?> slot : this.getMachineType().getSlots(this.getMachineTier())) {
            instance.addWidget(SlotWidget.build(slot));
        }
        for (SlotData<?> slot : this.getMachineType().getGuiData().getSlots().getSlots(SlotType.FL_IN, getMachineTier())) {
            instance.addWidget(FluidSlotWidget.build(index++, slot));
        }
        for (SlotData<?> slot : this.getMachineType().getGuiData().getSlots().getSlots(SlotType.FL_OUT, getMachineTier())) {
            instance.addWidget(FluidSlotWidget.build(index++, slot));
        }
        this.getMachineType().getCallbacks().forEach(t -> t.accept(instance));
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return getMachineType().getGuiData().getTexture(this.getMachineTier(), "machine");
    }

    @Override
    public GuiData getGui() {
        return getMachineType().getGuiData();
    }

    /**
     * RECIPE UTILITY METHODS
     **/

    //Called before a recipe ticks.
    public void onRecipePreTick() {
        //NOOP
    }

    //Called after a recipe ticks.
    public void onRecipePostTick() {
        //NOOP
    }

    //Called whenever a recipe is stopped.
    public void onMachineStop() {
        lastSoundTime = 0;
    }

    //Called whenever a recipe is activated, might be the same as before (e.g. no new recipe).
    public void onMachineStarted(IRecipe r) {

    }

    public void onBlockUpdate(BlockPos neighbor) {
        super.onBlockUpdate(neighbor);
        Direction facing = Utils.getOffsetFacing(this.getBlockPos(), neighbor);
        if (facing != null) {
            coverHandler.ifPresent(h -> h.onBlockUpdate(facing));
        }
        coverHandler.ifPresent(CoverHandler::onBlockUpdateAllSides);
        GTFactoryGrid.INSTANCE.addElement(this);
    }


    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state) {
        itemHandler.ifPresent(MachineItemHandler::onUpdate);
        energyHandler.ifPresent(MachineEnergyHandler::onUpdate);
        feHandler.ifPresent(MachineFEHandler::onUpdate);
        heatHandler.ifPresent(handler -> handler.update(getMachineState() == MachineState.ACTIVE));
        fluidHandler.ifPresent(MachineFluidHandler::onUpdate);
        coverHandler.ifPresent(MachineCoverHandler::onUpdate);
        this.recipeHandler.ifPresent(MachineRecipeHandler::onServerUpdate);

        if (allowExplosionsInRain()) {
            double d = Ref.RNG.nextDouble();
            if (d > 0.97D && this.level.isRainingAt(new BlockPos(this.worldPosition.getX(), this.worldPosition.getY() + 1, this.worldPosition.getZ()))) {
                if (this.energyHandler.map(t -> t.getEnergy() > 0).orElse(false)) {
                    Utils.createExplosion(this.level, worldPosition, 6.0F, Explosion.BlockInteraction.DESTROY);
                    level.playSound(null, this.worldPosition, Ref.MACHINE_EXPLODE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
        }
    }

    protected boolean allowExplosionsInRain(){
        return GTLibConfig.RAIN_EXPLODES_MACHINES.get();
    }

    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state) {
        coverHandler.ifPresent(MachineCoverHandler::onUpdate);
    }

    @Override
    protected boolean canClientTick() {
        return getMachineType().isClientTicking();
    }

    @Override
    public void onRemove() {
        if (isServerSide()) {
            coverHandler.ifPresent(MachineCoverHandler::onRemove);
            fluidHandler.ifPresent(MachineFluidHandler::onRemove);
            itemHandler.ifPresent(MachineItemHandler::onRemove);
            energyHandler.ifPresent(MachineEnergyHandler::onRemove);
            feHandler.ifPresent(MachineFEHandler::onRemove);
            recipeHandler.ifPresent(MachineRecipeHandler::onRemove);

            dispatch.invalidate();
            GTFactoryGrid.INSTANCE.removeElement(this);
        } else {
            if (level != null) SoundHelper.clear(level, worldPosition);
        }
    }

    public void onDrop(BlockState state, LootContext.Builder builder, List<ItemStack> drops){

    }

    public void dropInventory(BlockState state, LootContext.Builder builder, List<ItemStack> drops){
        itemHandler.ifPresent(t -> drops.addAll(t.getAllItems()));
    }

    public void dropCovers(BlockState state, LootContext.Builder builder, List<ItemStack> drops){
        coverHandler.ifPresent(c -> {
            if (!drops.isEmpty()) {
                ItemStack machine = drops.get(0);
                if (machine.getItem() == state.getBlock().asItem()){
                    c.writeToStack(machine);
                }
            }
        });
    }

    public void onPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack){
        if (!level.isClientSide()) {
            coverHandler.ifPresent(c -> c.readFromStack(stack));
        }
    }

    protected void markDirty() {
        this.getLevel().getChunkAt(this.getBlockPos()).setUnsaved(true);
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (this.getLevel() != null && !this.getLevel().isClientSide) {
            coverHandler.ifPresent(c -> c.onMachineEvent(event, data));
            itemHandler.ifPresent(i -> i.onMachineEvent(event, data));
            energyHandler.ifPresent(e -> e.onMachineEvent(event, data));
            feHandler.ifPresent(e -> e.onMachineEvent(event, data));
            fluidHandler.ifPresent(f -> f.onMachineEvent(event, data));
            recipeHandler.ifPresent(r -> r.onMachineEvent(event, data));
            /*if (event instanceof ContentEvent && openContainers.size() > 0) {
                //openContainers.forEach(ContainerMachine::detectAndSendLiquidChanges);
            }*/
            markDirty();
        }
    }

    /**
     * Getters
     **/
    public Machine<?> getMachineType() {
        if (type != null) return type;
        Block block = getBlockState().getBlock();
        if (!(block instanceof BlockMachine)) return null;
        return ((BlockMachine) block).getType();
    }

    public Tier getMachineTier() {
        if (tier != null) return tier;
        Block block = getBlockState().getBlock();
        if (!(block instanceof BlockMachine)) return Tier.LV;
        return ((BlockMachine) block).getTier();
    }

    //Returns the tier level for recipe overclocking.
    public Tier getPowerLevel() {
        return getMachineTier();
    }

    public boolean has(String flag) {
        return getMachineType().has(flag);
    }

    public int getWeakRedstonePower(Direction facing) {
        if (facing != null && this.getCover(facing).getWeakPower() >= 0) {
            return this.getCover(facing).getWeakPower();
        }
        return 0;
    }

    public int getStrongRedstonePower(Direction facing) {
        if (facing != null && this.getCover(facing).getStrongPower() >= 0) {
            return this.getCover(facing).getStrongPower();
        }
        return 0;
    }

    public Direction getFacing() {
        if (this.level == null) return Direction.SOUTH;
        BlockState state = getBlockState();
        return getFacing(state);
    }

    public Direction getFacing(BlockState state){
        if (state == AIR.defaultBlockState()) {
            return Direction.SOUTH;
        }
        if (getMachineType().isNoFacing()) return Direction.SOUTH;
        if (getMachineType().isVerticalFacingAllowed()) {
            return state.getValue(BlockStateProperties.FACING);
        }
        return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
    }

    public boolean setFacing(Direction side) {
        if (getMachineType().isNoFacing() || side == getFacing() || (side.getAxis() == Direction.Axis.Y && !getMachineType().isVerticalFacingAllowed()))
            return false;
        boolean isEmpty = coverHandler.map(ch -> ch.get(side).isEmpty()).orElse(true);
        if (isEmpty) {
            BlockState state = getBlockState();
            if (getMachineType().isVerticalFacingAllowed()) {
                state = state.setValue(BlockStateProperties.FACING, side);
            } else {
                state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, side);
            }
            getLevel().setBlockAndUpdate(getBlockPos(), state);
            invalidateCaps();

            return true;
        }
        return false;
    }

    protected boolean setFacing(Player player, Direction side) {
        boolean setFacing = setFacing(side);
        if (setFacing) player.playNotifySound(Ref.WRENCH, SoundSource.BLOCKS, 1.0f, 1.0f);
        return setFacing;
    }

    public boolean wrenchMachine(Player player, BlockHitResult res, boolean crouch) {
        if ((crouch || getMachineType().getOutputCover() == ICover.emptyFactory) && !type.isNoFacing()) {
            //Machine has no output
            return setFacing(player, Utils.getInteractSide(res));
        }
        return setOutputFacing(player, Utils.getInteractSide(res));
    }

    @Override
    public void onGuiEvent(IGuiEvent event, Player player) {
        if (event.getFactory() == ITEM_EJECT || event.getFactory() == FLUID_EJECT) {
            coverHandler.ifPresent(ch -> {
                ch.getOutputCover().onGuiEvent(event, player);
            });
        }
        if (event.getFactory() == SlotClickEvent.SLOT_CLICKED) {
            itemHandler.ifPresent(t -> {
               // ItemStack stack = player.get;
              //  GTLib.LOGGER.info("packet got");
            });
        }
    }

    @Override
    public AbstractGuiEventPacket createGuiPacket(IGuiEvent event) {
        return new TileGuiEventPacket(event, getBlockPos());
    }

    @Override
    public String handlerDomain() {
        return getDomain();
    }

    public void setMuffled(boolean muffled) {
        this.muffled = muffled;
        sidedSync(true);
        if (this.muffled && level != null && level.isClientSide) SoundHelper.clear(level, this.getBlockPos());
    }

    public Direction getOutputFacing() {
        if (type.getOutputCover() != null && !(type.getOutputCover() == ICover.emptyFactory) && coverHandler.isPresent()) {
            Direction dir = coverHandler.get().getOutputFacing();
            return dir == null ? getFacing().getOpposite() : dir;
        }
        return null;
    }

    public boolean setOutputFacing(Player player, Direction side) {
        return coverHandler.map(h -> h.setOutputFacing(player, side)).orElse(false);
    }

    public Direction getSecondaryOutputFacing() {
        if (type.getSecondaryOutputCover() != null && !(type.getSecondaryOutputCover() == ICover.emptyFactory)) {
            return coverHandler.map(MachineCoverHandler::getSecondaryOutputFacing).orElse(this.getFacing().getOpposite());
        }
        return null;
    }

    public boolean setSecondaryOutputFacing(Player player, Direction side) {
        return coverHandler.map(h -> h.setSecondaryOutputFacing(player, side)).orElse(false);
    }

    public MachineState getDefaultMachineState() {
        return MachineState.IDLE;
    }

    public boolean isDefaultMachineState() {
        return getMachineState() == getDefaultMachineState();
    }

    public long getMaxInputVoltage() {
        return energyHandler.map(EnergyHandler::getInputVoltage).orElse(0L);
    }

    public long getMaxOutputVoltage() {
        return energyHandler.map(EnergyHandler::getOutputVoltage).orElse(0L);
    }

    /**
     * Helpers
     **/
    public void resetMachine() {
        setMachineState(getDefaultMachineState());
    }

    public boolean toggleMachine() {
        if (getMachineState() == MachineState.DISABLED) {
            setMachineState(disabledState);
            disabledState = null;
            if (getMachineState().allowRecipeCheck()) {
                recipeHandler.ifPresent(MachineRecipeHandler::checkRecipe);
            }
        } else {
            disableMachine();
        }
        return true;
    }

    protected void disableMachine() {
        disabledState = getMachineState();
        if (!has(GENERATOR)) recipeHandler.ifPresent(MachineRecipeHandler::resetProgress);
        if (level != null && level.isClientSide) SoundHelper.clear(level, this.getBlockPos());
        setMachineState(MachineState.DISABLED);
    }

    public void setMachineState(MachineState newState) {
        if (this.machineState != newState) {
            MachineState old = this.machineState;
            this.machineState = newState;
            if (level != null) {
                sidedSync(true);
                if (!level.isClientSide) {
                    if (old == MachineState.ACTIVE) {
                        this.onMachineStop();
                    } else if (newState == MachineState.ACTIVE) {
                        if (recipeHandler.isPresent()) {
                            MachineRecipeHandler<?> handler = recipeHandler.get();
                            this.onMachineStarted(handler.getActiveRecipe());
                        }
                    }
                } else {
                    cacheInvalidate();
                }
            }
            setChanged();
            if (this.level != null && this.level.isClientSide && this.getMachineType().machineNoise != null) {
                if (newState == MachineState.ACTIVE) {
                    if (!muffled) SoundHelper.startLoop(this.type, level, this.getBlockPos());
                } else if (old == MachineState.ACTIVE) {
                    SoundHelper.clear(level, this.getBlockPos());
                }
            }
        }
    }

    public CoverFactory[] getValidCovers() {
        return GTAPI.all(CoverFactory.class).stream().filter(t -> t.getIsValid().test(this)).toArray(CoverFactory[]::new);
    }

    public ICover getCover(Direction side) {
        return coverHandler.map(h -> h.get(side)).orElse(ICover.empty);
    }

    public Function<Direction, Texture> getMultiTexture(){
        if (this.getMachineType() instanceof BasicMultiMachine<?>) return null;
        BlockEntityBasicMultiMachine mTile = StructureCache.getAnyMulti(this.getLevel(), worldPosition, BlockEntityBasicMultiMachine.class);
        if (mTile != null) {
            return dir -> mTile.getTextureForHatches(dir, worldPosition);
        }
        return null;
    }

    public InteractionResult onInteractBoth(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, @Nullable GTToolType type) {
        //DEFAULT
        return isServerSide() ? onInteractServer(state, world, pos, player, hand, hit, type) : onInteractClient(state, world, pos, player, hand, hit, type);
    }

    public InteractionResult onInteractServer(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, @Nullable GTToolType type){
        return InteractionResult.PASS;
    }

    public InteractionResult onInteractClient(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, @Nullable GTToolType type){
        return InteractionResult.PASS;
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return getMachineType().getDisplayName(getMachineTier());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory inv, @NotNull Player player) {
        return getMachineType().has(GUI) ? getMachineType().getGuiData().getMenuHandler().menu(this, inv, windowId) : null;
    }

    public boolean canPlayerOpenGui(Player playerEntity) {
        return true;
    }

    @Override
    public void invalidateCaps() {
        if (isServerSide()) {
            dispatch.invalidate();
        }
    }

    public void invalidateCaps(Direction side) {
        if (isServerSide()) {
            dispatch.invalidate(side);
        }
    }

    public void invalidateCap(Class<?> cap) {
        if (isServerSide()) {
            dispatch.invalidate(cap);
        }
    }

    public <V> boolean blocksCapability(@NotNull Class<V> cap, Direction side) {
        return coverHandler.map(t -> t.blocksCapability(cap, side)).orElse(false);
    }

    @NotNull
    @Override
    public <U> LazyOptional<U> getCapability(@NotNull Capability<U> cap, @Nullable Direction side) {
        int index = side == null ? 6 : side.get3DDataValue();
        if (side == getFacing() && !allowsFrontIO()) return LazyOptional.empty();
        if (blocksCapability(GTLibCaps.CAP_MAP.inverse().get(cap), side)) return LazyOptional.empty();
        return getCap(cap, side);
    }


    protected <U> LazyOptional<U> getCap(@NotNull Capability<U> cap, @Nullable Direction side) {
        int index = side == null ? 6 : side.get3DDataValue();
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && fluidHandler.isPresent()) {
            return fluidHandler.side(side).cast();
        }
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && itemHandler.isPresent()) {
            return itemHandler.side(side).cast();
        }
        if (cap == TesseractCaps.ENERGY_HANDLER_CAPABILITY && energyHandler.isPresent()) {
            return energyHandler.side(side).cast();
        }
        if (cap == CapabilityEnergy.ENERGY && feHandler.isPresent()){
            return feHandler.side(side).cast();
        }
        return super.getCapability(cap, side);
    }

    public final boolean allowsFrontIO() {
        return getMachineType().allowsFrontIO();
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);

        setMachineState(MachineState.VALUES[tag.getInt(Ref.KEY_MACHINE_STATE)]);
        if (tag.contains(Ref.KEY_MACHINE_MUFFLED)) {
            setMuffled(tag.getBoolean(Ref.KEY_MACHINE_MUFFLED));
        }
        if (tag.contains(Ref.KEY_MACHINE_STATE_D)) {
            disabledState = MachineState.VALUES[tag.getInt(Ref.KEY_MACHINE_STATE_D)];
        }
        if (tag.contains(Ref.KEY_MACHINE_ITEMS))
            itemHandler.ifPresent(i -> i.deserialize(tag.getCompound(Ref.KEY_MACHINE_ITEMS)));
        if (tag.contains(Ref.KEY_MACHINE_ENERGY)) {
            energyHandler.ifPresent(e -> e.deserialize(tag.getCompound(Ref.KEY_MACHINE_ENERGY)));
        }
        if (tag.contains(Ref.KEY_MACHINE_FE)){
            feHandler.ifPresent(e -> e.deserialize(tag.getCompound(Ref.KEY_MACHINE_FE)));
        }
        if (tag.contains(Ref.KEY_MACHINE_HEAT)){
            heatHandler.ifPresent(h -> h.deserialize(tag.getCompound(Ref.KEY_MACHINE_HEAT)));
        }
        if (tag.contains(Ref.KEY_MACHINE_COVER))
            coverHandler.ifPresent(e -> e.deserialize(tag.getCompound(Ref.KEY_MACHINE_COVER)));
        if (tag.contains(Ref.KEY_MACHINE_FLUIDS)) {
            fluidHandler.ifPresent(e -> e.deserialize(tag.getCompound(Ref.KEY_MACHINE_FLUIDS)));
            if (level != null && level.isClientSide) {
                cacheInvalidate();
            }

        }
        if (tag.contains(Ref.KEY_MACHINE_RECIPE))
            recipeHandler.ifPresent(e -> e.deserialize(tag.getCompound(Ref.KEY_MACHINE_RECIPE)));
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt(Ref.KEY_MACHINE_STATE, machineState.ordinal());
        tag.putBoolean(Ref.KEY_MACHINE_MUFFLED, muffled);
        if (disabledState != null)
            tag.putInt(Ref.KEY_MACHINE_STATE_D, disabledState.ordinal());
        itemHandler.ifPresent(i -> tag.put(Ref.KEY_MACHINE_ITEMS, i.serialize(new CompoundTag())));
        energyHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_ENERGY, e.serialize(new CompoundTag())));
        feHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_FE, e.serialize(new CompoundTag())));
        coverHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_COVER , e.serialize(new CompoundTag())));
        fluidHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_FLUIDS, e.serialize(new CompoundTag())));
        recipeHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_RECIPE, e.serialize()));
        heatHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_HEAT, e.serialize(new CompoundTag())));
    }

    @NotNull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        coverHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_COVER, e.serialize(new CompoundTag())));
        if (this.getMachineType().rendersContainedLiquids()) {
            fluidHandler.ifPresent(e -> tag.put(Ref.KEY_MACHINE_FLUIDS, e.serialize(new CompoundTag())));
        }
        tag.putInt(Ref.KEY_MACHINE_STATE, machineState.ordinal());
        tag.putBoolean(Ref.KEY_MACHINE_MUFFLED, muffled);
        return tag;
    }


    @Override
    public List<String> getInfo(boolean simple) {
        List<String> info = super.getInfo(simple);
        if (!simple) {
            info.add("Machine: " + getMachineType().getId() + " Tier: " + getMachineTier().getId());
            info.add("State: " + getMachineState().getId());
            String slots = "";
            if (getMachineType().has(ITEM)) {
                int inputs = getMachineType().getSlots(SlotType.IT_IN, getMachineTier()).size();
                int outputs = getMachineType().getSlots(SlotType.IT_OUT, getMachineTier()).size();
                if (inputs > 0) slots += (" IT_IN: " + inputs + ",");
                if (outputs > 0) slots += (" IT_OUT: " + outputs + ",");
            }
            if (getMachineType().has(FLUID) && getMachineType().has(GUI)) {
                int inputs = getMachineType().getSlots(SlotType.FL_IN, getMachineTier()).size();
                int outputs = getMachineType().getSlots(SlotType.FL_OUT, getMachineTier()).size();
                if (inputs > 0) slots += (" FL_IN: " + inputs + ",");
                if (outputs > 0) slots += (" FL_OUT: " + outputs + ",");
            }
            if (slots.length() > 0) info.add("Slots:" + slots);
            if (type.has(FE))
                feHandler.ifPresent(h -> info.add("FE: " + h.getEnergyStored() + " / " + h.getMaxEnergyStored()));
            if (type.has(EU))
                energyHandler.ifPresent(h -> info.add("EU: " + h.getEnergy() + " / " + h.getCapacity()));

            recipeHandler.ifPresent(rh -> {
                rh.getInfo(info);
            });
            //multiTexture.ifPresent(mt -> info.add("Rendering using texture " + mt.toString() + "."));
        }
        coverHandler.ifPresent(h -> {
            if (!simple){
                StringBuilder builder = new StringBuilder("Covers: ");
                for (Direction side : Ref.DIRS) {
                    builder.append(h.get(side).getId()).append(" ");
                }
                info.add(builder.toString());
            }
            h.getCovers().forEach((d, c) -> {
                if (!c.isEmpty()){
                    info.addAll(c.getInfo(simple));
                }
            });
        });
        return info;
    }

    public String getId() {
        return this.getMachineType().getId();
    }

    @Override
    public Optional<ICoverHandler<T>> getCoverHandler() {
        return coverHandler.map(c -> c);
    }

    @Override
    public boolean isOutput(Direction direction) {
        return energyHandler.map(e -> e.canOutput(direction)).orElse(false);
    }

    @Override
    public boolean insulated() {
        return true;
    }

    @Override
    public boolean connects(Direction direction) {
        BlockEntity neighbor = getCachedBlockEntity(direction);
        return neighbor instanceof BlockEntityCable<?> cable && cable.connects(direction.getOpposite());
    }

    @Override
    public boolean validate(Direction dir) {
        return connects(dir);
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public boolean isActuallyNode() {
        return true;
    }

    @Override
    public void getNeighbours(Collection<IGTCable> neighbours) {
        for (Direction dir : Direction.values()) {
            BlockEntity neigbor = getCachedBlockEntity(dir);
            if (neigbor instanceof BlockEntityCable<?> cable) {
                if (cable.connects(dir.getOpposite())){
                    neighbours.add(cable);
                }
            }
        }
    }

    @Override
    public GTFactoryNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(GTFactoryNetwork network) {
        this.network = network;
    }

    /**
     * The key used to build dynamic textures.
     */
    public static class DynamicKey {
        public final ResourceLocation model;
        public final Texture tex;
        public Direction facing;
        public final MachineState state;
        public GTLibProperties.MachineProperties properties;

        public DynamicKey(ResourceLocation model, Texture tex, Direction dir, MachineState state, GTLibProperties.MachineProperties properties) {
            this.model = model;
            this.tex = tex;
            this.facing = dir;
            this.state = state;
            this.properties = properties;
        }

        public void setDir(Direction dir) {
            this.facing = dir;
        }

        @Override
        public int hashCode() {
            return tex.hashCode() + facing.hashCode() + state.hashCode() + model.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof DynamicKey) {
                DynamicKey key = (DynamicKey) o;
                return key.state == state && key.facing == facing && tex.equals(key.tex) && model.equals(key.model);
            }
            return false;
        }
    }
}