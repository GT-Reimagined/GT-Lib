package org.gtreimagined.gtlib.worldgen.feature;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biome.ClimateSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier.Phase;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;

import java.util.Collection;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.SMALL_ORE;

public class FeatureSmallOres extends GTFeature<NoneFeatureConfiguration> {
    public FeatureSmallOres() {
        super(NoneFeatureConfiguration.CODEC, SmallOre.class);
    }

    @Override
    public String getId() {
        return "small_ores";
    }

    @Override
    public boolean enabled() {
        return GTLibConfig.SMALL_ORES.get() && !SmallOreData.INSTANCE.getVeins().isEmpty();
    }

    @Override
    public void init() {

    }


    @Override
    public void build(Phase phase, Holder<Biome> biomeHolder, ClimateSettings climate, BiomeSpecialEffects effects, BiomeGenerationSettingsBuilder gen, Builder spawns, Registry<PlacedFeature> placedFeatureRegistry) {
        if(phase == Phase.ADD) gen.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, getPlacedFeatureFromKey(placedFeatureRegistry, GTLibConfiguredFeatures.SMALL_ORES));
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> placer) {
        RandomSource random = placer.random();
        BlockPos blockpos = placer.origin();
        WorldGenLevel world = placer.level();

        final int chunkX = placer.origin().getX() >> 4;
        final int chunkZ = placer.origin().getZ() >> 4;
        final int chunkCornerX = chunkX * 16;
        final int chunkCornerZ = chunkZ * 16;
        final int worldMinY = world.dimensionType().minY();
        final int worldMaxY = world.dimensionType().minY() + world.dimensionType().height();
        Collection<SmallOre> smallOres = SmallOreData.INSTANCE.getVeins(world.getLevel()).values();
        int spawned = 0;
        for (SmallOre smallOre : smallOres) {
            if (!smallOre.material().has(SMALL_ORE)) continue;
            int minY = Math.max(worldMinY, smallOre.minY());
            int maxY = Math.min(worldMaxY, smallOre.maxY());
            int i = 0;
            for (int j = Math.max(1, smallOre.amountPerChunk() / 2 + random.nextInt(smallOre.amountPerChunk()) / 2); i < j; i++) {
                BlockPos pos = new BlockPos(chunkCornerX + random.nextInt(16), minY + random.nextInt(Math.max(1, maxY - minY)), chunkCornerZ + random.nextInt(16));
                if (!smallOre.isBiomeValid(world.getBiome(pos))) continue;
                boolean spawn = WorldGenHelper.setOre(world, pos, smallOre.material(), GTMaterialTypes.SMALL_ORE);
                if (spawn) spawned++;
            }
        }


        return spawned > 0;
    }
}
