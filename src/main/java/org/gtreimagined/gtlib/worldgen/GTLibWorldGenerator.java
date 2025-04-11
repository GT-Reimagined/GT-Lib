package org.gtreimagined.gtlib.worldgen;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.mixin.BiomeGenerationBuilderAccessor;
import org.gtreimagined.gtlib.registration.IGTObject;
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
import org.gtreimagined.gtlib.worldgen.object.WorldGenBase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static org.gtreimagined.gtlib.Ref.GSON;

public class GTLibWorldGenerator {
    static final GTFeature<NoneFeatureConfiguration> SMALL_ORE = new FeatureSmallOres();

    static final GTFeature<NoneFeatureConfiguration> VANILLA_ORE = new FeatureVanillaVeins();

    static final GTFeature<NoneFeatureConfiguration> VEIN_LAYER = new FeatureVein();
    static final GTFeature<NoneFeatureConfiguration> ORE = new FeatureOre();
    static final GTFeature<NoneFeatureConfiguration> STONE_LAYER = new FeatureStoneLayer();
    static final GTFeature<NoneFeatureConfiguration> BEDROCK_VEINS = new FeatureBedrockVeins();

    public static void clear() {
        GTAPI.all(GTFeature.class, t -> t.getRegistry().clear());
    }

    public static void preinit() {

    }

    public static void init() {
        GTAPI.runLaterCommon(() -> {
            WorldGenHelper.init();
            try {
                GTAPI.all(GTFeature.class).stream().filter(GTFeature::enabled).forEach(f -> {
                    f.onDataOverride(new JsonObject());
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
        //    AntimatterKubeJS.loadWorldgenScripts();
        //}
    }

    public static void register(Class<?> c, WorldGenBase<?> base) {
        GTFeature<?> feature = GTAPI.get(GTFeature.class, c.getName(), Ref.ID);
        if (feature != null)
            base.getDimensions().forEach(d -> feature.getRegistry().computeIfAbsent(d, k -> new LinkedList<>()).add(base));
    }

    public static <T> List<T> all(Class<T> c, ResourceKey<Level> dim) {
        GTFeature<?> feat = GTAPI.get(GTFeature.class, c.getName(), Ref.ID);
        return feat != null ? feat.getRegistry().computeIfAbsent(dim.location(), k -> new LinkedList<>()).stream().map(c::cast).collect(Collectors.toList()) : Collections.emptyList();
    }

    public static <T> List<T> all(Class<T> c) {
        GTFeature<?> feat = GTAPI.get(GTFeature.class, c.getName(), Ref.ID);
        return feat != null ? feat.getRegistry().values().stream().flatMap(Collection::stream).map(c::cast).distinct().toList() : Collections.emptyList();
    }

    private static void removeStoneFeatures(BiomeGenerationSettings.Builder builder) {
        removeDecoratedFeatureFromAllBiomes(builder, GenerationStep.Decoration.UNDERGROUND_ORES, Feature.ORE, Blocks.ANDESITE.defaultBlockState(), Blocks.GRANITE.defaultBlockState(), Blocks.DIORITE.defaultBlockState(), Blocks.TUFF.defaultBlockState(), Blocks.DIRT.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
    }

    private static void removeOreFeatures(BiomeGenerationSettings.Builder builder) {
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
    public static void removeDecoratedFeatureFromAllBiomes(BiomeGenerationSettings.Builder builder, @NotNull final GenerationStep.Decoration stage, @NotNull final Feature<?> featureToRemove, BlockState... states) {
        if (states.length == 0) Utils.onInvalidData("No BlockStates specified to be removed!");
        Set<BlockState> set = Set.of(states);
        // GTAPI.runLaterCommon(() -> {
        //  for (Biome biome : ForgeRegistries.BIOMES.getValues()) {
        ((BiomeGenerationBuilderAccessor)builder).getFeatures().get(stage.ordinal()).removeIf(f -> isDecoratedFeatureDisabled(f.value().feature().value(), featureToRemove, set));

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


    public static void reloadEvent(ResourceLocation name, Biome.ClimateSettings climate, Biome.BiomeCategory category, BiomeSpecialEffects effects, BiomeGenerationSettings.Builder gen, MobSpawnSettings.Builder spawns) {

        GTAPI.all(IGTFeature.class, t -> {
            t.build(name, climate, category, effects, gen, spawns);
        });
        handleFeatureRemoval(gen);
        GTAPI.all(IGTWorldgenFunction.class, t -> t.build(name, climate, category, effects, gen, spawns));
    }

    private static void handleFeatureRemoval(BiomeGenerationSettings.Builder gen) {
        if (GTLibConfig.VANILLA_ORE_GEN.get()) {
            removeOreFeatures(gen);
        }
        if (GTLibConfig.VANILLA_STONE_GEN.get()) {
            removeStoneFeatures(gen);
        }
    }

    public static void writeJson(JsonObject json, String id, String path) {
        File dir = new File(FMLPaths.CONFIGDIR.get().toFile(), "gtlib/" + path + "/default");
        File target = new File(dir, id + ".json");
        File readme = new File(dir, "README.txt");

        try {
            dir.mkdirs();
            if (!readme.exists()){
                BufferedWriter writer = Files.newBufferedWriter(readme.toPath());
                writer.write("This directory is used for default " + path + "worldgen, to override an entry copy the json to the overrides folder and modify it there.");
                writer.close();
            }
            BufferedWriter writer = Files.newBufferedWriter(target.toPath());
            writer.write(GSON.toJson(json));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T extends IGTObject> List<T> readCustomJsonObjects(Class<T> clazz, BiFunction<String, JsonObject, T> function, String path){
        File dir = new File(FMLPaths.CONFIGDIR.get().toFile(), "gtlib/" + path + "/custom");
        if (dir.listFiles() == null) return Collections.emptyList();
        List<File> files = Arrays.asList(dir.listFiles());
        List<T> objects = new ArrayList<>();
        files.forEach(f -> {
            if (f.isFile()){
                if (f.getAbsolutePath().endsWith(".json")){
                    try {
                        Reader reader = Files.newBufferedReader(f.toPath());
                        JsonObject parsed = JsonParser.parseReader(reader).getAsJsonObject();
                        T read = function.apply(f.getName().replace(".json", ""), parsed);
                        reader.close();
                        objects.add(read);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        return objects;
    }

    public static  <T extends IGTObject> T readJson(Class<T> clazz, T original, BiFunction<String, JsonObject, T> function, String path){
        File dir = new File(FMLPaths.CONFIGDIR.get().toFile(), "/" + path + "/overrides");
        File target = new File(dir, original.getId() + ".json");


        if(target.exists()) {
            try {
                Reader reader = Files.newBufferedReader(target.toPath());
                JsonObject parsed = JsonParser.parseReader(reader).getAsJsonObject();
                T read = function.apply(original.getId(), parsed);
                reader.close();
                return read;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return original;
    }
}