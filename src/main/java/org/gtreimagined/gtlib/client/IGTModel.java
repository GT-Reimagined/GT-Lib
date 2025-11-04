package org.gtreimagined.gtlib.client;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public interface IGTModel<T extends IGTModel<T>> extends IUnbakedGeometry<T> {

    default ModelState getModelTransform(ModelState base, int[] rots) {
        if (rots == null || rots.length != 3 || (rots[0] == 0 && rots[1] == 0 && rots[2] == 0)) return base;
        return new SimpleModelState(new Transformation(null, new Quaternion(rots[0], rots[1], rots[2], true), null, null));
    }

    BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc);

    @Override
    default BakedModel bake(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        try {
            return bakeModel(configuration, bakery, getter, transform, overrides, loc);
        } catch (Exception e) {
            //GTLib.LOGGER.error("ModelBaking Exception for " + owner.getModelName());
            e.printStackTrace();
            return ModelUtils.getMissingModel().bake(bakery, getter, transform, loc);
        }
    }
}
