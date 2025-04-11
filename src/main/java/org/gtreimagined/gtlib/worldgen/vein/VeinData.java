package org.gtreimagined.gtlib.worldgen.vein;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

import java.util.HashMap;
import java.util.Map;

public class VeinData extends BaseWorldGenData<Vein> {
    public static final VeinData INSTANCE = new VeinData();
    static int TOTAL_WEIGHT = 0;

    public VeinData() {
        super("veins", "vein");
    }

    @Override
    protected Codec<Vein> getCodec() {
        return Vein.CODEC;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, Vein> layers = new HashMap<>();
        for (var entry : map.entrySet()) {
            JsonObject vein = GsonHelper.convertToJsonObject(entry.getValue(), "vein");
            vein.addProperty("id", entry.getKey().toString());
            layers.put(entry.getKey(), Vein.CODEC.parse(JsonOps.INSTANCE, vein).getOrThrow(false, GTLib.LOGGER::error));
        }
        updateVeins(layers);
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
