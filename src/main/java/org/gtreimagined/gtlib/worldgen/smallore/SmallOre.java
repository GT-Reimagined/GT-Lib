package org.gtreimagined.gtlib.worldgen.smallore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.gtreimagined.gtlib.worldgen.vein.Vein;

import java.util.List;
import java.util.function.Predicate;

public record SmallOre(ResourceLocation id, Material material, int minY, int maxY, int amountPerChunk,
                       List<ResourceKey<Level>> dimensions, List<String> biomes,
                       boolean biomeBlacklist) implements IWorldgenObject<SmallOre> {
    public static final Codec<SmallOre> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(SmallOre::id),
            Material.CODEC.fieldOf("material").forGetter(SmallOre::material),
            Codec.INT.fieldOf("minY").forGetter(SmallOre::minY),
            Codec.INT.fieldOf("maxY").forGetter(SmallOre::maxY),
            Codec.INT.fieldOf("amountPerChunk").forGetter(SmallOre::amountPerChunk),
            ResourceKey.codec(Registries.DIMENSION).listOf().fieldOf("dimensions").forGetter(SmallOre::dimensions),
            Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(SmallOre::biomes),
            Codec.BOOL.optionalFieldOf("biomeBlacklist", true).forGetter(SmallOre::biomeBlacklist)
    ).apply(instance, SmallOre::new));

    @Override
    public boolean isBiomeValid(Holder<Biome> biome) {
        if (biomes.isEmpty()) return biomeBlacklist;
        Predicate<String> predicate = s -> {
            if (s.contains("#")) return biome.is(TagUtils.getBiomeTag(new ResourceLocation(s.replace("#", ""))));
            return biome.is(ResourceKey.create(Registries.BIOME, new ResourceLocation(s)));
        };
        return biomeBlacklist ? biomes.stream().anyMatch(predicate) : biomes.stream().noneMatch(predicate);
    }

    @Override
    public ResourceLocation getLoc() {
        return id;
    }

    @Override
    public String getSubDirectory() {
        return "small_ores";
    }

    @Override
    public Codec<SmallOre> getCodec() {
        return CODEC;
    }

    @Override
    public List<ResourceKey<Level>> getDimensions() {
        return dimensions;
    }
}
