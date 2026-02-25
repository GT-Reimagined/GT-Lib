package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.event.MaterialEvent;
import org.gtreimagined.gtlib.material.MaterialTags;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.List;

import static com.google.common.collect.ImmutableMap.of;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.material.MaterialTags.MOLTEN;
import static org.gtreimagined.gtlib.material.MaterialTags.QUARTZ_LIKE_BLOCKS;
import static net.minecraft.world.item.Tiers.*;

public class MaterialDataInit {
    public static void onMaterialEvent(MaterialEvent<?> event){
        event.setMaterial(GTLibMaterials.Stone).asDust(ROCK);
        event.setMaterial(GTLibMaterials.Granite).asDust(GTMaterialTypes.ROCK);
        event.setMaterial(GTLibMaterials.Diorite).asDust(GTMaterialTypes.ROCK);
        event.setMaterial(GTLibMaterials.Andesite).asDust(GTMaterialTypes.ROCK);
        event.setMaterial(GTLibMaterials.Deepslate).asDust(GTMaterialTypes.ROCK);
        event.setMaterial(GTLibMaterials.Tuff).asDust(GTMaterialTypes.ROCK);

        event.setMaterial(GTLibMaterials.Sand).asDust();
        event.setMaterial(GTLibMaterials.RedSand).asDust();
        event.setMaterial(GTLibMaterials.Blackstone).asDust(GTMaterialTypes.ROCK);

        event.setMaterial(GTLibMaterials.Endstone).asDust();
        event.setMaterial(GTLibMaterials.Netherrack).asDust();
        event.setMaterial(GTLibMaterials.Prismarine).asDust();
        event.setMaterial(GTLibMaterials.DarkPrismarine).asDust();

        event.setMaterial(GTLibMaterials.Wood).asDust(PLATE, MaterialTags.RUBBERTOOLS, MaterialTags.WOOD, MaterialTags.MINED_WITH_AXE)
                .tool().toolDamage(WOOD.getAttackDamageBonus()).toolSpeed(WOOD.getSpeed()).toolDurability(192).toolQuality(WOOD.getLevel()).allowedToolTypes(List.of(GTTools.SOFT_HAMMER)).build();
        event.setMaterial(GTLibMaterials.Lava).asFluid(0, 1300);
        event.setMaterial(GTLibMaterials.Water).asFluid();
    }
}
