package org.gtreimagined.gtlib.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.model.loader.DefaultModelLoader;
import org.gtreimagined.gtlib.client.model.loader.DynamicModelLoader;
import org.gtreimagined.gtlib.client.model.loader.MachineModelLoader;
import org.gtreimagined.gtlib.client.model.loader.FallbackModelLoader;
import org.gtreimagined.gtlib.client.model.loader.PipeFullModelLoader;
import org.gtreimagined.gtlib.client.model.loader.PipeModelLoader;
import org.gtreimagined.gtlib.client.model.loader.ProxyModelLoader;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class GTLibModelManager {

    private static final Object2ObjectOpenHashMap<String, Supplier<Int2ObjectOpenHashMap<BakedModel[]>>> STATIC_CONFIG_MAPS = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, IItemProviderOverride> ITEM_OVERRIDES = new Object2ObjectOpenHashMap<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, IBlockProviderOverride> BLOCK_OVERRIDES = new Object2ObjectOpenHashMap<>();

    public static final ResourceLocation LOADER_MAIN = new ResourceLocation(Ref.ID, "main");
    public static final ResourceLocation LOADER_COVER = new ResourceLocation(Ref.ID, "cover");

    public static final ResourceLocation LOADER_MACHINE_SIDE = new ResourceLocation(Ref.ID, "machine_side");


    public static final ResourceLocation LOADER_DYNAMIC = new ResourceLocation(Ref.ID, "dynamic");
    public static final ResourceLocation LOADER_MACHINE = new ResourceLocation(Ref.ID, "machine");
    public static final ResourceLocation LOADER_PIPE_FULL = new ResourceLocation(Ref.ID, "pipe_full");
    public static final ResourceLocation LOADER_PIPE = new ResourceLocation(Ref.ID, "pipe");

    public static final ResourceLocation LOADER_PROXY = new ResourceLocation(Ref.ID, "proxy");

    public static final ResourceLocation LOADER_FALLBACK = new ResourceLocation(Ref.ID, "fallback");

    public static void init() {
        new DefaultModelLoader(LOADER_MAIN);
        new MachineModelLoader.CoverModelLoader(LOADER_COVER);
        new MachineModelLoader.SideModelLoader(LOADER_MACHINE_SIDE);
        new DynamicModelLoader(LOADER_DYNAMIC);
        new MachineModelLoader(LOADER_MACHINE);
        new PipeFullModelLoader(LOADER_PIPE_FULL);
        new PipeModelLoader(LOADER_PIPE);
        new ProxyModelLoader(LOADER_PROXY);
        new FallbackModelLoader(LOADER_FALLBACK);
    }

    public static void registerStaticConfigMap(String staticMapId, Supplier<Int2ObjectOpenHashMap<BakedModel[]>> configMapSupplier) {
        STATIC_CONFIG_MAPS.put(staticMapId, configMapSupplier);
    }

    public static Int2ObjectOpenHashMap<BakedModel[]> getStaticConfigMap(String staticMapId) {
        return STATIC_CONFIG_MAPS.getOrDefault(staticMapId, Int2ObjectOpenHashMap::new).get();
    }

    public static void put(Item item, IItemProviderOverride override) {
        ITEM_OVERRIDES.put(RegistryUtils.getIdFromItem(item), override);
    }

    public static void put(Block block, IBlockProviderOverride override) {
        BLOCK_OVERRIDES.put(RegistryUtils.getIdFromBlock(block), override);
    }

    public static void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        IItemProviderOverride override = ITEM_OVERRIDES.get(RegistryUtils.getIdFromItem(item.asItem()));
        if (override != null) override.apply(item.asItem(), prov);
        else if (item instanceof IModelProvider) ((IModelProvider) item).onItemModelBuild(item, prov);
    }

    public static void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        IBlockProviderOverride override = BLOCK_OVERRIDES.get(RegistryUtils.getIdFromBlock(block));
        if (override != null) override.apply(block, prov, prov.getBuilder(block));
        else if (block instanceof IModelProvider) ((IModelProvider) block).onBlockModelBuild(block, prov);
    }

    public interface IItemProviderOverride {
        void apply(ItemLike item, GTItemModelProvider prov);
    }

    public interface IBlockProviderOverride {
        void apply(Block block, GTBlockStateProvider stateProv, GTBlockModelBuilder modelBuilder);
    }
}
