package org.gtreimagined.gtlib.worldgen.bedrockore

import com.mojang.serialization.Codec
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData

object BedrockVeinData: BaseWorldGenData<BedrockVein>("bedrock_veins", "bedrock_vein") {
    override fun getCodec(): Codec<BedrockVein> {
        return BedrockVein.CODEC
    }
}
