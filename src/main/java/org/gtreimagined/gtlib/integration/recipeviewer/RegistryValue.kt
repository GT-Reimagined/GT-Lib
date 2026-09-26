package org.gtreimagined.gtlib.integration.recipeviewer

import net.minecraft.resources.ResourceLocation
import org.gtreimagined.gtlib.gui.GuiProperties
import org.gtreimagined.gtlib.machine.Tier
import org.gtreimagined.gtlib.recipe.map.IRecipeMap


data class RegistryValue @JvmOverloads constructor(@JvmField var map: IRecipeMap, @JvmField var gui: GuiProperties, @JvmField var tier: Tier,
                         @JvmField var workstations: MutableList<ResourceLocation> = ArrayList()
) {

    fun addWorkstation(supplier: ResourceLocation?): RegistryValue {
        if (supplier != null && !workstations.contains(supplier)) {
            workstations.add(supplier)
        }
        return this
    }
}
