package org.gtreimagined.gtlib.integration.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.event.MaterialEvent;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;

public class AMMaterialEvent extends EventJS {
    final MaterialEvent event;
    public AMMaterialEvent(MaterialEvent event){
        this.event = event;
    }

    public MaterialEvent getEvent() {
        return event;
    }

    public MaterialType type(String type) {
        return GTAPI.get(MaterialType.class, type);
    }

    public void setReplacement(String material, String item, MaterialType type){
        Material material1 = Material.get(material);
        if (material1 == Material.NULL){
            return;
        }
        if (!RegistryUtils.itemExists(new ResourceLocation(item))){
            return;
        }
        type.replacement(material1, () -> RegistryUtils.getItemFromID(new ResourceLocation(item)));
    }
}
