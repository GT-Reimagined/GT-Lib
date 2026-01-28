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
import org.gtreimagined.gtlib.client.baked.PipeBakedModelNew;

import java.util.function.Function;

public class PipeModelNew implements IGTModel<PipeModelNew> {
    protected final UnbakedModel base;
    protected final UnbakedModel baseEnd;
    protected final UnbakedModel[] connections;
    protected final UnbakedModel[] connectionsEnd;
    protected final ResourceLocation particle;

    public PipeModelNew(UnbakedModel base, UnbakedModel baseEnd, UnbakedModel[] connections, UnbakedModel[] connectionsEnd, ResourceLocation particle) {
        this.base = base;
        this.baseEnd = baseEnd;
        this.connections = connections;
        this.connectionsEnd = connectionsEnd;
        this.particle = particle;
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        BakedModel[] connections = new BakedModel[this.connections.length];
        BakedModel[] connectionsEnd = new BakedModel[this.connectionsEnd.length];
        for (int i = 0; i < 6; i++) {
            connections[i] = this.connections[i].bake(bakery, getter, transform, loc);
            connectionsEnd[i] = this.connectionsEnd[i].bake(bakery, getter, transform, loc);
        }
        return new PipeBakedModelNew(getter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, particle)), base.bake(bakery, getter, transform, loc), baseEnd.bake(bakery, getter, transform, loc), connections, connectionsEnd);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        base.resolveParents(modelGetter);
        baseEnd.resolveParents(modelGetter);
        for (UnbakedModel connection : connections) {
            connection.resolveParents(modelGetter);
        }
        for (UnbakedModel connection : connectionsEnd) {
            connection.resolveParents(modelGetter);
        }
    }
}
