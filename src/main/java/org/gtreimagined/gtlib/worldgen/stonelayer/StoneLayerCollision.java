package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;

import java.util.List;

public record StoneLayerCollision(ResourceLocation id, BlockState top, BlockState bottom, List<StoneLayerOre> ores) implements IWorldgenObject<StoneLayerCollision> {
    public static final Codec<StoneLayerCollision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(StoneLayerCollision::id),
            BlockState.CODEC.fieldOf("top").forGetter(StoneLayerCollision::top),
            BlockState.CODEC.fieldOf("bottom").forGetter(StoneLayerCollision::bottom),
            StoneLayerOre.CODEC.listOf().fieldOf("ores").forGetter(StoneLayerCollision::ores)
    ).apply(instance, StoneLayerCollision::new));

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "stone_layer_collisions";
    }

    @Override
    public Codec<StoneLayerCollision> getCodec() {
        return CODEC;
    }

    @Override
    public List<ResourceKey<Level>> getDimensions() {
        return List.of(Level.OVERWORLD);
    }
}
