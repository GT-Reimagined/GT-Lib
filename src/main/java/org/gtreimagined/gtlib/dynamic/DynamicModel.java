package org.gtreimagined.gtlib.dynamic;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.client.IGTModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class DynamicModel implements IGTModel<DynamicModel> {

    protected Int2ObjectOpenHashMap<IGTModel[]> modelConfigs;
    protected String staticMapId;
    protected ResourceLocation particle;

    public DynamicModel(ResourceLocation particle, Int2ObjectOpenHashMap<IGTModel[]> modelConfigs, String staticMapId) {
        this.modelConfigs = modelConfigs;
        this.staticMapId = staticMapId;
        this.particle = particle;
    }

    public DynamicModel(DynamicModel copy) {
        this.modelConfigs = copy.modelConfigs;
        this.staticMapId = copy.staticMapId;
        this.particle = copy.particle;
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        return new DynamicBakedModel(getter.apply(new Material(InventoryMenu.BLOCK_ATLAS, particle)), getBakedConfigs(configuration, bakery, getter, transform, overrides, loc));
    }

    public Int2ObjectOpenHashMap<BakedModel[]> getBakedConfigs(IGeometryBakingContext owner, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        Int2ObjectOpenHashMap<BakedModel[]> bakedConfigs = GTLibModelManager.getStaticConfigMap(staticMapId);
        modelConfigs.forEach((k, v) -> {
            BakedModel[] baked = new BakedModel[v.length];
            for (int i = 0; i < baked.length; i++) {
                baked[i] = v[i].bake(owner, bakery, getter, transform, overrides, loc);
            }
            bakedConfigs.put((int) k, baked);
        });
        return bakedConfigs;
    }
}
