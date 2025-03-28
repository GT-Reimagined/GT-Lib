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

        event.setMaterial(GTLibMaterials.Basalt).asDust(GTMaterialTypes.ROCK);
        event.setMaterial(GTLibMaterials.Endstone).asDust();
        event.setMaterial(GTLibMaterials.Netherrack).asDust();
        event.setMaterial(GTLibMaterials.Prismarine).asDust();
        event.setMaterial(GTLibMaterials.DarkPrismarine).asDust();

        event.setMaterial(GTLibMaterials.Iron).asMetal(1811, PICKAXE_HEAD, AXE_HEAD, SHOVEL_HEAD, HOE_HEAD, SWORD_BLADE).asOre(1, 5, true).tool().toolDamage(IRON.getAttackDamageBonus()).toolSpeed(IRON.getSpeed()).toolDurability(256).toolQuality(IRON.getLevel()).build();
        event.setMaterial(GTLibMaterials.Gold).asMetal(1337, PICKAXE_HEAD, AXE_HEAD, SHOVEL_HEAD, HOE_HEAD, SWORD_BLADE).asOre(1, 5, true).tool().toolDamage(GOLD.getAttackDamageBonus()).toolSpeed(GOLD.getSpeed()).toolDurability(GOLD.getUses()).toolQuality(GOLD.getLevel()).toolEnchantments(of(Enchantments.SMITE, 3)).build().harvestLevel(2);
        //cause 1.18
        event.setMaterial(GTLibMaterials.Copper).asMetal(1357).asOre(1, 5, true).harvestLevel(1);

        event.setMaterial(GTLibMaterials.Glowstone).asDust();
        event.setMaterial(GTLibMaterials.Sugar).asDust();
        event.setMaterial(GTLibMaterials.Wood).asDust(PLATE, MaterialTags.RUBBERTOOLS, MaterialTags.WOOD)
                .tool().toolDamage(WOOD.getAttackDamageBonus()).toolSpeed(WOOD.getSpeed()).toolDurability(192).toolQuality(WOOD.getLevel()).allowedToolTypes(List.of(GTTools.SOFT_HAMMER)).build();
        event.setMaterial(GTLibMaterials.Blaze).asDust().addHandleStat(-10, -0.5F, of(Enchantments.FIRE_ASPECT, 1));

        event.setMaterial(GTLibMaterials.Flint).asDust(GTMaterialTypes.GEM, MaterialTags.FLINT)
                .tool().toolDamage(1.25f).toolSpeed(STONE.getSpeed()).toolDurability(128).toolQuality(1)
                .toolEnchantments(of(Enchantments.FIRE_ASPECT, 1)).allowedToolTypes(List.of(GTTools.PICKAXE, GTTools.AXE, GTTools.SHOVEL, GTTools.SWORD, GTTools.HOE, GTTools.MORTAR, GTTools.KNIFE)).build();

        event.setMaterial(GTLibMaterials.Charcoal).asDust(GTMaterialTypes.BLOCK);
        event.setMaterial(GTLibMaterials.Coal).asGemBasic(false).asOre(0, 2, true, GTMaterialTypes.ORE_STONE);
        event.setMaterial(GTLibMaterials.Diamond).asGemBasic(false, PICKAXE_HEAD, AXE_HEAD, SHOVEL_HEAD, HOE_HEAD, SWORD_BLADE).asOre(3, 7, true)
                .tool().toolDamage(DIAMOND.getAttackDamageBonus()).toolSpeed(DIAMOND.getSpeed()).toolDurability(DIAMOND.getUses()).toolQuality(DIAMOND.getLevel()).build();
        event.setMaterial(GTLibMaterials.Emerald).asGemBasic(false).asOre(3, 7, true).harvestLevel(2);
        event.setMaterial(GTLibMaterials.EnderPearl).asGemBasic(false);
        event.setMaterial(GTLibMaterials.EnderEye).asGemBasic(false);
        event.setMaterial(GTLibMaterials.Lapis).asGemBasic(false).asOre(2, 5, true).harvestLevel(1);
        event.setMaterial(GTLibMaterials.Redstone).asOre(1, 5, true, MOLTEN).harvestLevel(2);
        event.setMaterial(GTLibMaterials.Quartz).asOre(1, 5, true, QUARTZ_LIKE_BLOCKS).harvestLevel(1);
        event.setMaterial(GTLibMaterials.Netherite).asMetal(2246, PLATE, ROD);
                //.addTools(3.0F, 10, 500, NETHERITE.getLevel(), of(Enchantments.FIRE_ASPECT, 3)).addArmor(new int[]{0, 1, 1, 0}, 0.5F, 0.1F, 20);
        event.setMaterial(GTLibMaterials.NetherizedDiamond).asGemBasic(false)
                .tool().toolDamage(4).toolSpeed(12).toolDurability(NETHERITE.getUses()).toolQuality(NETHERITE.getLevel()).toolEnchantments(of(Enchantments.FIRE_ASPECT, 3, Enchantments.SHARPNESS, 4)).build()
                .addArmor(new int[]{1, 1, 2, 1}, 3.0F, 0.1F, 37, of(Enchantments.ALL_DAMAGE_PROTECTION, 4));
        event.setMaterial(GTLibMaterials.NetheriteScrap).asDust(GTMaterialTypes.CRUSHED, GTMaterialTypes.RAW_ORE);

        event.setMaterial(GTLibMaterials.Lava).asFluid(0, 1300);
        event.setMaterial(GTLibMaterials.Water).asFluid();
    }
}
