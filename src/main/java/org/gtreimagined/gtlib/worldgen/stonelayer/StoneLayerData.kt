package org.gtreimagined.gtlib.worldgen.stonelayer

import com.mojang.serialization.Codec
import net.minecraft.world.level.Level
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerCollisionData.getVeins

object StoneLayerData : BaseWorldGenData<StoneLayer>("stone_layers", "stone_layer") {
    override fun getCodec(): Codec<StoneLayer> {
        return StoneLayer.CODEC
    }

    fun getFlat(level: Level?): MutableList<StoneLayer> {
        val result: MutableList<StoneLayer> = ArrayList()
        val layers = getVeins(level).values
        for (layer in layers) {
            for (i in 0..<layer.weight) {
                result.add(layer)
            }
        }
        return result
    }

}
