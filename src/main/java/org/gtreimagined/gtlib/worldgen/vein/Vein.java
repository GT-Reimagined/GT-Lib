package org.gtreimagined.gtlib.worldgen.vein;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;

import java.util.List;

public record Vein(ResourceLocation id, int minY, int maxY, int weight, int density, int size, Material primary, Material secondary, Material between, Material sporadic, List<ResourceKey<Level>> dimensions) implements IWorldgenObject<Vein> {
    public static final Vein NO_ORES_IN_VEIN = new Vein(null,-64, 320, 0, 255, 16, Material.NULL, Material.NULL, Material.NULL, Material.NULL, List.of());
    public static final Codec<Vein> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(Vein::id),
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


    @Override
    public Codec<Vein> getCodec() {
        return CODEC;
    }

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "veins";
    }
}
