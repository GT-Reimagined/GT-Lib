package org.gtreimagined.gtlib.material

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.gtreimagined.gtlib.material.MaterialTags.RAINBOW_RGB
import org.gtreimagined.gtlib.util.CodeUtils
import kotlin.collections.getOrPut

class MaterialColorChanger(val material: Material) {
    var rgb: Int = material.rgb
        private set

    fun tick() {
        val rgb: Int = this.rgb


        val tDirection = (if (time % 100 < 50) +1 else -1)
        var r = CodeUtils.getR(rgb).toInt()
        var g = CodeUtils.getG(rgb).toInt()
        var b = CodeUtils.getB(rgb).toInt()
        if (material.has(MaterialTags.POSITIVE_CHANGING_RGB)) {
            r = CodeUtils.bind8((r + tDirection).toLong()).toInt()
            g = CodeUtils.bind8((g + tDirection).toLong()).toInt()
            b = CodeUtils.bind8((b + tDirection).toLong()).toInt()
        } else if (material.has(MaterialTags.NEGATIVE_CHANGING_RGB)) {
            r = CodeUtils.bind8((r - tDirection).toLong()).toInt()
            g = CodeUtils.bind8((g - tDirection).toLong()).toInt()
            b = CodeUtils.bind8((b - tDirection).toLong()).toInt()
        } else if (material.has(RAINBOW_RGB)) {
            val tNR = CodeUtils.inside(0, 99, ((time / 2) % 300).toLong())
            val tNG = CodeUtils.inside(50, 149, ((time / 2) % 300).toLong())
            val tNB = CodeUtils.inside(100, 199, ((time / 2) % 300).toLong())
            val tPR = CodeUtils.inside(100, 199, ((time / 2) % 300).toLong())
            val tPG = CodeUtils.inside(150, 249, ((time / 2) % 300).toLong())
            val tPB = CodeUtils.inside(200, 299, ((time / 2) % 300).toLong())

            if (tPR) r = CodeUtils.bind8((r + 1).toLong()).toInt()
            if (tPG) g = CodeUtils.bind8((g + 1).toLong()).toInt()
            if (tPB) b = CodeUtils.bind8((b + 1).toLong()).toInt()
            if (tNR) r = CodeUtils.bind8((r - 1).toLong()).toInt()
            if (tNG) g = CodeUtils.bind8((g - 1).toLong()).toInt()
            if (tNB) b = CodeUtils.bind8((b - 1).toLong()).toInt()
        }
        this.rgb = CodeUtils.getRGB(r, g, b)
    }

    companion object {
        @JvmField val RGB_CHANGING_MAP: MutableMap<Material, MaterialColorChanger> = Object2ObjectOpenHashMap()
        private var time: Int = 0
        @JvmStatic fun incrementTime() = time++
        @JvmStatic fun getOrCreateColorChanger(material: Material): MaterialColorChanger {
            return RGB_CHANGING_MAP.getOrPut(material){
                MaterialColorChanger(material)
            }
        }
    }
}