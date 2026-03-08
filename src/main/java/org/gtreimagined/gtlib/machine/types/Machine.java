package org.gtreimagined.gtlib.machine.types;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.widgets.slot.FluidSlot;
import brachy.modularui.widgets.slot.ItemSlot;
import brachy.modularui.widgets.slot.ModularSlot;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.registries.ForgeRegistries;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTCreativeTabs;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.GTItemBlock;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.client.dynamic.IDynamicModelProvider;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.gui.BarDir;
import org.gtreimagined.gtlib.gui.GuiData;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.MenuHandler;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.slot.ISlotProvider;
import org.gtreimagined.gtlib.gui.widget.BackgroundWidget;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.IMachineColorHandlerBlock;
import org.gtreimagined.gtlib.machine.IMachineColorHandlerItem;
import org.gtreimagined.gtlib.machine.IPanelFunction;
import org.gtreimagined.gtlib.machine.IShapeGetter;
import org.gtreimagined.gtlib.machine.ITooltipInfo;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.GTGuiTextures;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.structure.Structure;
import org.gtreimagined.gtlib.structure.StructureBuilder;
import org.gtreimagined.gtlib.texture.IOverlayModeler;
import org.gtreimagined.gtlib.texture.IOverlayTexturer;
import org.gtreimagined.gtlib.texture.ITextureHandler;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Dir;
import org.gtreimagined.gtlib.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static org.gtreimagined.gtlib.Data.COVEROUTPUT;
import static org.gtreimagined.gtlib.machine.MachineFlag.RECIPE;
import static org.gtreimagined.gtlib.machine.Tier.NONE;

/**
 * Machine represents the base class for an GTLib Machine. It provides tile entities, blocks as well as
 * features to configure machines such as vertical facing, the recipe map and smaller behaviours like if front IO is allowed.
 *
 * @param <T> this class as a generic argument.
 */
public class Machine<T extends Machine<T>> implements IGTObject, IRegistryEntryProvider, ISlotProvider<Machine<T>>, IGuiHandler.IHaveWidgets, IDynamicModelProvider {

    
    /**
     * Basic Members
     **/
    @Getter
    protected BlockEntityType<? extends BlockEntityMachine<?>> tileType;
    protected BlockEntityBase.BlockEntitySupplier<BlockEntityMachine<?>, T> tileFunc = BlockEntityMachine::new;
    protected BiFunction<Machine<T>, Tier, BlockMachine> blockFunc = BlockMachine::new;
    @Getter
    protected Function<BlockMachine, GTItemBlock> itemBlockFunction = GTItemBlock::new;

    protected Supplier<Class<? extends BlockMachine>> itemClassSupplier = () -> BlockMachine.class;
    @Getter
    protected TagKey<Block> toolTag = GTTools.WRENCH.getToolType();
    @Getter
    protected List<ITooltipInfo> tooltipFunctions = new ArrayList<>();
    @Getter
    protected IShapeGetter shapeGetter;
    @Getter
    protected String domain, id;
    @Getter
    protected List<Tier> tiers;

    /**
     * Recipe Members
     **/
    protected Map<String, IRecipeMap> tierRecipeMaps = new Object2ObjectOpenHashMap<>();

