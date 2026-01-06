package org.gtreimagined.gtlib.client.quad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class RetexturedBakedQuad extends BakedQuad {
    private final TextureAtlasSprite sprite;

    public RetexturedBakedQuad(BakedQuad original, TextureAtlasSprite sprite) {
        super(original.getVertices(), original.getTintIndex(), original.getDirection(), sprite, original.isShade());
        this.sprite = sprite;
    }

    @Override
    public TextureAtlasSprite getSprite() {
        return sprite;
    }
}
