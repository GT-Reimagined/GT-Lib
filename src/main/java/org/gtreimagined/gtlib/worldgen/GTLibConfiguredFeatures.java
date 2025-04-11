package org.gtreimagined.gtlib.worldgen;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.Collections;
import java.util.function.Supplier;

public class GTLibConfiguredFeatures {
    
    public static final Supplier<Holder<PlacedFeature>> SMALL_ORES = () -> getPlacedFeature("small_ores");
    public static final Supplier<Holder<PlacedFeature>> VANILLA_VEINS = () -> getPlacedFeature("vanilla_veins");
    public static final Supplier<Holder<PlacedFeature>> VEINS = () -> getPlacedFeature("veins");
    public static final Supplier<Holder<PlacedFeature>> ORE = () -> getPlacedFeature("ore");
    public static final Supplier<Holder<PlacedFeature>> STONE_LAYERS = () -> getPlacedFeature("stone_layers");
    public static final Supplier<Holder<PlacedFeature>> BEDROCK_VEINS = () -> getPlacedFeature("bedrock_veins");

    public static Holder<PlacedFeature> getPlacedFeature(String id){
        return BuiltinRegistries.PLACED_FEATURE.getHolderOrThrow(ResourceKey.create(BuiltinRegistries.PLACED_FEATURE.key(), new ResourceLocation(Ref.ID, id)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends FeatureConfiguration> Holder<PlacedFeature> register(String id, Holder<ConfiguredFeature<T, ?>> feature) {
        return PlacementUtils.register(Ref.ID + ":"+id, feature, Collections.emptyList());
    }

    public static void init() {
    }
}
