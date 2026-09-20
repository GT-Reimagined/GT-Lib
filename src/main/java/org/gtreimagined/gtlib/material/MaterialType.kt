package org.gtreimagined.gtlib.material

import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableMap
import com.mojang.serialization.Codec
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.minecraft.core.Registry
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.util.Tuple
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTLibConfig
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider
import org.gtreimagined.gtlib.registration.ISharedGTObject
import org.gtreimagined.gtlib.util.TagUtils
import org.gtreimagined.gtlib.util.Utils
import java.util.*
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.Supplier
import kotlin.Any
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.iterator
import kotlin.text.contains
import kotlin.text.lowercase
import kotlin.text.replace

open class MaterialType<T>(@JvmField val id: String, var visible: Boolean, var unitValue: Long) : IMaterialTag, ISharedGTObject,
    IRegistryEntryProvider {

    var generating: Boolean = true
    var blockType: Boolean = false

    var isSplitName: Boolean
    protected val materials: MutableSet<Material> =
        ObjectLinkedOpenHashSet() //Linked to preserve insertion order for JEI
    protected val tagMap: MutableMap<MaterialType<*>?, TagKey<*>?> =
        Object2ObjectOpenHashMap<MaterialType<*>?, TagKey<*>?>()

    var tagPrefix: String

    var lang: Function<Material, String>

    var idGetter: Function<Material, String> = Function {
        m ->
        val split = Utils.getLocalizedMaterialType(this)
        if (split.size > 1) {
            return@Function "${split[0].lowercase(Locale.getDefault()).replace(" ", "_")}_" +
                    "${m.id}_${split[1].lowercase(Locale.getDefault()).replace(" ", "_")}"
        } else {
            return@Function "${m.id}_${split[0]!!.lowercase(Locale.getDefault()).replace(" ", "_")}"
        }
    }
    var getter: T? = null
    var isHidden = false

    val replacements: BiMap<Material, Supplier<Item>> = HashBiMap.create()
    val dependents: MutableSet<IMaterialTag> = ObjectLinkedOpenHashSet()

    //since we have two instances stored in gt lib.
    var hasRegistered: Boolean = false

    init {
        this.isSplitName = id.contains("_")
        this.tagPrefix = Utils.getConventionalMaterialType(this)
        this.tagMap[this] = tagFromString(this.tagPrefix)
        this.lang = Function { m ->
            val split = Utils.getLocalizedMaterialType(this)
            if (split.size > 1) {
                return@Function "${split[0]} ${Utils.getLocalizedType(m)} ${split[1]}"
            } else {
                return@Function "${Utils.getLocalizedType(m)} ${split[0]}"
            }
        }
        register(MaterialType::class.java, id)
    }

    protected open fun tagFromString(name: String?): TagKey<*>? {
        return TagUtils.getForgelikeItemTag(name)
    }

    /**
     * Adds a list of dependent flags, that is all of these flags are added as well.
     * 
     * @param tags the list of tags.
     * @return this
     */
    fun dependents(vararg tags: IMaterialTag) {
        dependents.addAll(listOf(*tags))
    }

    /**
     * Forces these tags to not generate, assuming they have a replacement.
     */
    open fun replacement(mat: Material, replacement: Supplier<Item>) {
        if (!mat.enabled) return
        replacements[mat] = replacement
        this.add(mat)
        GTAPI.addReplacement<Item?>(getMaterialTag(mat), replacement)
    }

    open fun hasReplacement(mat: Material): Boolean {
        return replacements.containsKey(mat)
    }

    open fun getMaterialFromStack(stack: ItemStack): Material? {
        val item = stack.item
        if (item is MaterialItem) {
            if (item.getType() === this) return item.getMaterial()
            return null
        }

        //TODO better fix
        //return replacements.inverse().get(stack.getItem());
        for ((key, value) in replacements) {
            val item = value!!.get()
            if (item === stack.item) {
                return key
            }
        }
        // gets material from other mod items using the tags
        for (tagKey in stack.item.builtInRegistryHolder().tags().toList()) {
            val prefix = this.getTag<Any?>()!!.location().path + "/"
            if (tagKey.location().namespace == this.getTag<Any?>()!!.location().namespace && tagKey.location()
                    .path.contains(prefix)
            ) {
                val material = Material.get(tagKey.location().path.replace(prefix, ""))
                if (material !== Material.NULL) {
                    return material
                }
            }
        }
        return null
    }

    fun getMaterialTag(m: Material): TagKey<Item> {
        return tagFromString("${this.tagPrefix}/${if (id == "raw_ore_block") "raw_" else ""}${m.id}") as TagKey<Item>
    }

    open fun getMaterialIngredient(m: Material, count: Int): RecipeIngredient? {
        return RecipeIngredient.of(getMaterialTag(m), count)
    }

    fun nonGen(): MaterialType<T> = apply { generating = false }

    fun hidden(): MaterialType<T> = apply {this.isHidden = true}

    fun blockType(): MaterialType<T> {
        blockType = true
        this.tagMap[this] = TagUtils.getForgelikeBlockTag(Utils.getConventionalMaterialType(this))
        return this
    }

    open fun unSplitName(): MaterialType<T> {
        isSplitName = false
        this.tagPrefix = Utils.getConventionalMaterialType(this)
        this.tagMap[this] = tagFromString(tagPrefix)
        return this
    }

    fun lang(lang: Function<Material, String>): MaterialType<T> {
        this.lang = lang
        return this
    }

    fun lang(lang: BiFunction<MaterialType<*>, Material, String>): MaterialType<T> {
        return lang { m -> lang.apply(this, m) }
    }

    fun idGetter(idGetter: Function<Material, String>): MaterialType<T> = apply { this.idGetter = idGetter }

    fun tagPrefix(prefix: String): MaterialType<T> {
        this.tagPrefix = prefix
        tagMap[this] = tagFromString(prefix)
        return this
    }

    override fun add(vararg m: Material) {
        for (m2 in m) {
            if (m2.enabled) {
                all().add(m2)
                m2.types.add(this)
            }
        }
    }

    override fun remove(vararg m: Material) {
        for (m2 in m) {
            all().remove(m2)
            m2.types.remove(this)
        }
    }

    override fun getId(): String {
        return id
    }

    fun <T> getTag(): TagKey<T?>? {
        return tagMap[this] as TagKey<T?>?
    }

    fun set(getter: T): MaterialType<T> {
        this.getter = getter
        return this
    }

    override fun dependents(): MutableSet<IMaterialTag> {
        return this.dependents
    }

    fun get(): T? {
        return getter
    }

    override fun all(): MutableSet<Material> {
        return materials
    }

    fun isVisible(): Boolean {
        return visible || GTLibConfig.SHOW_ALL_MATERIAL_ITEMS.get()
    }

    fun allowGen(material: Material?): Boolean {
        return generating && materials.contains(material) && GTAPI.getReplacement(this, material) == null
    }

    override fun toString(): String {
        return id
    }


    override fun onRegistryBuild(registry: ResourceKey<out Registry<*>?>?) {
    }

    protected fun doRegister(): Boolean {
        val old = hasRegistered
        hasRegistered = true
        return !old
    }

    companion object {
        @JvmField
        val CODEC: Codec<MaterialType<*>> = Codec.STRING.xmap(Function { s ->
            GTAPI.get(MaterialType::class.java, s)
        }, Function { it.getId() })

        var tooltipCache: ImmutableMap<Item, Tuple<MaterialType<*>, Material>>? = null

        @JvmStatic
        @OnlyIn(Dist.CLIENT)
        fun buildTooltips() {
            val builder = ImmutableMap.builder<Item, Tuple<MaterialType<*>, Material>>()
            GTAPI.all(MaterialType::class.java) { t ->
                val map = t.replacements.inverse()
                for ((key, value) in map) {
                    builder.put(key!!.get(), Tuple<MaterialType<*>, Material>(t, value))
                }
            }
            tooltipCache = builder.build()
        }

        @JvmStatic
        fun addTooltip(stack: ItemStack, tooltips: MutableList<Component>, player: Player?, flag: TooltipFlag) {
            if (player == null) return
            if (tooltipCache == null) return
            val mat: Tuple<MaterialType<*>, Material>? = tooltipCache?.get(stack.item)
            if (mat == null) {
                val item = stack.item
                if (item is MaterialItem) {
                    MaterialItem.addTooltipsForMaterialItems(
                        stack,
                        item.material,
                        item.type,
                        player.level(),
                        tooltips,
                        flag
                    )
                }
                return
            }
            MaterialItem.addTooltipsForMaterialItems(stack, mat.getB(), mat.getA(), player.level(), tooltips, flag)
        }

        @JvmStatic
        fun getMaterialFromStackTypeless(stack: ItemStack): Material? {
            var material: Material? = null
            for (type in GTAPI.all(MaterialType::class.java)) {
                material = type.getMaterialFromStack(stack)
                if (material != null) {
                    break
                }
            }
            return material
        }
    }
}
