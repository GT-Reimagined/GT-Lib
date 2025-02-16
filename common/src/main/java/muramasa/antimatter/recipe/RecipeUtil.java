package muramasa.antimatter.recipe;

import com.google.gson.JsonObject;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import muramasa.antimatter.datagen.builder.AntimatterShapedRecipeBuilder;
import muramasa.antimatter.util.FluidPlatformUtils;
import muramasa.antimatter.util.ImplLoader;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

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

    public static JsonObject fluidstackToJson(FluidHolder stack){
        JsonObject object = new JsonObject();
        object.addProperty("fluid", FluidPlatformUtils.INSTANCE.getFluidId(stack.getFluid()).toString());
        object.addProperty("amount", stack.getFluidAmount());
        if (stack.getCompound() != null){
            object.addProperty("tag", stack.getCompound().toString());
        }
        return object;
    }
}
