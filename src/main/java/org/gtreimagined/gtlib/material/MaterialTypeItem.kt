package org.gtreimagined.gtlib.material

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.ForgeRegistries
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient
import org.gtreimagined.gtlib.util.Utils
import kotlin.Boolean
import kotlin.Int
import kotlin.Long

class MaterialTypeItem<T> @JvmOverloads constructor(id: String, visible: Boolean, unitValue: Long, val supplier: ItemSupplier = ItemSupplier(::MaterialItem)) : MaterialType<T?>(id, visible, unitValue) {
    fun interface ItemSupplier {
        fun createItems(domain: String?, type: MaterialType<*>?, material: Material?)
    }

    init {
        GTAPI.register(MaterialTypeItem::class.java, this)
    }

    override fun unSplitName(): MaterialTypeItem<T> {
        return super.unSplitName() as MaterialTypeItem<T>
    }

    fun allowItemGen(material: Material?): Boolean {
        return !replacements.containsKey(material) && allowGen(material) && !blockType
    }

    fun get(material: Material): Item {
        val replacement = GTAPI.getReplacement(this, material)
        if (replacement == null) {
            if (!allowItemGen(material)) Utils.onInvalidData("GET ERROR - DOES NOT GENERATE: T($id) M(${material.id})")
            else return GTAPI.get(MaterialItem::class.java, idGetter!!.apply(material))
        }
        return replacement
    }

    fun get(material: Material, count: Int): ItemStack {
        if (count < 1) Utils.onInvalidData("GET ERROR - MAT STACK EMPTY: T($id) M(${material.id})")
        return ItemStack(get(material), count)
    }

    fun getIngredient(material: Material, count: Int): RecipeIngredient {
        if (count < 1) Utils.onInvalidData("GET ERROR - MAT STACK EMPTY: T($id) M(${material.id})")
        return RecipeIngredient.of(getMaterialTag(material), count)
    }

    override fun onRegistryBuild(registry: ResourceKey<out Registry<*>?>?) {
        super.onRegistryBuild(registry)
        if (registry !== ForgeRegistries.Keys.BLOCKS) return
        if (doRegister()) {
            for (material in this.materials) {
                if (!material.enabled) continue
                if (allowItemGen(material)) this.supplier.createItems(material.materialDomain(), this, material)
            }
        }
    }
}
