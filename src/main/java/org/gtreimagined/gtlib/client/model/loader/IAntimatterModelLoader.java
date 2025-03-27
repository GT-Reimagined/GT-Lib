package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.client.IAntimatterModel;
import org.gtreimagined.gtlib.registration.IAntimatterObject;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public interface IAntimatterModelLoader<T extends IAntimatterModel<T>> extends IAntimatterObject, IModelLoader<T> {
    default int[] buildRotations(JsonObject e) {
        int[] rotations = new int[3];
        if (e.has("rotation") && e.get("rotation").isJsonArray()) {
            JsonArray array = e.get("rotation").getAsJsonArray();
            for (int i = 0; i < Math.min(rotations.length, array.size()); i++) {
                if (array.get(i).isJsonPrimitive() && array.get(i).getAsJsonPrimitive().isNumber()) {
                    rotations[i] = array.get(i).getAsJsonPrimitive().getAsInt();
                }
            }
        }
        return rotations;
    }

    @Override
    default void onResourceManagerReload(ResourceManager resourceManager){

    }
}
