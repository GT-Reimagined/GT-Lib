package org.gtreimagined.gtlib.worldgen.vein

import com.mojang.serialization.Codec
import net.minecraft.resources.ResourceLocation
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData

object VeinData : BaseWorldGenData<Vein>("veins", "vein") {
    var totalWeight: Int = 0
    override fun getCodec(): Codec<Vein> {
        return Vein.CODEC
    }

    override fun updateVeins(veins: MutableMap<ResourceLocation, Vein>) {
        super.updateVeins(veins)
        totalWeight = veins.values.stream().mapToInt(Vein::weight).sum()
    }
}
