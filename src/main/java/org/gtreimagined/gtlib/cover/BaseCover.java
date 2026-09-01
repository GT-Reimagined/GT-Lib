package org.gtreimagined.gtlib.cover;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.FluidHandler.FluidTankType;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.capability.item.FakeTrackedItemHandler;
import org.gtreimagined.gtlib.capability.item.ITrackedHandler;
import org.gtreimagined.gtlib.capability.item.TrackedItemHandler;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.SlotTypes;
import org.gtreimagined.gtlib.gui.slot.ISlotProvider;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

//The base Cover class. All cover classes extend from this.
public abstract class BaseCover implements ICover {
    @NotNull
    public final CoverFactory factory;
    @NotNull
    public final ICoverHandler<?> handler;
    @Nullable
    public final Tier tier;
    @Nullable
    public final GuiProperties gui;
    public final Direction side;
    protected Object2ObjectMap<FluidTankType, FluidTanks> fluidTanks = null;

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
            if (fluidTanks == null){
                fluidTanks = new Object2ObjectOpenHashMap<>();
            }
            List<SlotData<?>> slots = tier == null ? gui.getSlots().getAnySlots() : gui.getSlots().getSlots(tier);
            Map<SlotType<?>, List<SlotData<?>>> map = slots.stream().collect(Collectors.groupingBy(SlotData::type));
            if (map.containsKey(SlotTypes.FL_IN)){
                fluidTanks.put(FluidTankType.INPUT, new FluidTanks(map.get(SlotTypes.FL_IN).size(), 16000));
            }
            if (map.containsKey(SlotTypes.FL_OUT)){
                fluidTanks.put(FluidTankType.OUTPUT, new FluidTanks(map.get(SlotTypes.FL_OUT).size(), 16000));
            }
            if (map.containsKey(SlotTypes.FL_PHANTOM)){
                fluidTanks.put(FluidTankType.PHANTOM, new FluidTanks(map.get(SlotTypes.FL_PHANTOM).size(), 1000));
            }
            slots.forEach(s ->{
                for (Map.Entry<SlotType<?>, List<SlotData<?>>> entry : map.entrySet()) {
                    SlotType<?> type = entry.getKey();
                    if (type.slotSupplier() == null) continue;
                    int count = gui.getSlots().getCount(tier, entry.getKey());
                    if (type.phantom()) {
                        inventories.put(type, new FakeTrackedItemHandler<>(this, type, count, type.allowExternalOutput(), type.allowExternalInput(), type.tester()));
                    } else {
                        inventories.put(type, new TrackedItemHandler<>(this, type, count, type.allowExternalOutput(), type.allowExternalInput(), type.tester()));
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
    public Map<SlotType<?>, IItemHandler> getAll() {
        return (Map<SlotType<?>, IItemHandler>) (Object) inventories;
    }

    @Override
    public Object2ObjectMap<FluidTankType, FluidTanks> getFluidTanks() {
        return fluidTanks;
    }

    public ITrackedHandler getInventory(SlotType<?> type){
        return inventories.get(type);
    }

    @Override
    public void deserializeStack(@Nullable CompoundTag tag) {
        if (tag != null) {
            if (tag.contains("coverInventories")) {
                CompoundTag nbt = tag.getCompound("coverInventories");
                if (inventories != null && getFactory().hasGui()) {
                    this.inventories.forEach((f, i) -> {
                        if (!nbt.contains(f.getId())) return;
                        i.deserializeNBT(nbt.getCompound(f.getId()));
                    });
                    handler.getTile().setChanged();
                }
            }
            if (tag.contains("coverTanks")){
                CompoundTag nbt = tag.getCompound("coverTanks");
                if (fluidTanks != null && getFactory().hasGui()) {
                    this.fluidTanks.forEach((t, f) -> {
                        if (!nbt.contains(t.name().toLowerCase(Locale.ROOT))) return;
                        f.deserialize(nbt.getList(t.name().toLowerCase(Locale.ROOT), 10));
                    });
                    handler.getTile().setChanged();
                }

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
        if (fluidTanks != null && getFactory().hasGui()){
            CompoundTag nbt = new CompoundTag();
            this.fluidTanks.forEach((t, f) -> {
                if (f.isEmpty()) return;
                nbt.put(t.name().toLowerCase(Locale.ROOT), f.serialize());
            });
            if (!nbt.isEmpty()) tag.put("coverTanks", nbt);
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
            if (nbt != null) {
                if (nbt.contains("coverInventories")) {
                    CompoundTag tag = nbt.getCompound("coverInventories");
                    if (inventories != null && getFactory().hasGui()) {
                        this.inventories.forEach((f, i) -> {
                            if (!tag.contains(f.getId())) return;
                            i.deserializeNBT(tag.getCompound(f.getId()));
                        });
                        handler.getTile().setChanged();
                    }
                }
                if (nbt.contains("coverTanks")){
                    CompoundTag tag = nbt.getCompound("coverTanks");
                    if (fluidTanks != null && getFactory().hasGui()) {
                        this.fluidTanks.forEach((t, f) -> {
                            if (!tag.contains(t.name().toLowerCase(Locale.ROOT))) return;
                            f.deserialize(tag.getList(t.name().toLowerCase(Locale.ROOT), 10));
                        });
                        handler.getTile().setChanged();
                    }

                }
            }
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
        CompoundTag tag = new CompoundTag();
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
        if (fluidTanks != null && getFactory().hasGui()){
            CompoundTag nbt = new CompoundTag();
            this.fluidTanks.forEach((t, f) -> {
                if (f.isEmpty()) return;
                nbt.put(t.name().toLowerCase(Locale.ROOT), f.serialize());
            });
            if (!nbt.isEmpty()) tag.put("coverTanks", nbt);
        }
        return tag;
    }

    @Override
    public boolean isRemote() {
        return handler.getTile().getLevel().isClientSide();
    }

    @Override
    public CoverFactory getFactory() {
        return factory;
    }

    protected void markAndNotifySource(){
        source().getTile().getLevel().markAndNotifyBlock(source().getTile().getBlockPos(), source().getTile().getLevel().getChunkAt(source().getTile().getBlockPos()), source().getTile().getBlockState(), source().getTile().getBlockState(), 1, 512);
    }

}
