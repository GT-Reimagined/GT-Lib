package org.gtreimagined.gtlib.recipe.map

import com.mojang.datafixers.util.Either
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.util.Tuple
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.crafting.CompoundIngredient
import net.minecraftforge.common.crafting.PartialNBTIngredient
import net.minecraftforge.common.crafting.StrictNBTIngredient
import net.minecraftforge.fluids.FluidStack
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.gui.GuiProperties
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.InfoRenderers
import org.gtreimagined.gtlib.machine.Tier
import org.gtreimagined.gtlib.machine.types.Machine
import org.gtreimagined.gtlib.recipe.IRecipe
import org.gtreimagined.gtlib.recipe.Recipe
import org.gtreimagined.gtlib.recipe.ingredient.*
import org.gtreimagined.gtlib.recipe.serializer.IGTRecipeSerializer
import org.gtreimagined.gtlib.recipe.serializer.MachineRecipeSerializer
import org.gtreimagined.gtlib.util.onInvalidData
import java.util.*
import java.util.function.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.Comparator
import kotlin.Int
import kotlin.RuntimeException
import kotlin.String
import kotlin.arrayOfNulls

open class RecipeMap<B : RecipeBuilder?>(domain: String, categoryId: String, private val builder: B) : IRecipeMap {
    private val loc: ResourceLocation = ResourceLocation(domain, categoryId)
    private val LOOKUP = Branch()
    private val RECIPES_TO_COMPILE: MutableList<IRecipe> = ObjectArrayList<IRecipe>()
    private val ROOT: MutableSet<AbstractMapIngredient> = ObjectOpenHashSet()
    private val ROOT_SPECIAL: MutableList<AbstractMapIngredient> = ObjectArrayList()

    override var gui: GuiProperties? = null
    override var guiTier: Tier? = null
    override var proxy: Proxy? = null
    override val recipeType: RecipeType<out IRecipe>
        get() = recipeSerializer.recipeType

    var iconSupplier: Supplier<Any?>? = null
    override val icon: Any?
        get() = if (this.iconSupplier != null) this.iconSupplier!!.get() else null

    @OnlyIn(Dist.CLIENT)
    override var infoRenderer: IRecipeInfoRenderer = InfoRenderers.DEFAULT_RENDERER

    override val subCategories: MutableMap<String, SubCategory> = Object2ObjectOpenHashMap()

    var recipeSerializer: IGTRecipeSerializer<out IRecipe> = MachineRecipeSerializer.INSTANCE

    // Data allows you to set related data to the map, e.g. which tier the gui
    // displays.
    init {
        this.builder!!.map = this
        GTAPI.register<RecipeMap<*>>(this)
        GTAPI.register<IRecipeMap>(this)
    }

    // Object can be an IDrawable or an ItemStack or an IItemProvider.
    fun setIconSupplier(supplier: Supplier<Any?>?): RecipeMap<B> {
        this.iconSupplier = supplier
        return this
    }

    fun setIcon(supplier: Supplier<Any?>?): RecipeMap<B> {
        this.iconSupplier = supplier
        return this
    }

    fun guiTier(tier: Tier?): RecipeMap<B> = apply { this.guiTier = tier }

    /**
     * Sets the gui data and overrides the data in JEI.
     * 
     * @param gui the guidata.
     * @return this
     */
    fun setGuiData(gui: GuiProperties?): RecipeMap<B> {
        this.gui = gui
        GTAPI.registerJEICategory(this, this.gui!!)
        return this
    }

    /**
     * Sets the gui data and overrides the data in JEI.
     * 
     * @param gui     the guidata.
     * @param machine the machine.
     * @return this
     */
    fun setGuiData(gui: GuiProperties?, machine: Machine<*>): RecipeMap<B> {
        this.gui = gui
        GTAPI.registerJEICategory(this, this.gui!!, machine, machine.firstTier, true)
        return this
    }

    /**
     * Sets a proxy for this recipe map that is used to build recipes from other
     * maps.
     * 
     * @param proxy the proxy.
     * @return this.
     */
    fun proxy(proxy: Proxy?): RecipeMap<B> = apply { this.proxy = proxy }

    override fun getId(): String {
        return loc.path
    }

    override fun getDomain(): String {
        return loc.namespace
    }

    fun <T : IRecipe> setRecipeSerializer(serializer: IGTRecipeSerializer<T>): RecipeMap<B> {
        this.recipeSerializer = serializer
        RECIPE_TYPES.add(serializer.getRecipeType())
        return this
    }

