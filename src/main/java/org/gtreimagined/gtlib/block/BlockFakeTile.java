package org.gtreimagined.gtlib.block;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.ForgeRegistries.Keys;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.blockentity.BlockEntityFakeBlock;
import org.gtreimagined.gtlib.blockentity.BlockEntityTickable;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class BlockFakeTile extends BlockBasic implements IRegistryEntryProvider, EntityBlock {
    public static BlockEntityType<BlockEntityFakeBlock> TYPE;
    private static Set<Block> TILE_SET = new HashSet<>();
    public BlockFakeTile(String domain, String id, Properties properties) {
        super(domain, id, properties.isValidSpawn((blockState, blockGetter, blockPos, object) -> false));
        GTAPI.register(IRegistryEntryProvider.class, this);
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {
        if (registry == ForgeRegistries.Keys.BLOCKS){
            TILE_SET.add(this);
        } else if (registry == Keys.BLOCK_ENTITY_TYPES) {
            if (TYPE == null){
                TYPE = new BlockEntityType<>(BlockEntityFakeBlock::new, TILE_SET, null);
                //((IForgeRegistry<BlockEntityType<?>>)registry).register(TYPE);
                GTAPI.register(BlockEntityType.class, getId(), getDomain(), TYPE);
            }

        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BlockEntityFakeBlock fakeBlock){
            if (fakeBlock.getController() != null){
                if (fakeBlock.getController().getMachineType().has(MachineFlag.GUI) && fakeBlock.getController().canPlayerOpenGui(player)) {
                    if (!level.isClientSide){
                        NetworkHooks.openGui((ServerPlayer) player, fakeBlock.getController(), extra -> {
                            extra.writeBlockPos(fakeBlock.getController().getBlockPos());
                        });
                    }
                    return InteractionResult.sidedSuccess(!level.isClientSide());
                }
            }
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return BlockEntityTickable::commonTick;
    }
}
