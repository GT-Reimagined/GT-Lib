package org.gtreimagined.gtlib.datagen.providers;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.builder.GTItemModelBuilder;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.io.IOException;

public class GTItemModelProvider extends GTModelProvider<GTItemModelBuilder> implements IGTLibProvider {

    protected final String providerName;

    public GTItemModelProvider(String providerDomain, String providerName) {
        super(providerDomain, ITEM_FOLDER, GTItemModelBuilder::new);
        this.providerName = providerName;
    }

    @Override
    public void run(HashCache cache) throws IOException {

    }

    @Override
    public String getName() {
        return providerName;
    }

    @Override
    public void run() {
        registerModels();
    }

    @Override
    public void onCompletion() {
        buildAll();
    }

    protected void registerModels() {
        processItemModels(modid);
    }

    public void processItemModels(String domain) {
        AntimatterAPI.all(Item.class, domain).forEach(i -> GTLibModelManager.onItemModelBuild(i, this));
        AntimatterAPI.all(Block.class, domain).forEach(b -> GTLibModelManager.onItemModelBuild(b, this));
        AntimatterAPI.all(GTFluid.class, domain).forEach(f -> {
            modelAndTexture(f.getContainerItem(), "forge", "item/bucket").bucketProperties(f.getFluid());
            modelAndTexture(f.getFluidBlock(), GTBlockModelBuilder.getSimple()).tex(a -> a.put("all", f.getAttributes().getFlowingTexture().toString()));
        });
    }

    public GTItemModelBuilder getBuilder(ItemLike item) {
        return getBuilder(RegistryUtils.getIdFromItem(item.asItem()).getPath());
    }

    public GTItemModelBuilder tex(ItemLike item, ResourceLocation... textures) {
        return tex(item, "minecraft:item/generated", textures);
    }

    public GTItemModelBuilder tex(ItemLike item, String parent, ResourceLocation... textures) {
        GTItemModelBuilder builder = getBuilder(item);
        builder.parent(new ResourceLocation(parent));
        for (int i = 0; i < textures.length; i++) {
            builder.texture("layer" + i, textures[i]);
        }
        return builder;
    }

    public GTItemModelBuilder blockItem(Block block) {
        return blockItem(block.asItem());
    }

    public GTItemModelBuilder blockItem(ItemLike item) {
        return withParent(RegistryUtils.getIdFromItem(item.asItem()).getPath(), modLoc("block/" + RegistryUtils.getIdFromItem(item.asItem()).getPath()));
    }

    public ResourceLocation existing(String domain, String path) {
        return new ResourceLocation(domain, path);
    }

    public GTItemModelBuilder getAntimatterBuilder(ItemLike item) {
        return getBuilder(RegistryUtils.getIdFromItem(item.asItem()).getPath());
    }

    public GTItemModelBuilder modelAndTexture(ItemLike item, String namespace, String path) {
        return getAntimatterBuilder(item).parent(new ResourceLocation(namespace, path));
    }

    public GTItemModelBuilder modelAndTexture(ItemLike item, String resource) {
        return getAntimatterBuilder(item).parent(new ResourceLocation(resource));
    }
}
