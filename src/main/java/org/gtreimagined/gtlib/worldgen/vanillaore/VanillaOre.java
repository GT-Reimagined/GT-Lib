package org.gtreimagined.gtlib.worldgen.vanillaore;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.gtreimagined.gtlib.worldgen.OreObject;
import org.gtreimagined.gtlib.worldgen.vein.Vein;

import java.util.List;
import java.util.function.Predicate;

public record VanillaOre(ResourceLocation id, OreObject primary, OreObject secondary, float secondaryChance, float discardOnExposureChance,
                         int minY, int maxY, int weight, int size, int probability, boolean triangle, int plateau,
                         boolean spawnOnOceanFloor, List<ResourceKey<Level>> dimensions, List<String> biomes, boolean biomeBlacklist) implements IWorldgenObject<VanillaOre> {

    public static final Codec<VanillaOre> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(VanillaOre::id),
                OreObject.CODEC.fieldOf("primary").forGetter(VanillaOre::primary),
                OreObject.CODEC.fieldOf("secondary").forGetter(VanillaOre::secondary),
                Codec.FLOAT.fieldOf("secondaryChance").forGetter(VanillaOre::secondaryChance),
                Codec.FLOAT.fieldOf("discardOnExposureChance").forGetter(VanillaOre::discardOnExposureChance),
                Codec.INT.fieldOf("minY").forGetter(VanillaOre::minY),
                Codec.INT.fieldOf("maxY").forGetter(VanillaOre::maxY),
                Codec.INT.fieldOf("weight").forGetter(VanillaOre::weight),
                Codec.INT.fieldOf("size").forGetter(VanillaOre::size),
                Codec.INT.fieldOf("probability").forGetter(VanillaOre::probability),
                Codec.BOOL.fieldOf("triangle").forGetter(VanillaOre::triangle),
                Codec.INT.fieldOf("plateau").forGetter(VanillaOre::plateau),
                Codec.BOOL.fieldOf("spawnOnOceanFloor").forGetter(VanillaOre::spawnOnOceanFloor),
                ResourceKey.codec(Registry.DIMENSION_REGISTRY).listOf().fieldOf("dimensions").forGetter(VanillaOre::dimensions),
                Codec.STRING.listOf().fieldOf("biomes").forGetter(VanillaOre::biomes),
                Codec.BOOL.fieldOf("biomeBlacklist").forGetter(VanillaOre::biomeBlacklist)
        ).apply(instance, VanillaOre::new);
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
        return "vanilla_ores";
    }

    @Override
    public Codec<VanillaOre> getCodec() {
        return CODEC;
    }
}
