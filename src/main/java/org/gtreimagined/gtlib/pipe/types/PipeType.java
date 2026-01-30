package org.gtreimagined.gtlib.pipe.types;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.pipe.PipeItemBlock;
import org.gtreimagined.gtlib.pipe.PipeSize;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PipeType<T extends PipeType<T>> implements IRegistryEntryProvider, ISharedGTObject {

    /**
     * Basic Members
     **/
    public final String domain;
    @Getter
    protected Material material;
    @Getter
    protected ImmutableSet<PipeSize> sizes = ImmutableSet.of();
    protected BlockEntityType<? extends BlockEntityPipe<?>> tileType;
    protected Map<PipeSize, Block> registeredBlocks;

    protected final BlockEntityBase.BlockEntitySupplier<BlockEntityPipe<?>, T> tileFunc;

    public PipeType(String domain, Material material, BlockEntityBase.BlockEntitySupplier<BlockEntityPipe<?>, T> func) {
        this.domain = domain;
        this.material = material;
        sizes(PipeSize.VALUES);
        this.tileFunc = func;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (registry != ForgeRegistries.Keys.BLOCKS)
            return;
        Set<Block> blocks = getBlocks();
        registeredBlocks = blocks.stream().map(t -> new Pair<>(((BlockPipe<?>) t).getSize(),t))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        tileType = new BlockEntityType<>((pos,state) -> tileFunc.create((T) this, pos, state), blocks, null);
        GTAPI.register(BlockEntityType.class, getId(), getDomain(), getTileType());
    }

    public Block getBlock(PipeSize size) {
        return registeredBlocks.get(size);
    }

    public PipeItemBlock getBlockItem(PipeSize size) {
        return (PipeItemBlock) Item.BY_BLOCK.get(getBlock(size));
    }

    public abstract Set<Block> getBlocks();

    /*
     * public String getDomain() { return domain; }
     */

    public abstract String getType();

    @Override
    public String getId() {
        return getType() + "_" + material.getId();
    }

    public abstract String getTypeName();

    public BlockEntityType<?> getTileType() {
        return tileType;
    }

    public T sizes(PipeSize... sizes) {
        this.sizes = ImmutableSet.copyOf(sizes);
        return (T) this;
    }

    public String getSizeId(PipeSize size){
        return size.getId();
    }

    public String getModelPath(PipeSize size){
        return "pipe/" +  switch (size){
            case HUGE -> "12x12";
            case LARGE -> "10x10";
            case NORMAL -> "8x8";
            case SMALL -> "6x6";
            case TINY -> "4x4";
            case VTINY -> "2x2";
            default -> "";
        };
    }
}
