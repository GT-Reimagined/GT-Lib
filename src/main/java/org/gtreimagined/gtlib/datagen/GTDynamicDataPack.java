package org.gtreimagined.gtlib.datagen;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.server.packs.resources.IoSupplier;
import org.gtreimagined.gtlib.GTLib;
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;

//@ParametersAreNonnullByDefault
public class GTDynamicDataPack implements PackResources {

    protected static final ObjectSet<String> SERVER_DOMAINS = new ObjectOpenHashSet<>();
    protected static final GTDynamicPackContents CONTENTS = new GTDynamicPackContents();

    private final String name;

    static {
        SERVER_DOMAINS.addAll(Sets.newHashSet(Ref.ID, Ref.SHARED_ID, "minecraft", "forge", "c"));
    }

    public GTDynamicDataPack(String name, Collection<String> domains) {
        this.name = name;
        SERVER_DOMAINS.addAll(domains);
    }

    public static void clearServer() {
        CONTENTS.clearData();
    }

    private static void addToData(ResourceLocation location, byte[] bytes) {
        CONTENTS.addToData(location, bytes);
    }

    public static void addWorldgenObject(IWorldgenObject<?> object) {
        Path parent = FMLPaths.CONFIGDIR.get().getParent()
                .resolve("dumped/gtlib-dynamic-data/data");
        if (GTLibConfig.EXPORT_DEFAULT_DATA_AND_ASSETS.get()){
            writeJson(object.getLoc(), "gt_worldgen/" + object.getSubDirectory() + "/", parent, object.toJson());
        }
        addData(getWorldgenLoc(object.getLoc(), object.getSubDirectory()), object.toJson());
    }

    public static void addRecipe(FinishedRecipe recipe) {
        JsonObject recipeJson = recipe.serializeRecipe();
        Path parent = FMLPaths.CONFIGDIR.get().getParent()
                .resolve("dumped/gtlib-dynamic-data/data");
        if (GTLibConfig.EXPORT_DEFAULT_DATA_AND_ASSETS.get()){
            writeJson(recipe.getId(), "recipes", parent, recipeJson);
        }
        addData(getRecipeLoc(recipe.getId()), recipeJson);
        if (recipe.serializeAdvancement() != null) {
            JsonObject advancement = recipe.serializeAdvancement();
            if (GTLibConfig.EXPORT_DEFAULT_DATA_AND_ASSETS.get()){
                writeJson(recipe.getAdvancementId(), "advancements", parent, advancement);
            }
            addData(getAdvancementLoc(Objects.requireNonNull(recipe.getAdvancementId())), advancement);
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
        addData(l, obj);
    }

    public static void addData(ResourceLocation loc, JsonObject obj) {
        addToData(loc, obj.toString().getBytes());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... elements) {
        if (elements.length > 0 && elements[0].equals("pack.png")) {
            return () -> GTLib.class.getResourceAsStream("/assets/gtlib/icon.png");
        }
        return null;
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type == PackType.SERVER_DATA) {
            return CONTENTS.getResource(location);
        } else {
            return null;
        }
    }

    @Override
    public void listResources(PackType packType, String namespace, String path, ResourceOutput resourceOutput) {
        if (packType == PackType.SERVER_DATA) {
            CONTENTS.listResources(namespace, path, resourceOutput);
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? SERVER_DOMAINS : Set.of();
    }

    @Override
    public String packId() {
        return name;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metaReader) {
        String metadataSection = metaReader.getMetadataSectionName();
        if(metadataSection.equals("pack")) {
            JsonObject object = new JsonObject();
            object.addProperty("pack_format", 9);
            object.addProperty("description", "runtime data pack");
            return metaReader.fromJson(object);
        }
        if (metadataSection.equals("filter")){
            return null;
        }
        return metaReader.fromJson(new JsonObject());
    }

    @Override
    public boolean isBuiltin() {
       return true;
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
