package org.gtreimagined.gtlib.worldgen;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.teamresourceful.resourcefullib.common.lib.Constants;
import com.teamresourceful.resourcefullib.common.networking.PacketHelper;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.GTLib;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseWorldGenData<T extends IWorldgenObject<T>> extends SimpleJsonResourceReloadListener {
    protected final Map<ResourceLocation, T> VEINS = new Object2ObjectOpenHashMap<>();
    private final String name;
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public BaseWorldGenData(String subDirectory, String name) {
        super(GSON, "gt_worldgen/" + subDirectory);
        this.name = name;
    }

    protected abstract Codec<T> getCodec();

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        Map<ResourceLocation, T> layers = new HashMap<>();
        for (var entry : map.entrySet()) {
            JsonObject vein = GsonHelper.convertToJsonObject(entry.getValue(), name);
            vein.addProperty("id", entry.getKey().toString());
            layers.put(entry.getKey(), getCodec().parse(JsonOps.INSTANCE, vein).getOrThrow(false, GTLib.LOGGER::error));
        }
        updateVeins(layers);
    }

    public void updateVeins(Map<ResourceLocation, T> veins) {
        clear();
        VEINS.putAll(veins);
    }

    protected void clear() {
        VEINS.clear();
    }

    public Map<ResourceLocation, T> getVeins() {
        return VEINS;
    }

    public Map<ResourceLocation, T> getVeins(Level level){
        return VEINS.entrySet().stream().filter(v -> v.getValue().getDimensions().contains(level.dimension())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    public void encodeVeins(FriendlyByteBuf buf) {
        buf.writeVarInt(VEINS.size());
        VEINS.forEach((resourceLocation, vein) -> {
            buf.writeResourceLocation(resourceLocation);
            PacketHelper.writeWithYabn(buf, getCodec(), vein, true)
                    .get()
                    .mapRight(DataResult.PartialResult::message)
                    .ifRight(Constants.LOGGER::error);
        });

    }

    public Map<ResourceLocation, T> decodeVeins(FriendlyByteBuf buf) {
        Map<ResourceLocation, T> result = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            ResourceLocation key = buf.readResourceLocation();
            Optional<T> vein = PacketHelper.readWithYabn(buf, getCodec(), true)
                    .get()
                    .mapRight(DataResult.PartialResult::message)
                    .ifRight(Constants.LOGGER::error)
                    .left();
            if (vein.isPresent()) {
                result.put(key, vein.get());
            } else {
                GTLib.LOGGER.error(name + " packet errored", new NoSuchElementException("No value present"));
                return Collections.emptyMap();
            }
        }
        return result;
    }
}
