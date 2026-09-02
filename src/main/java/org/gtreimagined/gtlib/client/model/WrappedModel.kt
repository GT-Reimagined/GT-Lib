package org.gtreimagined.gtlib.client.model

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
import java.util.function.Function

class WrappedModel(val model: UnbakedModel): IGTModel<WrappedModel> {
    override fun bakeModel(configuration: IGeometryBakingContext, bakery: ModelBaker, getter: Function<Material, TextureAtlasSprite>,
                           transform: ModelState, overrides: ItemOverrides, loc: ResourceLocation): BakedModel {
        return model.bake(bakery, getter, transform, loc)!!
    }

    override fun resolveParents(modelGetter: Function<ResourceLocation, UnbakedModel>, context: IGeometryBakingContext) {
        model.resolveParents(modelGetter)
    }
}