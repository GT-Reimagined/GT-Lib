package org.gtreimagined.gtlib.client.model

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.ibm.icu.impl.LocaleUtility.fallback
import me.shedaniel.rei.impl.client.search.argument.Argument.cache
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.renderer.block.model.ItemOverrides
import net.minecraft.client.renderer.texture.TextureAtlasSprite
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.resources.model.Material
import net.minecraft.client.resources.model.ModelBaker
import net.minecraft.client.resources.model.ModelState
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.model.geometry.IGeometryBakingContext
import org.gtreimagined.gtlib.client.IGTModel
import org.gtreimagined.gtlib.client.ModelUtils
import org.gtreimagined.gtlib.mixin.client.ModelBakeryAccessor
import java.util.concurrent.TimeUnit
import java.util.function.Function

class FallbackModel(val baseModel: UnbakedModel, val fallbackModel: UnbakedModel): IGTModel<FallbackModel> {
    var useFallback = false
    override fun bakeModel(configuration: IGeometryBakingContext, bakery: ModelBaker, getter: Function<Material, TextureAtlasSprite>,
                           transform: ModelState, overrides: ItemOverrides, loc: ResourceLocation): BakedModel {
        val model = if (useFallback) this.baseModel else fallbackModel
        return model.bake(bakery, getter, transform, loc)!!
    }

    override fun resolveParents(modelGetter: Function<ResourceLocation, UnbakedModel>, context: IGeometryBakingContext) {
        if (baseModel is BlockModel){
            val parentLocation = baseModel.parentLocation
            if(parentLocation != null){
                useFallback = cache.get(parentLocation.toString()){
                    var fallback2 = false
                    if (ModelUtils.getModelBakery() != null){
                        try {
                            (ModelUtils.getModelBakery() as ModelBakeryAccessor).`gtlib$loadModel`(baseModel.parentLocation)
                        } catch (_: Exception){
                            fallback2 = true
                        }
                    }
                    fallback2
                }
            }
        }
        if (useFallback) {
            fallbackModel.resolveParents(modelGetter)
        } else {
            baseModel.resolveParents(modelGetter)
        }
    }

    companion object {
        val cache: Cache<String, Boolean> = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build()
    }
}