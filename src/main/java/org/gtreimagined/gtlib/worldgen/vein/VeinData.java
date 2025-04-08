package org.gtreimagined.gtlib.worldgen.vein;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.teamresourceful.resourcefullib.common.lib.Constants;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.network.PacketHelper;
import org.gtreimagined.gtlib.network.packets.ClientboundWorldgenSyncPacket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class VeinData extends SimpleJsonResourceReloadListener {
    private static final Map<ResourceLocation, Vein> VEINS = new Object2ObjectOpenHashMap<>();
    static int TOTAL_WEIGHT = 0;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public VeinData() {
        super(GSON, "gt_worldgen/veins");
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

    public static void updateVeins(Map<ResourceLocation, Vein> veins) {
        clear();
        VEINS.putAll(veins);
        TOTAL_WEIGHT = veins.values().stream().mapToInt(Vein::weight).sum();
    }

    private static void clear() {
        VEINS.clear();
    }

    public static Map<ResourceLocation, Vein> getVeins() {
        return VEINS;
    }

    public static Map<ResourceLocation, Vein> getVeins(Level level){
        return VEINS.entrySet().stream().filter(v -> v.getValue().dimensions().contains(level.dimension())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public static int getTotalWeight(){
        return TOTAL_WEIGHT;
    }

    public static void encodeVeins(FriendlyByteBuf buf) {
        buf.writeVarInt(VEINS.size());
        VEINS.forEach((resourceLocation, vein) -> {
            buf.writeResourceLocation(resourceLocation);
            PacketHelper.writeWithYabn(buf, Vein.CODEC, vein, true)
                    .get()
                    .mapRight(DataResult.PartialResult::message)
                    .ifRight(Constants.LOGGER::error);
        });

    }

    public static Map<ResourceLocation, Vein> decodeVeins(FriendlyByteBuf buf) {
        Map<ResourceLocation, Vein> result = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            Optional<Vein> vein = PacketHelper.readWithYabn(buf, Vein.CODEC, true)
                    .get()
                    .mapRight(DataResult.PartialResult::message)
                    .ifRight(Constants.LOGGER::error)
                    .left();
            if (vein.isPresent()) {
                result.put(key, vein.get());
            } else {
                GTLib.LOGGER.error("Vein packet errored", new NoSuchElementException("No value present"));
                return Collections.emptyMap();
            }
        }
        return result;
    }
}