    /**
     * GUI Members
     **/
    @Getter
    protected GuiData guiData;
    @Getter
    protected Supplier<ModularPanel<?>> modularPanelSupplier = () -> ModularPanel.defaultPanel(this.getId(), guiData.getXSize(), guiData.getYSize());
    @Getter
    protected IPanelFunction backGroundFunction = (modularPanel, machine, guiData1, syncManager, settings) -> {
        if (guiData.hasGTIcon()) {
            modularPanel.child(guiData.getGtIcon().asWidget().pos(guiData.getGtIconPos().x, guiData.getGtIconPos().y));
        }
        if (guiData.enablePlayerSlots()) {
            modularPanel.bindPlayerInventory();
        }
    };
    @Getter
    protected IPanelFunction slotFunction = (modularPanel, machine, guiData1, syncManager, settings) -> {
        Object2IntMap<String> slotIndexMap = new Object2IntOpenHashMap<>();
        for (SlotData<?> slotData : getSlots(machine.getMachineTier())){
            ResourceLocation overlay = slotData.getTexture().getPath().equals(Ref.ID) && (slotData.getTexture().getNamespace().endsWith("item.png")) ? null : slotData.getTexture();
            UITexture slotOverlay = overlay != null ? UITexture.fullImage(overlay) : null;
            boolean item = slotData.getType() != SlotType.FL_IN && slotData.getType() != SlotType.FL_OUT;
            slotIndexMap.computeIntIfAbsent(slotData.getType().getId(), k -> 0);
            if (item){
                Slot slot = slotData.getType().getSlotSupplier().get((SlotType) slotData.getType(), machine, machine.itemHandler.map(MachineItemHandler::getAll).orElse(null), slotIndexMap.getInt(slotData.getType().getId()), (SlotData) slotData);
                ItemSlot itemSlot = new ItemSlot().pos(slotData.getX(), slotData.getY());
                if (slot instanceof ModularSlot modularSlot){
                    itemSlot.slot(modularSlot);
                }
                modularPanel.child(itemSlot);
                //if (slotOverlay != null) modularPanel.child(slotOverlay.asWidget().pos(slotData.getX(), slotData.getY()));
            } else {
                FluidTanks tanks = slotData.getType() == SlotType.FL_IN ? machine.fluidHandler.get().getInputTanks() : machine.fluidHandler.get().getOutputTanks();
                FluidSlot fluidSlot = new FluidSlot().pos(slotData.getX(), slotData.getY()).alwaysShowFull(true)
                        .syncHandler(new FluidSlotSyncHandler(tanks.getTank(slotIndexMap.getInt(slotData.getType().getId()))));
                modularPanel.child(fluidSlot);
                //if (slotOverlay != null) fluidSlot.
            }
            slotIndexMap.computeInt(slotData.getType().getId(), (a, b) -> {
                if (b == null) return 0;
                return b + 1;
            });

        }
    };
    @Getter
    protected List<IPanelFunction> guiFunctions = new ArrayList<>();
    @Getter
    protected ResourceKey<CreativeModeTab> group = GTCreativeTabs.MACHINES.getKey();

    /**
     * Texture Members
     **/
    protected ITextureHandler baseTexture;
    @Getter
    protected IOverlayTexturer overlayTextures;
    protected IOverlayModeler overlayModels;
    @Getter
    protected ResourceLocation itemModelParent;
    @Getter
    protected IMachineColorHandlerBlock blockColorHandler = (state, world, pos, machine, i) -> -1;
    @Getter
    protected IMachineColorHandlerItem itemColorHandler = (stack, block, i) -> -1;
    @Getter
    protected ResourceLocation modelLoader = GTLibModelManager.LOADER_MACHINE;

    protected boolean tierSpecificLang = false;

    public SoundEvent machineNoise;
    public float soundVolume;
    /**
     * Multi Members
     **/
    protected Object2ObjectMap<Tier, Structure> structures = new Object2ObjectOpenHashMap<>();

    /**
     * Energy data
     **/
    protected ToIntFunction<Tier> efficiency = t -> 100 - (5 * (4 - t.getIntegerId()));
    //How many amps this machine requires.
    @Getter
    protected int amps = 1;

    /**
     * Behaviours
     **/
    @Accessors(fluent = true)
    @Getter
    protected boolean allowsFrontCovers = false;
    @Accessors(fluent = true)
    @Getter
    protected boolean allowsOutputCoversOnFacing = false;
    @Getter
    protected boolean verticalFacingAllowed = false;
    @Getter
    protected boolean noFacing = false;
    @Getter
    protected boolean noTextureRotation = false;
    @Accessors(fluent = true)
    @Getter
    protected boolean allowsFrontIO = false;
    @Getter
    protected boolean noOverclockCost = false;
    @Getter
    protected boolean clientTicking = false;
    @Getter
    protected boolean ambientTicking = false;

    /**
     * Rendering
     */
    @Accessors(fluent = true)
    @Getter
    protected boolean renderAsTesr = false;
    @Accessors(fluent = true)
    @Getter
    protected boolean rendersContainedLiquids = false;
    @Accessors(fluent = true)
    @Getter
    protected boolean rendersContainedLiquidLevel = false;
    @Getter
    @Setter
    protected int overlayLayers = 1;

    /**
     * Covers
     **/
    @Getter
    protected CoverFactory outputCover = COVEROUTPUT;
    @Getter
    protected Dir outputDir = Dir.BACK;
    @Getter
    protected CoverFactory secondaryOutputCover = ICover.emptyFactory;
    @Getter
    protected Dir secondaryOutputDir = Dir.FORWARD;

    /**
     * Slots
     **/
    @Getter
    private final Map<String, Object2IntOpenHashMap<SlotType<?>>> countLookup = new Object2ObjectOpenHashMap<>();
    @Getter
    private final Map<String, List<SlotData<?>>> slotLookup = new Object2ObjectOpenHashMap<>();

