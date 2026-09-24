package org.gtreimagined.gtlib.common.event

import com.blamejared.crafttweaker.natives.loot.param.ExpandLootContextParams.tool
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.tags.BlockTags
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.client.event.RecipesUpdatedEvent
import net.minecraftforge.event.*
import net.minecraftforge.event.entity.player.PlayerEvent
import net.minecraftforge.event.level.BlockEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.LogicalSide
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.MissingMappingsEvent
import net.minecraftforge.server.ServerLifecycleHooks
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTLibConfig
import org.gtreimagined.gtlib.GTRemapping
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe
import org.gtreimagined.gtlib.capability.fluid.FluidHandlerItem
import org.gtreimagined.gtlib.data.GTTools
import org.gtreimagined.gtlib.datagen.GTLibDynamics
import org.gtreimagined.gtlib.datagen.GTLoot
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider
import org.gtreimagined.gtlib.item.IFluidItem
import org.gtreimagined.gtlib.network.GTLibNetwork
import org.gtreimagined.gtlib.network.packets.ClientboundWorldgenSyncPacket
import org.gtreimagined.gtlib.pipe.BlockPipe
import org.gtreimagined.gtlib.pipe.TileTicker
import org.gtreimagined.gtlib.proxy.ClientHandler
import org.gtreimagined.gtlib.structure.StructureCache
import org.gtreimagined.gtlib.tool.IGTTool
import org.gtreimagined.gtlib.util.RegistryUtils
import java.util.function.Consumer

object ForgeCommonEvents {
    @SubscribeEvent
    fun onItemCrafted(e: PlayerEvent.ItemCraftedEvent) {
        val inv = e.inventory
        val player = e.entity
        if (!GTLibConfig.PLAY_CRAFTING_SOUNDS.get()) return
        for (i in 0..<inv.containerSize) {
            val tool = inv.getItem(i).item
            if (tool is IGTTool) {
                val type: SoundEvent? = tool.getGTToolType().useSound
                if (type != null) {
                    player.playSound(type, 0.75f, 0.75f)
                }
            }
        }
    }

