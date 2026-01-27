package org.gtreimagined.gtlib.client.model;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.client.SimpleModelState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class GTModel<T extends GTModel<T>> implements IGTModel<T> {

    protected UnbakedModel model;
    protected int[] rotations = new int[0];

    public GTModel() {

    }

    public GTModel(UnbakedModel model, int... rotations) {
        this.model = model;
        this.rotations = rotations;
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        return model != null ? model.bake(bakery, getter, getModelTransform(transform, rotations), loc) : ModelUtils.getMissingModel().bake(bakery, getter, transform, loc);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        model.resolveParents(modelGetter);
    }
}
