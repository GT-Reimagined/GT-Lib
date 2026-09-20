package org.gtreimagined.gtlib.client.model.loader

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.ibm.icu.impl.LocaleUtility.fallback
import net.minecraft.client.renderer.block.model.BlockModel
import net.minecraft.client.resources.model.UnbakedModel
import net.minecraft.resources.ResourceLocation
import org.gtreimagined.gtlib.client.model.FallbackModel

class FallbackModelLoader(resourceLocation: ResourceLocation) : GTModelLoader<FallbackModel>(resourceLocation) {
    override fun read(jsonObject: JsonObject, context: JsonDeserializationContext): FallbackModel {
        val baseElement: JsonElement = jsonObject["base"]
        val base = if (baseElement is JsonObject) baseElement else JsonObject().also {
            it.addProperty("parent", baseElement.asString)
        }
        val fallbackElement: JsonElement = jsonObject["fallback"]
        val fallback = if (fallbackElement is JsonObject) fallbackElement else JsonObject().also {
            it.addProperty("parent", fallbackElement.asString)
        }
        val baseModel: UnbakedModel  = context.deserialize<BlockModel>(base, BlockModel::class.java)
        val fallbackModel: UnbakedModel  = context.deserialize<BlockModel>(fallback, BlockModel::class.java)
        return FallbackModel(baseModel, fallbackModel)
    }

}