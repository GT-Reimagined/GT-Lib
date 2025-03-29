package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.registration.IGTObject;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelLoader;

public interface IGTModelLoader<T extends IGTModel<T>> extends IGTObject, IModelLoader<T> {
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
