package org.gtreimagined.gtlib.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier.Phase;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.registration.RegistrationEvent;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.feature.GTFeature;
import org.gtreimagined.gtlib.worldgen.feature.FeatureBedrockVeins;
import org.gtreimagined.gtlib.worldgen.feature.FeatureOre;
import org.gtreimagined.gtlib.worldgen.feature.FeatureSmallOres;
import org.gtreimagined.gtlib.worldgen.feature.FeatureStoneLayer;
import org.gtreimagined.gtlib.worldgen.feature.FeatureVanillaVeins;
import org.gtreimagined.gtlib.worldgen.feature.FeatureVein;
import org.gtreimagined.gtlib.worldgen.feature.IGTFeature;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GTLibWorldGenerator {
    static final GTFeature<NoneFeatureConfiguration> SMALL_ORE = new FeatureSmallOres();

    static final GTFeature<NoneFeatureConfiguration> VANILLA_ORE = new FeatureVanillaVeins();

    static final GTFeature<NoneFeatureConfiguration> VEIN_LAYER = new FeatureVein();
    static final GTFeature<NoneFeatureConfiguration> ORE = new FeatureOre();
    static final GTFeature<NoneFeatureConfiguration> STONE_LAYER = new FeatureStoneLayer();
    static final GTFeature<NoneFeatureConfiguration> BEDROCK_VEINS = new FeatureBedrockVeins();

    public static void preinit() {

    }

    public static void init() {
        GTAPI.runLaterCommon(() -> {
            WorldGenHelper.init();
            try {
                GTAPI.all(GTFeature.class).stream().filter(GTFeature::enabled).forEach(f -> {
                    f.init();
                });
            } catch (Exception ex) {
                GTLib.LOGGER.warn("Caught exception during World generator later init: " + ex.toString());
            }
        });
        /*
        try {
            //Path config = FMLPaths.CONFIGDIR.get().resolve("GregTech/WorldGenDefault.json");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("GTLibWorldGenerator caught an exception while initializing");
        }
         */
    }

    public static void setup() {
        GTLib.LOGGER.info("GTLib WorldGen Initialization Stage...");
        GTAPI.onRegistration(RegistrationEvent.WORLDGEN_INIT);
        //if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
        //    GTLibKubeJS.loadWorldgenScripts();
        //}
    }

    private static void removeStoneFeatures(BiomeGenerationSettingsBuilder builder) {
        removeDecoratedFeatureFromAllBiomes(builder, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE, Blocks.ANDESITE.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), Blocks.DIORITE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), Blocks.DIRT.defaultBlockState());
    }

    private static void removeOreFeatures(BiomeGenerationSettingsBuilder builder) {
        removeDecoratedFeatureFromAllBiomes(builder, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE, Blocks.COAL_ORE.defaultBlockState(), Blocks.IRON_ORE.defaultBlockState(), Blocks.GOLD_ORE.defaultBlockState(), Blocks.COPPER_ORE.defaultBlockState(), Blocks.EMERALD_ORE.defaultBlockState(), Blocks.REDSTONE_ORE.defaultBlockState(), Blocks.LAPIS_ORE.defaultBlockState(), Blocks.DIAMOND_ORE.defaultBlockState());
        removeDecoratedFeatureFromAllBiomes(builder, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState(), Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState(), Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState(), Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState(), Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState(), Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState(), Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState());
        removeDecoratedFeatureFromAllBiomes(builder, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.SCATTERED_ORE, Blocks.IRON_ORE.defaultBlockState(), Blocks.COPPER_ORE.defaultBlockState(), Blocks.DEEPSLATE_IRON_ORE.defaultBlockState(), Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState());
    }

    /**
     * Removes specific features, in specific generation stages, in all biomes registered
     *
     * @param stage           generation stage where the feature is added to
     * @param featureToRemove feature instance wishing to be removed
     * @param states          BlockStates wish to be removed
     */
    public static void removeDecoratedFeatureFromAllBiomes(BiomeGenerationSettingsBuilder builder, @NotNull final GenerationStep.Decoration stage, @NotNull final Feature<?> featureToRemove, BlockState... states) {
        if (states.length == 0) Utils.onInvalidData("No BlockStates specified to be removed!");
        Set<BlockState> set = Set.of(states);
        builder.getFeatures(stage).removeIf(f -> isDecoratedFeatureDisabled(f.value().feature().value(), featureToRemove, set));
    }

    /**
     * Removes specific features, in specific generation stages, in specific biomes
     *
     * @param biome           Biome wish to remove feature from
     * @param stage           generation stage where the feature is added to
     * @param featureToRemove feature instance wishing to be removed
     * @param states          BlockStates wish to be removed
     */
    public static void removeDecoratedFeaturesFromBiome(@NotNull final Biome biome, final @NotNull GenerationStep.Decoration stage, final @NotNull Feature<?> featureToRemove, BlockState... states) {
        if (states.length == 0) Utils.onInvalidData("No BlockStates specified to be removed!");
        GTAPI.runLaterCommon(() -> {
            for (BlockState state : states) {
                // biome.getFeatures(stage).removeIf(f -> isDecoratedFeatureDisabled(f, featureToRemove, state));
            }
        });
    }

    /**
     * Check with BlockState in a feature if it is disabled
     */
    public static boolean isDecoratedFeatureDisabled(@NotNull ConfiguredFeature<?, ?> configuredFeature, @NotNull Feature<?> featureToRemove, @NotNull Set<BlockState> state) {
        if (configuredFeature.config() instanceof OreConfiguration config) {
            return config.targetStates.stream().anyMatch(t -> state.contains(t.state));
        }
        /*if (configuredFeature.config instanceof Feat) {
            FeatureConfiguration config = configuredFeature.config;
            Feature<?> feature = null;
            while (config instanceof DecoratedFeatureConfiguration) {
                feature = ((DecoratedFeatureConfiguration) config).feature.get().feature;
                config = ((DecoratedFeatureConfiguration) config).feature.get().config;
                if (!(feature instanceof DecoratedFeature)) {
                    break;
                }
            }
            if (feature == null) return false;
            if (feature instanceof OreFeature && featureToRemove == Feature.ORE) {
                OreConfiguration conf = (OreConfiguration) config;
                BlockState configState = conf.state;
                //TODO: state or not?
                return state.getBlock() == configState.getBlock();

            }
            //TODO: should this also be ore or not?
            if (config instanceof BlockStateConfiguration && featureToRemove == Feature.ORE) {
                BlockState configState = ((BlockStateConfiguration) config).state; // Constructor BlockState var
                return state == configState;
            }
        }*/
        return false;
    }


    public static void reloadEvent(Phase phase, Holder<Biome> biome, ClimateSettings climate, BiomeSpecialEffects effects, BiomeGenerationSettingsBuilder gen, Builder spawns, Registry<PlacedFeature> placedFeatureRegistry) {
        GTAPI.all(IGTFeature.class, t -> {
            t.build(phase, biome, climate, effects, gen, spawns, placedFeatureRegistry);
        });
        GTAPI.all(IGTWorldgenFunction.class, t -> t.build(phase, biome, climate, effects, gen, spawns, placedFeatureRegistry));
        if (phase == Phase.REMOVE) {
            handleFeatureRemoval(gen);
        }
    }

    private static void handleFeatureRemoval(BiomeGenerationSettingsBuilder gen) {
        if (GTLibConfig.VANILLA_ORE_GEN.get()) {
            removeOreFeatures(gen);
        }
        if (GTLibConfig.VANILLA_STONE_GEN.get()) {
            removeStoneFeatures(gen);
        }
    }
}