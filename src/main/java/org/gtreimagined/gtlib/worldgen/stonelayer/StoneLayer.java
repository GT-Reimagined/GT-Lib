package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record StoneLayer(ResourceLocation id, @Nullable StoneType type, Block block, int weight, StoneLayerRestrictions restrictions,
                         List<ResourceKey<Level>> dimensions, List<StoneLayerOre> ores) implements IWorldgenObject<StoneLayer> {
    private static Int2ObjectOpenHashMap<List<StoneLayerOre>> COLLISION_MAP = new Int2ObjectOpenHashMap<>();
    public static final Codec<StoneLayer> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(StoneLayer::id),
            Registry.BLOCK.byNameCodec().fieldOf("block").forGetter(StoneLayer::block),
            Codec.INT.fieldOf("weight").forGetter(StoneLayer::weight),
            StoneLayerRestrictions.CODEC.optionalFieldOf("restrictions", StoneLayerRestrictions.EMPTY).forGetter(StoneLayer::restrictions),
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf().fieldOf("dimensions").forGetter(StoneLayer::dimensions),
            StoneLayerOre.CODEC.listOf().optionalFieldOf("ores", List.of()).forGetter(StoneLayer::ores)
    ).apply(instance, StoneLayer::new));

    public StoneLayer(ResourceLocation id, Block block, int weight, StoneLayerRestrictions restrictions, List<ResourceKey<Level>> dimensions, List<StoneLayerOre> ores){
        this(id, StoneType.fromBlock(block), block, weight, restrictions, dimensions, ores);
    }

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "stone_layers";
    }

    @Override
    public Codec<StoneLayer> getCodec() {
        return CODEC;
    }

    @Override
    public List<ResourceKey<Level>> getDimensions() {
        return dimensions;
    }

    public static void setCollisionMap(Int2ObjectOpenHashMap<List<StoneLayerOre>> collisionMap) {
        COLLISION_MAP = collisionMap;
    }

    public static List<StoneLayerOre> getCollision(StoneType middle, BlockState top, BlockState bottom) {
        if (middle == null) return Collections.emptyList();
        List<StoneLayerOre> list = COLLISION_MAP.get(Objects.hash(top, bottom));
        if (list == null) return Collections.emptyList();
        return list;
    }
}
