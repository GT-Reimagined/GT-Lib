package org.gtreimagined.gtlib.recipe;

import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class RecipeUtil {
    public static JsonObject itemstackToJson(ItemStack stack){
        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("item", Registry.ITEM.getKey(stack.getItem()).toString());
        if (stack.getCount() > 1) {
            resultObj.addProperty("count", stack.getCount());
        }
        if (stack.hasTag()) {
            CompoundTag nbt = stack.getTag();
            resultObj.addProperty("nbt", nbt.toString());
        }
        return resultObj;
    }

    public static JsonObject fluidstackToJson(FluidStack stack){
        JsonObject object = new JsonObject();
        object.addProperty("fluid", RegistryUtils.getIdFromFluid(stack.getFluid()).toString());
        object.addProperty("amount", stack.getAmount());
        if (stack.getTag() != null){
            object.addProperty("tag", stack.getTag().toString());
        }
        return object;
    }
}
