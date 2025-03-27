package org.gtreimagined.gtlib.util;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class RegistryUtils {
    private static ImmutableMap<Item, Integer> FUEL_LIST = null;

    public static Map<Item, Integer> getAllBurnables(){
        if (FUEL_LIST == null){
            ForgeHooks.updateBurns();
            ImmutableMap.Builder<Item, Integer> builder = ImmutableMap.builder();
            ForgeRegistries.ITEMS.getValues().forEach(i -> {
                int burnTime = ForgeHooks.getBurnTime(i.getDefaultInstance(), null);
                if (burnTime > 0){
                    builder.put(i, burnTime);
                }
            });
            FUEL_LIST = builder.build();
        }
        return FUEL_LIST;
    }

    public static boolean blockExists(ResourceLocation id){
        return ForgeRegistries.BLOCKS.containsKey(id);
    }

    public static boolean itemExists(ResourceLocation id){
        return ForgeRegistries.ITEMS.containsKey(id);
    }

    public static boolean fluidExists(ResourceLocation id){
        return ForgeRegistries.FLUIDS.containsKey(id);
    }

    public static Block getBlockFromId(ResourceLocation id){
        return ForgeRegistries.BLOCKS.getValue(id);
    }

    public static Item getItemFromID(ResourceLocation id){
        return ForgeRegistries.ITEMS.getValue(id);
    }

    public static Fluid getFluidFromID(ResourceLocation id){
        return ForgeRegistries.FLUIDS.getValue(id);
    }

    public static ResourceLocation getIdFromBlock(Block block){
        return ForgeRegistries.BLOCKS.getKey(block);
    }

    public static ResourceLocation getIdFromItem(Item item){
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static ResourceLocation getIdFromFluid(Fluid fluid){
        return ForgeRegistries.FLUIDS.getKey(fluid);
    }

    public static Block getBlockFromId(String domain, String id){
        return getBlockFromId(new ResourceLocation(domain, id));
    }

    public static Item getItemFromID(String domain, String id){
        return getItemFromID(new ResourceLocation(domain, id));
    }
}
