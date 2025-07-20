package org.gtreimagined.gtlib.fluid;

import lombok.Builder;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

@Builder
public class GTClientFluidTypeExtension implements IClientFluidTypeExtensions {
    ResourceLocation stillTexture;
    ResourceLocation flowingTexture;
    int tintColor = -1;
    ResourceLocation overlayTexture;

    @Override
    public int getTintColor() {
        return tintColor;
    }

    @Override
    public ResourceLocation getFlowingTexture() {
        return flowingTexture;
    }

    @Override
    public @Nullable ResourceLocation getOverlayTexture() {
        return overlayTexture;
    }

    @Override
    public ResourceLocation getStillTexture() {
        return stillTexture;
    }

}
