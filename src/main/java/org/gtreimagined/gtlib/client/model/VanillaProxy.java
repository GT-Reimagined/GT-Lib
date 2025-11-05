package org.gtreimagined.gtlib.client.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.model.loader.IGTModelLoader;
import org.gtreimagined.gtlib.mixin.client.BlockModelAccessor;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public class VanillaProxy implements ISimpleModel<VanillaProxy>
{
    private final List<BlockElement> elements;

    public VanillaProxy(List<BlockElement> list)
    {
        this.elements = list;
    }

    @Override
    public void addQuads(IGeometryBakingContext owner, IModelBuilder<?> modelBuilder, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ResourceLocation modelLocation)
    {
        for(BlockElement blockpart : elements) {
            for(Direction direction : blockpart.faces.keySet()) {
                BlockElementFace blockpartface = blockpart.faces.get(direction);
                TextureAtlasSprite textureatlassprite1 = spriteGetter.apply(owner.getMaterial(blockpartface.texture));
                if (blockpartface.cullForDirection == null) {
                    modelBuilder.addGeneralQuad(BlockModelAccessor.invokeBakeFace(blockpart, blockpartface, textureatlassprite1, direction, modelTransform, modelLocation));
                } else {
                    modelBuilder.addFaceQuad(
                            Direction.rotate(modelTransform.getRotation().getMatrix(), blockpartface.cullForDirection),
                            BlockModelAccessor.invokeBakeFace(blockpart, blockpartface, textureatlassprite1, direction, modelTransform, modelLocation));
                }
            }
        }
    }

    public static class Loader implements IGTModelLoader<VanillaProxy>
    {
        public static final VanillaProxy.Loader INSTANCE = new VanillaProxy.Loader();

        private Loader()
        {
        }

        @NotNull
        @Override
        public VanillaProxy read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
        {
            List<BlockElement> list = this.getModelElements(deserializationContext, modelContents);
            return new VanillaProxy(list);
        }

        private List<BlockElement> getModelElements(JsonDeserializationContext deserializationContext, JsonObject object) {
            List<BlockElement> list = Lists.newArrayList();
            if (object.has("elements")) {
                for(JsonElement jsonelement : GsonHelper.getAsJsonArray(object, "elements")) {
                    list.add(deserializationContext.deserialize(jsonelement, BlockElement.class));
                }
            }

            return list;
        }

        @Override
        public String getId() {
            return "vanilla_proxy";
        }
    }
}
