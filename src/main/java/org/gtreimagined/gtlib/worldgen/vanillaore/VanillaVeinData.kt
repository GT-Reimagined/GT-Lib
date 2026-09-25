package org.gtreimagined.gtlib.worldgen.vanillaore

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.util.GsonHelper
import net.minecraft.util.profiling.ProfilerFiller
import org.gtreimagined.gtlib.GTLib
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData
import java.util.function.Consumer

object VanillaVeinData : BaseWorldGenData<VanillaVein>("vanilla_veins", "vanilla_vein") {
    override fun getCodec(): Codec<VanillaVein> {
        return VanillaVein.CODEC
    }

    override fun apply(map: MutableMap<ResourceLocation, JsonElement>, resourceManager: ResourceManager, profilerFiller: ProfilerFiller) {
        val layers: MutableMap<ResourceLocation, VanillaVein> = HashMap()
        for (entry in map.entries) {
            val vein = GsonHelper.convertToJsonObject(entry.value, "vanilla_vein")
            vein.addProperty("id", entry.key.toString())
            layers[entry.key] = VanillaVein.CODEC.parse(JsonOps.INSTANCE, vein)
                .getOrThrow(false) { message -> GTLib.LOGGER.error(message) }
        }
        updateVeins(layers)
    }

}
