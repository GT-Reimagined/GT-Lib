package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

import java.util.ArrayList;
import java.util.List;

public class StoneLayerCollisionData extends BaseWorldGenData<StoneLayerCollision> {
    public static final StoneLayerCollisionData INSTANCE = new StoneLayerCollisionData();
    public StoneLayerCollisionData() {
        super("stone_layer_collisions", "stone_layer_collisons");
    }

    @Override
    protected Codec<StoneLayerCollision> getCodec() {
        return StoneLayerCollision.CODEC;
    }

    public List<StoneLayerOre> getCollisions(Level level, BlockState top, BlockState bottom){
        var collisions = getVeins(level);
        if (collisions.isEmpty()) return List.of();
        List<StoneLayerCollision> matching = collisions.values().stream().filter(c -> c.top() == top && c.bottom() == bottom).toList();
        if (matching.isEmpty()) return List.of();
        List<StoneLayerOre> ores = new ArrayList<>();
        for (StoneLayerCollision collision : matching){
            ores.addAll(collision.ores());
        }
        return ores;
    }
}
