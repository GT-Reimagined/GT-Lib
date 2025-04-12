package org.gtreimagined.gtlib.worldgen.feature;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.util.XSTR;
import org.gtreimagined.gtlib.worldgen.GTLibConfiguredFeatures;
import org.gtreimagined.gtlib.worldgen.VeinLayerResult;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;
import java.util.Map;

import static org.gtreimagined.gtlib.worldgen.VeinLayerResult.*;
import static org.gtreimagined.gtlib.worldgen.VeinLayerResult.ORE_PLACED;
import static org.gtreimagined.gtlib.worldgen.WorldGenHelper.setOre;
import static org.gtreimagined.gtlib.worldgen.vein.Vein.NO_ORES_IN_VEIN;

public class FeatureVein extends GTFeature<NoneFeatureConfiguration> {
    public static Long2ObjectOpenHashMap<Vein> VALID_VEINS = new Long2ObjectOpenHashMap<>();


    public FeatureVein() {
        super(NoneFeatureConfiguration.CODEC, Vein.class);
    }

    @Override
    public String getId() {
        return "veins";
    }

    @Override
    public boolean enabled() {
        return GTLibConfig.ORE_VEINS.get() && !VeinData.INSTANCE.getVeins().isEmpty();
    }

    @Override
    public void init() {

    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> placer) {
        int chunkX = placer.origin().getX() >> 4;
        int chunkZ = placer.origin().getZ() >> 4;
        for (Tuple<Integer, Integer> seed : getVeinSeeds(chunkX, chunkZ)) {
            generate(placer.level(), chunkX, chunkZ, seed.getA(), seed.getB());
        }
        return true;
    }

