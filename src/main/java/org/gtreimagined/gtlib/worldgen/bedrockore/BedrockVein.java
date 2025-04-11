package org.gtreimagined.gtlib.worldgen.bedrockore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public record BedrockVein(ResourceLocation id, int probability, Material material, boolean indicatorRocks,
                          boolean indicatorFlowers, Block flower,
                          List<ResourceKey<Level>> dimensions) implements IWorldgenObject<BedrockVein> {


    public static final Codec<BedrockVein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(BedrockVein::id),
            Codec.INT.fieldOf("probability").forGetter(BedrockVein::probability),
            Material.CODEC.fieldOf("material").forGetter(BedrockVein::material),
            Codec.BOOL.fieldOf("indicatorRocks").forGetter(BedrockVein::indicatorRocks),
            Codec.BOOL.fieldOf("indicatorFlowers").forGetter(BedrockVein::indicatorFlowers),
            Registry.BLOCK.byNameCodec().optionalFieldOf("flower", Blocks.AIR).forGetter(BedrockVein::flower),
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf().fieldOf("dimensions").forGetter(BedrockVein::dimensions)
    ).apply(instance, BedrockVein::new));

    @SafeVarargs
    public static BedrockVein create(ResourceLocation id, int probability, Material material, boolean indicatorRocks, boolean indicatorFlowers, Block flower, ResourceKey<Level>... dimensions) {
        return new BedrockVein(id, probability, material, indicatorRocks, indicatorFlowers, flower, List.of(dimensions));
    }

    @SafeVarargs
    public static BedrockVein create(ResourceLocation id, int probability, Material material, ResourceKey<Level>... dimensions) {
        return create(id, probability, material, true, dimensions);
    }

    @SafeVarargs
    public static BedrockVein create(ResourceLocation id, int probability, Material material, boolean indicatorRocks, ResourceKey<Level>... dimensions) {
        return create(id, probability, material, indicatorRocks, false, Blocks.AIR, dimensions);
    }

    @SafeVarargs
    public static BedrockVein create(ResourceLocation id, int probability, Material material, boolean indicatorRocks, Block flower, ResourceKey<Level>... dimensions) {
        return create(id, probability, material, indicatorRocks, true, flower, dimensions);
    }

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "bedrock_veins";
    }

    @Override
    public Codec<BedrockVein> getCodec() {
        return CODEC;
    }

    @Override
    public List<ResourceKey<Level>> getDimensions() {
        return dimensions;
    }
}