    @SubscribeEvent
    fun onServerTick(event: TickEvent.ServerTickEvent) {
        TileTicker.onServerWorldTick(ServerLifecycleHooks.getCurrentServer(), event.phase == TickEvent.Phase.START)
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onBlockPlace(event: BlockEvent.EntityPlaceEvent) {
        if (event.placedAgainst.block is BlockPipe<*> && event.placedBlock.block !is BlockPipe<*>) {
            if (event.entity is Player && !event.entity!!.isCrouching) {
                val blockEntity =
                    event.level.getBlockEntity(event.pos.relative(event.entity!!.direction))
                if (blockEntity is BlockEntityPipe<*> && event.placedBlock.block is EntityBlock) {
                    blockEntity.setConnection(event.entity!!.direction.opposite)
                }
            }
        }
    }

    @SubscribeEvent
    fun onAnvilUpdated(event: AnvilUpdateEvent) {
        val left = event.left
        val right = event.right
        val leftTool = left.item
        val rightTool = right.item
        if (left.item === right.item) {
            if (leftTool is IGTTool && rightTool is IGTTool) {
                if (leftTool.getPrimaryMaterial(left) !== rightTool.getPrimaryMaterial(right) ||
                    leftTool.getSecondaryMaterial(left) !== rightTool.getSecondaryMaterial(right)) {
                    event.setCanceled(true)
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    fun onLootTableLoad(event: LootTableLoadEvent) {
        if (event.table.lootTableId.path.startsWith("blocks/")) {
            val blockId = ResourceLocation(
                event.table.lootTableId.namespace,
                event.name.path.replace("blocks/", "")
            )
            if (RegistryUtils.blockExists(blockId)) {
                val block = RegistryUtils.getBlockFromId(blockId)
                if (block === Blocks.ICE || block === Blocks.PACKED_ICE || block === Blocks.BLUE_ICE) {
                    event.table.addPool(
                        LootPool.lootPool().setRolls(ConstantValue.exactly(1f)).`when`(
                            GTBlockLootProvider.SAW
                        ).add(LootItem.lootTableItem(block)).build()
                    )
                }
            }
        }
        GTLoot.onLootTableLoad(event.table.getPool("main"), event.name)
    }

    @SubscribeEvent
    fun onBreakSpeedEvent(event: PlayerEvent.BreakSpeed) {
        if (event.state.`is`(BlockTags.LOGS) && event.entity.mainHandItem.`is`(GTTools.AXE.getTag())) {
            if (event.position.isPresent) {
                val pos = event.position.get()
                var logs = 1
                for (y in pos.y + 1..<event.entity.level().maxBuildHeight) {
                    val newPos = BlockPos(pos.x, y, pos.z)
                    val newState = event.entity.level().getBlockState(newPos)
                    if (newState.`is`(BlockTags.LOGS)) {
                        logs++
                    }
                }
                if (logs > 1) {
                    event.newSpeed = event.originalSpeed / logs
                }
            }
        }
    }

    @SubscribeEvent
    fun onPlayerTick(event: TickEvent.PlayerTickEvent) {
        PlayerTickCallback.PLAYER_TICK_CALLBACKS.forEach { c ->
            c.onTick(event.phase == TickEvent.Phase.END, event.side == LogicalSide.SERVER, event.player)
        }
    }

    @SubscribeEvent
    fun onDataPackSync(event: OnDatapackSyncEvent) {
        if (event.player != null) {
            GTLibNetwork.NETWORK.sendToPlayer(ClientboundWorldgenSyncPacket(), event.player)
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            GTLibNetwork.NETWORK.sendToAllPlayers(ClientboundWorldgenSyncPacket(), ServerLifecycleHooks.getCurrentServer())
        }
    }

    @SubscribeEvent
    fun onAttachItemCapabilities(event: AttachCapabilitiesEvent<ItemStack>) {
        val fluidItem = event.`object`.item
        if (fluidItem is IFluidItem) {
            event.addCapability(
                ResourceLocation(Ref.ID, "fluid"),
                FluidHandlerItem(event.getObject()!!, fluidItem.getCapacity(), fluidItem.getFilter())
            )
        }
    }

    @SubscribeEvent
    fun remapMissingBlocksAndItems(event: MissingMappingsEvent) {
        for (modid in GTRemapping.getRemappingMap().keys) {
            for (mapping in event.getMappings(ForgeRegistries.Keys.BLOCKS, modid)) {
                val map: MutableMap<String, ResourceLocation> = GTRemapping.getRemappingMap()[modid]!!
                if (map.containsKey(mapping.getKey().path)) {
                    val replacement: Block? = GTAPI.get<Block>(map[mapping.getKey().path]!!)
                    if (replacement != null) {
                        mapping.remap(replacement)
                    }
                }
            }
            for (mapping in event.getMappings(ForgeRegistries.Keys.ITEMS, modid)) {
                val map: MutableMap<String, ResourceLocation> = GTRemapping.getRemappingMap()[modid]!!
                if (map.containsKey(mapping.getKey().path)) {
                    val replacement: Item? = GTAPI.get<Item>(map[mapping.getKey().path]!!)
                    if (replacement != null) {
                        mapping.remap(replacement)
                    }
                }
            }
        }
    }

    /**
     * Recipe event for local servers, builds recipes.
     * @param ev forge event callback.
     */
    @SubscribeEvent
    fun recipeEvent(ev: RecipesUpdatedEvent) {
        if (ClientHandler.isLocal()) {
            //GTLibDynamics.onResourceReload(false);
            GTLibDynamics.onRecipeCompile(false, ev.recipeManager)
        }
    }

    /**
     * Recipe event for online server, builds recipes.
     * @param ev forge event callback.
     */
    @SubscribeEvent
    fun tagsEvent(ev: TagsUpdatedEvent?) {
        if (FMLEnvironment.dist == Dist.CLIENT && !ClientHandler.isLocal()) {
            //GTLibDynamics.onResourceReload(false);
            GTLibDynamics.onRecipeCompile(true, Minecraft.getInstance().connection!!.recipeManager)
        }
    }

    @SubscribeEvent
    fun onWorldUnload(event: LevelEvent.Unload) {
        StructureCache.onWorldUnload(event.level)
    }
}
