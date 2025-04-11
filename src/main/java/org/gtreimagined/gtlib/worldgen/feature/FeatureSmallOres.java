package org.gtreimagined.gtlib.worldgen.feature;

import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import org.gtreimagined.gtlib.worldgen.object.WorldGenBase;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.BiPredicate;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE_SMALL;

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
    public void build(ResourceLocation name, Biome.ClimateSettings climate, Biome.BiomeCategory category, BiomeSpecialEffects effects, BiomeGenerationSettings.Builder gen, MobSpawnSettings.Builder spawns) {
        gen.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GTLibConfiguredFeatures.SMALL_ORES.get());
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> placer) {
        Random random = placer.random();
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
            if (!smallOre.material().has(ORE_SMALL)) continue;
            int minY = Math.max(worldMinY, smallOre.minY());
            int maxY = Math.min(worldMaxY, smallOre.maxY());
            int i = 0;
            for (int j = Math.max(1, smallOre.amountPerChunk() / 2 + random.nextInt(smallOre.amountPerChunk()) / 2); i < j; i++) {
                BlockPos pos = new BlockPos(chunkCornerX + random.nextInt(16), minY + random.nextInt(Math.max(1, maxY - minY)), chunkCornerZ + random.nextInt(16));
                if (!smallOre.isBiomeValid(world.getBiome(pos))) continue;
                boolean spawn = WorldGenHelper.setOre(world, pos, smallOre.material(), GTMaterialTypes.ORE_SMALL);
                if (spawn) spawned++;
            }
        }


        return spawned > 0;
    }
}
