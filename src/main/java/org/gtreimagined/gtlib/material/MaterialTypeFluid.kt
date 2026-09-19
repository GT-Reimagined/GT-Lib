package org.gtreimagined.gtlib.material

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import lombok.Getter
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.registration.IGTObject
import org.gtreimagined.gtlib.util.TagUtils
import org.gtreimagined.gtlib.util.Utils
import java.util.*
import java.util.function.Supplier

class MaterialTypeFluid<T>(id: String, layers: Int, visible: Boolean, unitValue: Long) :
    MaterialType<T?>(id, layers, visible, unitValue) {
    val fluidReplacements: Object2ObjectMap<Material, Supplier<Fluid>> = Object2ObjectOpenHashMap()

    init {
        GTAPI.register(MaterialTypeFluid::class.java, this)
    }

    fun addReplacement(material: Material, fluidSupplier: Supplier<Fluid>) {
        if (!material.enabled) return
        fluidReplacements[material] = fluidSupplier
        this.add(material)
    }

    override fun hasReplacement(mat: Material): Boolean {
        return fluidReplacements.containsKey(mat)
    }

    override fun replacement(mat: Material, replacement: Supplier<Item>) {
        //NOOP
    }

    override fun tagFromString(name: String?): TagKey<*>? {
        return TagUtils.getForgelikeFluidTag(name)
    }

    interface IFluidGetter {
        fun get(m: Material?, amount: Int): FluidStack?
    }

    companion object {
        @JvmStatic
        fun getEmptyFluidAndLog(type: MaterialType<*>, vararg objects: IGTObject): FluidStack {
            Utils.onInvalidData(
                "Tried to create " + type.getId() + " for objects: " + Arrays.stream(objects)
                    .map { it.getId() }.toList().toTypedArray()
                    .contentToString())
            return FluidStack(Fluids.WATER, 1)
        }
    }
}
