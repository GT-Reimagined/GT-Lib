package org.gtreimagined.gtlib.recipe.map

import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.items.IItemHandler
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine
import org.gtreimagined.gtlib.capability.Holder
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler
import org.gtreimagined.gtlib.gui.GuiProperties
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.InfoRenderers
import org.gtreimagined.gtlib.machine.Tier
import org.gtreimagined.gtlib.recipe.IRecipe
import org.gtreimagined.gtlib.registration.IGTObject
import org.gtreimagined.gtlib.util.translatable
import java.util.*
import java.util.function.Function
import java.util.function.Predicate

interface IRecipeMap : IGTObject {
    fun find(items: Array<ItemStack>, fluids: Array<FluidStack>, tier: Tier, canHandle: Predicate<IRecipe>): IRecipe?

    fun findByID(id: ResourceLocation): IRecipe? {
        return getRecipes(false).stream().filter { it.id == id }.findFirst().orElse(null)
    }

    fun add(recipe: IRecipe)
    fun compileRecipe(recipe: IRecipe)
    fun compile(manager: RecipeManager)
    fun resetCompiled()
    fun getRecipes(filterHidden: Boolean): MutableCollection<IRecipe>
    fun acceptsItem(item: ItemStack): Boolean
    fun acceptsFluid(fluid: FluidStack): Boolean

    val recipeType: RecipeType<out IRecipe>

    val subCategories: MutableMap<String, SubCategory>

    val guiTier: Tier?
        get() = null

    val icon: Any?
        get() = null

    fun <T : BlockEntityMachine<T>> find(itemHandler: Holder<IItemHandler, MachineItemHandler<T>>, fluidHandler: Holder<IFluidHandler, MachineFluidHandler<T>>, tier: Tier, validateRecipe: Predicate<IRecipe>): IRecipe? {
        return find(
            itemHandler.map { it.inputs }.orElse(EMPTY_ITEM)!!,
            fluidHandler.map { it.inputs }.orElse(EMPTY_FLUID)!!,
            tier, validateRecipe)
    }

    fun find(itemHandler: Optional<MachineItemHandler<*>>, fluidHandler: Optional<MachineFluidHandler<*>>, tier: Tier, validator: Predicate<IRecipe>): IRecipe? {
        return find(
            itemHandler.map { it.inputs }.orElse(EMPTY_ITEM)!!,
            fluidHandler.map { it.inputs }.orElse(EMPTY_FLUID)!!,
            tier, validator)
    }

    val gui: GuiProperties?
        get() = null

    val proxy: Proxy?
        get() = null

    @get:OnlyIn(Dist.CLIENT)
    val infoRenderer: IRecipeInfoRenderer?
        get() = InfoRenderers.DEFAULT_RENDERER

    val displayName: Component
        get() = translatable("gui.recipeviewer.category.${loc.toLanguageKey()}")

    companion object {
        @JvmField
        val EMPTY_ITEM: Array<ItemStack> = emptyArray()
        @JvmField
        val EMPTY_FLUID: Array<FluidStack> = emptyArray()
    }
}
