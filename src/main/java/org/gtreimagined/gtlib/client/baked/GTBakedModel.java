package org.gtreimagined.gtlib.client.baked;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.Objects;

public abstract class GTBakedModel<T> implements IGTBakedModel {

    protected TextureAtlasSprite particle;
    protected boolean onlyGeneralQuads = false; //If the model only has "general quads", like pipes

    public GTBakedModel(TextureAtlasSprite p) {
        this.particle = Objects.requireNonNull(p, "Missing particle texture in GTBakedModel");
    }


    public void onlyGeneralQuads() {
        this.onlyGeneralQuads = true;
    }

    @Override
    public boolean hasOnlyGeneralQuads() {
        return onlyGeneralQuads;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return particle;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(BlockAndTintGetter level, BlockPos pos) {
        return getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }
}
