package org.gtreimagined.gtlib.client.model.loader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import org.gtreimagined.gtlib.client.model.FallbackModel

class MaterialItemLoader(resourceLocation: ResourceLocation) : GTModelLoader<FallbackModel>(resourceLocation) {
    override fun read(jsonObject: JsonObject, context: JsonDeserializationContext): FallbackModel {
        val base = JsonObject()
        base.addProperty("parent", jsonObject["base"].asString)
        val fallback = JsonObject()
        fallback.addProperty("parent", jsonObject["fallback"].asString)
        val baseModel: UnbakedModel  = context.deserialize<BlockModel>(base, BlockModel::class.java)
        val fallbackModel: UnbakedModel  = context.deserialize<BlockModel>(fallback, BlockModel::class.java)
        return FallbackModel(baseModel, fallbackModel)
    }

}