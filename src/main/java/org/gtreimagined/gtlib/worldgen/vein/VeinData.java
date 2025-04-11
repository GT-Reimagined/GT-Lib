package org.gtreimagined.gtlib.worldgen.vein;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

import java.util.Map;

public class VeinData extends BaseWorldGenData<Vein> {
    public static final VeinData INSTANCE = new VeinData();
    static int TOTAL_WEIGHT = 0;

    private VeinData() {
        super("veins", "vein");
    }

    @Override
    protected Codec<Vein> getCodec() {
        return Vein.CODEC;
    }

    @Override
    public void updateVeins(Map<ResourceLocation, Vein> veins) {
        super.updateVeins(veins);
        TOTAL_WEIGHT = veins.values().stream().mapToInt(Vein::weight).sum();
    }


    public static int getTotalWeight(){
        return TOTAL_WEIGHT;
    }
}
