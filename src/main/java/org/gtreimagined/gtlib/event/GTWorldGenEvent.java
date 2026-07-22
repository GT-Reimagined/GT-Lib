package org.gtreimagined.gtlib.event;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.registration.IGTRegistrar;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerCollision;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerOre;
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVein;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayer;
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVein;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GTWorldGenEvent extends GTEvent {

    public final List<Vein> VEINS = new ObjectArrayList<>();
    public final List<StoneLayer> STONE_LAYERS = new ObjectArrayList<>();

    public final List<SmallOre> SMALL_ORES = new ObjectArrayList<>();

    public final List<VanillaVein> VANILLA_ORES = new ObjectArrayList<>();
    public final List<BedrockVein> BEDROCK_VEINS = new ObjectArrayList<>();
    public final List<StoneLayerCollision> COLLISIONS = new ObjectArrayList<>();

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

    public void stoneLayer(StoneLayer veins) {
        STONE_LAYERS.add(veins);
    }

    public void smallOre(SmallOre veins) {
        if (SMALL_ORES.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate small ore spawn, aborting. Id: " + veins.getId());
            return;
        }
        SMALL_ORES.add(veins);
    }

    public void vanillaOre(VanillaVein veins) {
        if (VANILLA_ORES.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate vanilla ore vein, aborting. Id: " + veins.getId());
            return;
        }
        VANILLA_ORES.add(veins);
    }

    public void bedrockOre(BedrockVein veins) {
        if (BEDROCK_VEINS.stream().anyMatch(s -> s.getId().equals(veins.getId()))){
            GTLib.LOGGER.warn("Duplicate bedrock vein, aborting. Id: " + veins.getId());
            return;
        }
        BEDROCK_VEINS.add(veins);
    }

    public void addCollisionTopBottom(ResourceLocation id, BlockState top, BlockState bottom, StoneLayerOre... oresToAdd) {
        COLLISIONS.add(new StoneLayerCollision(id, top, bottom, List.of(oresToAdd)));
    }

    public void addCollisionBothSides(ResourceLocation baseID, BlockState first, BlockState second, StoneLayerOre... oresToAdd) {
        COLLISIONS.add(new StoneLayerCollision(new ResourceLocation(baseID.getNamespace(), baseID.getPath() + "_top"), first, second, List.of(oresToAdd)));
        COLLISIONS.add(new StoneLayerCollision(new ResourceLocation(baseID.getNamespace(), baseID.getPath() + "_bottom"), second, first, List.of(oresToAdd)));
    }
}
