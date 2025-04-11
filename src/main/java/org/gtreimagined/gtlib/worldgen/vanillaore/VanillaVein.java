package org.gtreimagined.gtlib.worldgen.vanillaore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.gtreimagined.gtlib.worldgen.OreObject;

import java.util.List;
import java.util.function.Predicate;

public record VanillaVein(ResourceLocation id, OreObject primary, OreObject secondary, float secondaryChance, float discardOnExposureChance,
                          int minY, int maxY, int weight, int size, int probability, boolean triangle, int plateau,
                          boolean spawnOnOceanFloor, List<ResourceKey<Level>> dimensions, List<String> biomes, boolean biomeBlacklist) implements IWorldgenObject<VanillaVein> {

    public static final Codec<VanillaVein> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(VanillaVein::id),
                OreObject.CODEC.fieldOf("primary").forGetter(VanillaVein::primary),
                OreObject.CODEC.fieldOf("secondary").forGetter(VanillaVein::secondary),
                Codec.FLOAT.fieldOf("secondaryChance").forGetter(VanillaVein::secondaryChance),
                Codec.FLOAT.fieldOf("discardOnExposureChance").forGetter(VanillaVein::discardOnExposureChance),
                Codec.INT.fieldOf("minY").forGetter(VanillaVein::minY),
                Codec.INT.fieldOf("maxY").forGetter(VanillaVein::maxY),
                Codec.INT.fieldOf("weight").forGetter(VanillaVein::weight),
                Codec.INT.fieldOf("size").forGetter(VanillaVein::size),
                Codec.INT.fieldOf("probability").forGetter(VanillaVein::probability),
                Codec.BOOL.fieldOf("triangle").forGetter(VanillaVein::triangle),
                Codec.INT.fieldOf("plateau").forGetter(VanillaVein::plateau),
                Codec.BOOL.fieldOf("spawnOnOceanFloor").forGetter(VanillaVein::spawnOnOceanFloor),
                ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf().fieldOf("dimensions").forGetter(VanillaVein::dimensions),
                Codec.STRING.listOf().fieldOf("biomes").forGetter(VanillaVein::biomes),
                Codec.BOOL.fieldOf("biomeBlacklist").forGetter(VanillaVein::biomeBlacklist)
        ).apply(instance, VanillaVein::new);
    });

    @Override
    public boolean isBiomeValid(Holder<Biome> biome) {
        if (biomes.isEmpty()) return biomeBlacklist;
        Predicate<String> predicate = s -> {
            if (s.contains("#")) return biome.is(TagUtils.getBiomeTag(new ResourceLocation(s.replace("#", ""))));
            return biome.is(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s)));
        };
        return biomeBlacklist ? biomes.stream().anyMatch(predicate) : biomes.stream().noneMatch(predicate);
    }

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "vanilla_veins";
    }

    @Override
    public Codec<VanillaVein> getCodec() {
        return CODEC;
    }
}
