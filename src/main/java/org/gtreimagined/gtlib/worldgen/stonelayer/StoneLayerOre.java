package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public record StoneLayerOre(Material material, long chance, int minY, int maxY, List<String> biomes, boolean biomeBlacklist) {
    public static final Codec<StoneLayerOre> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Material.CODEC.fieldOf("material").forGetter(StoneLayerOre::material),
            Codec.LONG.fieldOf("chance").forGetter(StoneLayerOre::chance),
            Codec.INT.fieldOf("minY").forGetter(StoneLayerOre::minY),
            Codec.INT.fieldOf("maxY").forGetter(StoneLayerOre::maxY),
            Codec.STRING.listOf().optionalFieldOf("biomes", List.of()).forGetter(StoneLayerOre::biomes),
            Codec.BOOL.optionalFieldOf("biomeBlacklist", true).forGetter(StoneLayerOre::biomeBlacklist)
    ).apply(instance, StoneLayerOre::new));

    public StoneLayerOre(Material material, long chance, int minY, int maxY) {
        this(material, bind(1, Ref.U, chance), minY, maxY, List.of(), true);
    }

    private StoneLayerOre addFilteredBiome(String biomeID){
        List<String> biomes = new ArrayList<>(this.biomes);
        if (!biomes.contains(biomeID)) {
            biomes.add(biomeID);
        }
        return new StoneLayerOre(material, chance, minY, maxY, biomes, !this.biomes.isEmpty() && biomeBlacklist);
    }

    public StoneLayerOre setBiomeBlacklist(boolean blacklist){
        return new StoneLayerOre(material, chance, minY, maxY, biomes, blacklist);
    }

    public StoneLayerOre addFilteredBiome(ResourceKey<Biome> biome){
        return addFilteredBiome(biome.location().toString());
    }

    public StoneLayerOre addFilteredBiome(TagKey<Biome> biomeTagKey){
        return addFilteredBiome("#" + biomeTagKey.location());
    }

    public boolean canPlace(BlockPos pos, Random rand, LevelAccessor world) {
        Holder<Biome> biome = world.getBiome(pos);
        boolean biomeValid = isBiomeValid(biome);
        return biomeValid && pos.getY() >= minY && pos.getY() <= maxY && rand.nextLong(Ref.U) < chance;
    }

    public static long bind(long min, long max, long boundValue) {
        return min > max ? Math.max(max, Math.min(min, boundValue)) : Math.max(min, Math.min(max, boundValue));
    }

    public boolean isBiomeValid(Holder<Biome> biome) {
        if (biomes.isEmpty()) return biomeBlacklist;
        Predicate<String> predicate = s -> {
            if (s.contains("#")) return biome.is(TagUtils.getBiomeTag(new ResourceLocation(s.replace("#", ""))));
            return biome.is(ResourceKey.create(Registry.BIOME_REGISTRY, new ResourceLocation(s)));
        };
        return biomeBlacklist ? biomes.stream().anyMatch(predicate) : biomes.stream().noneMatch(predicate);
    }
}
