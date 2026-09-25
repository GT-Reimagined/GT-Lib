@file:JvmName("Utils")
package org.gtreimagined.gtlib.util

import com.google.common.base.CaseFormat
import com.google.common.collect.ImmutableSet
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.advancements.critereon.ContextAwarePredicate
import net.minecraft.advancements.critereon.EnterBlockTrigger
import net.minecraft.advancements.critereon.InventoryChangeTrigger
import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.advancements.critereon.MinMaxBounds
import net.minecraft.advancements.critereon.StatePropertiesPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MobType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.DyeColor
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.BlockHitResult
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.ForgeHooks
import net.minecraftforge.energy.IEnergyStorage
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fluids.capability.IFluidHandler
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.IItemHandlerModifiable
import org.apache.commons.lang3.StringUtils
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTLib
import org.gtreimagined.gtlib.GTLibConfig
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.data.GTLibTags
import org.gtreimagined.gtlib.entity.IRadiationEntity
import org.gtreimagined.gtlib.material.Material
import org.gtreimagined.gtlib.material.MaterialType
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.recipe.IRecipe
import org.gtreimagined.gtlib.recipe.Recipe
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient
import org.gtreimagined.gtlib.registration.IGTObject
import org.gtreimagined.gtlib.tool.GTToolType
import org.gtreimagined.gtlib.tool.IBasicGTTool
import org.gtreimagined.gtlib.tool.IGTTool
import org.gtreimagined.gtlib.tool.behaviour.BehaviourTreeFelling
import org.gtreimagined.tesseract.api.eu.IEnergyHandler
import org.gtreimagined.tesseract.api.hu.IHeatHandler
import java.awt.Color
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Collections
import java.util.Locale
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.function.Supplier
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.BooleanArray
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.RuntimeException
import kotlin.apply
import kotlin.arrayOf
import kotlin.arrayOfNulls
import kotlin.assert
import kotlin.check
import kotlin.code
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.MutableSet
import kotlin.collections.dropLastWhile
import kotlin.collections.indices
import kotlin.collections.isEmpty
import kotlin.collections.toTypedArray
import kotlin.math.max
import kotlin.math.min
import kotlin.text.indexOf
import kotlin.text.isEmpty
import kotlin.text.split
import kotlin.text.substring
import kotlin.text.toRegex

private val DECIMAL_FORMAT = NumberFormat.getInstance(Locale.US) as DecimalFormat
private val DECIMAL_SYMBOLS = DECIMAL_FORMAT.decimalFormatSymbols.apply {
    groupingSeparator = ' '
}

/**
 * Returns true of A is not empty, has the same Item and damage is equal to B
 */
fun equals(a: ItemStack, b: ItemStack): Boolean {
    return ItemStack.isSameItem(a, b)
}

/**
 * Returns true of A has the same Fluid as B
 */
fun equals(a: FluidStack?, b: FluidStack?): Boolean {
    if (a === b) return true
    if (a == null || b == null) return false

    return a.fluid === b.fluid && a.tag === b.tag
}

/**
 * Returns true if A equals() B and A amount >= B amount
 */
fun contains(a: ItemStack, b: ItemStack): Boolean {
    return equals(a, b) && a.count >= b.count
}

/**
 * Returns true if A equals() B and A amount >= B amount
 */
fun contains(a: FluidStack, b: FluidStack): Boolean {
    return equals(a, b) && a.amount >= b.amount
}

/**
 * Returns the index of an item in a list, or -1 if not found
 */
fun contains(list: MutableList<ItemStack>, item: ItemStack): Int {
    val size = list.size
    for (i in 0..<size) {
        if (equals(list[i], item)) return i
    }
    return -1
}

fun dirFromState(state: BlockState): Direction {
    if (state.hasProperty(BlockStateProperties.FACING)) return state.getValue(BlockStateProperties.FACING)
    return state.getValue(BlockStateProperties.HORIZONTAL_FACING)
}

fun extractAny(handler: IItemHandler): ItemStack {
    for (i in 0..<handler.slots) {
        val stack = handler.extractItem(
            i,
            min(handler.getStackInSlot(i).count, handler.getStackInSlot(i).maxStackSize),
            false
        )
        if (!stack.isEmpty) return stack
    }
    return ItemStack.EMPTY
}

/**
 * Returns the index of a fluid in a list, or -1 if not found
 */
fun contains(list: MutableList<FluidStack>, fluid: FluidStack): Int {
    val size = list.size
    for (i in 0..<size) {
        if (equals(list[i], fluid)) return i
    }
    return -1
}

/**
 * Merges B into A
 */
fun mergeItems(a: MutableList<ItemStack>, b: MutableList<ItemStack>): MutableList<ItemStack> {
    var position: Int
    val size = b.size
    for (stack in b) {
        if (stack.isEmpty) continue
        position = contains(a, stack)
        if (position == -1) a.add(stack)
        else a[position].grow(stack.count)
    }
    return splitStacks(a)
}

fun tryCondenseInventory(itemHandler: IItemHandlerModifiable) {
    tryCondenseInventory(itemHandler, 0, itemHandler.slots)
}

fun tryCondenseInventory(tile: IItemHandlerModifiable, startSlot: Int, endSlot: Int) {
    for (i in startSlot..<endSlot) {
        for (j in startSlot..<endSlot) {
            if (i == j) {
                continue
            }
            val stack1 = tile.getStackInSlot(i)
            val stack2 = tile.getStackInSlot(j)
            if (!stack1.isEmpty && !stack2.isEmpty && (equals(stack1, stack2) && stack1.count < stack1.maxStackSize)) {
                val max = stack1.maxStackSize - stack1.count
                val available = stack2.count
                val size = Mth.clamp(available, 1, max)
                stack1.grow(size)
                stack2.shrink(size)
            }
            if (stack2.isEmpty && !stack1.isEmpty && j < i) {
                tile.setStackInSlot(j, stack1.copy())
                tile.setStackInSlot(i, ItemStack.EMPTY)
            }
        }
    }
}

