package org.gtreimagined.gtlib.datagen;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSerializer;
import dev.latvian.mods.kubejs.script.ScriptType;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.json.JGTLibModel;
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider;
import org.gtreimagined.gtlib.datagen.providers.GTLanguageProvider;
import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider;
import org.gtreimagined.gtlib.datagen.providers.GTTagProvider;
import org.gtreimagined.gtlib.event.GTCraftingEvent;
import org.gtreimagined.gtlib.event.GTLoaderEvent;
import org.gtreimagined.gtlib.event.GTProvidersEvent;
import org.gtreimagined.gtlib.event.GTWorldGenEvent;
import org.gtreimagined.gtlib.integration.kubejs.AMWorldEvent;
import org.gtreimagined.gtlib.integration.kubejs.KubeJSRegistrar;
import org.gtreimagined.gtlib.integration.kubejs.RecipeLoaderEventKubeJS;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.loader.IRecipeRegistrate;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeBuilder;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.registration.IGTRegistrar;
import org.gtreimagined.gtlib.registration.ModRegistrar;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerOre;
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVein;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayer;
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVein;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.loot.JCondition;
import net.devtech.arrp.json.models.JTextures;
import net.devtech.arrp.util.UnsafeByteArrayOutputStream;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GTLibDynamics {
    public static final RuntimeResourcePack DYNAMIC_RESOURCE_PACK = RuntimeResourcePack.create(new ResourceLocation(Ref.ID, "dynamic"));
    public static final RuntimeResourcePack RUNTIME_DATA_PACK = RuntimeResourcePack.create(new ResourceLocation(Ref.ID, "data"), 8);
    public static final Gson GSON = Deserializers.createLootTableSerializer()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .registerTypeAdapter(Advancement.Builder.class, (JsonSerializer<Advancement.Builder>) (src, typeOfSrc, context) -> src.serializeToJson())
            .registerTypeAdapter(FinishedRecipe.class, (JsonSerializer<FinishedRecipe>) (src, typeOfSrc, context) -> src.serializeRecipe())
            .registerTypeAdapter(JGTLibModel.class, new JGTLibModel.JAntimatterModelSerializer())
            .registerTypeAdapter(JTextures.class, new JTextures.Serializer())
            .registerTypeAdapter(JCondition.class, new JCondition.Serializer())
            .create();
    private static final boolean exportPack = true;

    private static boolean initialized = false;

    public static final Set<ResourceLocation> RECIPE_IDS = Sets.newHashSet();

    public static final Consumer<FinishedRecipe> FINISHED_RECIPE_CONSUMER = f -> {
        if (RECIPE_IDS.add(f.getId())){
            DynamicDataPack.addRecipe(f);
        } else {
            GTLib.LOGGER.catching(new RuntimeException("Recipe duplicated: " + f.getId()));
        }
    };

    private static final Object2ObjectOpenHashMap<String, List<Supplier<IGTLibProvider>>> PROVIDERS = new Object2ObjectOpenHashMap<>();

    public static void addResourcePacks(Consumer<PackResources> function){
        function.accept(DYNAMIC_RESOURCE_PACK);
    }

    public static void setInitialized(){
        initialized = true;
    }

    public static void addDataPacks(Consumer<PackResources> function){
        if (initialized) {
            GTLibDynamics.onResourceReload(FMLEnvironment.dist.isDedicatedServer());
        }
        function.accept(RUNTIME_DATA_PACK);
        function.accept(new DynamicDataPack("gtlib:recipes", GTAPI.all(IGTRegistrar.class).stream().map(IGTRegistrar::getDomain).collect(Collectors.toSet())));

    }

    /**
     * Providers and Dynamic Resource Pack Section
     **/
    public static void clientProvider(String domain, Supplier<IGTLibProvider> providerFunc) {
        PROVIDERS.computeIfAbsent(domain, k -> new ObjectArrayList<>()).add(providerFunc);
    }

    public static void runDataProvidersDynamically() {
        GTBlockLootProvider.init();
        GTProvidersEvent ev = new GTProvidersEvent(GTLib.INSTANCE);
        ModLoader.get().postEvent(ev);
        Collection<IGTLibProvider> providers = ev.getProviders();
        long time = System.currentTimeMillis();
        Stream<IGTLibProvider> async = providers.stream().filter(IGTLibProvider::async).parallel();
        Stream<IGTLibProvider> sync = providers.stream().filter(t -> !t.async());
        Stream.concat(async, sync).forEach(IGTLibProvider::run);
        providers.forEach(IGTLibProvider::onCompletion);
        GTTagProvider.afterCompletion();
        GTBlockLootProvider.afterCompletion();
        GTLib.LOGGER.info("Time to run data providers: " + (System.currentTimeMillis() - time) + " ms.");
        if (GTLibConfig.EXPORT_DEFAULT_RECIPES.get() || !FMLEnvironment.production) {
            RUNTIME_DATA_PACK.dump(FMLPaths.CONFIGDIR.get().getParent().resolve("dumped"));
        }
    }

    public static void runAssetProvidersDynamically() {
        List<IGTLibProvider> providers = PROVIDERS.object2ObjectEntrySet().stream()
                .flatMap(v -> v.getValue().stream().map(Supplier::get)).toList();
        long time = System.currentTimeMillis();
        Stream<IGTLibProvider> async = providers.stream().filter(IGTLibProvider::async).parallel();
        Stream<IGTLibProvider> sync = providers.stream().filter(t -> !t.async());
        Stream.concat(async, sync).forEach(IGTLibProvider::run);
        providers.forEach(IGTLibProvider::onCompletion);
        GTLanguageProvider.postCompletion();
        GTLib.LOGGER.info("Time to run asset providers: " + (System.currentTimeMillis() - time) + " ms.");
        if (!FMLEnvironment.production) {
            DYNAMIC_RESOURCE_PACK.dump(FMLPaths.CONFIGDIR.get().getParent().resolve("dumped"));
        }
    }

    /**
     * Collects all antimatter registered recipes, pushing them to @rec.
     *
     * @param rec consumer for IFinishedRecipe.
     */
    public static void collectRecipes(GTRecipeProvider provider, Consumer<FinishedRecipe> rec) {
        GTCraftingEvent event = new GTCraftingEvent(GTLib.INSTANCE);
        ModLoader.get().postEvent(event);
        for (ICraftingLoader loader : event.getLoaders()) {
            loader.loadRecipes(rec, provider);
        }
    }

    public static void onRecipeManagerBuild(Consumer<FinishedRecipe> objectIn) {
        GTLib.LOGGER.info("GTLib recipe manager running..");
        collectRecipes(new GTRecipeProvider(Ref.ID, "provider"), objectIn);
        GTAPI.all(ModRegistrar.class, t -> {
            for (String mod : t.modIds()) {
                if (!GTAPI.isModLoaded(mod))
                    return;
            }
            t.craftingRecipes(new GTRecipeProvider(Ref.ID, "Custom recipes"));
        });
        GTLib.LOGGER.info("GTLib recipe manager done..");
    }

    public static void onRecipeCompile(boolean server, RecipeManager manager) {
        GTLib.LOGGER.info("Compiling GT recipes");
        long time = System.nanoTime();

        for (RecipeMap<?> m : GTAPI.all(RecipeMap.class)) {
            if (m.getProxy() != null) {
                List<net.minecraft.world.item.crafting.Recipe<?>> recipes = (List<net.minecraft.world.item.crafting.Recipe<?>>) manager.getAllRecipesFor(m.getProxy().loc());
                recipes.forEach(recipe -> m.compileRecipe(m.getProxy().handler().apply(recipe, m.RB())));
            }
        }

        for (RecipeType<? extends IRecipe> recipeType : RecipeMap.getRecipeTypes()) {
            List<? extends IRecipe> recipes = manager.getAllRecipesFor(recipeType);
            Map<String, List<IRecipe>> map = recipes.stream().collect(Collectors.groupingBy(IRecipe::getMapId));

            for (Map.Entry<String, List<IRecipe>> entry : map.entrySet()) {
                String[] split = entry.getKey().split(":");
                String name;
                if (split.length == 2) {
                    name = split[1];
                } else if (split.length == 1) {
                    name = split[0];
                } else {
                    continue;
                }
                IRecipeMap rmap = GTAPI.get(IRecipeMap.class, name);
                if (rmap != null){
                    entry.getValue().forEach(rmap::compileRecipe);
                    //entry.getValue().forEach(rmap::add);
                    //rmap.compile(manager);
                }
            }
        }


        time = System.nanoTime() - time;
        int size = GTAPI.all(IRecipeMap.class).stream().mapToInt(t -> t.getRecipes(false).size()).sum();

        GTLib.LOGGER.info("Time to compile GT recipes: (ms) " + (time) / (1000 * 1000));
        GTLib.LOGGER.info("No. of GT recipes: " + size);
        GTLib.LOGGER.info("Average loading time / recipe: (µs) " + (size > 0 ? time / size : time) / 1000);

        /*
         * GTAPI.all(RecipeMap.class, t -> {
         * GTLib.LOGGER.info("Recipe map " + t.getId() + " compiled " +
         * t.getRecipes(false).size() + " recipes."); });
         */
        // Invalidate old tag getter.
       // TagUtils.resetSupplier();
    }
    /**
     * Reloads dynamic assets during resource reload.
     */
    public static void onResourceReload(boolean serverEvent) {
        GTRecipeProvider provider = new GTRecipeProvider(Ref.ID, "provider");
        DynamicDataPack.clearServer();
        RECIPE_IDS.clear();
        collectRecipes(provider , FINISHED_RECIPE_CONSUMER);
        GTAPI.all(RecipeMap.class, RecipeMap::reset);
        final Set<ResourceLocation> filter;
        if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
            if (serverEvent) KubeJSRegistrar.checkKubeJSServerScriptManager();
            RecipeLoaderEventKubeJS ev = RecipeLoaderEventKubeJS.createAndPost(serverEvent);
            filter = ev.forLoaders;
        } else {
            filter = Collections.emptySet();
        }
        Map<ResourceLocation, IRecipeRegistrate.IRecipeLoader> loaders = new Object2ObjectOpenHashMap<>(30);
        MinecraftForge.EVENT_BUS.post(new GTLoaderEvent(GTLib.INSTANCE, (a, b, c) -> {
            if (filter.contains(new ResourceLocation(a, b)))
                return;
            if (loaders.put(new ResourceLocation(a, b), c) != null) {
                GTLib.LOGGER.warn("Duplicate recipe loader: " + new ResourceLocation(a, b));
            }
        }));
        List<Vein> veins = new ObjectArrayList<>();
        List<StoneLayer> stoneLayers = new ObjectArrayList<>();
        List<SmallOre> smallOres = new ObjectArrayList<>();
        List<VanillaVein> vanillaVeins = new ObjectArrayList<>();
        List<BedrockVein> bedrockVeins = new ObjectArrayList<>();
        Int2ObjectOpenHashMap<List<StoneLayerOre>> collisionMap = new Int2ObjectOpenHashMap<>();
        boolean runRegular = true;
        if (GTAPI.isModLoaded(Ref.MOD_KJS) && serverEvent) {
            AMWorldEvent ev = new AMWorldEvent();
            ev.post(ScriptType.SERVER, "gtlib.worldgen");
            veins.addAll(ev.VEINS);
            stoneLayers.addAll(ev.STONE_LAYERS);
            collisionMap.putAll(ev.COLLISION_MAP);
            runRegular = !ev.disableBuiltin;
        }
        if (runRegular) {
            GTWorldGenEvent ev = new GTWorldGenEvent(GTLib.INSTANCE);
            MinecraftForge.EVENT_BUS.post(ev);
            veins.addAll(ev.VEINS);
            smallOres.addAll(ev.SMALL_ORES);
            stoneLayers.addAll(ev.STONE_LAYERS);
            vanillaVeins.addAll(ev.VANILLA_ORES);
            bedrockVeins.addAll(ev.BEDROCK_VEINS);
            ev.COLLISION_MAP.forEach((i, l) -> {
                collisionMap.computeIfAbsent(i, i2 -> new ArrayList<>()).addAll(l);
            });
        }
        GTLibWorldGenerator.clear();
        for (Vein vein : veins) {
            DynamicDataPack.addWorldgenObject(vein);
        }
        for (StoneLayer stoneLayer : stoneLayers) {
            DynamicDataPack.addWorldgenObject(stoneLayer);
        }
        StoneLayer.setCollisionMap(collisionMap);
        for (SmallOre smallOre : smallOres){
            DynamicDataPack.addWorldgenObject(smallOre);
        }
        for (VanillaVein vanillaVein : vanillaVeins){
            DynamicDataPack.addWorldgenObject(vanillaVein);
        }
        for (BedrockVein vein : bedrockVeins){
            DynamicDataPack.addWorldgenObject(vein);
        }
        if (GTLibConfig.REGENERATE_DEFAULT_WORLDGEN_JSONS.get()) {
            GTLibConfig.REGENERATE_DEFAULT_WORLDGEN_JSONS.set(false);
            GTLibConfig.CONFIG_COMMON.save();
            GTLibConfig.CONFIG_COMMON.reload();
        }
        loaders.forEach((r, l) -> {
            RecipeBuilder.setCurrentModId(r.getNamespace());
            l.init();
            RecipeBuilder.setCurrentModId(Ref.SHARED_ID);
        });
        GTAPI.all(ModRegistrar.class, t -> {
            for (String mod : t.modIds()) {
                if (!GTAPI.isModLoaded(mod))
                    return;
            }
            // t.antimatterRecipes(GTAPI.getRecipeRegistrate(Ref.ID));
        });

        GTLib.LOGGER.info("Amount of GTLib Recipe Loaders registered: " + loaders.size());
    }

    public static ResourceLocation getTagLoc(String identifier, ResourceLocation tagId) {
        return new ResourceLocation(tagId.getNamespace(), String.join("", identifier, "/", tagId.getPath()));
    }

    public static byte[] serialize(Object object) {
        UnsafeByteArrayOutputStream ubaos = new UnsafeByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(ubaos);
        GSON.toJson(object, writer);
        try {
            writer.close();
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
        return ubaos.getBytes();
    }

    public static ResourceLocation fix(ResourceLocation identifier, String prefix, String append) {
        return new ResourceLocation(identifier.getNamespace(), prefix + '/' + identifier.getPath() + '.' + append);
    }
    /*
     * public static void runBackgroundProviders() {
     * GTLib.LOGGER.info("Running DummyTagProviders...");
     * Ref.BACKGROUND_GEN.addProviders(DummyTagProviders.DUMMY_PROVIDERS); try {
     * Ref.BACKGROUND_GEN.run(); } catch (IOException e) { e.printStackTrace(); } }
     */
}
