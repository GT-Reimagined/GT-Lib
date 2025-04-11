package org.gtreimagined.gtlib.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;

public record OreObject(Material material, MaterialType<?> type) {
    public static final Codec<OreObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("material").forGetter(OreObject::material),
            MaterialType.CODEC.fieldOf("type").forGetter(OreObject::type)
    ).apply(instance, OreObject::new));
}
