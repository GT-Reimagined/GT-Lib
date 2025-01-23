package muramasa.antimatter.recipe.serializer;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.Antimatter;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.recipe.BaseRecipeSerializer;
import muramasa.antimatter.recipe.IRecipe;
import muramasa.antimatter.recipe.RecipeTag;
import muramasa.antimatter.recipe.RecipeUtil;
import muramasa.antimatter.recipe.ingredient.FluidIngredient;
import muramasa.antimatter.recipe.ingredient.RecipeIngredient;
import muramasa.antimatter.recipe.map.RecipeMap;
import muramasa.antimatter.util.AntimatterPlatformUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tesseract.FluidPlatformUtils;
import tesseract.TesseractGraphWrappers;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AntimatterRecipeSerializer<T extends IRecipe> extends BaseRecipeSerializer<T> implements IAntimatterRecipeSerializer<T> {

    protected AntimatterRecipeSerializer(String domain, String id){
        super(domain, id);
    }

    @Override
    public abstract RecipeType<T> getRecipeType();

    public abstract T createRecipe(@NotNull List<Ingredient> stacksInput, ItemStack[] stacksOutput, @NotNull List<FluidIngredient> fluidsInput, FluidHolder[] fluidsOutput, int duration, long power, int special, int amps);

    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject json) {
        try {
            String mapId = json.get("map").getAsString();
            RecipeMap<?> map = AntimatterAPI.get(RecipeMap.class, mapId);
            if (map == null) throw new IllegalStateException("Recipe map: " + mapId + " is unknown");
            if (map.getRecipeSerializer().getRecipeType() != this.getRecipeType()) throw new IllegalStateException("Recipe map: " + mapId + " doesn't use recipe type: " + this.getRecipeType());
            List<Ingredient> list = new ObjectArrayList<>();
            if (json.has("inputItems")) {
                JsonArray array = json.getAsJsonArray("inputItems");
                for (JsonElement element : array) {
                    list.add(RecipeIngredient.fromJson(element));
                }
            }
            ItemStack[] outputs = null;
            if (json.has("outputItems")) {
                outputs = Streams.stream(json.getAsJsonArray("outputItems")).map(t -> RecipeUtil.INSTANCE.getItemStack(t.getAsJsonObject(), true)).toArray(ItemStack[]::new);
            }
            List<FluidIngredient> fluidInputs = new ObjectArrayList<>();
            if (json.has("inputFluids")) {
                JsonArray array = json.getAsJsonArray("inputFluids");
                for (JsonElement element : array) {
                    fluidInputs.add(getFluidIngredient(element));
                }
            }
            FluidHolder[] fluidOutputs = null;
            if (json.has("outputFluids")) {
                fluidOutputs = Streams.stream(json.getAsJsonArray("outputFluids")).map(AntimatterRecipeSerializer::getStack).toArray(FluidHolder[]::new);
            }
            long eut = json.get("eu").getAsLong();
            int duration = json.get("duration").getAsInt();
            int amps = json.has("amps") ? json.get("amps").getAsInt() : 1;
            int special = json.has("special") ? json.get("special").getAsInt() : 0;
            T r = createRecipe(list, outputs, fluidInputs, fluidOutputs, duration, eut, special, amps);
            if (json.has("outputChances")) {
                List<Integer> chances = new ObjectArrayList<>();
                for (JsonElement el : json.getAsJsonArray("outputChances")) {
                    chances.add(el.getAsInt());
                }
                r.addOutputChances(chances.stream().mapToInt(i -> i).toArray());
            }
            if (json.has("inputChances")) {
                List<Integer> chances = new ObjectArrayList<>();
                for (JsonElement el : json.getAsJsonArray("inputChances")) {
                    chances.add(el.getAsInt());
                }
                r.addInputChances(chances.stream().mapToInt(i -> i).toArray());
            }
            r.setHidden(json.get("hidden").getAsBoolean());
            r.setFake(json.get("fake").getAsBoolean());
            if (json.has("tags")){
                JsonArray array = json.getAsJsonArray("tags");
                Set<RecipeTag> tags = Streams.stream(array).map(e -> {
                    String[] strings = e.getAsString().split(":", 1);
                    return AntimatterAPI.get(RecipeTag.class, strings[1], strings[0]);
                }).collect(Collectors.toSet());
                r.addTags(tags);
            }
            r.setIds(recipeId, mapId);
            return r;
        } catch (Exception ex) {
            Antimatter.LOGGER.error(ex);
            Antimatter.LOGGER.error(json.toString());
        }
        return null;
    }
    public static FluidHolder getStack(JsonElement element) {
        try {
            if (!(element.isJsonObject())) {
                return FluidHooks.emptyFluid();
            }
            JsonObject obj = (JsonObject) element;
            ResourceLocation fluidName = new ResourceLocation(obj.get("fluid").getAsString());
            Fluid fluid = AntimatterPlatformUtils.INSTANCE.getFluidFromID(fluidName);
            if (fluid == null) {
                return FluidHooks.emptyFluid();
            }
            FluidHolder stack = FluidPlatformUtils.createFluidStack(fluid, obj.has("amount") ? obj.get("amount").getAsLong() : 1000 * TesseractGraphWrappers.dropletMultiplier);

            if (obj.has("tag")) {
                stack.setCompound(TagParser.parseTag(obj.get("tag").getAsString()));
            }
            return stack;
        } catch (Exception ex) {
            Antimatter.LOGGER.error(ex);
        }
        return FluidHooks.emptyFluid();
    }

    public static FluidIngredient getFluidIngredient(JsonElement element) {
        try {
            if (!(element.isJsonObject())) {
                return FluidIngredient.EMPTY;
            }
            JsonObject obj = (JsonObject) element;
            if (obj.has("fluidTag")) {
                ResourceLocation tagType = new ResourceLocation(obj.get("tag").getAsString());
                long amount = obj.has("amount") ? obj.get("amount").getAsLong() : 1000 * TesseractGraphWrappers.dropletMultiplier;
                return FluidIngredient.of(tagType, amount);
            }
            return FluidIngredient.of(getStack(element));
        } catch (Exception ex) {
            Antimatter.LOGGER.error(ex);
        }
        return FluidIngredient.EMPTY;
    }

    @Nullable
    @Override
    public T fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer) {
        String mapId = buffer.readUtf();
        int size = buffer.readInt();
        List<Ingredient> ings = new ObjectArrayList<>(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                ings.add(RecipeUtil.INSTANCE.fromNetwork(buffer));
            }
        }
        size = buffer.readInt();
        ItemStack[] out = new ItemStack[size];
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                out[i] = buffer.readItem();
            }
        }
        size = buffer.readInt();
        List<FluidIngredient> in = new ObjectArrayList<>(size);
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                in.add(FluidIngredient.of(buffer));
            }
        }
        size = buffer.readInt();
        FluidHolder[] outf = new FluidHolder[size];
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                outf[i] = FluidPlatformUtils.INSTANCE.readFromPacket(buffer);
            }
        }
        size = buffer.readInt();
        int[] outputChances = new int[size];
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                outputChances[i] = buffer.readInt();
            }
        }
        size = buffer.readInt();
        int[] inputChances = new int[size];
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                inputChances[i] = buffer.readInt();
            }
        }
        long power = buffer.readLong();
        int dur = buffer.readInt();
        int special = buffer.readInt();
        int amps = buffer.readInt();
        boolean hidden = buffer.readBoolean();
        boolean fake = buffer.readBoolean();

        T r = createRecipe(
                ings,
                out.length == 0 ? null : out,
                in,
                outf.length == 0 ? null : outf,
                dur,
                power,
                special,
                amps
        );
        if (outputChances.length > 0)
            r.addOutputChances(outputChances);
        if (inputChances.length > 0){
            r.addInputChances(inputChances);
        }
        r.setIds(recipeId, mapId);
        r.setHidden(hidden);
        r.setFake(fake);
        return r;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {
        buffer.writeUtf(recipe.getMapId());
        buffer.writeInt(!recipe.hasInputItems() ? 0 : recipe.getInputItems().size());
        if (recipe.hasInputItems()) {
            recipe.getInputItems().forEach(t -> RecipeUtil.INSTANCE.write(buffer, t));
        }
        buffer.writeInt(!recipe.hasOutputItems() ? 0 : recipe.getOutputItems(false).length);
        if (recipe.hasOutputItems()) {
            Arrays.stream(recipe.getOutputItems(false)).forEach(buffer::writeItem);
        }
        buffer.writeInt(!recipe.hasInputFluids() ? 0 : recipe.getInputFluids().size());
        if (recipe.hasInputFluids()) {
            recipe.getInputFluids().stream().forEach(t -> t.write(buffer));
        }
        buffer.writeInt(!recipe.hasOutputFluids() ? 0 : recipe.getOutputFluids().length);
        if (recipe.hasOutputFluids()) {
            Arrays.stream(recipe.getOutputFluids()).forEach(stack -> FluidPlatformUtils.INSTANCE.writeToPacket(buffer, stack));
        }
        buffer.writeInt(recipe.hasOutputChances() ? recipe.getOutputChances().length : 0);
        if (recipe.hasOutputChances()) {
            Arrays.stream(recipe.getOutputChances()).forEach(buffer::writeInt);
        }
        buffer.writeInt(recipe.hasInputChances() ? recipe.getInputChances().length : 0);
        if (recipe.hasInputChances()) {
            Arrays.stream(recipe.getInputChances()).forEach(buffer::writeInt);
        }
        buffer.writeLong(recipe.getPower());
        buffer.writeInt(recipe.getDuration());
        buffer.writeInt(recipe.getSpecialValue());
        buffer.writeInt(recipe.getAmps());
        buffer.writeBoolean(recipe.isHidden());
        buffer.writeBoolean(recipe.isFake());
    }

    @Override
    public void toJson(JsonObject json, IRecipe recipe) {
        json.addProperty("recipeID", recipe.getId().toString());
        json.addProperty("map", recipe.getMapId());
        JsonArray array = new JsonArray();
        for (Ingredient ingredient : recipe.getInputItems()) {
            array.add(ingredient.toJson());
        }
        if (!array.isEmpty()){
            json.add("inputItems", array);
        }
        array = new JsonArray();
        if (recipe.getOutputItems(false) != null){
            for (ItemStack stack : recipe.getOutputItems(false)){
                array.add(RecipeUtil.INSTANCE.itemstackToJson(stack));
            }
        }
        if (!array.isEmpty()){
            json.add("outputItems", array);
        }
        array = new JsonArray();
        for (FluidIngredient f : recipe.getInputFluids()) {
            array.add(f.toJson());
        }
        if (!array.isEmpty()){
            json.add("inputFluids", array);
        }
        array = new JsonArray();
        if (recipe.getOutputFluids() != null){
            for (FluidHolder stack : recipe.getOutputFluids()){
                array.add(RecipeUtil.fluidstackToJson(stack));
            }
        }
        if (!array.isEmpty()){
            json.add("outputFluids", array);
        }
        array = new JsonArray();
        json.addProperty("eu", recipe.getPower());
        json.addProperty("duration", recipe.getDuration());
        json.addProperty("amps", recipe.getAmps());
        json.addProperty("special", recipe.getSpecialValue());
        if (recipe.hasOutputChances()) {
            for (int d : recipe.getOutputChances()){
                array.add(d);
            }
        }
        if (!array.isEmpty()){
            json.add("outputChances", array);
        }

        if (recipe.hasInputChances()) {
            for (int d : recipe.getInputChances()){
                array.add(d);
            }
        }
        if (!array.isEmpty()){
            json.add("inputChances", array);
        }
        json.addProperty("hidden", recipe.isHidden());
        json.addProperty("fake", recipe.isFake());
        array = new JsonArray();
        for (RecipeTag tag : recipe.getTags()){
            array.add(tag.getLoc().toString());
        }
        if (!array.isEmpty()){
            json.add("tags", array);
        }
    }
}