    // How to evaluate oregen distribution
    // - Enable Ref.debugOreVeins
    // - Fly around for a while, or teleport jumping ~320 blocks at a time, with
    //   a 15-30s pause for worldgen to catch up
    // - Do this across a large area, at least 2000x2000 blocks for good numbers
    // - Open logs\gregtech.log
    // - Using notepad++, do a Search | Find  - enter "Added" for the search term
    // - Select Find All In Current Document
    // - In the Search window, right-click and Select All
    // - Copy and paste to a new file
    // - Delete extraneous stuff at top, and blank line at bottom.  Line count is
    //   # of total oreveins
    // - For simple spot checks, use Find All in Current Document for specific
    //   oremixes, ie ore.mix.diamond, to check how many appear in the list.
    // - For more complex work, import file into Excel, and sort based on oremix
    //   column.  Drag select the oremix names, in the bottom right will be how many
    //   entries to add in a seperate tab to calculate %ages.
    //
    // When using the ore weights, discount or remove the high altitude veins since
    // their high weight are offset by their rareness. I usually just use zero for them.
    // Actual spawn rates will vary based upon the average height of the stone layers
    // in the dimension. For example veins that range above and below the average height
    // will be less, and veins that are completely above the average height will be much less.
    public static void generate(WorldGenLevel world, int chunkX, int chunkZ, int oreSeedX, int oreSeedZ) {
        Map<ResourceLocation, Vein> veins = VeinData.INSTANCE.getVeins(world.getLevel());
        if (veins.isEmpty())
            return;

        // Explanation of oreveinseed implementation.
        // (long)this.world.getSeed()<<16)    Deep Dark does two oregen passes, one with getSeed set to +1 the original world seed.  This pushes that +1 off the low bits of oreSeedZ, so that the hashes are far apart for the two passes.
        // ((this.world.provider.getDimension() & 0xffL)<<56)    Puts the dimension in the top bits of the hash, to make sure to get unique hashes per dimension
        // ((long)oreSeedX & 0x000000000fffffffL) << 28)    Puts the chunk X in the bits 29-55. Cuts off the top few bits of the chunk so we have bits for dimension.
        // ((long)oreSeedZ & 0x000000000fffffffL))    Puts the chunk Z in the bits 0-27. Cuts off the top few bits of the chunk so we have bits for dimension.
        long oreVeinSeed = getOreVeinSeed(world, oreSeedX, oreSeedZ);
        XSTR oreVeinRNG = new XSTR(oreVeinSeed);
        int oreVeinPercentageRoll = oreVeinRNG.nextInt(100); // Roll the dice, see if we get an orevein here at all
        if (Ref.debugOreVein)
            GTLib.LOGGER.info("Finding oreveins for oreVeinSeed=" + oreVeinSeed + " chunkX=" + chunkX + " chunkZ=" + chunkZ + " oreSeedX=" + oreSeedX + " oreSeedZ=" + oreSeedZ + " worldSeed=" + world.getSeed());

        // Search for a valid orevein for this dimension
        if (!VALID_VEINS.containsKey(oreVeinSeed)) {
            int veinCount = veins.size();
            if (oreVeinPercentageRoll < GTLibConfig.ORE_VEIN_CHANCE.get() && VeinData.getTotalWeight() > 0 && veinCount > 0) {
                int placementAttempts = 0;
                boolean oreVeinFound = false;
                int i;

                for (i = 0; i < GTLibConfig.ORE_VEIN_FIND_ATTEMPTS.get() && !oreVeinFound && placementAttempts < GTLibConfig.ORE_VEIN_PLACE_ATTEMPTS.get(); i++) {
                    int tRandomWeight = oreVeinRNG.nextInt(VeinData.getTotalWeight());
                    for (var veinEntry : veins.entrySet()) {
                        Vein vein = veinEntry.getValue();
                        tRandomWeight -= vein.weight();
                        if (tRandomWeight <= 0) {
                            // Adjust the seed so that this vein has a series of unique random numbers.  Otherwise multiple attempts at this same oreseed will get the same offset and X/Z values. If an orevein failed, any orevein with the
                            // same minimum heights would fail as well.  This prevents that, giving each orevein a unique height each pass through here.
                            VeinLayerResult placementResult = generateChunkified(vein, world, new XSTR(oreVeinSeed ^ vein.primary().hashCode()), chunkX * 16, chunkZ * 16, oreSeedX * 16, oreSeedZ * 16);
                            switch (placementResult) {
                                case ORE_PLACED -> {
                                    if (Ref.debugOreVein)
                                        GTLib.LOGGER.info("Added near oreVeinSeed=" + oreVeinSeed + " " + veinEntry.getKey() + " tries at oremix=" + i + " placementAttempts=" + placementAttempts + " dimension=" + world.getLevel().dimension().location());
                                    VALID_VEINS.put(oreVeinSeed, vein);
                                    oreVeinFound = true;
                                }
                                case NO_ORE_IN_BOTTOM_LAYER -> placementAttempts++;
                                // Should do retry in this case until out of chances
                                case NO_OVERLAP -> {
                                    if (Ref.debugOreVein)
                                        GTLib.LOGGER.info("Added far oreVeinSeed=" + oreVeinSeed + " " + veinEntry.getKey() + " tries at oremix=" + i + " placementAttempts=" + placementAttempts + " dimension=" + world.getLevel().dimension().location());
                                    VALID_VEINS.put(oreVeinSeed, vein);
                                    oreVeinFound = true;
                                }
                                case NO_OVERLAP_AIR_BLOCK -> {
                                    if (Ref.debugOreVein)
                                        GTLib.LOGGER.info("No overlap and air block in test spot=" + oreVeinSeed + " " + veinEntry.getKey() + " tries at oremix=" + i + " placementAttempts=" + placementAttempts + " dimension=" + world.getLevel().dimension().location());
                                    placementAttempts++;
                                } // Should do retry in this case until out of chances
                            }
                            break; // Try the next orevein
                        }
                    }
                }
                // Only add an empty orevein if unable to place a vein at the oreseed chunk.
                if (!oreVeinFound && chunkX == oreSeedX && chunkZ == oreSeedZ) {
                    if (Ref.debugOreVein)
                        GTLib.LOGGER.info("Empty oreVeinSeed=" + oreVeinSeed + " chunkX=" + chunkX + " chunkZ=" + chunkZ + " oreSeedX=" + oreSeedX + " oreSeedZ=" + oreSeedZ + " tries at oremix=" + i + " placementAttempts=" + placementAttempts + " dimension=" + world.getLevel().dimension().location());
                    VALID_VEINS.put(oreVeinSeed, NO_ORES_IN_VEIN);
                }
            } else if (oreVeinPercentageRoll >= GTLibConfig.ORE_VEIN_CHANCE.get()) {
                if (Ref.debugOreVein)
                    GTLib.LOGGER.info("Skipped oreVeinSeed=" + oreVeinSeed + " chunkX=" + chunkX + " chunkZ=" + chunkZ + " oreSeedX=" + oreSeedX + " oreSeedZ=" + oreSeedZ + " RNG=" + oreVeinPercentageRoll + " %=" + GTLibConfig.ORE_VEIN_CHANCE.get() + " dimension=" + world.getLevel().dimension().location());
                VALID_VEINS.put(oreVeinSeed, NO_ORES_IN_VEIN);
            }
        } else {
            // oreseed is located in the previously processed table
            if (Ref.debugOreVein)
                GTLib.LOGGER.info("Valid oreVeinSeed=" + oreVeinSeed + " VALID_VEINS.size()=" + VALID_VEINS.size() + " ");
            Vein vein = VALID_VEINS.get(oreVeinSeed);
            if (vein == null)
                throw new IllegalStateException("Valid veins returned null in WorldGenVeinlayer. This is an error");
            if (vein.primary() != null)
                oreVeinRNG.setSeed(oreVeinSeed ^ vein.primary().hashCode());  // Reset RNG to only be based on oreseed X/Z and type of vein
            VeinLayerResult placementResult = generateChunkified(vein, world, oreVeinRNG, chunkX * 16, chunkZ * 16, oreSeedX * 16, oreSeedZ * 16);
            switch (placementResult) {
                case NO_ORE_IN_BOTTOM_LAYER:
                    if (Ref.debugOreVein)
                        GTLib.LOGGER.info(" No ore in bottom layer");
                    break;
                case NO_OVERLAP:
                    if (Ref.debugOreVein)
                        GTLib.LOGGER.info(" No overlap");
                    break;
            }
        }
    }

