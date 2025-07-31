package org.gtreimagined.gtlib.mixin;

import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

import static org.gtreimagined.gtlib.tool.behaviour.BehaviourTorchPlacing.tryPlace;

@Mixin(PickaxeItem.class)
public class PickaxeItemMixin extends DiggerItem {
    public PickaxeItemMixin(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties) {
        super(attackDamageModifier, attackSpeedModifier, tier, blocks, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext c) {
        ItemStack stack = ItemStack.EMPTY;
        if (c.getPlayer() == null) return InteractionResult.PASS;
        for (ItemStack stack1 : c.getPlayer().getInventory().items) {
            if (stack1.getItem() == Items.TORCH || stack1.getItem() == Items.SOUL_TORCH) {
                stack = stack1;
                break;
            }
        }
        if (!stack.isEmpty() || c.getPlayer().isCreative()) {
            InteractionResult resultType = tryPlace(new BlockPlaceContext(c), stack);
            if (resultType.consumesAction()) {
                if (!c.getPlayer().isCreative()) stack.shrink(1);
                return resultType;
            }
        }
        return super.useOn(c);
    }
}
