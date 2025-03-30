package org.gtreimagined.gtlib.cover;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTRemapping;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityCable;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.gui.MenuHandler;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class CoverFactory implements IGTObject {

    final String id;
    final String domain;

    private final CoverSupplier supplier;
    private Map<Tier, Item> itemStacks = Collections.emptyMap();
    private Item itemStack;
    private List<Texture> textures;
    @Getter
    private MenuHandler<?> menuHandler = Data.COVER_MENU_HANDLER;
    @Getter
    private Predicate<BlockEntity> isValid = b -> !(b instanceof BlockEntityCable<?>);

    protected boolean gui = false;

    protected CoverFactory(String domain, String id, CoverSupplier supplier) {
        this.id = id;
        this.supplier = supplier;
        this.domain = domain;
        GTAPI.register(CoverFactory.class, this);
    }

    public final CoverSupplier get() {
        return this.supplier;
    }

    public ItemStack getItem(Tier tier) {
        return tier == null ? getItem() : itemStacks.getOrDefault(tier, Items.AIR).getDefaultInstance();
    }

    public Tier getValidTier() {
        return itemStacks.size() > 1 ? itemStacks.keySet().iterator().next() : null;
    }

    public List<Texture> getTextures() {
        return textures == null ? Collections.emptyList() : textures;
    }

    public ItemStack getItem() {
        return itemStack == null ? ItemStack.EMPTY : itemStack.getDefaultInstance();
    }

    void setItems(Map<Tier, Item> stacks) {
        this.itemStack = stacks.remove(null);
        if (itemStack == null)
            itemStack = Items.AIR;
        this.itemStacks = ImmutableMap.copyOf(stacks);
    }

    void addTextures(List<Texture> textures) {
        this.textures = textures;
    }

    void setHasGui() {
        this.gui = true;
    }

    void setMenuHandler(MenuHandler<?> handler) {
        this.menuHandler = handler;
    }

    void setIsValid(Predicate<BlockEntity> isValid) {
        this.isValid = isValid;
    }

    public boolean hasGui() {
        return this.gui;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    public static Builder builder(final CoverSupplier supplier) {
        return new Builder(supplier);
    }

    @Override
    public String getId() {
        return id;
    }

    public static CompoundTag writeCover(CompoundTag nbt, ICover cover, Direction dir, boolean stackDrop) {
        CoverFactory factory = cover.getFactory();
        nbt.putString(dir.get3DDataValue() + "d", factory.getDomain());
        nbt.putString(dir.get3DDataValue() + "i", factory.getId());
        if (cover.getTier() != null)
            nbt.putString(dir.get3DDataValue() + "t", cover.getTier().getId());
        CompoundTag inner = stackDrop ? cover.serializeStack(new CompoundTag()) : cover.serialize();
        if (!inner.isEmpty())
            nbt.put(dir.get3DDataValue() + "c", inner);
        return nbt;
    }

    public static ICover readCover(ICoverHandler<?> source, Direction dir, CompoundTag nbt, boolean stackDrop) {
        if (!nbt.contains(dir.get3DDataValue() + "d"))
            return null;
        String domain = nbt.getString(dir.get3DDataValue() + "d");
        String id = nbt.getString(dir.get3DDataValue() + "i");
        ResourceLocation location = new ResourceLocation(domain, id);
        if (GTRemapping.getCoverRemappingMap().containsKey(location)) location = GTRemapping.getCoverRemappingMap().get(location);
        CoverFactory factory = GTAPI.get(CoverFactory.class, location);
        if (factory == null) {
            throw new IllegalStateException("Reading a cover with null factory, game in bad state");
        }
        Tier tier = nbt.contains(dir.get3DDataValue() + "t")
                ? GTAPI.get(Tier.class, nbt.getString(dir.get3DDataValue() + "t"))
                : null;
        ICover cover = factory.supplier.get(source, tier, dir, factory);
        cover.onCreate();
        if (nbt.contains(dir.get3DDataValue() + "c")) {
            if (!stackDrop) {
                cover.deserialize((CompoundTag) nbt.get(dir.get3DDataValue() + "c"));
            } else {
                cover.deserializeStack((CompoundTag) nbt.get(dir.get3DDataValue() + "c"));
            }
        }
        return cover;
    }

    public static ICover readCoverRotated(ICoverHandler<?> source, Direction dir, Direction rotated, CompoundTag nbt) {
        if (!nbt.contains(dir.get3DDataValue() + "d"))
            return null;
        String domain = nbt.getString(dir.get3DDataValue() + "d");
        String id = nbt.getString(dir.get3DDataValue() + "i");
        ResourceLocation location = new ResourceLocation(domain, id);
        if (GTRemapping.getCoverRemappingMap().containsKey(location)) location = GTRemapping.getCoverRemappingMap().get(location);
        CoverFactory factory = GTAPI.get(CoverFactory.class, location);
        if (factory == null) {
            throw new IllegalStateException("Reading a cover with null factory, game in bad state");
        }
        Tier tier = nbt.contains(dir.get3DDataValue() + "t")
                ? GTAPI.get(Tier.class, nbt.getString(dir.get3DDataValue() + "t"))
                : null;
        ICover cover = factory.supplier.get(source, tier, rotated, factory);
        cover.onCreate();
        if (nbt.contains(dir.get3DDataValue() + "c"))
            cover.deserializeStack((CompoundTag) nbt.get(dir.get3DDataValue() + "c"));
        return cover;
    }

    public static class Builder {
        List<Tier> tiers = Collections.singletonList(null);

        final CoverSupplier supplier;
        BiFunction<CoverFactory, Tier, Item> itemBuilder;
        boolean gui = false;
        List<Texture> textures;
        MenuHandler<?> menuHandler;
        Predicate<BlockEntity> isValid;

        public Builder(final CoverSupplier supplier) {
            this.supplier = supplier;
        }

        public Builder setTiers(Tier... tiers) {
            this.tiers = Arrays.asList(tiers);
            return this;
        }

        public Builder item(BiFunction<CoverFactory, Tier, Item> item) {
            this.itemBuilder = item;
            return this;
        }

        public Builder gui() {
            this.gui = true;
            return this;
        }

        public Builder setMenuHandler(MenuHandler<?> handler) {
            this.menuHandler = handler;
            return this;
        }

        public Builder addTextures(List<Texture> textures) {
            this.textures = textures;
            return this;
        }

        public Builder addTextures(Texture... textures) {
            this.textures = Arrays.asList(textures);
            return this;
        }

        public Builder setIsValid(Predicate<BlockEntity> isValid) {
            this.isValid = isValid;
            return this;
        }

        public CoverFactory build(String domain, String id) {
            CoverFactory factory = new CoverFactory(domain, id, this.supplier);
            if (this.itemBuilder != null) {
                Map<Tier, Item> map = new Object2ObjectOpenHashMap<>();
                for (Tier tier : this.tiers) {
                    Item stack = this.itemBuilder.apply(factory, tier);
                    map.put(tier, stack);
                }
                factory.setItems(map);
            }
            if (gui) {
                factory.setHasGui();
                if (menuHandler != null) {
                    factory.setMenuHandler(menuHandler);
                }
            }
            if (textures != null)
                factory.addTextures(textures);
            if (isValid != null){
                factory.setIsValid(isValid);
            }
            return factory;
        }

    }

    public interface CoverSupplier {
        ICover get(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory);
    }

}
