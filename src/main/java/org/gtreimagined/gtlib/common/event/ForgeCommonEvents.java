package org.gtreimagined.gtlib.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import net.minecraftforge.registries.MissingMappingsEvent;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.GTRemapping;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.capability.fluid.FluidHandlerItem;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.GTLoot;
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider;
import org.gtreimagined.gtlib.gui.container.IGTContainer;
import org.gtreimagined.gtlib.item.IFluidItem;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.network.packets.ClientboundWorldgenSyncPacket;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.pipe.TileTicker;
import org.gtreimagined.gtlib.proxy.ClientHandler;
import org.gtreimagined.gtlib.structure.StructureCache;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.tool.behaviour.BehaviourTreeFelling;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.DUST;
import static org.gtreimagined.gtlib.data.GTLibMaterials.Stone;
import static org.gtreimagined.gtlib.material.Material.NULL;

@Mod.EventBusSubscriber(modid = Ref.ID)
public class ForgeCommonEvents {

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open ev) {
        if (ev.getEntity() instanceof ServerPlayer serverPlayer) {
            if (ev.getContainer() instanceof IGTContainer gtContainer) {
                gtContainer.listeners().add(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent e) {
        Container inv = e.getInventory();
        Player player = e.getEntity();
        if (!GTLibConfig.PLAY_CRAFTING_SOUNDS.get()) return;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() instanceof IGTTool tool) {
                SoundEvent type = tool.getGTToolType().getUseSound();
                if (type != null) {
                    player.playSound(type, 0.75F, 0.75F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event){
        TileTicker.onServerWorldTick(ServerLifecycleHooks.getCurrentServer(), event.phase == TickEvent.Phase.START);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event){
        if (event.getPlacedAgainst().getBlock() instanceof BlockPipe && !(event.getPlacedBlock().getBlock() instanceof BlockPipe)){
            if (event.getEntity() instanceof Player && !event.getEntity().isCrouching()){
                BlockEntity blockEntity = event.getLevel().getBlockEntity(event.getPos().relative(event.getEntity().getDirection()));
                if (blockEntity instanceof BlockEntityPipe<?> pipe && event.getPlacedBlock().getBlock() instanceof EntityBlock){
                    pipe.setConnection(event.getEntity().getDirection().getOpposite());
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAnvilUpdated(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        if (left.getItem() == right.getItem()) {
            if (left.getItem() instanceof IGTTool leftTool && right.getItem() instanceof IGTTool rightTool) {
                if (leftTool.getPrimaryMaterial(left) != rightTool.getPrimaryMaterial(right) || leftTool.getSecondaryMaterial(left) != rightTool.getSecondaryMaterial(right)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (event.getTable().getLootTableId().getPath().startsWith("blocks/")) {
            ResourceLocation blockId = new ResourceLocation(event.getTable().getLootTableId().getNamespace(), event.getName().getPath().replace("blocks/", ""));
            if (RegistryUtils.blockExists(blockId)) {
                Block block = RegistryUtils.getBlockFromId(blockId);
                if (block == Blocks.ICE || block == Blocks.PACKED_ICE || block == Blocks.BLUE_ICE) {
                    event.getTable().addPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(GTBlockLootProvider.SAW).add(LootItem.lootTableItem(block)).build());
                }
            }
        }
        GTLoot.onLootTableLoad(event.getTable().getPool("main"), event.getName());
    }

    @SubscribeEvent
    public static void onBreakSpeedEvent(PlayerEvent.BreakSpeed event) {
        if (event.getState().is(BlockTags.LOGS) && event.getEntity().getMainHandItem().is(GTTools.AXE.getTag())){
            if (event.getPosition().isPresent()){
                BlockPos pos = event.getPosition().get();
                int logs = 1;
                for (int y = pos.getY() + 1; y < event.getEntity().level().getMaxBuildHeight(); y++) {
                    BlockPos newPos = new BlockPos(pos.getX(), y, pos.getZ());
                    BlockState newState = event.getEntity().level().getBlockState(newPos);
                    if (newState.is(BlockTags.LOGS)) {
                        logs++;
                    }
                }
                if (logs > 1) {
                    event.setNewSpeed(event.getOriginalSpeed() / logs);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event){
        PlayerTickCallback.PLAYER_TICK_CALLBACKS.forEach(c -> {
            c.onTick(event.phase == TickEvent.Phase.END, event.side == LogicalSide.SERVER, event.player);
        });
    }

    @SubscribeEvent
    public static void onDataPackSync(OnDatapackSyncEvent event){
        if (event.getPlayer() != null){
            GTLibNetwork.NETWORK.sendToPlayer(new ClientboundWorldgenSyncPacket(), event.getPlayer());
        } else if (ServerLifecycleHooks.getCurrentServer() != null) {
            GTLibNetwork.NETWORK.sendToAllPlayers(new ClientboundWorldgenSyncPacket(), ServerLifecycleHooks.getCurrentServer());
        }
    }

    @SubscribeEvent
    public static void onAttachItemCapabilities(AttachCapabilitiesEvent<ItemStack> event) {
        if (event.getObject().getItem() instanceof IFluidItem fluidItem){
            event.addCapability(new ResourceLocation(Ref.ID, "fluid"), new FluidHandlerItem(event.getObject(), fluidItem.getCapacity(), fluidItem.getFilter()));
        }
    }

    @SubscribeEvent
    public static void remapMissingBlocksAndItems(final MissingMappingsEvent event) {
        for (String modid : GTRemapping.getRemappingMap().keySet()) {
            for (MissingMappingsEvent.Mapping<Block> mapping : event.getMappings(Keys.BLOCKS, modid)) {
                var map = GTRemapping.getRemappingMap().get(modid);
                if (map.containsKey(mapping.getKey().getPath())){
                    Block replacement = GTAPI.get(Block.class, map.get(mapping.getKey().getPath()));
                    if (replacement != null){
                        mapping.remap(replacement);
                    }
                }
            }
            for (MissingMappingsEvent.Mapping<Item> mapping : event.getMappings(Keys.ITEMS, modid)) {
                var map = GTRemapping.getRemappingMap().get(modid);
                if (map.containsKey(mapping.getKey().getPath())){
                    Item replacement = GTAPI.get(Item.class, map.get(mapping.getKey().getPath()));
                    if (replacement != null){
                        mapping.remap(replacement);
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
    public static void recipeEvent(RecipesUpdatedEvent ev) {
        if (ClientHandler.isLocal()) {
            //GTLibDynamics.onResourceReload(false);
            GTLibDynamics.onRecipeCompile(false, ev.getRecipeManager());
        }
    }

    /**
     * Recipe event for online server, builds recipes.
     * @param ev forge event callback.
     */
    @SubscribeEvent
    public static void tagsEvent(TagsUpdatedEvent ev) {
        if (FMLEnvironment.dist == Dist.CLIENT && !ClientHandler.isLocal()) {
            //GTLibDynamics.onResourceReload(false);
            GTLibDynamics.onRecipeCompile(true, Minecraft.getInstance().getConnection().getRecipeManager());
        }
    }

    @SubscribeEvent
    public static void onWorldUnload(LevelEvent.Unload event){
        StructureCache.onWorldUnload(event.getLevel());
    }

}
