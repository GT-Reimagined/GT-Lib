package org.gtreimagined.gtlib.common.event;

import net.minecraftforge.event.OnDatapackSyncEvent;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.GTRemapping;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.capability.fluid.FluidHandlerItem;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
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
        if (ev.getPlayer() instanceof ServerPlayer serverPlayer) {
            if (ev.getContainer() instanceof IGTContainer gtContainer) {
                gtContainer.listeners().add(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent e) {
        Container inv = e.getInventory();
        Player player = e.getPlayer();
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
                BlockEntity blockEntity = event.getWorld().getBlockEntity(event.getPos().relative(event.getEntity().getDirection()));
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
    public static void remapMissingEnchantments(final RegistryEvent.MissingMappings<Enchantment> event){
        event.getMappings("antimatter").forEach(m -> {
            if (m.key.getPath().equals("energy_efficiency")){
                m.remap(Data.ENERGY_EFFICIENCY);
            }
            if (m.key.getPath().equals("implosion")){
                m.remap(Data.IMPLOSION);
            }
        });
    }

    @SubscribeEvent
    public static void remapMissingBlocks(final RegistryEvent.MissingMappings<Block> event) {
        for (String modid : GTRemapping.getRemappingMap().keySet()) {
            for (RegistryEvent.MissingMappings.Mapping<Block> mapping : event.getMappings(modid)) {
                var map = GTRemapping.getRemappingMap().get(modid);
                if (map.containsKey(mapping.key.getPath())){
                    Block replacement = GTAPI.get(Block.class, map.get(mapping.key.getPath()));
                    if (replacement != null){
                        mapping.remap(replacement);
                    }
                }
            }
        }
        event.getMappings(Ref.MOD_KJS).forEach(map -> {
            String domain = map.key.getNamespace();
            String id = map.key.getPath();
            if (id.startsWith("block_")) {
                Material mat = Material.get(id.replace("block_", ""));
                if (mat != NULL) {
                    map.remap(GTMaterialTypes.BLOCK.get().get(mat).asBlock());
                    return;
                }
            }
            if (id.startsWith("ore_")) {
                Block replacement = GTAPI.get(BlockOre.class, id);
                if (replacement != null) {
                    map.remap(replacement);
                    return;
                }
            }
            Block replacement = GTAPI.get(Block.class, id, Ref.SHARED_ID);
            if (replacement != null) {
                map.remap(replacement);
            }
        });
        event.getMappings(Ref.SHARED_ID).forEach(map -> {
            String id = map.key.getPath();
            if (id.equals("basalt")){
                map.remap(Blocks.BASALT);
                return;
            }
            String replacement = "";
            if (id.startsWith("fluid_")){
                replacement = id.replace("fluid_", "fluid_pipe_");
            } else if (id.startsWith("item_")){
                replacement = id.replace("item_", "item_pipe_");
            } else if (id.contains("vanilla_basalt")){
                replacement = id.replace("vanilla_basalt", "basalt");
            } else if (id.contains("sad_red")){
                replacement = id.replace("sand_red", "red_sand");
            }
            if (id.contains("__")){
                replacement = replacement.isEmpty() ? id.replace("__", "_") : replacement.replace("__", "_");
            }
            if (!replacement.isEmpty()) {
                Block replacementBlock = GTAPI.get(Block.class, replacement, Ref.SHARED_ID);
                if (replacementBlock != null){
                    map.remap(replacementBlock);
                }
            }
        });
    }

    @SubscribeEvent
    public static void remapMissingItems(final RegistryEvent.MissingMappings<Item> event) {
        for (String modid : GTRemapping.getRemappingMap().keySet()) {
            for (RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings(modid)) {
                var map = GTRemapping.getRemappingMap().get(modid);
                if (map.containsKey(mapping.key.getPath())){
                    Item replacement = GTAPI.get(Item.class, map.get(mapping.key.getPath()));
                    if (replacement != null){
                        mapping.remap(replacement);
                    }
                }
            }
        }
        event.getMappings(Ref.ID).forEach(map -> {
            Item replacement = GTAPI.get(Item.class, map.key.getPath(), Ref.SHARED_ID);
            if (replacement != null) {
                map.remap(replacement);
            }
        });

        event.getMappings(Ref.SHARED_ID).forEach(map -> {
            String id = map.key.getPath();
            if (id.equals("basalt")){
                map.remap(Items.BASALT);
                return;
            }
            if (id.equals("dust_gravel")){
                map.remap(DUST.get(Stone));
                return;
            }
            if (id.startsWith("rock_")){
                Item replacement = GTAPI.get(Item.class, id.replace("rock_", "bearing_rock_"), Ref.SHARED_ID);
                if (replacement != null) {
                    map.remap(replacement);
                    return;
                }
            }
            if (id.contains("crushed_centrifuged")){
                Item replacement = GTAPI.get(Item.class, id.replace("centrifuged", "refined"), Ref.SHARED_ID);
                if (replacement != null) {
                    map.remap(replacement);
                    return;
                }
            }
            String replacement = "";
            if (id.startsWith("fluid_")){
                replacement = id.replace("fluid_", "fluid_pipe_");
            } else if (id.startsWith("item_")){
                replacement = id.replace("item_", "item_pipe_");
            } else if (id.contains("vanilla_basalt")){
                replacement = id.replace("vanilla_basalt", "basalt");
            } else if (id.contains("sand_red")){
                replacement = id.replace("sand_red", "red_sand");
            }
            if (id.contains("__")){
                replacement = replacement.isEmpty() ? id.replace("__", "_") : replacement.replace("__", "_");
            }
            if (!replacement.isEmpty()) {
                Item replacementBlock = GTAPI.get(Item.class, replacement, Ref.SHARED_ID);
                if (replacementBlock != null){
                    map.remap(replacementBlock);
                }
            }
        });
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
    public static void biomeLoadEvent(BiomeLoadingEvent event){
        GTLibWorldGenerator.reloadEvent(event.getName(),  event.getClimate(), event.getCategory(), event.getEffects(), event.getGeneration(), event.getSpawns());
    }

    @SubscribeEvent
    public static void onWorldUnload(WorldEvent.Unload event){
        StructureCache.onWorldUnload(event.getWorld());
    }

}
