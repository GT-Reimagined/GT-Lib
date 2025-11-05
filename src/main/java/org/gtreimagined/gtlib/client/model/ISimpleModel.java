package org.gtreimagined.gtlib.client.model;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.IGTModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

public interface ISimpleModel<T extends ISimpleModel<T>> extends IGTModel<T> {
    @Override
    default BakedModel bakeModel(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
    {
        TextureAtlasSprite particle = spriteGetter.apply(owner.getMaterial("particle"));

        IModelBuilder<?> builder = IModelBuilder.of(owner, overrides, particle);

        addQuads(owner, builder, bakery, spriteGetter, modelTransform, modelLocation);

        return builder.build();
    }

    void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation);
}
