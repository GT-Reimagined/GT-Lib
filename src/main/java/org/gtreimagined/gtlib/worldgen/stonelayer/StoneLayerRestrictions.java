package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.gtreimagined.gtlib.ore.StoneType;
import org.jetbrains.annotations.Nullable;

public record StoneLayerRestrictions(int minY, int maxY, @Nullable StoneType topReplacementStone, Block topReplacement, @Nullable StoneType bottomReplacementStone, Block bottomReplacement) {
    public static final StoneLayerRestrictions EMPTY = new StoneLayerRestrictions(Integer.MIN_VALUE, Integer.MAX_VALUE, null, Blocks.AIR, null, Blocks.AIR);

    public static final Codec<StoneLayerRestrictions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("minY", Integer.MIN_VALUE).forGetter(StoneLayerRestrictions::minY),
            Codec.INT.optionalFieldOf("maxY", Integer.MAX_VALUE).forGetter(StoneLayerRestrictions::maxY),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("topReplacement", Blocks.AIR).forGetter(StoneLayerRestrictions::topReplacement),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("bottomReplacement", Blocks.AIR).forGetter(StoneLayerRestrictions::bottomReplacement)
    ).apply(instance, StoneLayerRestrictions::new));

    public StoneLayerRestrictions(int minY, int maxY, Block topReplacement, Block bottomReplacement){
        this(minY, maxY, StoneType.fromBlock(topReplacement), topReplacement, StoneType.fromBlock(bottomReplacement), bottomReplacement);
    }


}
