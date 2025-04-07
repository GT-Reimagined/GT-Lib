package org.gtreimagined.gtlib.datagen;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraftforge.fml.loading.FMLPaths;
import org.gtreimagined.gtlib.worldgen.IWorldgenObject;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//@ParametersAreNonnullByDefault
public class DynamicDataPack implements PackResources {

    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();
    protected static final Map<ResourceLocation, JsonObject> DATA = new HashMap<>();

    private final String name;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(Ref.ID, Ref.SHARED_ID, "minecraft", "forge", "c"));
    }

    public DynamicDataPack(String name, Collection<String> domains) {
        this.name = name;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        DATA.clear();
    }

    public static void addWorldgenObject(IWorldgenObject object) {
        Path parent = FMLPaths.CONFIGDIR.get().getParent()
                .resolve("dumped/gtlib-dynamic-data/data");
        if (GTLibConfig.EXPORT_DEFAULT_RECIPES.get()){
            writeJson(object.getLoc(), "gt_worldgen/" + object.getSubDirectory() + "/", parent, object.toJson());
        }
        DATA.put(getWorldgenLoc(object.getLoc(), object.getSubDirectory()), object.toJson());
    }

    public static void addRecipe(FinishedRecipe recipe) {
        JsonObject recipeJson = recipe.serializeRecipe();
        Path parent = FMLPaths.CONFIGDIR.get().getParent()
                .resolve("dumped/gtlib-dynamic-data/data");
        if (GTLibConfig.EXPORT_DEFAULT_RECIPES.get()){
            writeJson(recipe.getId(), "recipes", parent, recipeJson);
        }
        DATA.put(getRecipeLoc(recipe.getId()), recipeJson);
        if (recipe.serializeAdvancement() != null) {
            JsonObject advancement = recipe.serializeAdvancement();
            if (GTLibConfig.EXPORT_DEFAULT_RECIPES.get()){
                writeJson(recipe.getAdvancementId(), "advancements", parent, advancement);
            }
            DATA.put(getAdvancementLoc(Objects.requireNonNull(recipe.getAdvancementId())), advancement);
        }
    }

    private static void writeJson(ResourceLocation id, String subdir, Path parent, JsonObject json){
        try {
            Path file = parent.resolve(id.getNamespace()).resolve(subdir).resolve(id.getPath() + ".json");
            Files.createDirectories(file.getParent());
            try(OutputStream output = Files.newOutputStream(file)) {
                output.write(json.toString().getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addAdvancement(ResourceLocation loc, JsonObject obj) {
        ResourceLocation l = getAdvancementLoc(loc);
        synchronized (DATA) {
            DATA.put(l, obj);
        }
    }

    @Override
    public InputStream getResource(PackType type, ResourceLocation location) throws IOException {
        if (type == PackType.SERVER_DATA) {
            if (DATA.containsKey(location))
                return new ByteArrayInputStream(DATA.get(location).toString().getBytes(StandardCharsets.UTF_8));
            else throw new FileNotFoundException("Can't find " + location + " " + getName());
        } else {
            return new ByteArrayInputStream(new byte[0]);
        }
    }

    @Override
    public InputStream getRootResource(String fileName) {
        throw new UnsupportedOperationException("Dynamic Resource Pack cannot have root resources");
    }

    @Override
    public boolean hasResource(PackType type, ResourceLocation location) {
        if (type == PackType.CLIENT_RESOURCES) {
            return false;
        } else {
            return DATA.containsKey(location);
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType type, String namespace, String path, int maxDepth, Predicate<String> filter) {
        if (type == PackType.SERVER_DATA)
            return DATA.keySet().stream().filter(loc -> loc.getPath().startsWith(path) && filter.test(loc.getPath())).collect(Collectors.toList());
        return Collections.emptyList();//LANG.keySet().stream().filter(loc -> loc.getPath().startsWith(path) && filter.test(loc.getPath())).collect(Collectors.toList());
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? SERVER_DOMAINS : Set.of();
    }

    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        if(metaReader.getMetadataSectionName().equals("pack")) {
            JsonObject object = new JsonObject();
            object.addProperty("pack_format", 9);
            object.addProperty("description", "runtime data pack");
            return metaReader.fromJson(object);
        }
        return metaReader.fromJson(new JsonObject());
    }

    @Override
    public void close() {
        //NOOP
    }

    public static ResourceLocation getRecipeLoc(ResourceLocation recipeId) {
        return new ResourceLocation(recipeId.getNamespace(), String.join("", "recipes/", recipeId.getPath(), ".json"));
    }

    public static ResourceLocation getWorldgenLoc(ResourceLocation worldgenId, String subDirectory) {
        return new ResourceLocation(worldgenId.getNamespace(), String.join("", "gt_worldgen/", subDirectory, "/", worldgenId.getPath(), ".json"));
    }

    public static ResourceLocation getAdvancementLoc(ResourceLocation advancementId) {
        return new ResourceLocation(advancementId.getNamespace(), String.join("", "advancements/", advancementId.getPath(), ".json"));
    }

    public static ResourceLocation getTagLoc(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", "tags/", identifier, "/", tagId.getPath(), ".json"));
    }
}
