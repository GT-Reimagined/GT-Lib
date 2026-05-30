package org.gtreimagined.gtlib.cover;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.capability.item.FakeTrackedItemHandler;
import org.gtreimagined.gtlib.capability.item.ITrackedHandler;
import org.gtreimagined.gtlib.capability.item.TrackedItemHandler;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.gui.slot.ISlotProvider;
import org.gtreimagined.gtlib.gui.widget.BackgroundWidget;
import org.gtreimagined.gtlib.gui.widget.CoverModeHandlerWidget;
import org.gtreimagined.gtlib.gui.widget.SlotWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.network.packets.AbstractGuiEventPacket;
import org.gtreimagined.gtlib.network.packets.CoverGuiEventPacket;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

//The base Cover class. All cover classes extend from this.
public abstract class BaseCover implements ICover, IGuiHandler.IHaveWidgets {
    @NotNull
    public final CoverFactory factory;
    @NotNull
    public final ICoverHandler<?> handler;
    @Nullable
    public final Tier tier;
    @Nullable
    public final GuiProperties gui;
    public final Direction side;
    private final List<Consumer<GuiInstance>> guiCallbacks = new ObjectArrayList<>();

    protected Object2ObjectMap<SlotType<?>, TrackedItemHandler<?>> inventories = null;

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        if (type.equals("pipe"))
            return PIPE_COVER_MODEL;
        return new ResourceLocation(getDomain() + ":block/cover/" + getRenderId());
    }

    @Override
    public Direction side() {
        return side;
    }

    @Override
    public ICoverHandler<?> source() {
        return handler;
    }

    public BaseCover(@NotNull ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        this.factory = Objects.requireNonNull(factory, "Missing factory in BaseCover");
        this.handler = source;
        this.tier = tier;
        this.side = side;
        if (factory.hasGui()) {
            this.gui = new GuiProperties(this);
            gui.setEnablePlayerSlots(true);
            gui.setSlots(ISlotProvider.DEFAULT());
            this.addGuiCallback(t -> {
                t.addWidget(BackgroundWidget.build(t.handler.getGuiTexture(), t.handler.guiSize(), t.handler.guiHeight(), t.handler.guiTextureSize(), t.handler.guiTextureHeight()));
                if (BaseCover.this instanceof ICoverModeHandler){
                    t.addWidget(CoverModeHandlerWidget.build());
                }
                List<SlotData<?>> slots = tier == null ? gui.getSlots().getAnySlots() : gui.getSlots().getSlots(tier);
                slots.forEach(s ->{
                    t.addWidget(SlotWidget.build(s));
                });
            });
        } else {
            this.gui = null;
        }
    }

    @Override
    public void onPlace() {
        onCreate();
    }

    @Override
    public void onCreate() {
        setInventory();
    }

    private void setInventory(){
        if (factory.hasGui()){
            if (inventories == null){
                inventories = new Object2ObjectOpenHashMap<>();
            }
            List<SlotData<?>> slots = tier == null ? gui.getSlots().getAnySlots() : gui.getSlots().getSlots(tier);
            Map<SlotType<?>, List<SlotData<?>>> map = slots.stream().collect(Collectors.groupingBy(SlotData::getType));
            slots.forEach(s ->{
                for (Map.Entry<SlotType<?>, List<SlotData<?>>> entry : map.entrySet()) {
                    SlotType<?> type = entry.getKey();
                    if (type.getSlotSupplier() == null) continue;
                    int count = gui.getSlots().getCount(tier, entry.getKey());
                    if (type.isPhantom()) {
                        inventories.put(type, new FakeTrackedItemHandler<>(this, type, count, type.isOutput(), type.isInput(), type.getTester()));
                    } else {
                        inventories.put(type, new TrackedItemHandler<>(this, type, count, type.isOutput(), type.isInput(), type.getTester()));
                    }

                }
            });
        }
    }

    @Override
    public @Nullable Tier getTier() {
        return tier;
    }

    @Override
    public List<Consumer<GuiInstance>> getCallbacks() {
        return this.guiCallbacks;
    }

    @Override
    public Map<SlotType<?>, IItemHandler> getAll() {
        return (Map<SlotType<?>, IItemHandler>) (Object) inventories;
    }
    public ITrackedHandler getInventory(SlotType<?> type){
        return inventories.get(type);
    }

    @Override
    public void deserializeStack(@Nullable CompoundTag tag) {
        if (tag != null && tag.contains("coverInventories")){
            CompoundTag nbt = tag.getCompound("coverInventories");
            if (inventories != null && getFactory().hasGui()){
                this.inventories.forEach((f, i) -> {
                    if (!nbt.contains(f.getId())) return;
                    i.deserializeNBT(nbt.getCompound(f.getId()));
                });
                handler.getTile().setChanged();
            }
        }
    }

    @Override
    public CompoundTag serializeStack(CompoundTag tag) {
        if (inventories != null && getFactory().hasGui()){
            CompoundTag nbt = new CompoundTag();
            this.inventories.forEach((f, i) -> {
                if (i.isEmpty()) return;
                nbt.put(f.getId(), i.serializeNBT());
            });
            if (!nbt.isEmpty()) {
                tag.put("coverInventories", nbt);
            }
        }
        return tag;
    }

    @Override
    public void setTextures(BiConsumer<String, Texture> texer) {
        texer.accept("overlay", factory.getTextures().isEmpty() ? new Texture(factory.getDomain(), "block/cover/" + getRenderId()) : factory.getTextures().get(factory.getTextures().size() == 6 ? side.get3DDataValue() : 0));
    }

    public Texture[] getTextures() {
        List<Texture> l = new ArrayList<>();
        setTextures((name, tex) -> l.add(tex));
        return l.toArray(new Texture[0]);
    }

    // Useful for using the same model for multiple tiers where id is dependent on
    // tier.
    protected String getRenderId() {
        return getId();
    }

    // The default cover model
    public static ResourceLocation getBasicModel() {
        return new ResourceLocation(Ref.ID + ":block/cover/basic");
    }

    // The default cover model with depth, see Output and Conveyor cover.
    public static ResourceLocation getBasicDepthModel() {
        return new ResourceLocation(Ref.ID + ":block/cover/basic_depth");
    }

    @Override
    public ItemStack getItem() {
        return factory.getItem(tier);
    }

    @Override
    public void deserialize(CompoundTag nbt) {
        if (getFactory().hasGui()){
            this.inventories.forEach((f, i) -> {
                if (!nbt.contains(f.getId())) return;
                i.deserializeNBT(nbt.getCompound(f.getId()));
            });
        }
    }

    @Override
    public boolean hasGui() {
        return factory.hasGui();
    }

    @Override
    public GuiProperties getGuiProperties() {
        return gui;
    }

    @Override
    public CompoundTag serialize() {
        CompoundTag nbt = new CompoundTag();
        if (inventories != null && getFactory().hasGui()){
            this.inventories.forEach((f, i) -> {
                if (i.isEmpty()) return;
                nbt.put(f.getId(), i.serializeNBT());
            });
        }
        return nbt;
    }

    @Override
    public boolean isRemote() {
        return handler.getTile().getLevel().isClientSide();
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return new ResourceLocation(Ref.ID, "textures/gui/background/machine_basic.png");
    }

    @Override
    public AbstractGuiEventPacket createGuiPacket(IGuiEvent event) {
        return new CoverGuiEventPacket(event, this.handler.getTile().getBlockPos(), this.side);
    }

    @Override
    public CoverFactory getFactory() {
        return factory;
    }

    protected void markAndNotifySource(){
        source().getTile().getLevel().markAndNotifyBlock(source().getTile().getBlockPos(), source().getTile().getLevel().getChunkAt(source().getTile().getBlockPos()), source().getTile().getBlockState(), source().getTile().getBlockState(), 1, 512);
    }

}
