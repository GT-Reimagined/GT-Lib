package org.gtreimagined.gtlib.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;

public record OreObject(Material material, MaterialType<?> type, Block block) {
    public static final Codec<OreObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("material").forGetter(OreObject::material),
            MaterialType.CODEC.fieldOf("type").forGetter(OreObject::type),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block", Blocks.AIR).forGetter(OreObject::block)
    ).apply(instance, OreObject::new));

    public OreObject(Material material, MaterialType<?> type) {
        this(material, type, Blocks.AIR);
    }

    public OreObject(Block block){
        this(Material.NULL, GTMaterialTypes.ORE, block);
    }
}
