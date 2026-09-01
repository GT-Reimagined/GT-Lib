package org.gtreimagined.gtlib.client.model;

import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.client.baked.PipeFullBakedModel;

import java.util.function.Function;

public class PipeFullModel implements IGTModel<PipeFullModel> {
    protected final UnbakedModel open;
    protected final UnbakedModel closed;
    protected final ResourceLocation particle;

    public PipeFullModel(UnbakedModel open, UnbakedModel closed, ResourceLocation particle) {
        this.open = open;
        this.closed = closed;
        this.particle = particle;
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        return new PipeFullBakedModel(getter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, particle)), open.bake(bakery, getter, transform, loc), closed.bake(bakery, getter, transform, loc));
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        open.resolveParents(modelGetter);
        closed.resolveParents(modelGetter);
    }
}