    fun addSubCategory(id: String, subCategory: SubCategory): RecipeMap<B> {
        subCategories[id] = subCategory
        return this
    }

    fun RB(): B {
        builder!!.clear()
        return builder
    }

    /**
     * Gets the set of recipes.
     * 
     * @param filterHidden whether or not to filter hidden recipes.
     * @return collection of recipes.
     */
    override fun getRecipes(filterHidden: Boolean): MutableCollection<IRecipe> {
        // Collectors.toSet is very important since there are duplicate recipes but they
        // point to the same memory location
        // so == works to remove them.
        // Or maybe not, I'm not sure but let's make a set anyways
        return LOOKUP.getRecipes(filterHidden)
            .sorted(Comparator.comparingLong<IRecipe?>(ToLongFunction { obj: IRecipe? -> obj!!.getPower() }))
            .collect(Collectors.toCollection(Supplier { LinkedHashSet() }))
    }

    override fun add(recipe: IRecipe) {
        RECIPES_TO_COMPILE.add(recipe)
    }

    /**
     * Adds a recipe to this map. If the recipe is empty it is not added. If it
     * collides with another recipe it is added to the collection and the first
     * recipe that matches a predicate is returned upon find.
     * 
     * @param recipe the recipe to add.
     */
    override fun compileRecipe(recipe: IRecipe) {
        if (recipe.hasOutputItems()) {
            for (stack in recipe.getOutputItems()) {
                if (stack.isEmpty) {
                    onInvalidData("RECIPE WITH EMPTY OUTPUT ITEM (ID): " + recipe.id)
                    return
                }
            }
        }
        var flag = false
        if (recipe.hasInputItems()) {
            for (inputItem in recipe.getInputItems()) {
                if (isIngredientSpecial(inputItem)) {
                    flag = true
                    continue
                }
                if (inputItem.isEmpty || (inputItem.items.size == 1
                            && inputItem.items[0].item === Items.BARRIER)
                ) {
                    onInvalidData("RECIPE WITH EMPTY INPUT (ID): " + recipe.id)
                    return
                }
            }
        }
        if (flag) {
            recipe.sortInputItems()
        }
        val items = fromRecipe(recipe, true)

        // Recipe r = recurseItemTreeFind(items, map, rr -> true);
        // if (r != null) {
        // GTLib.LOGGER.warn("Recipe collision, adding both but only first is
        // available.");
        // }
        if (recurseItemTreeAdd(recipe, items, LOOKUP, 0, 0)) {
            items.forEach { t ->
                t.forEach { ing ->
                    if (!ing.isSpecial) ROOT.add(ing)
                    else ROOT_SPECIAL.add(ing)
                }
            }
        }
        recipe.setMapId(this.loc.toString())
    }

    protected fun buildFromFluids(builder: MutableList<MutableList<AbstractMapIngredient>>, ingredients: MutableList<FluidIngredient>, insideMap: Boolean) {
        for (t in ingredients) {
            val inner: MutableList<AbstractMapIngredient> = ObjectArrayList(t.getStacks().size)
            for (stack in t.getStacks()) {
                inner.add(MapFluidIngredient(stack, insideMap))
            }
            builder.add(inner)
        }
    }

    protected fun buildFromFluidStacks(builder: MutableList<MutableList<AbstractMapIngredient?>>, ingredients: MutableList<FluidStack?>, insideMap: Boolean) {
        for (t in ingredients) {
            builder.add(mutableListOf(MapFluidIngredient(t, insideMap)))
        }
    }

    protected fun fromRecipe(r: IRecipe, insideMap: Boolean): MutableList<MutableList<AbstractMapIngredient>> {
        val list: MutableList<MutableList<AbstractMapIngredient>> = ObjectArrayList(
            (if (r.hasInputItems()) r.getInputItems().size else 0)
                    + (if (r.hasInputFluids()) r.getInputFluids().size else 0)
        )
        if (r.hasInputItems()) {
            buildFromItems(list, r.getInputItems(), insideMap)
        }
        if (r.hasInputFluids()) {
            buildFromFluids(list, r.getInputFluids(), insideMap)
        }
        return list
    }

