package org.gtreimagined.gtlib.material.data;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.item.enchantment.Enchantment;

public record HandleData(int durability, float speed, ImmutableMap<Enchantment, Integer> toolEnchantment) {
}
