package org.gtreimagined.gtlib.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.baked.PipeBakedModel;
import org.gtreimagined.gtlib.dynamic.DynamicModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PipeModelLoader extends DynamicModelLoader{
        public PipeModelLoader(ResourceLocation location) {
            super(location);
        }

        @NotNull
        @Override
        public DynamicModel read(JsonObject json, JsonDeserializationContext context) {
            return new DynamicModel(super.read(json, context)) {
                @Override
                public BakedModel bakeModel(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
                    return new PipeBakedModel(getter.apply(new Material(InventoryMenu.BLOCK_ATLAS, particle)), getBakedConfigs(owner, bakery, getter, transform, overrides, loc));
                }
            };
        }
    }