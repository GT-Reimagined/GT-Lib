package org.gtreimagined.gtlib.worldgen.stonelayer;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StoneLayerData extends BaseWorldGenData<StoneLayer> {
    public static final StoneLayerData INSTANCE = new StoneLayerData();
    private StoneLayerData() {
        super("stone_layers", "stone_layer");
    }

    @Override
    protected Codec<StoneLayer> getCodec() {
        return StoneLayer.CODEC;
    }

    public List<StoneLayer> getFlat(Level level) {
        List<StoneLayer> result = new ArrayList<>();
        Collection<StoneLayer> layers = getVeins(level).values();
        for (StoneLayer layer : layers) {
            for (int i = 0; i < layer.weight(); i++){
                result.add(layer);
            }
        }
        return result;
    }
}