fun splitStacks(stacks: MutableList<ItemStack>): MutableList<ItemStack> {
    val returned: MutableList<ItemStack> = ArrayList()
    for (stack in stacks) {
        if (stack.count > stack.maxStackSize) {
            var left = stack.count
            while (left > 0) {
                val toAdd: ItemStack = ca(min(stack.maxStackSize, left), stack)
                left -= toAdd.count
                returned.add(toAdd)
            }
        } else {
            returned.add(stack)
        }
    }
    return returned
}

/**
 * Merges two Lists of FluidStacks, ignoring max amount
 */
fun mergeFluids(a: MutableList<FluidStack>, b: MutableList<FluidStack>): MutableList<FluidStack> {
    var position: Int
    val size = b.size
    for (stack in b) {
        position = contains(a, stack)
        if (position == -1) a.add(stack)
        else a[position].amount += stack.amount
    }
    return a
}

fun ca(amount: Int, toCopy: ItemStack): ItemStack {
    val stack = toCopy.copy()
    stack.count = amount
    return stack
}

fun ca(amount: Int, toCopy: FluidStack): FluidStack {
    val stack = toCopy.copy()
    if (!stack.isEmpty) {
        stack.amount = amount
    }
    return stack
}

fun damageStack(stack: ItemStack, player: LivingEntity) {
    var durability = 1
    if (stack.item is IGTTool) {
        durability = (stack.item as IGTTool).getGTToolType().useDurability
    }
    damageStack(durability, stack, player)
}


fun damageStack(stack: ItemStack, hand: InteractionHand, player: LivingEntity) {
    var durability = 1
    if (stack.item is IGTTool) {
        durability = (stack.item as IGTTool).getGTToolType().useDurability
    }
    stack.hurtAndBreak(durability, player) { p ->
        p.broadcastBreakEvent(hand)
    }
}


fun damageStack(durability: Int, stack: ItemStack, player: LivingEntity) {
    stack.hurtAndBreak(durability, player) { p ->
        p.broadcastBreakEvent(
            EquipmentSlot.MAINHAND
        )
    }
}

fun mul(amount: Int, stack: ItemStack): ItemStack {
    return ca(stack.count * amount, stack)
}

fun mul(amount: Int, stack: FluidStack): FluidStack {
    return ca(stack.amount * amount, stack)
}

fun hasNoConsumeTag(stack: FluidStack): Boolean {
    return stack.tag != null && stack.tag.contains(Ref.KEY_STACK_NO_CONSUME)
}

fun areItemsValid(vararg items: ItemStack): Boolean {
    if (items.isEmpty()) return false
    for (item in items) {
        if (item.isEmpty) return false
    }
    return true
}

fun areItemsValid(vararg itemArrays: Array<ItemStack>): Boolean {
    for (itemArray in itemArrays) {
        if (!areItemsValid(*itemArray)) return false
    }
    return true
}

fun areFluidsValid(vararg fluids: FluidStack): Boolean {
    if (fluids.isEmpty()) return false
    for (fluid in fluids) {
        if (fluid.fluid === Fluids.EMPTY) return false
    }
    return true
}

fun areFluidsValid(vararg fluids: FluidIngredient): Boolean {
    if (fluids.isEmpty()) return false
    for (fluid in fluids) {
        if (!areFluidsValid(*fluid.getStacks())) return false
    }
    return true
}


fun areFluidsValid(vararg fluidArrays: Array<FluidStack>): Boolean {
    for (fluidArray in fluidArrays) {
        if (!areFluidsValid(*fluidArray)) return false
    }
    return true
}

fun doItemsMatchAndSizeValid(a: Array<ItemStack>?, b: Array<ItemStack>?): Boolean {
    if (a == null || b == null) return false
    var matchCount = 0
    for (stack in a) {
        for (itemStack in b) {
            if (contains(itemStack, stack)) {
                matchCount++
                break
            }
        }
    }
    return matchCount >= a.size
}

fun doItemsMatchAndSizeValid(a: MutableList<Ingredient>?, b: Array<ItemStack?>?): Boolean {
    if (a == null || b == null) return false
    var matchCount = 0
    for (stack in a) {
        for (itemStack in b) {
            if (stack.test(itemStack)) {
                matchCount++
                break
            }
        }
    }
    return matchCount >= a.size
}

fun doFluidsMatchAndSizeValid(a: Array<FluidStack>?, b: Array<FluidStack>?): Boolean {
    if (a == null && b == null) return true
    if (a == null || b == null) return false
    var matchCount = 0
    for (fluidStack in a) {
        for (stack in b) {
            if (contains(stack, fluidStack)) {
                matchCount++
                break
            }
        }
    }
    return matchCount >= a.size
}

fun transferItems(from: IItemHandler, to: IItemHandler?, once: Boolean): Boolean {
    return transferItems(from, to, once, Predicate { stack: ItemStack? -> true })
}

