package org.gtreimagined.gtlib.material

import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.registration.IGTObject
import org.gtreimagined.gtlib.texture.Texture

class TextureSet @JvmOverloads constructor(
    private val domain: String,
    private val id: String,
    private val force: Boolean = false
) : IGTObject {
    init {
        GTAPI.register(TextureSet::class.java, this)
    }

    override fun getDomain(): String {
        return domain
    }

    override fun getId(): String {
        return id
    }

    fun getTexture(type: MaterialType<*>, layer: Int): Texture {
        val builder = StringBuilder()
        val prefix = if (type is MaterialTypeBlock<*> || type is MaterialTypeFluid<*>) "block" else "item"
        builder.append(prefix).append("/material/")
        builder.append(id).append("/")
        //TODO return different numbered overlay based on current layer
        builder.append(type.getId()).append(if (layer == 0) "" else "_overlay" /*"_overlay_" + layer*/)
        return Texture(domain, builder.toString())
    }

    val path: String
        get() = "material/$id"

    fun getTextures(type: MaterialType<*>): Array<Texture?> {
        val textures = arrayOfNulls<Texture>(type.getLayers())
        for (i in 0..<type.getLayers()) {
            textures[i] = getTexture(type, i)
        }
        return textures
    }

    companion object {
        @JvmField
        val NONE: TextureSet = TextureSet(Ref.ID, "none")
        @JvmField
        val CUBE: TextureSet = TextureSet(Ref.ID, "cube")
        @JvmField
        val DULL: TextureSet = TextureSet(Ref.ID, "dull")
        @JvmField
        val METALLIC: TextureSet = TextureSet(Ref.ID, "metallic")
        @JvmField
        val SHINY: TextureSet = TextureSet(Ref.ID, "shiny")
        @JvmField
        val ROUGH: TextureSet = TextureSet(Ref.ID, "rough")
        @JvmField
        val MAGNETIC: TextureSet = TextureSet(Ref.ID, "magnetic")
        @JvmField
        val DIAMOND: TextureSet = TextureSet(Ref.ID, "diamond")
        @JvmField
        val RUBY: TextureSet = TextureSet(Ref.ID, "ruby")
        @JvmField
        val LAPIS: TextureSet = TextureSet(Ref.ID, "lapis")
        @JvmField
        val GEM_H: TextureSet = TextureSet(Ref.ID, "gem_h")
        @JvmField
        val GEM_V: TextureSet = TextureSet(Ref.ID, "gem_v")
        @JvmField
        val GARNET: TextureSet = TextureSet(Ref.ID, "garnet")
        @JvmField
        val QUARTZ: TextureSet = TextureSet(Ref.ID, "quartz")
        @JvmField
        val FINE: TextureSet = TextureSet(Ref.ID, "fine")
        val FLINT: TextureSet = TextureSet(Ref.ID, "flint")
        @JvmField
        val LIGNITE: TextureSet = TextureSet(Ref.ID, "lignite")
        @JvmField
        val WOOD: TextureSet = TextureSet(Ref.ID, "wood")
        @JvmField
        val REDSTONE: TextureSet = TextureSet(Ref.ID, "redstone")
        @JvmField
        val RAD: TextureSet = TextureSet(Ref.ID, "rad")
        @JvmField
        val RUBBER: TextureSet = TextureSet(Ref.ID, "rubber")

        @JvmStatic
        fun init() {
        }
    }
}