    @Getter
    private final List<Consumer<GuiInstance>> callbacks = new ObjectArrayList<>(1);
    private static final Map<String, Set<Machine<?>>> FLAG_MAP = new Object2ObjectOpenHashMap<>();

    public Machine(String domain, String id) {
        this.domain = domain;
        this.id = id;
        //Default implementation.
        overlayTextures = (type, state, tier, i) -> {
            state = state.getTextureState();
            String stateDir = state == MachineState.IDLE ? "" : state.getId() + "/";
            return new Texture[]{
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "bottom"),
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "top"),
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "back"),
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "front"),
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "side"),
                    new Texture(domain, "block/machine/overlay/" + id + "/" + stateDir + "side"),
            };
        };
        baseTexture = (m, tier, state) -> new Texture[]{tier.getBaseTexture(m.getDomain())};
        overlayModels = (a,s,d) -> {
            return new ResourceLocation(Ref.ID, "block/machine/overlay/invalid/" + d.getName());
        };
        itemModelParent = new ResourceLocation(Ref.ID, "block/preset/layered");
        tiers = Arrays.asList(Tier.getStandard());
        GTAPI.register(Machine.class, this);
        //if (FMLEnvironment.dist.isClient()) {
        setupGui();
        //}
    }

    protected void setupGui() {
        addGuiCallback(t -> t.addWidget(BackgroundWidget.build(t.handler.getGuiTexture(), t.handler.guiSize(), t.handler.guiHeight(), t.handler.guiTextureSize(), t.handler.guiTextureHeight())));
    }

    public Direction handlePlacementFacing(BlockPlaceContext ctxt, Property<?> which, Direction dir) {
        return dir;
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (registry != ForgeRegistries.Keys.BLOCKS) return;
        tileType = new BlockEntityType<>(new BlockEntityBase.BlockEntityGetter<>(tileFunc, (T)this), tiers.stream().map(t -> getBlock(this, t)).collect(Collectors.toSet()), null);
        GTAPI.register(BlockEntityType.class, getId(), getDomain(), getTileType());
    }

    /**
     * Registers the recipemap into JEI. This can be overriden in RecipeMap::setGuiData.
     */
    public void registerJei() {
        if (this.guiData != null) {
            tierRecipeMaps.forEach((s, r) -> {
                if (s.isEmpty()){
                    for (int i = 0; i < tiers.size(); i++) {
                        Tier tier = tiers.get(i);
                        if (i == 0 && r.getGui() == null && !GTLibXEIPlugin.containsCategory(r)){
                            GTAPI.registerJEICategory(r, this.guiData, this, tier, false);
                        } else {
                            GTAPI.registerJEICategoryWorkstation(r, this, tier);
                        }
                    }
                    return;
                }
                Tier t = GTAPI.get(Tier.class, s);
                //If the recipe map has another GUI present don't register it.
                if (r.getGui() == null && !GTLibXEIPlugin.containsCategory(r)) {
                    GTAPI.registerJEICategory(r, this.guiData, this, t, false);
                } else {
                    GTAPI.registerJEICategoryWorkstation(r, this, t);
                }
            });

        }
    }

    /**
     * Setters
     */

    /**
     * Sets the required amps for this machine.
     *
     * @param amps amperage
     * @return this.
     */
    public T setAmps(int amps) {
        this.amps = amps;
        return (T) this;
    }

    public T setEfficiency(ToIntFunction<Tier> function){
        this.efficiency = function;
        return (T) this;
    }

    /**
     * Can you place covers on the front face of this machine?
     *
     * @return this.
     */
    public T setAllowsFrontCovers() {
        allowsFrontCovers = true;
        return (T) this;
    }

    public T setAllowsOutputCoversOnFacing(boolean allow) {
        allowsOutputCoversOnFacing = allow;
        return (T) this;
    }

    public T setSound(SoundEvent loc, float volume) {
        this.soundVolume = volume;
        this.machineNoise = loc;
        return (T) this;
    }

    public T setTierSpecificLang(){
        this.tierSpecificLang = true;
        return (T) this;
    }

    /**
     * Sets the output cover fort his machine, which is per default placed on the opposite side of the machine
     * upon placement.
     *
     * @param cover the cover.
     * @return this.
     */
    public T setOutputCover(CoverFactory cover) {
        this.outputCover = cover;
        return (T) this;
    }

    public T setOutputDir(Dir dir) {
        this.outputDir = dir;
        return (T) this;
    }

    public T setSecondaryOutputCover(CoverFactory cover) {
        this.secondaryOutputCover = cover;
        return (T) this;
    }

    public T setSecondaryOutputDir(Dir dir) {
        this.secondaryOutputDir = dir;
        return (T) this;
    }

    public T setNoOutputCover(){
        setOutputCover(ICover.emptyFactory);
        return (T) this;
    }

    public T setAllowsFrontIO() {
        this.allowsFrontIO = true;
        return (T) this;
    }

    public T addTier(Tier tier) {
        Collection<Tier> tiers = getTiers();
        tiers.add(tier);
        setTiers(!tiers.isEmpty() ? tiers.toArray(new Tier[0]) : Tier.getStandard());
        return (T) this;
    }

    /**
     * Sets the recipe map this machine uses for lookup. This will also register it in JEI
     * but it can be overriden by setGuiData in the RecipeMap.
     *
     * @param map the recipe map.
     * @param tiers optional array of tiers this map is for
     * @return this.
     */
    public T setMap(IRecipeMap map, Tier... tiers){
        if (tiers.length == 0) {
            this.tierRecipeMaps.put("", map);
        } else {
            for (Tier tier : tiers) {
                this.tierRecipeMaps.put(tier.getId(), map);
            }
        }
        addFlags(RECIPE);
        registerJei();
        return (T) this;
    }

    public T setBaseTexture(Texture tex) {
        this.baseTexture = (m, tier, state) -> new Texture[]{tex};
        return (T) this;
    }

    /**
     * Set the getter for overlayTextures. All AM machines are base + overlay textures, this represents the getter for overlay texture. See default
     * behaviour in constructor.
     *
     * @param texturer the texture handler
     * @return this
     */
    public T setOverlayTextures(IOverlayTexturer texturer) {
        this.overlayTextures = texturer;
        return (T) this;
    }

    /**
     * Set the getter for baseTexture. All AM machines are base + overlay textures, this represents the getter for base texture. See default
     * behaviour in constructor.
     *
     * @param handler the texture handler
     * @return this
     */
    public T setBaseTexture(ITextureHandler handler) {
        this.baseTexture = handler;
        return (T) this;
    }

    public T setItemModelParent(ResourceLocation parent){
        this.itemModelParent = parent;
        return (T) this;
    }

    public T setBlockColorHandler(IMachineColorHandlerBlock handlerBlock){
        this.blockColorHandler = handlerBlock;
        return (T) this;
    }

    public T setItemColorHandler(IMachineColorHandlerItem handlerItem){
        this.itemColorHandler = handlerItem;
        return (T) this;
    }

    public T setModelLoader(ResourceLocation modelLoader){
        this.modelLoader = modelLoader;
        return (T) this;
    }

    public T setItemGroup(ResourceKey<CreativeModeTab> group) {
        this.group = group;
        return (T) this;
    }

    public T setTile(BlockEntityBase.BlockEntitySupplier<BlockEntityMachine<?>, T> func) {
        this.tileFunc = func;
        return (T) this;
    }

    public T setBlock(BiFunction<Machine<T>, Tier, BlockMachine> function){
        this.blockFunc = function;
        return (T) this;
    }

    public T setItemBlockClass(Supplier<Class<? extends BlockMachine>> function){
        this.itemClassSupplier = function;
        return (T) this;
    }

    public T setItemBlock(Function<BlockMachine, GTItemBlock> function){
        itemBlockFunction = function;
        return (T) this;
    }

    public T setToolTag(TagKey<Block> toolTag){
        this.toolTag = toolTag;
        return (T) this;
    }

    public T addTooltipInfo(String translationKey){
        return addTooltipInfo((m, s,w,t,f) -> t.add(Utils.translatable(translationKey)));
    }

    public T addTooltipInfo(Component tooltip){
        return addTooltipInfo((m, s,w,t,f) -> t.add(tooltip));
    }

    public T addTooltipInfo(ITooltipInfo info){
        this.tooltipFunctions.add(info);
        return (T) this;
    }

    public T setCustomShape(VoxelShape shape){
        this.shapeGetter = (state, world, pos, context) -> shape;
        return (T) this;
    }

    public T setCustomShape(IShapeGetter shapeGetter){
        this.shapeGetter = shapeGetter;
        return (T) this;
    }

    public T setVerticalFacingAllowed(boolean verticalFacingAllowed) {
        this.verticalFacingAllowed = verticalFacingAllowed;
        return (T) this;
    }

    public T setNoFacing(boolean noFacing){
        this.noFacing = noFacing;
        if (noFacing){
            setAllowsFrontIO();
            setAllowsFrontCovers();
        }
        return (T) this;
    }

    public T setNoTextureRotation(boolean noTextureRotation){
        this.noTextureRotation = noTextureRotation;
        return (T) this;
    }

    public T setNoOverclockCost(boolean noOverclockCost){
        this.noOverclockCost = true;
        return (T) this;
    }

    public T setClientTicking() {
        this.clientTicking = true;
        return (T) this;
    }

    public T setAmbientTicking(){
        this.ambientTicking = true;
        return (T) this;
    }

    public T setCustomModel() {
        return setCustomModel(IOverlayModeler.defaultOverride);
    }

    public T setCustomModel(IOverlayModeler modeler) {
        this.overlayModels = modeler;
        return (T)this;
    }

    public T addFlags(String... flags) {
        for (String flag : flags) {
            FLAG_MAP.computeIfAbsent(flag, s -> new ObjectOpenHashSet<>()).add(this);
        }
        return (T) this;
    }

    public T removeFlags(String... flags) {
        for (String flag : flags) {
            FLAG_MAP.computeIfAbsent(flag, s -> new ObjectOpenHashSet<>()).remove(this);
        }
        return (T) this;
    }

    public void setFlags(String... flags) {
        FLAG_MAP.forEach((s, m) -> m.remove(this));
        addFlags(flags);
    }

    public T setTiers(Tier... tiers) {
        boolean none = false;
        for (Tier t : tiers){
            if (t == NONE) none = true;
        }
        if (none) this.setTierSpecificLang();
        this.tiers = new ObjectArrayList<>(Arrays.asList(tiers));
        return (T) this;
    }

    /**
     * Sets this machines GUI handler which provides containers and screens.
     *
     * @param menuHandler the menu handler.
     */
    public void setGUI(MenuHandler<?> menuHandler) {
        guiData = new GuiData(this, menuHandler);
        guiData.setSlots(this);
        registerJei();
    }

    public T setGuiProgressBarForJEI(BarDir dir, boolean barFill){
        guiData.getMachineData().setDir(dir);
        guiData.getMachineData().setBarFill(barFill);
        return (T) this;
    }

    public T setGuiTiers(ImmutableMap.Builder<Tier, Tier> tiers) {
        guiData.setTieredGui(tiers);
        return (T) this;
    }

    /**
     * Set the multiblock structure for this machine, for all tiers.
     * Useless if the tile is not a multiblock.
     *
     * @param func the function to build a structure.
     */
    public <U extends BlockEntityBasicMultiMachine<U>> void setStructure(Class<U> clazz, Function<StructureBuilder<U>, Structure> func) {
        getTiers().forEach(t -> setStructure(clazz, t, func));
    }

    /**
     * Set the multiblock structure for this machine, for one tier.
     * Useless if the tile is not a multiblock.
     *
     * @param func the function to build a structure.
     */
    public <U extends BlockEntityBasicMultiMachine<U>> void setStructure(Class<U> clazz, Tier tier, Function<StructureBuilder<U>, Structure> func) {
        structures.put(tier, func.apply(new StructureBuilder<>()));
    }

    public T setRenderAsTesr() {
        this.renderAsTesr = true;
        return (T) this;
    }

    public T setRendersContainedLiquids(boolean renderContainedLiquidLevel) {
        this.rendersContainedLiquids = true;
        this.rendersContainedLiquidLevel = renderContainedLiquidLevel;
        return setRenderAsTesr();
    }

    /**
     * Whether or not this machine has the given machine flag.
     *
     * @param flag the flag.
     * @return if it has it;.
     */
    public boolean has(String flag) {
        return FLAG_MAP.containsKey(flag) && FLAG_MAP.get(flag).contains(this);
    }

    /**
     * Getters
     **/

    protected Block getBlock(Machine<T> type, Tier tier) {
        return blockFunc.apply(type, tier);
    }

    public BlockMachine getBlockState(Tier tier) {
        if (tileType == null) return null;
        return GTAPI.get(itemClassSupplier.get(), this.getIdFromTier(tier), this.getDomain());
    }

    /**
     * Returns the item variant of this machine given the tier. Only use after registration or this is null!
     *
     * @param tier the tier to get.
     * @return this as an item.
     */
    public Item getItem(Tier tier) {
        return BlockItem.BY_BLOCK.get(GTAPI.get(itemClassSupplier.get(), this.getIdFromTier(tier), getDomain()));
    }

    public String getIdFromTier(Tier tier){
        return id + (tier == NONE ? "" : "_" + tier.getId());
    }

    public Component getDisplayName(Tier tier) {
        String keyAddition = tierSpecificLang ? "." + tier.getId() : "";
        return Utils.translatable("machine." + id + keyAddition, Utils.literal(tier.getId().toUpperCase(Locale.ROOT)).withStyle(tier.getRarityFormatting()));
    }

    public int getMachineEfficiency(Tier tier) {
        return efficiency.applyAsInt(tier);
    }

    public List<Texture> getTextures() {
        List<Texture> textures = new ObjectArrayList<>();
        for (Tier tier : getTiers()) {
            //textures.addAll(Arrays.asList(baseHandler.getBase(this, tier)));
            textures.addAll(Arrays.asList(getBaseTexture(tier, MachineState.IDLE)));
            textures.addAll(Arrays.asList(getBaseTexture(tier, MachineState.ACTIVE)));
            for (int i = 0; i < overlayLayers; i++) {
                textures.addAll(Arrays.asList(getOverlayTextures(MachineState.IDLE, tier, i)));
                textures.addAll(Arrays.asList(getOverlayTextures(MachineState.ACTIVE, tier, i)));
            }
        }
        return textures;
    }

    public Texture[] getBaseTexture(Tier tier, MachineState state) {
        return getDatedBaseHandler().getBase(this, tier, state);
    }

    public Texture getBaseTexture(Tier tier, Direction dir, MachineState state) {
        Texture[] texes = getDatedBaseHandler().getBase(this, tier, state);
        if (texes.length == 1) return texes[0];
        return texes[dir.get3DDataValue()];
    }

    public Texture[] getOverlayTextures(MachineState state, Tier tier, int index) {
        return getDatedOverlayHandler().getOverlays(this, state, tier, index);
    }

    public Texture[] getOverlayTextures(MachineState state, int index) {
        return getDatedOverlayHandler().getOverlays(this, state, this.getFirstTier(), index);
    }

    public ResourceLocation getOverlayModel(MachineState state,Direction side) {
        return overlayModels.getOverlayModel(this, state, side);
    }

    public IRecipeMap getRecipeMap(Tier tier) {
        if (tierRecipeMaps.containsKey(tier.getId())){
            return tierRecipeMaps.get(tier.getId());
        }
        return tierRecipeMaps.get("");
    }

    public boolean hasTierSpecificLang(){
        return tierSpecificLang;
    }

    public Tier getFirstTier() {
        return tiers.get(0);
    }

    public <U extends BlockEntityBasicMultiMachine<U>> Structure<U> getStructure(Tier tier) {
        return structures.get(tier);
    }

    /**
     * Static Methods
     **/
    public static Optional<Machine<?>> get(String name, String domain) {
        Machine<?> machine = GTAPI.get(Machine.class, name, domain);
        return Optional.ofNullable(machine);
    }

    public static Collection<Machine<?>> getTypes(String... flags) {
        List<Machine<?>> types = new ObjectArrayList<>();
        for (var flag : flags) {
            if (FLAG_MAP.containsKey(flag)){
                types.addAll(FLAG_MAP.get(flag));
            }

        }
        return types;
    }

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        return getOverlayModel(MachineState.IDLE, dir);
    }

    public static final IOverlayTexturer TROLL_OVERLAY_HANDLER = (type, state, tier, i) -> new Texture[] {
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
    };

    public static final ITextureHandler TROLL_BASE_HANDLER = (type, tier, state) -> new Texture[] {
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
            new Texture(Ref.ID, "block/machine/troll"),
    };

    public static boolean isAprilFools(){
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        return (calendar.get(Calendar.MONTH) == Calendar.APRIL && calendar.get(Calendar.DATE) == 1) ||
        (calendar.get(Calendar.MONTH) == Calendar.MARCH && calendar.get(Calendar.DATE) == 31);
    }

    private IOverlayTexturer getDatedOverlayHandler(){
        return isAprilFools() ? TROLL_OVERLAY_HANDLER : overlayTextures;
    }

    private ITextureHandler getDatedBaseHandler(){
        return isAprilFools() ? TROLL_BASE_HANDLER : baseTexture;
    }

    @Override
    public String getLang(String lang) {
        return Utils.lowerUnderscoreToUpperSpaced(this.getId());
    }
}