fun transferItems(from: IItemHandler, to: IItemHandler?, once: Boolean, filter: Predicate<ItemStack?>): Boolean {
    var successful = false
    for (i in 0..<from.slots) {
        val toInsert =
            from.extractItem(i, min(from.getStackInSlot(i).count, from.getStackInSlot(i).maxStackSize), true)
        if (toInsert.isEmpty || !filter.test(toInsert)) {
            continue
        }
        val inserted = insertItem(to, toInsert, true)
        if (inserted.isEmpty) {
            insertItem(to, toInsert, false)
            from.extractItem(i, toInsert.count, false)
            if (!successful) successful = true
            if (once) break
        } else if (inserted.count < toInsert.count) {
            val actual = toInsert.count - inserted.count
            toInsert.count -= inserted.count
            insertItem(to, toInsert, false)
            from.extractItem(i, actual, false)
            if (!successful) successful = true
            if (once) break
        }
    }
    return successful
}

fun insertItem(to: IItemHandler?, stack: ItemStack, simulate: Boolean): ItemStack {
    var stack = stack
    if (to == null || stack.isEmpty) return stack

    for (i in 0..<to.slots) {
        stack = to.insertItem(i, stack, simulate)
        if (stack.isEmpty) {
            return ItemStack.EMPTY
        }
    }

    return stack
}

/**
 * Transfers up to maxAmps between energy handlers, without loss.
 *
 * @param from the handler to extract from
 * @param to   the handler to insert
 * @return if energy was inserted
 */
fun transferEnergy(from: IEnergyHandler, to: IEnergyHandler): Boolean {
    var transferred = false
    for (amp in 0..<from.availableAmpsOutput()) {
        val extracted = from.extractEu(from.getOutputVoltage(), true)
        if (extracted > 0) {
            val insertEu = to.insertEu(extracted, true)
            if (insertEu > 0) {
                from.extractEu(to.insertEu(extracted, false), false)
                transferred = true
            }
        }
    }
    return transferred
}

fun transferEnergy(from: IEnergyStorage, to: IEnergyStorage): Boolean {
    val extracted = from.extractEnergy(Int.MAX_VALUE, true)
    if (extracted > 0) {
        val inserted = to.receiveEnergy(extracted, false)
        if (inserted > 0) {
            from.extractEnergy(inserted, false)
            return true
        }
    }
    return false
}

fun transferHeat(from: IHeatHandler, to: IHeatHandler): Boolean {
    val extracted = from.extract(Int.MAX_VALUE, true)
    if (extracted > 0) {
        val inserted = to.insert(extracted, false)
        if (inserted > 0) {
            from.extract(inserted, false)
            return true
        }
    }
    return false
}

fun addEnergy(to: IEnergyHandler, eu: Long): Boolean {
    return to.insertEu(eu, false) > 0
}

fun removeEnergy(from: IEnergyHandler, eu: Long): Boolean {
    return from.extractEu(eu, false) > 0
}

/**
 * Transfer energy with loss.
 *
 * @param from energy handler to extract from
 * @param to   energy handler to insert from
 * @param loss energy loss
 * @return number of amps
 */
fun transferEnergyWithLoss(from: IEnergyHandler, to: IEnergyHandler, loss: Int): Boolean {
    var transferred = false
    for (amp in 0..<from.availableAmpsOutput()) {
        val extracted = from.extractEu(from.getOutputVoltage(), true)
        if (extracted > 0) {
            val insertEu = to.insertEu(extracted - loss, true)
            if (insertEu > 0) {
                from.extractEu(to.insertEu(extracted - loss, false) + loss, false)
                transferred = true
            }
        }
    }
    return transferred
}

fun transferFluids(from: IFluidHandler, to: IFluidHandler, cap: Int, filter: Predicate<FluidStack?>): Boolean {
    var successful = false
    for (i in 0..<to.tanks) {
        //if (i >= from.getTanks()) break;
        var toInsert: FluidStack?
        for (j in 0..<from.tanks) {
            if (cap > 0) {
                var fluid = from.getFluidInTank(j)
                if (fluid.isEmpty || !filter.test(fluid)) {
                    continue
                }
                fluid = fluid.copy()
                val toDrain = Math.min(cap, fluid.amount)
                fluid.amount = toDrain
                toInsert = from.drain(fluid, IFluidHandler.FluidAction.SIMULATE)
            } else {
                toInsert = from.drain(from.getFluidInTank(j), IFluidHandler.FluidAction.SIMULATE)
            }
            val filled = to.fill(toInsert, IFluidHandler.FluidAction.SIMULATE)
            if (filled > 0) {
                toInsert.amount = filled
                to.fill(from.drain(toInsert, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE)
                successful = true
            }
        }
    }
    return successful
}

fun transferFluids(from: IFluidHandler, to: IFluidHandler, cap: Int): Boolean {
    return transferFluids(from, to, cap) { true }
}

fun entitiesAround(level: Level, pos: BlockPos, cb: BiConsumer<Direction?, BlockEntity?>) {
    var mutPos = int3()
    for (dir in Ref.DIRS) {
        mutPos.set(pos)
        mutPos = mutPos.offset(1, dir)
        val ent = level.getBlockEntity(mutPos) ?: continue
        cb.accept(dir, ent)
    }
}

fun applyRadioactivity(aEntity: Entity?, aLevel: Int, aAmountOfItems: Int): Boolean {
    if (aLevel > 0 && aEntity is LivingEntity && aEntity.isAlive && aEntity.mobType !== MobType.UNDEAD && aEntity.mobType !== MobType.ARTHROPOD && !isFullHazmatSuit(aEntity)) {
        val radiationEntity = aEntity as IRadiationEntity
        radiationEntity.changeRadiation(aLevel * aAmountOfItems)
        return true
    }
    return false
}

fun isFullHazmatSuit(livingEntity: LivingEntity): Boolean {
    var radiationProof = 0
    for (stack in livingEntity.armorSlots) {
        if (stack.`is`(GTLibTags.RADIATION_PROOF)) radiationProof++
    }
    return radiationProof == 4
}

fun transferFluids(from: IFluidHandler, to: IFluidHandler): Boolean {
    return transferFluids(from, to, -1)
}

/**
 * Creates a new [EnterBlockTrigger] for use with recipe unlock criteria.
 */
fun enteredBlock(blockIn: Block?): EnterBlockTrigger.TriggerInstance {
    return EnterBlockTrigger.TriggerInstance(ContextAwarePredicate.ANY, blockIn, StatePropertiesPredicate.ANY)
}

/**
 * Creates a new [InventoryChangeTrigger] that checks for a player having a certain item.
 */
fun hasItem(itemIn: ItemLike): InventoryChangeTrigger.TriggerInstance {
    return hasItem(ItemPredicate.Builder.item().of(itemIn).build())
}

/**
 * Creates a new [InventoryChangeTrigger] that checks for a player having an item within the given tag.
 */
fun hasItem(tagIn: TagKey<Item?>): InventoryChangeTrigger.TriggerInstance {
    return hasItem(ItemPredicate.Builder.item().of(tagIn).build())
}

/**
 * Creates a new [InventoryChangeTrigger] that checks for a player having an item within the given tag.
 */
@SafeVarargs
fun hasItems(vararg tagIn: TagKey<Item?>): InventoryChangeTrigger.TriggerInstance {
    val predicates = arrayOfNulls<ItemPredicate>(tagIn.size)
    for (i in tagIn.indices) {
        val tag: TagKey<Item?> = tagIn[i]
        predicates[i] = ItemPredicate.Builder.item().of(tag).build()
    }
    return hasItem(*predicates)
}

/**
 * Creates a new [InventoryChangeTrigger] that checks for a player having a certain item.
 */
fun hasItem(vararg predicates: ItemPredicate?): InventoryChangeTrigger.TriggerInstance {
    return InventoryChangeTrigger.TriggerInstance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, predicates)
}

