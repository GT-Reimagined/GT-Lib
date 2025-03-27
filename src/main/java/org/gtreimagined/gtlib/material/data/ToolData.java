package org.gtreimagined.gtlib.material.data;

import com.google.common.collect.ImmutableMap;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.tool.AntimatterToolType;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public record ToolData(float toolDamage, float toolSpeed, int toolDurability, int toolQuality, Material handleMaterial,
                       ImmutableMap<Enchantment, Integer> toolEnchantment, List<AntimatterToolType> toolTypes) {
}
