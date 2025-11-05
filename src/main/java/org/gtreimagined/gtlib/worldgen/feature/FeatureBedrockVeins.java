package org.gtreimagined.gtlib.worldgen.feature;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVein;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVeinData;

import java.util.Collection;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.BEARING_ROCK;

public class FeatureBedrockVeins extends GTFeature<NoneFeatureConfiguration> {

    public FeatureBedrockVeins() {
        super(NoneFeatureConfiguration.CODEC, BedrockVein.class);
    }

    @Override
    public String getId() {
        return "bedrock_veins";
    }

    @Override
    public boolean enabled() {
        return GTLibConfig.BEDROCK_VEINS.get() && !BedrockVeinData.INSTANCE.getVeins().isEmpty();
    }

    @Override
    public void init() {

    }

    @Override
    public void build(ResourceLocation name, ClimateSettings climate, BiomeSpecialEffects effects, BiomeGenerationSettingsBuilder gen, Builder spawns) {
        gen.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GTLibConfiguredFeatures.BEDROCK_VEINS);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctxt) {
        WorldGenLevel world = ctxt.level();
        BlockPos pos = ctxt.origin();
        RandomSource rand = ctxt.random();

        Collection<BedrockVein> veins = BedrockVeinData.INSTANCE.getVeins(world.getLevel()).values();
        if (veins.isEmpty()) return false;
        for (BedrockVein vein : veins) {
            if(generateBedrockVein(vein, world, pos.getX(), pos.getZ(), pos.getX() + 16, pos.getZ() + 16, rand)){
                return true;
            }
        }
        return false;
    }

    public static boolean generateBedrockVein(BedrockVein vein, LevelAccessor level, int minX, int minZ, int maxX, int maxZ, RandomSource random) {
        if (random.nextInt(vein.probability()) != 0) return false;
        if (!generateVein(vein.material(), level, minX, minZ, random)) return false;


        if ((vein.indicatorRocks() || vein.indicatorFlowers())) {
            boolean tFlowers = vein.indicatorFlowers() && vein.flower() != Blocks.AIR, tRocks = vein.indicatorRocks() && vein.material().has(BEARING_ROCK);


            // Generate first an 8x8 of 4, then a 16x16 of 8, and at the end a 32x32 of 16 Rocks/Flowers. That way the Pattern gets denser in the middle, and Chunk Boundary Issues of GalactiCraft wont be as terrible.
            for (int tD = 4; tD <= 16; tD *= 2) {
                try {
                    for (int i = 0; i < tD; i++) {
                        int tX = minX+random.nextInt(tD*2)+8-tD, tZ = minZ+random.nextInt(tD*2)+8-tD;
                        int y = Math.min(level.getHeight(Heightmap.Types.OCEAN_FLOOR, tX, tZ), level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tX, tZ));
                        BlockPos offset = new BlockPos(tX, y, tZ);
                        BlockState below = level.getBlockState(new BlockPos(tX, y - 1, tZ));
                        if (tFlowers && (!tRocks || random.nextInt(4) > 0) && vein.flower().canSurvive(vein.flower().defaultBlockState(), level, offset)){
                            level.setBlock(offset, vein.flower().defaultBlockState(), 0);
                        } else if (tRocks){
                            if (!below.isAir() && below != WorldGenHelper.WATER_STATE && GTLibConfig.STONE_LAYER_ROCKS.get() && GTLibConfig.SURFACE_ROCKS.get()) {
                                WorldGenHelper.setRock(level, offset, vein.material(), Blocks.BEDROCK.defaultBlockState(), 1);
                            }
                        }
                    }
                } catch(Throwable e) {
                    GTLib.LOGGER.error(e);
                }
            }
        }

        return true;
    }

    public static boolean generateVein(Material material, LevelAccessor level, int minX, int minZ, RandomSource random) {
        try {
            Block tStone = level.getBlockState(new BlockPos(minX+8, level.getMinBuildHeight(), minZ+8)).getBlock();
            // Requires existing Bedrock!
            if (tStone != Blocks.BEDROCK) return false;
            // Generate the bedrock Ore Blocks.
            for (int tX = 5; tX < 11; tX++) {
                for (int tZ = 5; tZ < 11; tZ++) {
                    switch (random.nextInt(6)) {
                        case 0 -> WorldGenHelper.setState(level, new BlockPos(minX + tX, level.getMinBuildHeight(), minZ + tZ), GTMaterialTypes.ORE.get().get(material, VanillaStoneTypes.BEDROCK).asState());
                        case 1, 2 -> WorldGenHelper.setState(level, new BlockPos(minX + tX, level.getMinBuildHeight(), minZ + tZ), GTMaterialTypes.SMALL_ORE.get().get(material, VanillaStoneTypes.BEDROCK).asState());
                    }
                }
            }
            // At least one Ore Block must be there. So force place a large one somewhere in the Center.
            WorldGenHelper.setState(level, new BlockPos(minX + 6 + random.nextInt(4), level.getMinBuildHeight(), minZ + 6 + random.nextInt(4)), GTMaterialTypes.ORE.get().get(material, VanillaStoneTypes.BEDROCK).asState());
            // Use Deepslate if available, except in the Nether.
            tStone = Blocks.DEEPSLATE;
            int yOffset = level.getMinBuildHeight() < 0 ? Math.abs(level.getMinBuildHeight()) : -level.getMinBuildHeight();
            // Keep Distances within the Chunk for this important step.
            int[] tD1 = new int[] { 5,  4,  2,  1,  0,  2,  5};
            int[] tD2 = new int[] {11, 12, 14, 15, 16, 14, 11};
            // Portion a Muffin shaped Ore Blob around the Bedrock Spot.
            for (int tY = 1; tY < tD1.length; tY++) for (int tX = tD1[tY]; tX < tD2[tY]; tX++) for (int tZ = tD1[tY]; tZ < tD2[tY]; tZ++) {
                level.setBlock(new BlockPos(minX + tX, tY - yOffset, minZ + tZ), tStone.defaultBlockState(), 0);
                switch (random.nextInt(6)) {
                    case 0 -> WorldGenHelper.setOre(level, new BlockPos(minX + tX, tY - yOffset, minZ + tZ), material, GTMaterialTypes.ORE);
                    case 1, 2 -> WorldGenHelper.setOre(level, new BlockPos(minX + tX, tY - yOffset, minZ + tZ), material, GTMaterialTypes.SMALL_ORE);
                }
            }

            for (int i = 5+random.nextInt(3); i-->0;) {
                int tX = 5+random.nextInt(6), tZ = 5+random.nextInt(6), tW = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + tX, minZ + tZ);

                for (int tY = tD1.length - yOffset; tY < tW; tY++) {
                    switch(random.nextInt(7)) {
                        case 0 -> {
                            tX++;
                            tW = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + tX, minZ + tZ);
                        }
                        case 1 -> {
                            tX--;
                            tW = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + tX, minZ + tZ);
                        }
                        case 2 -> {
                            tZ++;
                            tW = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + tX, minZ + tZ);
                        }
                        case 3 -> {
                            tZ--;
                            tW = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, minX + tX, minZ + tZ);
                        }
                    }
                    if (tY >= tW) break;
                    if (tX <= 0 || tX >= 15 || tZ <= 0 || tZ >= 15) {
                        WorldGenHelper.setOre(level, new BlockPos(minX + tX, tY, minZ + tZ), material, GTMaterialTypes.SMALL_ORE);
                        break;
                    } else if (random.nextInt(3) != 0) {
                        WorldGenHelper.setOre(level, new BlockPos(minX + tX, tY, minZ + tZ), material, GTMaterialTypes.SMALL_ORE);
                    }
                }
            }

            return true;
        } catch(Throwable e) {
            GTLib.LOGGER.error(e);
        }
        return false;
    }
}