fun translatable(key: String, vararg objects: Any?): MutableComponent {
    return Component.translatable(key, *objects)
}

fun literal(text: String): MutableComponent {
    return Component.literal(text)
}

fun getModName(modid: String?): String? {
    return ModList.get().getModContainerById(modid)
        .map { m -> m.getModInfo().displayName }.orElse(modid)
}

fun getVoltageTier(voltage: Long): Int {
    var tier = 0
    for (i in Ref.V.indices) {
        if (voltage <= Ref.V[i]) {
            tier = i
            break
        }
    }
    return max(1, tier)
}

/**
 * Safe version of world.getTileEntity
 */
fun getTile(reader: BlockGetter?, pos: BlockPos): BlockEntity? {
    if (reader == null) return null
    return reader.getBlockEntity(pos)
}

fun getTileFromBuf(buf: FriendlyByteBuf): BlockEntity? {
    return DistExecutor.unsafeRunForDist( {
        Supplier {
            GTLib.PROXY.getClientWorld().getBlockEntity(buf.readBlockPos())
        }
    }) {
        Supplier {
            throw RuntimeException("Shouldn't be called on server!")
        }
    }
}

fun <T> unsafeRunForDist(clientTarget: Supplier<Supplier<T?>?>, serverTarget: Supplier<Supplier<T?>?>): T? {
    return when (FMLEnvironment.dist) {
        Dist.CLIENT -> clientTarget.get()!!.get()
        Dist.DEDICATED_SERVER -> serverTarget.get()!!.get()
    }
}

fun unsafeRunForDistVoid(clientTarget: Supplier<Runnable?>, serverTarget: Supplier<Runnable?>) {
    when (FMLEnvironment.dist) {
        Dist.CLIENT -> clientTarget.get()!!.run()
        Dist.DEDICATED_SERVER -> serverTarget.get()!!.run()
    }
}

/**
 * Syncs NBT between Client & Server
 */
fun markTileForNBTSync(tile: BlockEntity) {
    val state = tile.getLevel()!!.getBlockState(tile.blockPos)
    tile.getLevel()!!.sendBlockUpdated(tile.blockPos, state, state, 3)
}

/**
 * Sends block update to clients
 */
fun markTileForRenderUpdate(tile: BlockEntity) {
    val state = tile.getLevel()!!.getBlockState(tile.blockPos)
    if (tile.getLevel()!!.isClientSide) {
        tile.getLevel()!!.sendBlockUpdated(tile.blockPos, state, state, 11)
        tile.getLevel()!!.modelDataManager!!.requestRefresh(tile)
    }
}

