package org.gtreimagined.gtlib.worldgen.vanillaore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

import java.util.HashMap;
import java.util.Map;

public class VanillaVeinData extends BaseWorldGenData<VanillaVein> {
    public static final VanillaVeinData INSTANCE = new VanillaVeinData();

    private VanillaVeinData() {
        super("vanilla_veins", "vanilla_vein");
    }

    @Override
    protected Codec<VanillaVein> getCodec() {
        return VanillaVein.CODEC;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, VanillaVein> layers = new HashMap<>();
        for (var entry : map.entrySet()) {
            JsonObject vein = GsonHelper.convertToJsonObject(entry.getValue(), "vanilla_vein");
            vein.addProperty("id", entry.getKey().toString());
            layers.put(entry.getKey(), VanillaVein.CODEC.parse(JsonOps.INSTANCE, vein).getOrThrow(false, GTLib.LOGGER::error));
        }
        updateVeins(layers);
    }
}
