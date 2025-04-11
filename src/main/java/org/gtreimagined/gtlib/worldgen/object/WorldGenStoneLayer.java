package org.gtreimagined.gtlib.worldgen.object;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.Getter;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.worldgen.StoneLayerOre;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Getter
public class WorldGenStoneLayer extends WorldGenBase<WorldGenStoneLayer> {

    @Nullable
    private StoneType stoneType;
    private BlockState stoneState;
    private StoneLayerOre[] ores = new StoneLayerOre[0];
    private int minY, maxY, weight;

    protected WorldGenStoneLayer(String id, @Nullable StoneType stoneType, BlockState state, int weight, int minY, int maxY, List<ResourceKey<Level>> dims) {
        super(id, WorldGenStoneLayer.class, dims);
        if (state == null || state.isAir())
            throw new IllegalStateException("WorldGenStoneLayer has been passed a null stone block state!");
        this.stoneState = state;
        this.stoneType = stoneType;
        this.minY = minY;
        this.maxY = maxY;
        this.weight = weight;
    }

    protected WorldGenStoneLayer addOres(StoneLayerOre... ores) {
        this.ores = ores;
        return this;
    }

    static List<WorldGenStoneLayer> getFlat(String id, int weight, int minY, int maxY, @Nullable StoneType stoneType, @Nullable BlockState stoneState, StoneLayerOre[] ores, List<ResourceKey<Level>> dimensions) {
        return IntStream.range(0, weight).mapToObj(i -> {
            return new WorldGenStoneLayer(
                    id,
                    stoneType,
                    stoneState,
                    1,
                    minY,
                    maxY,
                    dimensions
            ).addOres(ores);
        }).collect(Collectors.toList());
    }

    static List<WorldGenStoneLayer> getFlat(WorldGenStoneLayer vein){
        return getFlat(vein.getId(), vein.weight, vein.minY, vein.maxY, vein.stoneType, vein.stoneState, vein.ores, vein.getDimensions().stream().map(r -> ResourceKey.create(Registry.DIMENSION_REGISTRY, r)).toList());
    }

    public JsonObject toJson(){
        JsonObject json = new JsonObject();
        json.addProperty("weight", weight);
        if (minY > Integer.MIN_VALUE) {
            json.addProperty("minY", minY);
        }
        if (maxY < Integer.MAX_VALUE) {
            json.addProperty("maxY", maxY);
        }
        if (stoneType != null){
            json.addProperty("stoneType", stoneType.getId());
        }
        json.addProperty("stoneState", RegistryUtils.getIdFromBlock(stoneState.getBlock()).toString());
        JsonArray array = new JsonArray();
        if (!array.isEmpty()) {
            json.add("ores", array);
        }
        JsonArray array2 = new JsonArray();
        getDimensions().forEach(r -> array2.add(r.toString()));
        if (!array2.isEmpty()){
            json.add("dims", array2);
        }
        return json;
    }

    public static WorldGenStoneLayer fromJson(String id, JsonObject json){
        List<StoneLayerOre> ores = new ArrayList<>();
        List<ResourceKey<Level>> dims = new ArrayList<>();
        if (json.has("dims")){
            JsonArray array = json.getAsJsonArray("dims");
            array.forEach(j -> {
                if (j instanceof JsonPrimitive object){
                    dims.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(object.getAsString())));
                }
            });
        }
        BlockState fill = json.has("fill") ? RegistryUtils.getBlockFromId(new ResourceLocation(json.get("fill").getAsString())).defaultBlockState() : null;
        StoneType stoneType = null;
        if (json.has("stoneType")){
            stoneType = StoneType.get(json.get("stoneType").getAsString());
            if (stoneType == null) throw new IllegalStateException("stone type: " + json.get("stoneType").getAsString() + " does not exist!");
        }
        BlockState stoneState = RegistryUtils.getBlockFromId(new ResourceLocation(json.get("stoneState").getAsString())).defaultBlockState();
        WorldGenStoneLayer stoneLayer = new WorldGenStoneLayer(
                id,
                stoneType,
                stoneState,
                json.get("weight").getAsInt(),
                json.has("minY") ? json.get("minY").getAsInt() : Integer.MIN_VALUE,
                json.has("maxY") ? json.get("maxY").getAsInt() : Integer.MAX_VALUE,
                dims
        );
        if (!ores.isEmpty()) {
            stoneLayer.addOres(ores.toArray(new StoneLayerOre[0]));
        }
        return stoneLayer;
    }
}