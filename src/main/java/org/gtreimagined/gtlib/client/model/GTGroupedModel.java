package org.gtreimagined.gtlib.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.client.baked.BakedMachineSide;
import org.gtreimagined.gtlib.client.baked.CoverBakedModel;
import org.gtreimagined.gtlib.client.baked.GroupedBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GTGroupedModel implements IGTModel<GTGroupedModel> {
    final Map<String, IGTModel<?>> models;
    final ResourceLocation particle;

    public GTGroupedModel(ResourceLocation particle, Map<String, IGTModel<?>> models) {
        this.models = models;
        this.particle = particle;
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        ImmutableMap.Builder<String, BakedModel> builder = buildParts(configuration, bakery, getter, transform, overrides, loc, this.models.entrySet());
        return new GroupedBakedModel(getter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation())), builder.build());
    }

    protected static ImmutableMap.Builder<String, BakedModel> buildParts(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc, Set<Map.Entry<String, IGTModel<?>>> entries) {
        ImmutableMap.Builder<String, BakedModel> builder = ImmutableMap.builder();
        for (Map.Entry<String, IGTModel<?>> entry : entries) {
            builder.put(entry.getKey(), entry.getValue().bake(configuration, bakery, getter, transform, overrides, loc));
        }
        return builder;
    }

    public static class CoverModel extends GTGroupedModel {

        public CoverModel(GTGroupedModel model) {
            super(model.particle, model.models);
        }

        @Override
        public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
            ImmutableMap.Builder<String, BakedModel> builder = buildParts(configuration, bakery, getter, transform, overrides, loc, this.models.entrySet());
            return new CoverBakedModel(getter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation())), builder.build());
        }
    }

    public static class MachineSideModel extends GTGroupedModel {

        public MachineSideModel(GTGroupedModel model) {
            super(model.particle, model.models);
        }

        @Override
        public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
            ImmutableMap.Builder<String, BakedModel> builder = buildParts(configuration, bakery, getter, transform, overrides, loc, this.models.entrySet());
            return new BakedMachineSide(getter.apply(new Material(InventoryMenu.BLOCK_ATLAS, MissingTextureAtlasSprite.getLocation())), builder.build());
        }
    }
}