    protected fun buildFromItems(list: MutableList<MutableList<AbstractMapIngredient>>, ingredients: MutableList<Ingredient>, insideMap: Boolean) {
        for (t in ingredients) {
            if (!isIngredientSpecial(t)) {
                val rl: Optional<ResourceLocation> =
                    Optional.empty() //MapTagIngredient.findCommonTag(t, tags);
                if (rl.isPresent) {
                    list.add(mutableListOf(MapTagIngredient(rl.get(), insideMap)))
                } else {
                    val inner: MutableList<AbstractMapIngredient> =
                        ObjectArrayList(t.items.size)
                    for (stack in t.items) {
                        if (t is RecipeIngredient && t.ignoreNbt()) {
                            inner.add(MapItemIngredient(stack.item, insideMap))
                        } else {
                            inner.add(MapItemStackIngredient(stack, insideMap))
                        }
                    }
                    list.add(inner)
                }
            } else {
                list.add(mutableListOf(SpecialIngredientWrapper(t)))
            }
        }
    }

    protected fun buildFromItemStacks(list: MutableList<MutableList<AbstractMapIngredient?>>, ingredients: Array<ItemStack>) {
        for (t in ingredients) {
            val c = t.item.builtInRegistryHolder().tags().count().toInt()
            val ls: MutableList<AbstractMapIngredient?> = ObjectArrayList<AbstractMapIngredient?>(2 + c)
            ls.add(MapItemIngredient(t.item, false))
            ls.add(MapItemStackIngredient(t, false))
            /*for (ResourceLocation rl : t.getItem().getTags()) {
                ls.add(new MapTagIngredient(rl, false));
            }*/
            list.add(ls)
        }
    }

    /**
     * Adds a recipe to the map. (recursive part)
     * 
     * @param recipe      the recipe to add.
     * @param ingredients list of input ingredients.
     * @param map         the current place in the recursion.
     * @param index       where in the ingredients list we are.
     * @param count       how many added already.
     */
    protected fun recurseItemTreeAdd(recipe: IRecipe, ingredients: MutableList<MutableList<AbstractMapIngredient>>, map: Branch, index: Int, count: Int): Boolean {
        if (count >= ingredients.size) return true
        if (index >= ingredients.size) {
            throw RuntimeException("Index out of bounds for recurseItemTreeAdd, should not happen")
        }
        // Loop through NUMBER_OF_INGREDIENTS times.
        val current = ingredients.get(index)
        var r: Either<MutableList<IRecipe>, Branch>?
        for (obj in current) {
            if (!obj.isSpecial) {
                // Either add the recipe or create a branch.
                r = map.NODES.compute(obj) { k, v ->
                    var v = v
                    if (count == ingredients.size - 1) {
                        if (v == null) {
                            v = Either.left(ObjectArrayList())
                        }
                        v.ifLeft { it!!.add(recipe) }
                        return@compute v
                    } else if (v == null) {
                        val traverse = Branch()
                        v = Either.right(traverse)
                    }
                    v
                }
                // At the end, return.
                if (count == ingredients.size - 1) continue
                // If left was present before. Shouldn't be needed?
                /*
                 * if (r.left().isPresent()) { Utils.onInvalidData("COLLISION DETECTED!");
                 * current.forEach(map.NODES::remove); return false; }
                 */
                // should always be present but this gives no warning.
                if (r!!.right().map { m ->
                        !recurseItemTreeAdd(recipe, ingredients, m, (index + 1) % ingredients.size, count + 1)
                    }.orElse(false)!!
                ) {
                    current.forEach(Consumer { key: AbstractMapIngredient? -> map.NODES.remove(key) })
                    return false
                }
            } else {
                if (count == ingredients.size - 1) {
                    map.SPECIAL_NODES.add(Tuple(obj, Either.left(recipe)))
                } else {
                    val branch = Branch()
                    val ok = recurseItemTreeAdd(
                        recipe, ingredients, branch, (index + 1) % ingredients.size,
                        count + 1
                    )
                    if (!ok) {
                        current.forEach(Consumer { key: AbstractMapIngredient? -> map.NODES.remove(key) })
                        return false
                    } else {
                        map.SPECIAL_NODES.add(Tuple(obj, Either.right(branch)))
                    }
                }
            }
        }
        return true
    }

    /**
     * Recursively finds a recipe, top level. call this to find a recipe
     * 
     * @param items the items part
     * @param map   the root branch to search from.
     * @return a recipe
     */
    protected fun recurseItemTreeFind(
        items: MutableList<MutableList<AbstractMapIngredient?>>, map: Branch,
        canHandle: Predicate<IRecipe>
    ): IRecipe? {
        // Try each ingredient as a starting point, adding it to the skiplist.
        for (i in items.indices) {
            val r = recurseItemTreeFind(items, map, canHandle, i, 0, (1L shl i))
            if (r != null) return r
        }
        return null
    }

