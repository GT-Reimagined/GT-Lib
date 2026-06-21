package org.gtreimagined.gtlib.worldgen.feature;

import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier.Phase;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.block.BlockSurfaceRock;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.NoiseGenerator;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerOre;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayer;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData;

import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.BEARING_ROCK;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.ROCK;

public class FeatureStoneLayer extends GTFeature<NoneFeatureConfiguration> {

    public FeatureStoneLayer() {
        super(NoneFeatureConfiguration.CODEC, StoneLayer.class);
    }

    @Override
    public String getId() {
        return "stone_layers";
    }

    @Override
    public boolean enabled() {
        return GTLibConfig.STONE_LAYERS.get() && !StoneLayerData.INSTANCE.getVeins().isEmpty();
    }

    @Override
    public void init() {
    }

    @Override
    public void build(Phase phase, Holder<Biome> biomeHolder, ClimateSettings climate, BiomeSpecialEffects effects, BiomeGenerationSettingsBuilder gen, Builder spawns, Registry<PlacedFeature> placedFeatureRegistry) {
        if(phase == Phase.ADD) gen.addFeature(Decoration.RAW_GENERATION, getPlacedFeatureFromKey(placedFeatureRegistry, GTLibConfiguredFeatures.STONE_LAYERS));
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctxt) {
        WorldGenLevel world = ctxt.level();
        BlockPos pos = ctxt.origin();
        RandomSource rand = ctxt.random();
        List<StoneLayer> stones = StoneLayerData.INSTANCE.getFlat(world.getLevel());
        if (stones.isEmpty()) return false;
        StoneLayer[] layers = new StoneLayer[7];
        NoiseGenerator noise = new NoiseGenerator(world);
        int stonesSize = stones.size(), stonesMax = stonesSize - 1;
        BlockState existing;
        int maxHeight;
        boolean isAir;
        Pair<StoneType, Material> lastMaterialAndStone;
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                int tX = pos.getX() + i, tZ = pos.getZ() + j;

                layers[0] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, -2, tZ) + 1) / 2) * stonesSize)));
                layers[1] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, -1, tZ) + 1) / 2) * stonesSize)));
                layers[2] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 0, tZ) + 1) / 2) * stonesSize)));
                layers[3] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 1, tZ) + 1) / 2) * stonesSize)));
                layers[4] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 2, tZ) + 1) / 2) * stonesSize)));
                layers[5] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 3, tZ) + 1) / 2) * stonesSize)));
                layers[6] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, 4, tZ) + 1) / 2) * stonesSize)));

                StoneType topStoneType = null;
                boolean placedRock = false;
                maxHeight = world.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, pos.offset(i, 0, j)).getY() + 1; //+1 for placing rocks on top of the max height
                int minHeight = world.getMinBuildHeight();
                for (int tY = -63; tY < maxHeight; tY++) {
                    int offsetY = tY + 64;
                    lastMaterialAndStone = null;
                    BlockPos offset = pos.offset(i, offsetY, j);
                    existing = world.getBlockState(offset);
                    isAir = existing.isAir();
                    StoneType rockType = null;
                    boolean setStone = false;

                    //If we haven't placed an ore, and not trying to set the same state as existing.
                    if (!isAir) {
                        if (existing.getBlock() != layers[3].block()) {
                            int y = offset.getY();
                            var restrictions = layers[3].restrictions();
                            Block toPlace = layers[3].block();
                            StoneType type = layers[3].type();
                            if (y >= restrictions.maxY() && restrictions.maxY() < maxHeight && restrictions.topReplacement() != Blocks.AIR) {
                                if (!placeReplacement(y, restrictions.maxY(), restrictions.maxY() + 8, rand)){
                                    toPlace = restrictions.topReplacement();
                                    type = restrictions.topReplacementStone();
                                }
                            }
                            if (restrictions.minY() > minHeight && y <= restrictions.minY() + 8 && restrictions.bottomReplacement() != Blocks.AIR) {
                                if (placeReplacement(y, restrictions.minY(), restrictions.minY() + 8, rand)){
                                    toPlace = restrictions.bottomReplacement();
                                    type = restrictions.bottomReplacementStone();
                                }
                            }
                            if (WorldGenHelper.setStone(world, offset, existing, toPlace.defaultBlockState())) {
                                setStone = true;
                                if (toPlace instanceof BlockOreStone stone) {
                                    if (ROCK.get().get(stone.getMaterial()).asBlock() instanceof BlockSurfaceRock surfaceRock) {
                                        rockType = surfaceRock.getStoneType();
                                        topStoneType = rockType;
                                    }
                                } else {
                                    lastMaterialAndStone = type != null ? Pair.of(type, type.getMaterial()) : null;
                                    if (type != null){
                                        topStoneType = type;
                                    }
                                }

                            }
                        } else {
                            setStone = true;
                            StoneType type = layers[3].type();
                            lastMaterialAndStone = type != null ? Pair.of(type, type.getMaterial()) : null;
                            if (type != null){
                                topStoneType = type;
                            }
                        }
                    }

                    if (setStone && GTLibConfig.STONE_LAYER_ORES.get()) {
                        if (layers[1] == layers[5]) {
                            for (StoneLayerOre ore : layers[3].ores()) {
                                if (ore.canPlace(offset, rand, world) && WorldGenHelper.addOre(world, offset, ore.material(), layers[0] == layers[6])) {
                                    lastMaterialAndStone = Pair.of(layers[3].type(), ore.material());
                                    break;
                                }
                            }
                        } else {
                            StoneType type = layers[3].type() != null ? layers[3].type() : rockType;
                            if (type != null && type.doesGenerateOre()) {
                                for (StoneLayerOre ore : StoneLayer.getCollision(type, layers[5].block().defaultBlockState(), layers[1].block().defaultBlockState())) {
                                    if (ore.canPlace(offset, rand, world) && WorldGenHelper.addOre(world, offset, ore.material(), true)) {
                                        lastMaterialAndStone = Pair.of(type, ore.material());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if (!placedRock && lastMaterialAndStone != null && lastMaterialAndStone.right().has(GTMaterialTypes.ORE) && lastMaterialAndStone.right().has(BEARING_ROCK)) {
                        BlockState below = world.getBlockState(offset.offset(0, -1, 0));
                        int y = Math.min(world.getHeight(Heightmap.Types.OCEAN_FLOOR, offset.getX(), offset.getZ()), world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offset.getX(), offset.getZ()));
                        if (!below.isAir() && below != WorldGenHelper.WATER_STATE && GTLibConfig.STONE_LAYER_ORE_ROCKS.get() && GTLibConfig.SURFACE_ROCKS.get()) {
                            if (WorldGenHelper.setRock(world, new BlockPos(offset.getX(), y, offset.getZ()), lastMaterialAndStone.right(), below, GTLibConfig.STONE_LAYER_ORE_ROCK_CHANCE.get())){
                                placedRock = true;
                            } else if (lastMaterialAndStone.left().getMaterial().has(ROCK)) {
                                if (WorldGenHelper.setRock(world, new BlockPos(offset.getX(), y, offset.getZ()), Material.NULL, lastMaterialAndStone.left().getState(), GTLibConfig.STONE_LAYER_ROCK_CHANCE.get())) {
                                    placedRock = true;
                                }
                            }
                        }
                    }
                    if (!placedRock && rockType != null){
                        BlockState below = world.getBlockState(offset.offset(0, -1, 0));
                        int y = Math.min(world.getHeight(Heightmap.Types.OCEAN_FLOOR, offset.getX(), offset.getZ()), world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offset.getX(), offset.getZ()));
                        if (!below.isAir() && below != WorldGenHelper.WATER_STATE && GTLibConfig.STONE_LAYER_DENSE_ORE_ROCKS.get() && GTLibConfig.SURFACE_ROCKS.get()) {
                            if (WorldGenHelper.setRock(world, offset.mutable().setY(y).immutable(), Material.NULL, rockType.getState(), GTLibConfig.STONE_LAYER_DENSE_ORE_ROCK_CHANCE.get())){
                                placedRock = true;
                            }
                        }
                    }

                    // And scan for next Block on the Stone Layer Type.
                    System.arraycopy(layers, 1, layers, 0, layers.length - 1);
                    layers[6] = stones.get(Math.min(stonesMax, (int) (((noise.get(tX, offsetY + 4, tZ) + 1) / 2) * stonesSize)));
                }
                if (!placedRock && topStoneType != null){
                    BlockPos offset = pos.offset(i, 0, j);
                    int y = Math.min(world.getHeight(Heightmap.Types.OCEAN_FLOOR, offset.getX(), offset.getZ()), world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, offset.getX(), offset.getZ()));
                    BlockState below = world.getBlockState(offset.mutable().setY(y - 1));
                    if (!below.isAir() && below != WorldGenHelper.WATER_STATE && GTLibConfig.STONE_LAYER_ROCKS.get() && GTLibConfig.SURFACE_ROCKS.get()) {
                        WorldGenHelper.setRock(world, offset.mutable().setY(y).immutable(), Material.NULL, topStoneType.getState(), GTLibConfig.STONE_LAYER_ROCK_CHANCE.get());
                    }
                }
            }
        }
        return true;
    }

    public boolean placeReplacement(int y, int bottom, int top, RandomSource random) {
        if (y <= bottom) {
            return true;
        } else if (y >= top) {
            return false;
        } else {
            double d0 = Mth.map((double) y, (double) bottom, (double) top, (double) 1.0F, (double) 0.0F);
            return (double) random.nextFloat() < d0;

        }
    }
}