private val TRANSFORM = arrayOf(
    arrayOf( //DOWN
        Direction.SOUTH,
        Direction.NORTH,
        Direction.DOWN,
        Direction.UP,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf( //UP
        Direction.NORTH,
        Direction.SOUTH,
        Direction.UP,
        Direction.DOWN,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf( //NORTH
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf(
        //SOUTH
        Direction.DOWN,
        Direction.UP,
        Direction.SOUTH,
        Direction.NORTH,
        Direction.EAST,
        Direction.WEST,
    ),
    arrayOf( //WEST
        Direction.DOWN,
        Direction.UP,
        Direction.WEST,
        Direction.EAST,
        Direction.SOUTH,
        Direction.NORTH
    ),
    arrayOf( //EAST
        Direction.DOWN,
        Direction.UP,
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    )
)

private val TRANSFORM_INVERSE = arrayOf(
    arrayOf( //DOWN
        Direction.NORTH,
        Direction.SOUTH,
        Direction.UP,
        Direction.DOWN,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf( //UP
        Direction.SOUTH,
        Direction.NORTH,
        Direction.DOWN,
        Direction.UP,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf( //NORTH
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf(
        //SOUTH
        Direction.DOWN,
        Direction.UP,
        Direction.SOUTH,
        Direction.NORTH,
        Direction.EAST,
        Direction.WEST,
    ),
    arrayOf( //WEST
        Direction.DOWN,
        Direction.UP,
        Direction.EAST,
        Direction.WEST,
        Direction.NORTH,
        Direction.SOUTH
    ),
    arrayOf( //EAST
        Direction.DOWN,
        Direction.UP,
        Direction.WEST,
        Direction.EAST,
        Direction.SOUTH,
        Direction.NORTH
    )
)

fun rotateModel(facing: Direction?, side: Direction): Direction? {
    if (facing == Direction.UP || facing == Direction.DOWN) {
        return TRANSFORM[facing.get3DDataValue()]!![side.get3DDataValue()]
    }
    if (side == Direction.DOWN || side == Direction.UP) return side
    if (facing == Direction.WEST) return side.counterClockWise
    if (facing == Direction.EAST) return side.clockWise
    if (facing == Direction.SOUTH) return side
    return side.opposite
}

fun rotate(facing: Direction, side: Direction): Direction? {
    return TRANSFORM[facing.get3DDataValue()]!![side.get3DDataValue()]
}

fun rotateInverse(facing: Direction, side: Direction): Direction? {
    return TRANSFORM_INVERSE[facing.get3DDataValue()]!![side.get3DDataValue()]
}

private val COVER_ROTATION = arrayOf(
    arrayOf( //DOWN
        Direction.NORTH,
        Direction.SOUTH,
        Direction.DOWN,
        Direction.DOWN,
        Direction.DOWN,
        Direction.DOWN
    ),
    arrayOf( //UP
        Direction.SOUTH,
        Direction.NORTH,
        Direction.DOWN,
        Direction.DOWN,
        Direction.DOWN,
        Direction.DOWN
    ),
    arrayOf( //NORTH
        Direction.UP,
        Direction.DOWN,
        Direction.SOUTH,
        Direction.NORTH,
        Direction.WEST,
        Direction.EAST
    ),
    arrayOf(
        //SOUTH
        Direction.DOWN,
        Direction.UP,
        Direction.NORTH,
        Direction.SOUTH,
        Direction.EAST,
        Direction.WEST,
    ),
    arrayOf( //WEST
        Direction.WEST,
        Direction.WEST,
        Direction.EAST,
        Direction.WEST,
        Direction.SOUTH,
        Direction.NORTH
    ),
    arrayOf( //EAST
        Direction.EAST,
        Direction.EAST,
        Direction.WEST,
        Direction.EAST,
        Direction.NORTH,
        Direction.SOUTH
    )
)

fun coverRotateFacing(toRotate: Direction, rotateBy: Direction): Direction? {
    return COVER_ROTATION[toRotate.get3DDataValue()]!![rotateBy.get3DDataValue()]
}

fun getOffsetFacing(center: BlockPos, offset: BlockPos): Direction? {
    if (center.x == offset.x + 1) return Direction.WEST
    else if (center.x + 1 == offset.x) return Direction.EAST
    else if (center.z == offset.z + 1) return Direction.NORTH
    else if (center.z + 1 == offset.z) return Direction.SOUTH
    else if (center.y == offset.y + 1) return Direction.DOWN
    else if (center.y + 1 == offset.y) return Direction.UP
    else return null
}

const val INTERACTION_OFFSET = 0.25

fun getInteractSide(res: BlockHitResult): Direction {
    val vec = res.getLocation()
    return getInteractSide(
        res.direction,
        vec.x.toFloat() - res.blockPos.x,
        vec.y.toFloat() - res.blockPos.y,
        vec.z.toFloat() - res.blockPos.z
    )
}

fun getInteractSide(side: Direction, x: Float, y: Float, z: Float): Direction {
    val backSide = side.opposite
    when (side.get3DDataValue()) {
        0, 1 -> {
            if (x < INTERACTION_OFFSET) {
                if (z < INTERACTION_OFFSET) return backSide
                if (z > 1 - INTERACTION_OFFSET) return backSide
                return Direction.WEST
            }
            if (x > 1 - INTERACTION_OFFSET) {
                if (z < INTERACTION_OFFSET) return backSide
                if (z > 1 - INTERACTION_OFFSET) return backSide
                return Direction.EAST
            }
            if (z < INTERACTION_OFFSET) return Direction.NORTH
            if (z > 1 - INTERACTION_OFFSET) return Direction.SOUTH
            return side
        }

        2, 3 -> {
            if (x < INTERACTION_OFFSET) {
                if (y < INTERACTION_OFFSET) return backSide
                if (y > 1 - INTERACTION_OFFSET) return backSide
                return Direction.WEST
            }
            if (x > 1 - INTERACTION_OFFSET) {
                if (y < INTERACTION_OFFSET) return backSide
                if (y > 1 - INTERACTION_OFFSET) return backSide
                return Direction.EAST
            }
            if (y < INTERACTION_OFFSET) return Direction.DOWN
            if (y > 1 - INTERACTION_OFFSET) return Direction.UP
            return side
        }

        4, 5 -> {
            if (z < INTERACTION_OFFSET) {
                if (y < INTERACTION_OFFSET) return backSide
                if (y > 1 - INTERACTION_OFFSET) return backSide
                return Direction.NORTH
            }
            if (z > 1 - INTERACTION_OFFSET) {
                if (y < INTERACTION_OFFSET) return backSide
                if (y > 1 - INTERACTION_OFFSET) return backSide
                return Direction.SOUTH
            }
            if (y < INTERACTION_OFFSET) return Direction.DOWN
            if (y > 1 - INTERACTION_OFFSET) return Direction.UP
            return side
        }
    }
    return side
}

fun getCubicPosArea(
    area: int3,
    side: Direction?,
    origin: BlockPos,
    player: Player,
    excludeAir: Boolean
): MutableSet<BlockPos?> {
    val xRadius: Int
    val yRadius: Int
    val zRadius: Int
    val center: BlockPos

    if (side == null) {
        center = origin
        xRadius = area.x
        yRadius = area.y
        zRadius = area.z
    } else {
        center = origin.relative(side.opposite, area.z)
        if (side.axis === Direction.Axis.Y) {
            xRadius = if (player.direction.axis === Direction.Axis.X) area.y else area.x
            yRadius = area.z
            zRadius = if (player.direction.axis === Direction.Axis.Z) area.y else area.x
        } else {
            xRadius = if (player.direction.axis === Direction.Axis.X) area.z else area.x
            yRadius = area.y
            zRadius = if (player.direction.axis === Direction.Axis.Z) area.z else area.x
        }
    }

    val set: MutableSet<BlockPos?> = ObjectOpenHashSet<BlockPos?>()
    var state: BlockState?
    for (x in center.x - xRadius..center.x + xRadius) {
        for (y in center.y - yRadius..center.y + yRadius) {
            for (z in center.z - zRadius..center.z + zRadius) {
                val harvestPos = BlockPos(x, y, z)
                if (harvestPos == origin) continue
                if (excludeAir) {
                    state = player.level().getBlockState(harvestPos)
                    if (state.isAir) continue
                }
                set.add(BlockPos(x, y, z))
            }
        }
    }
    return set
}

fun createExplosion(world: Level?, pos: BlockPos, explosionRadius: Float, modeIn: Level.ExplosionInteraction) {
    if (world != null) {
        if (!world.isClientSide) {
            world.explode(null, pos.x.toDouble(), pos.y + 0.0625, pos.z.toDouble(), explosionRadius, modeIn)
        } else {
            world.addParticle(ParticleTypes.SMOKE, pos.x.toDouble(), pos.y + 0.5, pos.z.toDouble(), 0.0, 0.0, 0.0)
        }
        if (modeIn != Level.ExplosionInteraction.NONE) {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState())
        }
    }
}

fun createFireAround(world: Level?, pos: BlockPos) {
    if (world != null) {
        var fired = false
        for (side in Ref.DIRS) {
            val offset = pos.relative(side)
            if (world.getBlockState(offset) === Blocks.AIR.defaultBlockState()) {
                world.setBlockAndUpdate(offset, Blocks.FIRE.defaultBlockState())
                fired = true
            }
        }
        if (!fired) world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState())
    }
}

/**
 * Custom Block Breaking implementation, normally used when breaking extra blocks during/after onBlockDestroyed
 *
 * @param world  World instance
 * @param player Player instance, preferably not a ClientPlayerEntity as BlockBreakEvent won't be fired
 * @param stack  Player's heldItemStack
 * @param pos    BlockPos of the block that is about to be destroyed
 * @param damage Damage that should be taken for the ItemStack
 * @return true if block is successfully broken, false if not
 */
fun breakBlock(world: Level, player: Player?, stack: ItemStack, pos: BlockPos, damage: Int): Boolean {
    if (world.isClientSide) return false
    val state = world.getBlockState(pos)
    val serverPlayer = if (player == null) null else (player as ServerPlayer)
    val exp = if (player == null) -1 else ForgeHooks.onBlockBreakEvent(world, serverPlayer!!.gameMode.gameModeForPlayer, serverPlayer, pos)
    val fluidState = world.getFluidState(pos)
    val destroyed = world.setBlockAndUpdate(pos, fluidState.createLegacyBlock()) // world.destroyBlock(pos, !player.isCreative(), player);
    if (destroyed) {
        if (player != null) {
            if (state.canHarvestBlock(world, pos, player)) {
                state.block.playerDestroy(world, player, pos, state, world.getBlockEntity(pos), stack)
            }
            stack.hurtAndBreak(if (state.getDestroySpeed(world, pos) != 0.0f) damage else 0, player) { onBroken ->
                onBroken!!.broadcastBreakEvent(
                    EquipmentSlot.MAINHAND
                )
            }
        }
    }
    if (exp > 0) state.block.popExperience(world as ServerLevel, pos, exp)
    return destroyed
}

/**
 * Performs tree logging. If Configs.GAMEPLAY.TREE_DETECTION is true, it will do a more complex search for branches, if set to false, it will do a normal vertical loop only
 *
 * @param stack  Player's heldItem
 * @param start  onBlockDestroy's BlockPos
 * @param player ServerPlayerEntity instance
 * @param world  World instance
 * @return if tree logging was successful
 */
fun treeLogging(tool: GTToolType, stack: ItemStack, start: BlockPos, player: Player, world: Level): Boolean {
    val harvested = BooleanArray(1)
    if (!GTLibConfig.SMARTER_TREE_DETECTION.get()) {
        val tpCompare = world.getBlockState(start)
        if (!BehaviourTreeFelling.isLog(tpCompare)) return false
        for (y in start.y + 1..<start.y + world.height) {
            if (stack.isEmpty) break
            val tempPos = BlockPos(start.x, y, start.z)
            val state = world.getBlockState(tempPos)
            if (state.`is`(BlockTags.LOGS)) {
                if (breakBlock(world, player, stack, tempPos, tool.useDurability)) {
                    harvested[0] = true
                } else {
                    break
                }
            } else {
                break
            }
        }
    } else {
        val stopped = BooleanArray(1)
        BehaviourTreeFelling.findTree(world, start).logs.forEach { b ->
            if (stack.isEmpty) return@forEach
            if (stopped[0]) return@forEach
            val state = world.getBlockState(b)
            if (state.isAir || !ForgeHooks.isCorrectToolForDrops(state, player)) return@forEach
            else if (state.`is`(BlockTags.LOGS)) {
                if (breakBlock(world, player, stack, b, tool.useDurability)) {
                    harvested[0] = true
                } else {
                    stopped[0] = true
                }
            }
        }
    }
    return harvested[0]
}

/**
 * Gets harvestables out of a ImmutableSet of block positions, this is IGTTool sensitive, and will not work for normal ItemStacks, for that, check out BlockState#isToolEffective
 *
 * @param world  World instance of the PlayerEntity
 * @param player PlayerEntity that is breaking the blocks
 * @param column vertical amount of blocks
 * @param row    horizontal amount of blocks
 * @param depth  depth amount of blocks
 * @return set of harvestable BlockPos in the specified range with specified player
 */
fun getHarvestableBlocksToBreak(world: Level, player: Player, tool: IBasicGTTool, stack: ItemStack, column: Int, row: Int, depth: Int): ImmutableSet<BlockPos> {
    val totalBlocks = getBlocksToBreak(world, player, column, row, depth)
    return totalBlocks.stream().filter { b ->
        tool.genericIsCorrectToolForDrops(stack, world.getBlockState(b)) && world.getBlockState(b).getDestroySpeed(world, b) >= 0
    }.collect(
        ImmutableSet.toImmutableSet()
    )
}

/**
 * Gets blocks to be broken in a column (radius), row (radius) and depth. This is axis-sensitive
 *
 * @param world  = World instance of the PlayerEntity
 * @param player = PlayerEntity that is breaking the blocks
 * @param column = vertical amount of blocks
 * @param row    = horizontal amount of blocks
 * @param depth  = depth amount of blocks
 * @return set of BlockPos in the specified range
 */
fun getBlocksToBreak(world: Level, player: Player, column: Int, row: Int, depth: Int): ImmutableSet<BlockPos> {
    val lookPos = player.getEyePosition(1f)
    val rotation = player.getViewVector(1f)
    val realLookPos = lookPos.add(rotation.x * 5, rotation.y * 5, rotation.z * 5)
    val result =
        world.clip(ClipContext(lookPos, realLookPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player))
    val playerDirection = player.direction
    val playerAxis = playerDirection.axis
    val faceAxis = result.direction.axis
    val faceAxisDir = result.direction.axisDirection
    val blockPositions = ImmutableSet.builder<BlockPos>()
    if (faceAxis.isVertical) {
        val isX = playerAxis === Direction.Axis.X
        val isDown = faceAxisDir == Direction.AxisDirection.NEGATIVE
        for (y in 0..<depth) {
            for (x in (if (isX) -column else -row)..(if (isX) column else row)) {
                for (z in (if (isX) -row else -column)..(if (isX) row else column)) {
                    if (!(x == 0 && y == 0 && z == 0)) blockPositions.add(
                        result.blockPos.offset(x, if (isDown) y else -y, z)
                    )
                }
            }
        }
    } else { // FaceAxis - Horizontal
        val isX = faceAxis === Direction.Axis.X
        val isNegative = faceAxisDir == Direction.AxisDirection.NEGATIVE
        for (x in 0..<depth) {
            for (y in -column..column) {
                for (z in -row..row) {
                    if (!(x == 0 && y == 0 && z == 0)) blockPositions.add(
                        result.blockPos.offset(
                            if (isX) (if (isNegative) x else -x) else (if (isNegative) z else -z),
                            y,
                            if (isX) (if (isNegative) z else -z) else (if (isNegative) x else -x)
                        )
                    )
                }
            }
        }
    }
    return blockPositions.build()
}

/**
 * Scrappy but efficient way of determining an DyeColor from mere RGB values
 *
 * @param rgb int colour
 * @return DyeColor that is the closest to the RGB input
 */
fun determineColour(rgb: Int): DyeColor? {
    val colour = Color(rgb)
    val distances: Double2ObjectMap<DyeColor?> = Double2ObjectOpenHashMap<DyeColor?>()
    for (dyeColour in DyeColor.entries) {
        val enumColour = Color(dyeColour.mapColor.col)
        val distance =
            ((colour.red - enumColour.red) * (colour.red - enumColour.red) + (colour.green - enumColour.green) * (colour.green - enumColour.green) + (colour.blue - enumColour.blue) * (colour.blue - enumColour.blue)).toDouble()
        distances.put(distance, dyeColour)
    }
    return distances.get(Collections.min(distances.keys) as Double)
}

fun lowerUnderscoreToUpperSpaced(string: String?): String? {
    val split = StringUtils.split(string, "_")
    val split2 = arrayOfNulls<String>(split.size)
    for (i in split.indices) {
        var str = split[i]
        if (str.isEmpty() || !Character.isDigit(str[0])) {
            str = underscoreToUpperCamel(str)
        }
        split2[i] = str
    }
    return StringUtils.join(split2, ' ')
}

fun lowerUnderscoreToUpperSpacedRotated(string: String): String? {
    val strings =
        StringUtils.splitByCharacterTypeCamelCase(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string))
    val newStrings = arrayOfNulls<String>(strings.size)

    newStrings[0] = strings[strings.size - 1]
    for (i in 1..<strings.size) {
        newStrings[i] = strings[i - 1]
    }
    return StringUtils.join(newStrings, ' ')
}

fun lowerUnderscoreToUpperSpacedReversed(string: String): String? {
    val strings =
        StringUtils.splitByCharacterTypeCamelCase(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string))
    val newStrings = arrayOfNulls<String>(strings.size)
    for (i in strings.indices) {
        newStrings[i] = strings[(strings.size - 1) - i]
    }
    return StringUtils.join(newStrings, ' ')
}

fun lowerUnderscoreToUpperSpaced(string: String, offset: Int): String? {
    val strings =
        StringUtils.splitByCharacterTypeCamelCase(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string))
    assert(offset > strings.size)
    return StringUtils.join(strings.copyOfRange(offset, strings.size), ' ')
}

fun underscoreToUpperCamel(string: String): String {
    return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, string)
}

