package org.gtreimagined.gtlib.pipe.types;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.pipe.PipeItemBlock;
import org.gtreimagined.gtlib.pipe.PipeSize;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class PipeType<T extends PipeType<T>> implements IRegistryEntryProvider, ISharedAntimatterObject {

    /**
     * Basic Members
     **/
    public final String domain;
    protected Material material;
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
    public void onRegistryBuild(IForgeRegistry<?> registry) {
        if (registry != ForgeRegistries.BLOCKS)
            return;
        Set<Block> blocks = getBlocks();
        registeredBlocks = blocks.stream().map(t -> new Pair<>(((BlockPipe<?>) t).getSize(),t))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        tileType = new BlockEntityType<>((pos,state) -> tileFunc.create((T) this, pos, state), blocks, null);
        AntimatterAPI.register(BlockEntityType.class, getId(), getDomain(), getTileType());
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

    public Material getMaterial() {
        return material;
    }

    public ImmutableSet<PipeSize> getSizes() {
        return sizes;
    }

    public BlockEntityType<?> getTileType() {
        return tileType;
    }

    public T sizes(PipeSize... sizes) {
        this.sizes = ImmutableSet.copyOf(sizes);
        return (T) this;
    }
}