    static VeinLayerResult generateChunkified(Vein vein, WorldGenLevel world, XSTR rand, int posX, int posZ, int seedX, int seedZ) {
        if (vein.equals(NO_ORES_IN_VEIN)) return NO_ORES_VEIN;
        int tMinY = vein.minY() + rand.nextInt(vein.maxY() - vein.minY() - 5);

        //If the selected tMinY is more than the max height if the current position, escape
//        if (tMinY > world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, posX, posZ)) {
//            return CHUNK_HEIGHT_TOO_LOW;
//        }

        // Determine West/East ends of orevein
        int wXVein = seedX - rand.nextInt(vein.size());        // West side
        int eXVein = seedX + 16 + rand.nextInt(vein.size());
        // Limit Orevein to only blocks present in current chunk
        int wX = Math.max(wXVein, posX);
        int eX = Math.min(eXVein, posX + 16);

        // Get a block at the center of the chunk and the bottom of the orevein.

        BlockPos centerPos = new BlockPos(posX + 7, tMinY, posZ + 9);
        BlockState centerState = world.getBlockState(centerPos);
        //Block tBlock = world.getBlock(posX + 7, tMinY, posZ + 9);

        if (wX >= eX) {  //No overlap between orevein and this chunk exists in X
            if (WorldGenHelper.ORE_PREDICATE.test(centerState)) {
                return NO_OVERLAP; // Didn't reach, but could have placed. Save orevein for future use.
            } else {
                return NO_OVERLAP_AIR_BLOCK;// Didn't reach, but couldn't place in test spot anywys, try for another orevein
            }
        }
        // Determine North/Sound ends of orevein
        int nZVein = seedZ - rand.nextInt(vein.size());
        int sZVein = seedZ + 16 + rand.nextInt(vein.size());

        int nZ = Math.max(nZVein, posZ);
        int sZ = Math.min(sZVein, posZ + 16);
        if (nZ >= sZ) { //No overlap between orevein and this chunk exists in Z
            if (WorldGenHelper.ORE_PREDICATE.test(centerState)) {
                return NO_OVERLAP; // Didn't reach, but could have placed. Save orevein for future use.
            } else {
                return NO_OVERLAP_AIR_BLOCK; // Didn't reach, but couldn't place in test spot anywys, try for another orevein
            }
        }

        if (Ref.debugOreVein)
            GTLib.LOGGER.info("Trying Orevein:" + vein.getLoc() + " Dimension=" + world.getLevel().dimension() + " posX=" + posX / 16 + " posZ=" + posZ / 16 + " oreseedX=" + seedX / 16 + " oreseedZ=" + seedZ / 16 + " cY=" + tMinY);
        if (!generateSquare(vein, world, rand, posX, posZ, seedX, seedZ, tMinY, wXVein, eXVein, nZVein, sZVein, wX, eX, nZ, sZ))
            //if (!generateByFunction(world, rand, tMinY, wXVein, eXVein, nZVein, sZVein, wX, eX, nZ, sZ))
            return NO_ORE_IN_BOTTOM_LAYER;  // Exit early, didn't place anything in the bottom layer

        //Place small ores for the vein
        if (GTLibConfig.ORE_VEIN_SMALL_ORE_MARKERS.get()) {
            int nSmallOres = (eX - wX) * (sZ - nZ) * vein.density() / 20 * GTLibConfig.ORE_VEIN_SMALL_ORE_MARKERS_MULTI.get();
            generateMarkers(vein, world, rand, posX, posZ, nSmallOres, 7);
        }
        // Something (at least the bottom layer must have 1 block) must have been placed, return true
        return ORE_PLACED;
    }

