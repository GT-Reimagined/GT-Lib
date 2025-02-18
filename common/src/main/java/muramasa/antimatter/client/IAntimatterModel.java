package muramasa.antimatter.client;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public interface IAntimatterModel<T extends IAntimatterModel<T>> extends IModelGeometry<T> {

    default ModelState getModelTransform(ModelState base, int[] rots) {
        if (rots == null || rots.length != 3 || (rots[0] == 0 && rots[1] == 0 && rots[2] == 0)) return base;
        return new SimpleModelState(new Transformation(null, new Quaternion(rots[0], rots[1], rots[2], true), null, null));
    }

    BakedModel bakeModel(ModelBakery bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ResourceLocation loc);

    default BakedModel bakeModel(IModelConfiguration configuration, ModelBakery bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc){
        return bakeModel(bakery, getter, transform, loc);
    }

    @Override
    default BakedModel bake(IModelConfiguration configuration, ModelBakery bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides, ResourceLocation loc) {
        try {
            return bakeModel(configuration, bakery, getter, transform, overrides, loc);
        } catch (Exception e) {
            //Antimatter.LOGGER.error("ModelBaking Exception for " + owner.getModelName());
            e.printStackTrace();
            return ModelUtils.getMissingModel().bake(bakery, getter, transform, loc);
        }
    }
}
