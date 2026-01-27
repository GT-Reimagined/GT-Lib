package org.gtreimagined.gtlib.mixin.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import org.gtreimagined.gtlib.client.quad.ITextureReferenceBakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements ITextureReferenceBakedQuad {
    @Unique
    private String textureId = "";
    @Override
    public String gtLib$getTextureId() {
        return textureId;
    }

    @Override
    public void gtLib$setTextureId(String textureId) {
        this.textureId = textureId;
    }
}
