package org.gtreimagined.gtlib.material

import com.google.common.collect.HashBiMap
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import lombok.Getter
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.registries.ForgeRegistries
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient
import org.gtreimagined.gtlib.registration.IGTObject
import org.gtreimagined.gtlib.util.TagUtils
import org.gtreimagined.gtlib.util.Utils
import java.util.*
import java.util.function.Supplier
import kotlin.Boolean
import kotlin.Int
import kotlin.Long

class MaterialTypeBlock<T>(id: String, visible: Boolean, unitValue: Long, supplier: BlockSupplier) :
    MaterialType<T>(id, visible, unitValue) {
    val oreReplacements: MutableMap<Material, MutableMap<StoneType, Supplier<Item>>> = HashBiMap.create()

    fun interface BlockSupplier {
        fun createBlocks(domain: String, type: MaterialType<*>, material: Material)
    }

    private val supplier: BlockSupplier

    init {
        GTAPI.register(MaterialTypeBlock::class.java, this)
        this.supplier = supplier
        this.blockType()
    }

    /**
     * Forces these tags to not generate, assuming they have a replacement.
     */
    fun replacement(mat: Material, type: StoneType, replacement: Supplier<Item>) {
        if (!mat.enabled) return
        if (get() is IOreGetter) {
            val subMap =
                oreReplacements.computeIfAbsent(mat) { Object2ObjectArrayMap() }
            subMap[type] = replacement
            this.add(mat)
            GTAPI.addReplacement(getMaterialTag(mat, type), replacement)
        }
    }

    override fun getMaterialIngredient(m: Material, count: Int): RecipeIngredient {
        return RecipeIngredient.of(getMaterialTag(m), count)
    }

    fun getMaterialIngredient(m: Material, s: StoneType, count: Int): RecipeIngredient {
        return RecipeIngredient.of(getMaterialTag(m, s), count)
    }

    fun getBlockMaterialTag(m: Material): TagKey<Block?>? {
        return TagUtils.getForgelikeBlockTag(
            "${Utils.getConventionalMaterialType(this)}/${if (id == "raw_ore_block") "raw_" else ""}${m.id}"
        )
    }

    fun getMaterialTag(m: Material, s: StoneType): TagKey<Item> {
        if (this.get() !is IOreGetter) return getMaterialTag(m)
        return TagUtils.getForgelikeItemTag("${s.id}_${Utils.getConventionalMaterialType(this)}/${m.id}")
    }

    fun allowBlockGen(material: Material): Boolean {
        return !replacements.containsKey(material) && allowGen(material)
    }

    override fun onRegistryBuild(registry: ResourceKey<out Registry<*>?>?) {
        super.onRegistryBuild(registry)
        if (registry !== ForgeRegistries.Keys.BLOCKS) return
        if (doRegister()) {
            for (material in this.materials) {
                if (!material.enabled) continue
                if (allowBlockGen(material)) supplier.createBlocks(material.materialDomain(), this, material)
            }
        }
    }

    fun interface IBlockGetter {
        fun get(m: Material): Container
    }

    fun interface IOreGetter {
        fun get(m: Material, s: StoneType): Container
    }

    class Container(private var state: BlockState) {
        fun asState(): BlockState {
            return state
        }

        fun asBlock(): Block {
            return state.block
        }

        fun asItem(): Item {
            return asBlock().asItem()
        }

        @JvmOverloads
        fun asStack(count: Int = 1): ItemStack {
            return ItemStack(asItem(), count)
        }

        fun asIngredient(): RecipeIngredient {
            return RecipeIngredient.of(asStack(1))
        }
    }

    companion object {
        @JvmStatic
        fun getEmptyBlockAndLog(type: MaterialType<*>, vararg objects: IGTObject): Container {
            Utils.onInvalidData(
                "Tried to create " + type.getId() + " for objects: " + Arrays.stream(objects)
                    .map { it.getId() }
                    .toList()
                    .toTypedArray().contentToString()
            )
            return Container(Blocks.AIR.defaultBlockState())
        }
    }
}
