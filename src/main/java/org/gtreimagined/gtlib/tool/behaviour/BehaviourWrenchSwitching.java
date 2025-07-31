package org.gtreimagined.gtlib.tool.behaviour;

import org.gtreimagined.gtlib.behaviour.IItemRightClick;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.tool.IBasicGTTool;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import static org.gtreimagined.gtlib.data.GTTools.WRENCH;
import static org.gtreimagined.gtlib.data.GTTools.WRENCH_ALT;

public class BehaviourWrenchSwitching implements IItemRightClick<IBasicGTTool> {

    public static BehaviourWrenchSwitching INSTANCE = new BehaviourWrenchSwitching();
    @Override
    public InteractionResultHolder<ItemStack> onRightClick(IBasicGTTool instance, Level level, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (player.isShiftKeyDown() && !level.isClientSide && instance instanceof IGTTool gtTool){
            GTToolType toolType = instance.getGTToolType() == WRENCH ? WRENCH_ALT : WRENCH;
            Item newWrench = toolType.getToolStack(gtTool.getPrimaryMaterial(stack)).getItem();
            ItemStack newStack = new ItemStack(newWrench);
            newStack.setTag(stack.getTag());
            player.setItemSlot(usedHand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND, newStack);
            return InteractionResultHolder.success(newStack);
        }
        return InteractionResultHolder.pass(stack);
    }
}
