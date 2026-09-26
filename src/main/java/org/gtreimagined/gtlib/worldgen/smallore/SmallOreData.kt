package org.gtreimagined.gtlib.worldgen.smallore

import com.mojang.serialization.Codec
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData

object SmallOreData: BaseWorldGenData<SmallOre>("small_ores", "small_ore") {
    override fun getCodec(): Codec<SmallOre> {
        return SmallOre.CODEC
    }
}
