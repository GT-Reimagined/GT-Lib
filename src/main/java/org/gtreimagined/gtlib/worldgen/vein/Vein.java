package org.gtreimagined.gtlib.worldgen.vein;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.material.Material;

import java.util.List;

public record Vein(int minY, int maxY, int weight, int density, int size, Material primary, Material secondary, Material between, Material sporadic, List<ResourceKey<Level>> dimensions) {
    public static final Codec<Vein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("minY").forGetter(Vein::minY),
            Codec.INT.fieldOf("maxY").forGetter(Vein::maxY),
            Codec.INT.fieldOf("weight").forGetter(Vein::weight),
            Codec.INT.fieldOf("density").forGetter(Vein::density),
            Codec.INT.fieldOf("size").forGetter(Vein::size),
            Material.CODEC.fieldOf("primary").forGetter(Vein::primary),
            Material.CODEC.fieldOf("secondary").forGetter(Vein::secondary),
            Material.CODEC.fieldOf("between").forGetter(Vein::between),
            Material.CODEC.fieldOf("sporadic").forGetter(Vein::sporadic),
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf().fieldOf("dimensions").forGetter(Vein::dimensions)
    ).apply(instance, Vein::new));
}
