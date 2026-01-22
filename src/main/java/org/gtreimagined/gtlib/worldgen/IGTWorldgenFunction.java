package org.gtreimagined.gtlib.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;

public interface IGTWorldgenFunction {
    void build(Holder<Biome> biomeHolder, Biome.ClimateSettings climate, BiomeSpecialEffects effects, BiomeGenerationSettingsBuilder gen, MobSpawnSettings.Builder spawns, Registry<PlacedFeature> placedFeatureRegistry);
}
