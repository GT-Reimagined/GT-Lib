package org.gtreimagined.gtlib.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.registration.IGTRegistrar;
import org.gtreimagined.gtlib.worldgen.StoneLayerOre;
import org.gtreimagined.gtlib.worldgen.bedrockore.WorldGenBedrockVein;
import org.gtreimagined.gtlib.worldgen.object.WorldGenStoneLayer;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import org.gtreimagined.gtlib.worldgen.vanillaore.WorldGenVanillaOre;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GTWorldGenEvent extends GTEvent {

    public final List<Vein> VEINS = new ObjectArrayList<>();
    public final List<WorldGenStoneLayer> STONE_LAYERS = new ObjectArrayList<>();

    public final List<SmallOre> SMALL_ORES = new ObjectArrayList<>();

    public final List<WorldGenVanillaOre> VANILLA_ORES = new ObjectArrayList<>();
    public final List<WorldGenBedrockVein> BEDROCK_VEINS = new ObjectArrayList<>();
    public final Int2ObjectOpenHashMap<List<StoneLayerOre>> COLLISION_MAP = new Int2ObjectOpenHashMap<>();

    public GTWorldGenEvent(IGTRegistrar registrar) {
        super(registrar);
    }

    public void vein(Vein veins) {
        if (veins.id() == null){
            GTLib.LOGGER.warn("Vein id is null, aborting.");
            return;
        }
        if (VEINS.stream().anyMatch(s -> s.equals(veins))){
            GTLib.LOGGER.warn("Duplicate vein layer spawn, aborting. Id: " + veins.id());
            return;
        }
        VEINS.add(veins);
    }

    public void stoneLayer(List<WorldGenStoneLayer> veins) {
        STONE_LAYERS.addAll(veins);
    }

    public void smallOre(SmallOre veins) {
        if (SMALL_ORES.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate small ore spawn, aborting. Id: " + veins.getId());
            return;
        }
        SMALL_ORES.add(veins);
    }

    public void vanillaOre(WorldGenVanillaOre veins) {
        if (VANILLA_ORES.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate vanilla ore vein, aborting. Id: " + veins.getId());
            return;
        }
        VANILLA_ORES.add(veins);
    }

    public void bedrockOre(WorldGenBedrockVein veins) {
        if (BEDROCK_VEINS.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate bedrock vein, aborting. Id: " + veins.getId());
            return;
        }
        BEDROCK_VEINS.add(veins);
    }

    public void addCollisionTopBottom(BlockState top, BlockState bottom, StoneLayerOre... oresToAdd) {
        COLLISION_MAP.computeIfAbsent(Objects.hash(top, bottom), k -> new ObjectArrayList<>()).addAll(Arrays.asList(oresToAdd));
    }

    public void addCollisionBothSides(BlockState first, BlockState second, StoneLayerOre... oresToAdd) {
        COLLISION_MAP.computeIfAbsent(Objects.hash(first, second), k -> new ObjectArrayList<>()).addAll(Arrays.asList(oresToAdd));
        COLLISION_MAP.computeIfAbsent(Objects.hash(second, first), k -> new ObjectArrayList<>()).addAll(Arrays.asList(oresToAdd));
    }
}