/**
 * Used primarily in chemical formula tooltips
 *
 * @param string input
 * @return string with its digits swapped to its subscript variant
 */
fun digitsToSubscript(string: String): String {
    if (string.isEmpty()) return ""
    val chars = string.toCharArray()
    for (i in chars.indices) {
        val index = chars[i].code - '0'.code
        if (index in 0..9) {
            val newChar = '\u2080'.code + index
            chars[i] = newChar.toChar()
        }
    }
    return String(chars)
}

fun getConventionalStoneType(type: StoneType): String? {
    val string = type.id
    // breaks generation in stones with underscores cause the stones are generated without the 2 below lines in the name
    /*int index = string.indexOf("_");
        if (index != -1) return String.join("", string.substring(index + 1), "_", string.substring(0, index));*/
    return string
}

fun getConventionalMaterialType(type: MaterialType<*>): String {
    var id = type.getId()
    val index = id.indexOf("_")
    if (index != -1 && type.isSplitName) {
        id = "${id.substring(0, index)}_${id.substring(index + 1)}s"
        return id
    }
    return if (id[id.length - 1] == 's') (id + "es") else (id + "s")
}

/**
 * Spawns a new item entity
 *
 * @param tile the active tile
 * @param item the item to spawn, 1.
 * @param dir  the direction to spawn it in.
 */