    //Small ores are placed in the whole chunk in which the vein appears. Rocks are placed randomly on top.
    static void generateMarkers(Vein vein, LevelAccessor world, XSTR rand, int posX, int posZ, int nSmallOres, int nRocks) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int nSmallOresCount = 0; nSmallOresCount < nSmallOres; nSmallOresCount++) {
            int tX = rand.nextInt(16) + posX;
            int tZ = rand.nextInt(16) + posZ;
            int tY = rand.nextInt(224) - 54; // Y height can vary from -54 to 170 for small ores.
            pos.set(tX, tY, tZ);
            setOre(world, pos, world.getBlockState(pos), vein.primary(), GTMaterialTypes.ORE_SMALL);

            tX = rand.nextInt(16) + posX;
            tZ = rand.nextInt(16) + posZ;
            tY = rand.nextInt(224) - 54; // Y height can vary from -54 to 170 for small ores.
            pos.set(tX, tY, tZ);
            setOre(world, pos, world.getBlockState(pos), vein.secondary(), GTMaterialTypes.ORE_SMALL);

            tX = rand.nextInt(16) + posX;
            tZ = rand.nextInt(16) + posZ;
            tY = rand.nextInt(224) - 54; // Y height can vary from -54 to 170 for small ores.
            pos.set(tX, tY, tZ);
            setOre(world, pos, world.getBlockState(pos), vein.between(), GTMaterialTypes.ORE_SMALL);

            tX = rand.nextInt(16) + posX;
            tZ = rand.nextInt(16) + posZ;
            tY = rand.nextInt(254) - 54; // Y height can vary from -54 to 200 for small ores.
            pos.set(tX, tY, tZ);
            setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE_SMALL);
        }
        if (GTLibConfig.ORE_VEIN_ROCKS.get() && GTLibConfig.SURFACE_ROCKS.get()){
            for (int rockCount = 0; rockCount < nRocks; rockCount++) {
                int tX = rand.nextInt(16) + posX;
                int tZ = rand.nextInt(16) + posZ;
                WorldGenHelper.addRock(world, pos.set(tX, 0, tZ), vein.primary(), GTLibConfig.ORE_VEIN_ROCK_CHANCE.get());
                tX = rand.nextInt(16) + posX;
                tZ = rand.nextInt(16) + posZ;
                WorldGenHelper.addRock(world, pos.set(tX, 0, tZ), vein.primary(),  GTLibConfig.ORE_VEIN_ROCK_CHANCE.get()*3/2);
                tX = rand.nextInt(16) + posX;
                tZ = rand.nextInt(16) + posZ;
                WorldGenHelper.addRock(world, pos.set(tX, 0, tZ), vein.primary(),  GTLibConfig.ORE_VEIN_ROCK_CHANCE.get()*2);
                tX = rand.nextInt(16) + posX;
                tZ = rand.nextInt(16) + posZ;
                WorldGenHelper.addRock(world, pos.set(tX, 0, tZ), vein.primary(),  GTLibConfig.ORE_VEIN_ROCK_CHANCE.get()*5/2);
            }
        }
    }

    static boolean generateSquare(Vein vein, LevelAccessor world, XSTR rand, int posX, int posZ, int seedX, int seedZ, int tMinY, int wXVein, int eXVein, int nZVein, int sZVein, int wX, int eX, int nZ, int sZ) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int[] placeCount = new int[4];

        // Adjust the density down the more chunks we are away from the oreseed.  The 5 chunks surrounding the seed should always be max density due to truncation of Math.sqrt().
        int localDensity = Math.max(1, vein.density() / (int) Math.sqrt(2 + Math.pow(posX / 16 - seedX / 16, 2) + Math.pow(posZ / 16 - seedZ / 16, 2)));

        // To allow for early exit due to no ore placed in the bottom layer (probably because we are in the sky), unroll 1 pass through the loop
        // Now we do bottom-level-first oregen, and work our way upwards.
        // Layer -1 Secondary and Sporadic
        int level = tMinY - 1; //Dunno why, but the first layer is actually played one below tMinY.  Go figure.
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.secondary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        if (placeCount[1] + placeCount[3] == 0) {
            if (Ref.debugOreVein)
                GTLib.LOGGER.info(" No ore in bottom layer");
            return false;
        }
        // Layers 0 & 1 Secondary and Sporadic
        for (level = tMinY; level < tMinY + 2; level++) {
            for (int tX = wX; tX < eX; tX++) {
                int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
                for (int tZ = nZ; tZ < sZ; tZ++) {
                    int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                    if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                        pos.set(tX, level, tZ);
                        if (setOre(world, pos, world.getBlockState(pos), vein.secondary(), GTMaterialTypes.ORE))
                            placeCount[1]++;
                    } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                        pos.set(tX, level, tZ);
                        if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                            placeCount[3]++;
                    }
                }
            }
        }
        // Layer 2 is Secondary, in-between, and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(2) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Between are reduce by 1/2 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.between(), GTMaterialTypes.ORE))
                        placeCount[2]++;
                } else if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.secondary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        level++; // Increment level to next layer
        // Layer 3 is In-between, and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(2) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Between are reduce by 1/2 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.between(), GTMaterialTypes.ORE))
                        placeCount[2]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        level++; // Increment level to next layer
        // Layer 4 is In-between, Primary and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(2) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Between are reduce by 1/2 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.between(), GTMaterialTypes.ORE))
                        placeCount[2]++;
                } else if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.primary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        level++; // Increment level to next layer
        // Layer 5 is In-between, Primary and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(2) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Between are reduce by 1/2 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.between(), GTMaterialTypes.ORE))
                        placeCount[2]++;
                } else if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.primary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        level++; // Increment level to next layer
        // Layer 6 is Primary and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.primary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        level++; // Increment level to next layer
        // Layer 7 is Primary and sporadic
        for (int tX = wX; tX < eX; tX++) {
            int placeX = Math.max(1, Math.max(Mth.abs(wXVein - tX), Mth.abs(eXVein - tX)) / localDensity);
            for (int tZ = nZ; tZ < sZ; tZ++) {
                int placeZ = Math.max(1, Math.max(Mth.abs(sZVein - tZ), Mth.abs(nZVein - tZ)) / localDensity);
                if (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0) {
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.primary(), GTMaterialTypes.ORE))
                        placeCount[1]++;
                } else if (rand.nextInt(7) == 0 && (rand.nextInt(placeZ) == 0 || rand.nextInt(placeX) == 0)) {  // Sporadics are reduce by 1/7 to compensate
                    pos.set(tX, level, tZ);
                    if (setOre(world, pos, world.getBlockState(pos), vein.sporadic(), GTMaterialTypes.ORE))
                        placeCount[3]++;
                }
            }
        }
        if (Ref.debugOreVein)
            GTLib.LOGGER.info(" wXVein" + wXVein + " eXVein" + eXVein + " nZVein" + nZVein + " sZVein" + sZVein + " locDen=" + localDensity
                    + " Den=" + vein.density() + " Sec=" + placeCount[1] + " Spo=" + placeCount[3] + " Bet=" + placeCount[2] + " Pri=" + placeCount[0]);
        return true;
    }

    public static long getOreVeinSeed(WorldGenLevel world, long oreSeedX, long oreSeedZ) {
        return world.getSeed() << 16 ^ ((world.getLevel().dimension().location().hashCode() & 0xffL) << 56 | (oreSeedX & 0x000000000fffffffL) << 28 | oreSeedZ & 0x000000000fffffffL);
    }

    public static List<Tuple<Integer, Integer>> getVeinSeeds(int chunkX, int chunkZ) {
        // Determine bounding box on how far out to check for ore veins affecting this chunk
        int westX = chunkX - (GTLibConfig.ORE_VEIN_MAX_SIZE.get() / 16);
        int eastX = chunkX + (GTLibConfig.ORE_VEIN_MAX_SIZE.get() / 16 + 1); // Need to add 1 since it is compared using a <
        int northZ = chunkZ - (GTLibConfig.ORE_VEIN_MAX_SIZE.get() / 16);
        int southZ = chunkZ + (GTLibConfig.ORE_VEIN_MAX_SIZE.get() / 16 + 1);
        List<Tuple<Integer, Integer>> res = new ObjectArrayList<>();
        // Search for oreVein seeds and add to the list;
        for (int x = westX; x < eastX; x++) {
            for (int z = northZ; z < southZ; z++) {
                if (((Math.abs(x) % 3) == 1) && ((Math.abs(z) % 3) == 1)) { //Determine if this X/Z is an oreVein seed
                    res.add(new Tuple<>(x, z));
                }
            }
        }
        return res;
    }

    @Override
    public void build(ResourceLocation name, Biome.ClimateSettings climate, Biome.BiomeCategory category, BiomeSpecialEffects effects, BiomeGenerationSettings.Builder gen, MobSpawnSettings.Builder spawns) {
        gen.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, GTLibConfiguredFeatures.VEINS);
    }
}
