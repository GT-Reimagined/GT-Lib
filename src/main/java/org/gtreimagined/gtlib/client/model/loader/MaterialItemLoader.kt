package org.gtreimagined.gtlib.client.model.loader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import org.gtreimagined.gtlib.client.model.WrappedModel

class MaterialItemLoader(resourceLocation: ResourceLocation) : GTModelLoader<WrappedModel>(resourceLocation) {
    override fun read(jsonObject: JsonObject, context: JsonDeserializationContext): WrappedModel {
        val base: JsonObject = JsonObject()
        base.addProperty("parent", jsonObject["base"].asString)
        val fallback: JsonObject = JsonObject()
        fallback.addProperty("parent", jsonObject["fallback"].asString)
        val baseModel: UnbakedModel  = context.deserialize<BlockModel>(fallback, BlockModel::class.java)
        return WrappedModel(baseModel)
    }

}