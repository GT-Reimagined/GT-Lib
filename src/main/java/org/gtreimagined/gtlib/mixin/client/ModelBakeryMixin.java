package org.gtreimagined.gtlib.mixin.client;

import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @Inject(method = "loadTopLevel", at = @At("HEAD"))
    private void gtlib$injectInit(ModelResourceLocation location, CallbackInfo ci){
        if (ModelUtils.getModelBakery() == null) {
            ModelUtils.setModelBakery((ModelBakery) (Object) this);
        }
    }
}