    /**
     * Recursively finds a recipe
     * 
     * @param items     the items part
     * @param map       the current branch of the tree
     * @param canHandle predicate to test found recipe.
     * @param index     the index of the wrapper to get
     * @param count     how deep we are in recursion, < items.length
     * @param skip      bitmap of items to skip, i.e. which items are used in the
     * recursion.
     * @return a recipe
     */
    protected fun recurseItemTreeFind(items: MutableList<MutableList<AbstractMapIngredient?>>, map: Branch, canHandle: Predicate<IRecipe>, index: Int, count: Int, skip: Long): IRecipe? {
        if (count == items.size) return null
        val wr = items[index]
        // Iterate over current level of nodes.
        for (t in wr) {
            val result = map.NODES[t]
            if (result != null) {
                // Either return recipe or continue branch.
                val r = result.map({ left ->
                    for (recipe in left!!) {
                        if (canHandle.test(recipe) && !recipe.isFake()) {
                            return@map recipe
                        }
                    }
                    null
                }, { right -> callback(items, right!!, canHandle, index, count, skip) })
                if (r != null) return r
            }
            if (map.SPECIAL_NODES.isNotEmpty()) {
                // Iterate over special nodes.
                for (tuple in map.SPECIAL_NODES) {
                    val special: AbstractMapIngredient = tuple.getA()
                    if (special == t) {
                        return tuple.getB().map<IRecipe?>(
                            { r -> if (canHandle.test(r)) r else null },
                            { branch -> callback(items, branch!!, canHandle, index, count, skip) })
                    }
                }
            }
        }
        return null
    }

    private fun callback(items: MutableList<MutableList<AbstractMapIngredient?>>, map: Branch, canHandle: Predicate<IRecipe>, index: Int, count: Int, skip: Long): IRecipe? {
        // We loop around items.size() if we reach the end.
        var counter = (index + 1) % items.size
        while (counter != index) {
            // Have we already used this ingredient? If so, skip this one.
            if (((skip and (1L shl counter)) == 0L)) {
                // Recursive call.
                val found = recurseItemTreeFind(items, map, canHandle, counter, count + 1, skip or (1L shl counter))
                if (found != null) return found
            }
            counter = (counter + 1) % items.size
        }
        return null
    }

    override fun find(items: Array<ItemStack>, fluids: Array<FluidStack>, tier: Tier, canHandle: Predicate<IRecipe>): IRecipe? {
        // First, check if items and fluids are valid.
        if (items.size + fluids.size > Long.SIZE_BITS) {
            onInvalidData(
                "ERROR! TOO LARGE INPUT IN RECIPEMAP, time to fix a real bitmap. Probably never get this!"
            )
            return null
        }
        if (items.isEmpty() && fluids.isEmpty()) return null

        // Filter out empty fluids.

        // Build input.
        val list: MutableList<MutableList<AbstractMapIngredient?>> =
            ObjectArrayList<MutableList<AbstractMapIngredient?>>(items.size + fluids.size)
        if (items.isNotEmpty()) {
            buildFromItemStacks(list, uniqueItems(items))
        }
        if (fluids.isNotEmpty()) {
            val stack: MutableList<FluidStack?> = ObjectArrayList<FluidStack?>(fluids.size)
            for (f in fluids) {
                if (!f.isEmpty) stack.add(f)
            }
            if (stack.isNotEmpty()) buildFromFluidStacks(list, stack, false)
        }
        if (list.isEmpty()) return null
        // Find recipe.
        // long current = System.nanoTime();
        val r = recurseItemTreeFind(list, LOOKUP, canHandle)

        // GTLib.LOGGER.info("Time to lookup (µs): " + ((System.nanoTime() -
        // current) / 1000));
        return r
    }

    fun reset() {
        this.RECIPES_TO_COMPILE.clear()
    }

    override fun resetCompiled() {
        this.getRecipes(false).forEach(Consumer { obj: IRecipe? -> obj!!.invalidate() })
        this.LOOKUP.clear()
        this.ROOT.clear()
        this.ROOT_SPECIAL.clear()
    }

    override fun compile(manager: RecipeManager) {
        resetCompiled()
        if (RECIPES_TO_COMPILE.isNotEmpty()) {
            // Recipes with special ingredients have to be compiled first as you cannot
            // verify that a special recipe collides with a regular, but the opposite works.
            val regular: MutableList<IRecipe> = ObjectArrayList(RECIPES_TO_COMPILE.size)
            val special: MutableList<IRecipe> = ObjectArrayList()
            for (recipe in RECIPES_TO_COMPILE) {
                if (recipe.hasSpecialIngredients()) {
                    special.add(recipe)
                } else {
                    regular.add(recipe)
                }
            }
            special.forEach { recipe -> this.compileRecipe(recipe) }
            regular.forEach { recipe -> this.compileRecipe(recipe) }
        }

        this.LOOKUP.finish()
    }

