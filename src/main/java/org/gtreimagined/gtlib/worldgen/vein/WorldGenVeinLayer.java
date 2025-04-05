package org.gtreimagined.gtlib.worldgen.vein;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import lombok.Getter;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.util.XSTR;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import org.gtreimagined.gtlib.worldgen.VeinLayerResult;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import org.gtreimagined.gtlib.worldgen.object.WorldGenBase;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.worldgen.VeinLayerResult.*;
import static org.gtreimagined.gtlib.worldgen.WorldGenHelper.setOre;

/**
 * Most of the WorldGenVeinLayer code is from the GTNewHorizons GT5 fork, refactored for 1.12 and somewhat optimised
 * Written in 1.7 by moronwmachinegun and mitchej123, adapted by Muramasa
 **/
public class WorldGenVeinLayer extends WorldGenBase<WorldGenVeinLayer> {

    public static int TOTAL_WEIGHT;


    private Material[] materials;
    private String primary, secondary, between, sporadic;
    @Getter
    private final int minY;
    @Getter
    private final int maxY;
    @Getter
    private final int weight;
    private final int density;
    private final int size;

    WorldGenVeinLayer(String id, int minY, int maxY, int weight, int density, int size, Material primary, Material secondary, Material between, Material sporadic, List<ResourceKey<Level>> dimensions) {
        super(id, WorldGenVeinLayer.class, dimensions);
        this.minY = minY;
        this.maxY = maxY;
        this.weight = weight;
        this.density = density;
        this.size = size;
        this.materials = new Material[]{primary, secondary, between, sporadic};
        if (primary != null && primary != Material.NULL) {
            this.primary = primary.getId();
            this.secondary = secondary.getId();
            this.between = between.getId();
            this.sporadic = sporadic.getId();
        }
        TOTAL_WEIGHT += weight;
    }

    public static void resetTotalWeight(){
        TOTAL_WEIGHT = 0;
    }

    @Override
    public WorldGenVeinLayer onDataOverride(JsonObject json) {
        super.onDataOverride(json);
        //if (json.has("primary")) primary = Utils.parseString(dataMap.get("primary"), primary);
        //if (json.has("secondary")) secondary = Utils.parseString(dataMap.get("secondary"), secondary);
        //if (json.has("between")) between = Utils.parseString(dataMap.get("between"), between);
        //if (json.has("sporadic")) sporadic = Utils.parseString(dataMap.get("sporadic"), sporadic);
        //if (json.has("minY")) minY = Utils.parseInt(dataMap.get("minY"), minY);
        //if (json.has("maxY")) maxY = Utils.parseInt(dataMap.get("maxY"), maxY);
        //if (json.has("weight")) weight = Utils.parseInt(dataMap.get("weight"), weight);
        //if (json.has("density")) density = Utils.parseInt(dataMap.get("density"), density);
        //if (json.has("size")) size = Utils.parseInt(dataMap.get("size"), size);
        return this;
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
        json.addProperty("density", density);
        json.addProperty("size", size);
        json.addProperty("primary", primary);
        json.addProperty("secondary", secondary);
        json.addProperty("between", between);
        json.addProperty("sporadic", sporadic);
        JsonArray array2 = new JsonArray();
        getDimensions().forEach(r -> array2.add(r.toString()));
        if (!array2.isEmpty()){
            json.add("dims", array2);
        }
        return json;
    }

    public static WorldGenVeinLayer fromJson(String id, JsonObject json){
        List<ResourceKey<Level>> dims = new ArrayList<>();
        if (json.has("dims")){
            JsonArray array = json.getAsJsonArray("dims");
            array.forEach(j -> {
                if (j instanceof JsonPrimitive object){
                    dims.add(ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(object.getAsString())));
                }
            });
        }
        return new WorldGenVeinLayer(
                id,
                json.has("minY") ? json.get("minY").getAsInt() : Integer.MIN_VALUE,
                json.has("maxY") ? json.get("maxY").getAsInt() : Integer.MAX_VALUE,
                json.get("weight").getAsInt(),
                json.get("density").getAsInt(),
                json.get("size").getAsInt(),
                Material.get(json.get("primary").getAsString()),
                Material.get(json.get("secondary").getAsString()),
                Material.get(json.get("between").getAsString()),
                Material.get(json.get("sporadic").getAsString()),
                dims
        );
    }



    public Material getMaterial(int i) {
        return materials[i];
    }

    public static int getTotalWeight() {
        return TOTAL_WEIGHT;
    }

}