fun dropItemInWorldAtTile(tile: BlockEntity, item: Item, dir: Direction) {
    val entity = ItemEntity(tile.getLevel()!!, (tile.blockPos.x + dir.stepX).toDouble(), (tile.blockPos.y + dir.stepY).toDouble(), (tile.blockPos.z + dir.stepZ).toDouble(), ItemStack(item, 1))
    tile.getLevel()!!.addFreshEntity(entity)
}

fun getLocalizedMaterialType(type: MaterialType<*>): Array<String> {
    val id = type.getId()
    val index = id.indexOf("_")
    if (index != -1 && type.isSplitName) {
        val joined = "${id.substring(0, index)}_${id.substring(index + 1)}"
        return lowerUnderscoreToUpperSpaced(joined)!!.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    }
    return arrayOf(lowerUnderscoreToUpperSpaced(id)!!.replace('_', ' '))
}

fun getLocalizedType(type: IGTObject): String? {
    val id = type.getId()
    if (type is Material) {
        return type.displayNameString
    }
    val index = id.indexOf("_")
    if (index != -1) {
        if (type is MaterialType<*>) {
            return StringUtils.join(*getLocalizedMaterialType(type))
        }
        return StringUtils.replaceChars(lowerUnderscoreToUpperSpaced(id), '_', ' ')
    }
    return StringUtils.capitalize(id)
}

