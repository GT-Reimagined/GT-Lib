package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public interface IModelProvider {

    default void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        if (item instanceof Block) prov.blockItem(item);
        else if (item instanceof ITextureProvider) prov.tex(item, ((ITextureProvider) item).getTextures());
        else prov.getBuilder(item);
    }

    default void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        if (block instanceof ITextureProvider) prov.state(block, ((ITextureProvider) block).getTextures());
    }
}
