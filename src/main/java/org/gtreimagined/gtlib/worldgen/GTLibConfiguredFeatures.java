package org.gtreimagined.gtlib.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Collections;

public class GTLibConfiguredFeatures {
    
    public static final ResourceKey<PlacedFeature> SMALL_ORES = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "small_ores"));
    public static final ResourceKey<PlacedFeature> VANILLA_VEINS = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "vanilla_veins"));
    public static final ResourceKey<PlacedFeature> VEINS = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "veins"));
    public static final ResourceKey<PlacedFeature> ORE = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "ore"));
    public static final ResourceKey<PlacedFeature> STONE_LAYERS = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "stone_layers"));
    public static final ResourceKey<PlacedFeature> BEDROCK_VEINS = ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Ref.ID, "bedrock_veins"));

    public static void init() {
    }
}
