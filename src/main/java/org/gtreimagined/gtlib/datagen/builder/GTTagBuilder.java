package org.gtreimagined.gtlib.datagen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class GTTagBuilder<T> {
    public TagBuilder builder;
    public final ResourceKey<Registry<T>> registry;
    public final List<T> removeElements = new ArrayList<>();
    private final String source;
    boolean replace = false;
    private final Function<T, ResourceKey<T>> keyExtractor;

    public GTTagBuilder(TagBuilder builder, ResourceKey<Registry<T>> registry, String string, @Nullable Function<T, ResourceKey<T>> keyExtractor) {
        this.builder = builder;
        this.registry = registry;
        this.source = string;
        this.keyExtractor = keyExtractor;
    }

    public GTTagBuilder<T> add(T item) {
        if (keyExtractor != null) {
            return add(keyExtractor.apply(item));
        }
        return this;
    }

    public GTTagBuilder<T> add(ResourceKey<T> key) {
        this.builder.addElement(key.location());
        return this;
    }

    public GTTagBuilder<T> add(TagEntry builderEntry){
        this.builder.add(builderEntry);
        return this;
    }

    public GTTagBuilder<T> add(ResourceKey<T>... resourceKeys) {
        for(ResourceKey<T> resourceKey : resourceKeys) {
            this.builder.addElement(resourceKey.location());
        }

        return this;
    }

    public GTTagBuilder<T> add(ResourceLocation... ids) {
        for(ResourceLocation id : ids) {
            this.builder.addElement(id);
        }
        return this;
    }

    public GTTagBuilder<T> addOptional(ResourceLocation location) {
        this.builder.addOptionalElement(location);
        return this;
    }

    public GTTagBuilder<T> addTag(TagKey<T> tag) {
        this.builder.addTag(tag.location());
        return this;
    }

    public GTTagBuilder<T> addTag(ResourceLocation tag){
        this.builder.addTag(tag);
        return this;
    }

    public GTTagBuilder<T> addOptionalTag(ResourceLocation location) {
        this.builder.addOptionalTag(location);
        return this;
    }

    @SafeVarargs
    public final GTTagBuilder<T> add(T... toAdd) {
        if (keyExtractor != null) {
            Stream.of(toAdd).map(keyExtractor).forEach(key -> this.builder.addElement(key.location()));
        }
        return this;
    }

    @SafeVarargs
    public final GTTagBuilder<T> remove(T... remove){
        removeElements.addAll(Arrays.asList(remove));
        return this;
    }

    public GTTagBuilder<T> replace() {
        return replace(true);
    }

    public GTTagBuilder<T> replace(boolean value) {
        replace = value;
        return this;
    }

    public GTTagBuilder<T> addFromJson(JsonObject json, String source) {
        if (json.get("replace").getAsBoolean()) {
            builder = new TagBuilder();
        }
        JsonArray array = json.getAsJsonArray("values");
        if (!array.isEmpty()) {
            for (JsonElement element : array) {
                if (element.isJsonPrimitive()){
                    String entry = element.getAsString();
                    ResourceLocation id = new ResourceLocation(entry.replace("#", "").replace("?", ""));
                    if (entry.startsWith("#")) {
                        if (entry.endsWith("?")) {
                            addOptionalTag(id);
                        } else {
                            addTag(id);
                        }
                    } else {
                        if (entry.endsWith("?")) {
                            addOptional(id);
                        } else {
                            add(id);
                        }
                    }
                }
            }
        }
        return this;
    }

    public JsonObject serializeToJson() {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (TagEntry entry : builder.build()){
            jsonArray.add(entry.toString());
        }

        jsonObject.addProperty("replace", replace);
        jsonObject.add("values", jsonArray);
        return jsonObject;
    }
}
