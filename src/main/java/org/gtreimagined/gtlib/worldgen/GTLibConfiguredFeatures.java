package org.gtreimagined.gtlib.worldgen;

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
    
    public static final Holder<PlacedFeature> SMALL_ORES = register("small_ores", FeatureUtils.register("small_ores", GTLibWorldGenerator.SMALL_ORE, NoneFeatureConfiguration.NONE));
    public static final Holder<PlacedFeature> VANILLA_VEINS = register("vanilla_veins", FeatureUtils.register("vanilla_veins", GTLibWorldGenerator.VANILLA_ORE, NoneFeatureConfiguration.NONE));
    public static final Holder<PlacedFeature> VEINS = register("veins", FeatureUtils.register("veins", GTLibWorldGenerator.VEIN_LAYER, NoneFeatureConfiguration.NONE));
    public static final Holder<PlacedFeature> ORE = register("ore", FeatureUtils.register("ore", GTLibWorldGenerator.ORE, NoneFeatureConfiguration.NONE));
    public static final Holder<PlacedFeature> STONE_LAYERS = register("stone_layers", FeatureUtils.register("stone_layers", GTLibWorldGenerator.STONE_LAYER, NoneFeatureConfiguration.NONE));
    public static final Holder<PlacedFeature> BEDROCK_VEINS = register("bedrock_veins", FeatureUtils.register("bedrock_veins", GTLibWorldGenerator.BEDROCK_VEINS, NoneFeatureConfiguration.NONE));

    @SuppressWarnings("unchecked")
    public static <T extends FeatureConfiguration> Holder<PlacedFeature> register(String id, Holder<ConfiguredFeature<T, ?>> feature) {
        return PlacementUtils.register(Ref.ID + ":"+id, feature, Collections.emptyList());
    }

    public static void init() {
    }
}
