package org.gtreimagined.gtlib.tool.behaviour;

import org.gtreimagined.gtlib.behaviour.IItemUse;
import org.gtreimagined.gtlib.tool.IBasicGTTool;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class BehaviourCropHarvesting implements IItemUse<IBasicGTTool> {
    public static final BehaviourCropHarvesting INSTANCE = new BehaviourCropHarvesting();


    @Override
    public String getId() {
        return "crop_harvesting";
    }

    @Override
    public InteractionResult onItemUse(IBasicGTTool instance, UseOnContext c) {
        BlockPos original = c.getClickedPos();
        BlockState clickedState = c.getLevel().getBlockState(original);
        if (clickedState.getBlock() instanceof CropBlock && c.getLevel() instanceof ServerLevel serverLevel){
            AABB boundingBox = new AABB(original.offset(1,0,1), original.offset(-1,0,-1));
            boolean[] success = new boolean[1];
            BlockPos.betweenClosedStream(boundingBox).forEach(p -> {
                BlockState blockState =  serverLevel.getBlockState(p);
                if (blockState.getBlock() instanceof CropBlock cropBlock){
                    int age = cropBlock.getAge(blockState);
                    if (age == cropBlock.getMaxAge()){
                        List<ItemStack> drops = CropBlock.getDrops(blockState, serverLevel, p, null);
                        ItemStack replant = blockState.getBlock().getCloneItemStack(serverLevel, p, blockState);
                        for (ItemStack drop : drops) {
                            if (drop.getItem() == replant.getItem()){
                                drop.shrink(1);
                                break;
                            }
                        }
                        for (ItemStack drop : drops) {
                            if (!c.getPlayer().addItem(drop)){
                                c.getPlayer().drop(drop, true);
                            }
                            serverLevel.setBlock(p, cropBlock.getStateForAge(0), 3);
                        }
                        success[0] = true;


                    }
                }
            });
            if (success[0]){
                Utils.damageStack(c.getItemInHand(), c.getPlayer());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
