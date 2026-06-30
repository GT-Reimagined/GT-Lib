package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

public class StoneLayerCollisionData extends BaseWorldGenData<StoneLayerCollision> {
    public static final StoneLayerCollisionData INSTANCE = new StoneLayerCollisionData();
    public StoneLayerCollisionData() {
        super("stone_layer_collisions", "stone_layer_collisons");
    }

    @Override
    protected Codec<StoneLayerCollision> getCodec() {
        return StoneLayerCollision.CODEC;
    }
}
