package org.gtreimagined.gtlib.mixin.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.client.quad.ITextureReferenceBakedQuad;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockModel.class)
public class BlockModelMixin {
    @Inject(method = "bakeFace", at = @At("RETURN"), cancellable = true)
    private static void gtlib$injectBakeFace(BlockElement part, BlockElementFace partFace, TextureAtlasSprite sprite, Direction direction, ModelState transform, ResourceLocation location, CallbackInfoReturnable<BakedQuad> cir){
        if (partFace != null) {
            BakedQuad original = cir.getReturnValue();
            ((ITextureReferenceBakedQuad)original).gtLib$setTextureId(partFace.texture);
        }
    }
}
