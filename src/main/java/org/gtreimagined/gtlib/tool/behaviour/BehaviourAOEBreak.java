package org.gtreimagined.gtlib.tool.behaviour;

import lombok.Getter;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.behaviour.IAddInformation;
import org.gtreimagined.gtlib.behaviour.IBlockDestroyed;
import org.gtreimagined.gtlib.behaviour.IDestroySpeed;
import org.gtreimagined.gtlib.behaviour.IItemRightClick;
import org.gtreimagined.gtlib.tool.IBasicGTTool;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BehaviourAOEBreak implements IBlockDestroyed<IBasicGTTool>, IItemRightClick<IBasicGTTool>, IAddInformation<IBasicGTTool>, IDestroySpeed<IBasicGTTool> {

    @Getter
    protected int column, row, depth;
    protected int destroySpedDivider;
    protected String tooltipKey;

    public BehaviourAOEBreak(int column, int row, int depth, int destroySpeedDivider, String tooltipKey) {
        if (column == 0 && row == 0) Utils.onInvalidData("BehaviourAOEBreak was set to break empty rows and columns!");
        this.column = column;
        this.row = row;
        this.depth = depth;
        this.destroySpedDivider = destroySpeedDivider;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public String getId() {
        return "aoe_break";
    }

    @Override
    public float getDestroySpeed(IBasicGTTool instance, float currentDestroySpeed, ItemStack stack, BlockState state) {
        CompoundTag tag = instance.getDataTag(stack);
        if (tag == null || !tag.getBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK)) return currentDestroySpeed;
        return currentDestroySpeed / destroySpedDivider;
    }

    @Override
    public void onAddInformation(IBasicGTTool instance, ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = instance.getDataTag(stack);
        boolean enabled = tag != null && tag.getBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK);
        tooltip.add(Utils.translatable("gtlib.tooltip.behaviour.aoe_right_click", Utils.translatable("gtlib.behaviour." + tooltipKey)));
        String suffix = enabled ? "enabled" : "disabled";
        tooltip.add(Utils.translatable("gtlib.tooltip.behaviour.aoe_" + suffix, Utils.translatable("gtlib.behaviour." + tooltipKey)));
    }

    @Override
    public InteractionResultHolder<ItemStack> onRightClick(IBasicGTTool instance, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && !level.isClientSide){
            CompoundTag tag = instance.getOrCreateDataTag(stack);
            boolean enabled = tag.getBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK);
            tag.putBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK, !enabled);
            player.displayClientMessage(Utils.literal("Mode set to " + !enabled), false);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean onBlockDestroyed(IBasicGTTool instance, ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        //if(!super.onBlockDestroyed(stack, world, state, pos, entity)) return false;
        if (!(entity instanceof Player player)) return true;
        CompoundTag tag = instance.getDataTag(stack);
        if (tag == null || !tag.getBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK) || player.isCrouching() || player.level().isClientSide) return true;
        for (BlockPos blockPos : Utils.getHarvestableBlocksToBreak(world, player, instance, stack, column, row, depth)) {
            if (!instance.hasEnoughDurability(stack, instance.getGTToolType().getUseDurability(), instance.getGTToolType().isPowered()))
                return true;
            if (!Utils.breakBlock(world, player, stack, blockPos, instance.getGTToolType().getUseDurability()))
                break;
        }
        return true;
    }
}