    override fun acceptsItem(item: ItemStack): Boolean {
        val i = MapItemStackIngredient(item, false)
        val j = MapItemIngredient(item.item, false)
        if (ROOT.contains(i)) return true
        if (ROOT.contains(j)) return true
        val tag = MapTagIngredient(null, false)
        return item.item.builtInRegistryHolder().tags().anyMatch { t: TagKey<Item?>? ->
            tag.setTag(t!!.location())
            if (ROOT.contains(tag)) {
                return@anyMatch true
            }
            ROOT_SPECIAL.contains(tag)
        }
    }

    override fun acceptsFluid(fluid: FluidStack): Boolean {
        val i = MapFluidIngredient(fluid, false)
        if (ROOT.contains(i)) return true
        val tag = MapTagIngredient(null, false)
        return fluid.fluid.builtInRegistryHolder().tags().anyMatch { t: TagKey<Fluid?>? ->
            tag.setTag(t!!.location())
            ROOT.contains(tag)
        }
    }

    protected class Branch {
        internal var NODES: MutableMap<AbstractMapIngredient, Either<MutableList<IRecipe>, Branch>> =
            Object2ObjectOpenHashMap()

        internal val SPECIAL_NODES: MutableList<Tuple<AbstractMapIngredient, Either<IRecipe, Branch>>> =
            ObjectArrayList()

        fun getRecipes(filterHidden: Boolean): Stream<IRecipe?> {
            var stream = NODES.values.stream()
                .flatMap<IRecipe?> { t ->
                    t!!.map(
                        { obj -> obj!!.stream() },
                        { branch -> branch!!.getRecipes(filterHidden) })
                }
            if (SPECIAL_NODES.isNotEmpty()) {
                stream = Stream.concat(
                    stream, SPECIAL_NODES.stream()
                        .flatMap { t ->
                            t!!.getB().map(
                                Function { t -> Stream.of(t) },
                                Function { branch -> branch!!.getRecipes(filterHidden) })
                        })
            }
            if (filterHidden) stream = stream.filter { t -> !t!!.isHidden() }
            return stream
        }

        fun clear() {
            NODES = Object2ObjectOpenHashMap()
            SPECIAL_NODES.clear()
        }

        fun finish() {
            // NODES.forEach((k,v) -> v.ifRight(Branch::finish));
            // this.NODES = ImmutableMap.<AbstractMapIngredient, Either<List<Recipe>,
            // Branch>>builder().putAll(NODES).build();
        }
    }

    companion object {
        private val EMPTY_ITEM = arrayOfNulls<ItemStack>(0)
        private val EMPTY_FLUID = arrayOfNulls<FluidStack>(0)

        private val RECIPE_TYPES: MutableSet<RecipeType<out IRecipe>> = HashSet<RecipeType<out IRecipe>>(
            mutableSetOf<RecipeType<Recipe>>(Recipe.RECIPE_TYPE)
        )

        // In the case of split stacks, merge the items, 2 aluminium dust in separate
        // stacks -> 1 stack with additive count.
        fun uniqueItems(input: Array<ItemStack>): Array<ItemStack> {
            val list: MutableList<ItemStack> = ObjectArrayList(input.size)
            loop@ for (item in input) {
                for (obj in list) {
                    if (ItemStack.matches(item, obj)) {
                        obj.grow(item.count)
                        continue@loop
                    }
                }
                // Add a copy here or it might mutate the stack.
                list.add(item.copy())
            }
            return list.toTypedArray<ItemStack>()
        }

        @JvmStatic
        val recipeTypes: Set<RecipeType<out IRecipe>>
            get() = Collections.unmodifiableSet(
                RECIPE_TYPES
            )

        /**
         * Whether or not an ingredient is hashable.
         * 
         * @param i ingredient
         * @return if it can be hashed.
         */
        @JvmStatic
        fun isIngredientSpecial(i: Ingredient): Boolean {
            val clazz: Class<out Ingredient?> = i.javaClass
            return clazz != RecipeIngredient::class.java && (clazz != Ingredient::class.java && clazz != PartialNBTIngredient::class.java && clazz != StrictNBTIngredient::class.java && clazz != CompoundIngredient::class.java)
        }
    }
}