fun getLocalizeStoneType(type: StoneType): String? {
    return getLocalizedType(type)
}

fun doesStackHaveToolTypes(stack: ItemStack, vararg types: TagKey<Item?>): Boolean {
    if (!stack.isEmpty) {
        for (type in types) {
            if (stack.item.builtInRegistryHolder().`is`(type)) {
                return true
            }
        }
    }
    return false
}

fun doesStackHaveToolTypes(stack: ItemStack, vararg types: GTToolType): Boolean {
    val ret: MutableList<TagKey<Item?>?> = ObjectArrayList<TagKey<Item?>?>()
    for (ty in types) {
        ret.add(ty.getForgeTag())
    }
    val t = ret.toTypedArray<TagKey<*>?>() as Array<TagKey<Item?>>
    return doesStackHaveToolTypes(stack, *t)
}


fun isPlayerHolding(player: Player, hand: InteractionHand, vararg t: GTToolType): Boolean {
    return doesStackHaveToolTypes(player.getItemInHand(hand), *t)
}

fun getToolType(player: Player): GTToolType? {
    val stack = player.mainHandItem
    for (ty in GTAPI.all<GTToolType>()) {
        if (!ty.hasOriginalTag()) continue
        if (stack.`is`(ty.getTag())) {
            return ty
        }
    }
    return null
}

/**
 * @return an empty instance of Recipe
 */
fun getEmptyRecipe(): IRecipe {
    return Recipe(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), 1, 1, 0, 1)
}

/**
 * @param msg to be printed with IllegalStateException, normally used when dev/user enters invalid input of data
 */
fun onInvalidData(msg: String?) {
    check(!Ref.DATA_EXCEPTIONS) { msg!! }
    GTLib.LOGGER.error(msg)
}

/**
 * @param msg redirects to the main logger with a border
 */
fun printError(msg: String?) {
    GTLib.LOGGER.error("====================================================")
    GTLib.LOGGER.error(msg)
    GTLib.LOGGER.error("====================================================")
}

fun <T> cast(o: Any?): T? {
    return o as T?
}