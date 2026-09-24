package org.gtreimagined.gtlib.worldgen.stonelayer

import com.mojang.serialization.Codec
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData

object StoneLayerCollisionData :
    BaseWorldGenData<StoneLayerCollision>("stone_layer_collisions", "stone_layer_collisons") {
    override fun getCodec(): Codec<StoneLayerCollision> {
        return StoneLayerCollision.CODEC
    }

    fun getCollisions(level: Level?, top: BlockState?, bottom: BlockState?): MutableList<StoneLayerOre> {
        val collisions = getVeins(level)
        if (collisions.isEmpty()) return mutableListOf()
        val matching =
            collisions.values.stream().filter { c: StoneLayerCollision -> c.top === top && c.bottom === bottom }
                .toList()
        if (matching.isEmpty()) return mutableListOf()
        val ores: MutableList<StoneLayerOre> = ArrayList()
        for (collision in matching) {
            ores.addAll(collision.ores)
        }
        return ores
    }
}
