package org.gtreimagined.gtlib.tool;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.CreativeModeTab;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.behaviour.IAddInformation;
import org.gtreimagined.gtlib.behaviour.IBehaviour;
import org.gtreimagined.gtlib.behaviour.IBlockDestroyed;
import org.gtreimagined.gtlib.behaviour.IInteractEntity;
import org.gtreimagined.gtlib.behaviour.IInventoryTick;
import org.gtreimagined.gtlib.behaviour.IItemHighlight;
import org.gtreimagined.gtlib.behaviour.IItemRightClick;
import org.gtreimagined.gtlib.behaviour.IItemUse;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.TierSortingRegistry;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.gtreimagined.gtlib.data.GTTools.KNIFE;
import static org.gtreimagined.gtlib.data.GTTools.SCYTHE;

public interface IBasicGTTool extends IGTObject, IColorHandler, ITextureProvider, IModelProvider, IAbstractToolMethods, ICreativeTabProvider {
    GTToolType getGTToolType();

    Tier getItemTier();

    default String getTextureDomain(){
        return getDomain();
    }

    default Item getItem() {
        return (Item) this;
    }

    default Map<String, IBehaviour<IBasicGTTool>> getBehaviours(ItemStack stack){
        return getGTToolType().getBehaviours();
    }

    @Override
    default boolean allowedIn(ResourceKey<CreativeModeTab> tab){
        return tab == getGTToolType().getItemGroup();
    }

    default Set<TagKey<Block>> getActualTags() {
        return getGTToolType().getToolTypes();
    }

    default CompoundTag getDataTag(ItemStack stack) {
        return stack.getTagElement(Ref.TAG_TOOL_DATA);
    }

    default CompoundTag getOrCreateDataTag(ItemStack stack) {
        return stack.getOrCreateTagElement(Ref.TAG_TOOL_DATA);
    }

    default Tier getTier(ItemStack stack) {
        return getItemTier();
    }

    default boolean genericIsCorrectToolForDrops(ItemStack stack, BlockState state) {
        GTToolType type = this.getGTToolType();
        boolean containsEffectiveBlock = false;
        if (type.getEffectiveBlocks().contains(state.getBlock())) {
            containsEffectiveBlock = true;
        }
        for (TagKey<Block> effectiveBlockTag : type.getEffectiveBlockTags()) {
            if (state.is(effectiveBlockTag)){
                containsEffectiveBlock = true;
                break;
            }
        }
        for (TagKey<Block> toolType : getGTToolType().getToolTypes()) {
            if (state.is(toolType)){
                containsEffectiveBlock = true;
                break;
            }
        }
        return containsEffectiveBlock && TierSortingRegistry.isCorrectTierForDrops(getTier(stack), state);
    }

    default float getDefaultMiningSpeed(ItemStack stack){
        return getTier(stack).getSpeed() * getGTToolType().getMiningSpeedMultiplier();
    }

    default void onGenericAddInformation(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        if (getGTToolType().getTooltip().size() != 0) tooltip.addAll(getGTToolType().getTooltip());
        tooltip.add(Utils.translatable("gtlib.tooltip.mining_level", getTier(stack).getLevel()).withStyle(ChatFormatting.YELLOW));
        tooltip.add(Utils.translatable("gtlib.tooltip.tool_speed", Utils.literal("" + getDefaultMiningSpeed(stack)).withStyle(ChatFormatting.LIGHT_PURPLE)));
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(stack).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IAddInformation addInformation)) continue;
            addInformation.onAddInformation(this, stack, tooltip, flag);
        }
    }

    default boolean onGenericHitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker, float volume, float pitch) {
        if (getGTToolType().getUseSound() != null)
            target.getCommandSenderWorld().playSound(null, target.getX(), target.getY(), target.getZ(), getGTToolType().getUseSound(), SoundSource.HOSTILE, volume, pitch);
        Utils.damageStack(getGTToolType().getAttackDurability(), stack, attacker);
        if (attacker instanceof Player player) refillTool(stack, player);
        return true;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default boolean onGenericBlockDestroyed(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        if (entity instanceof Player player) {
            if (getGTToolType().getUseSound() != null)
                player.playNotifySound(getGTToolType().getUseSound(), SoundSource.BLOCKS, 0.84F, 0.75F);
            boolean isPlant = GTAPI.isModLoaded(Ref.MOD_TFC) && (this.getGTToolType() == KNIFE || this.getGTToolType() == SCYTHE) && state.is(TagUtils.getBlockTag(new ResourceLocation(Ref.MOD_TFC, "plants")));
            boolean isToolEffective = isPlant || genericIsCorrectToolForDrops(stack, state);
            if (state.getDestroySpeed(world, pos) != 0.0F || isPlant) {
                int damage = isToolEffective ? getGTToolType().getUseDurability() : getGTToolType().getUseDurability() + 1;
                Utils.damageStack(damage, stack, entity);
            }
        }
        boolean returnValue = true;
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(stack).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IBlockDestroyed)) continue;
            returnValue = ((IBlockDestroyed) b).onBlockDestroyed(this, stack, world, state, pos, entity);
        }
        if (entity instanceof Player player) refillTool(stack, player);
        return returnValue;
    }

    default void refillTool(ItemStack stack, Player player){}

    default InteractionResult genericInteractLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand){
        InteractionResult result = InteractionResult.PASS;
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(stack).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IInteractEntity interactEntity)) continue;
            InteractionResult r = interactEntity.interactLivingEntity(this, stack, player, interactionTarget, usedHand);
            if (result != InteractionResult.SUCCESS) result = r;
        }
        return result;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default InteractionResult onGenericItemUse(UseOnContext ctx) {
        InteractionResult result = InteractionResult.PASS;
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(ctx.getItemInHand()).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IItemUse itemUse)) continue;
            InteractionResult r = itemUse.onItemUse(this, ctx);
            if (result != InteractionResult.SUCCESS) result = r;
        }
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    default InteractionResultHolder<ItemStack> onGenericRightclick(Level level, Player player, InteractionHand usedHand){
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(player.getItemInHand(usedHand)).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IItemRightClick rightClick)) continue;
            InteractionResultHolder<ItemStack> r = rightClick.onRightClick(this, level, player, usedHand);
            if (r.getResult().shouldAwardStats()) return r;
        }
        return InteractionResultHolder.pass(player.getItemInHand(usedHand));
    }

    @SuppressWarnings("rawtypes")
    default InteractionResult onGenericHighlight(Player player, LevelRenderer levelRenderer, Camera camera, HitResult target, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        InteractionResult result = InteractionResult.PASS;
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(player.getMainHandItem()).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IItemHighlight)) continue;
            InteractionResult type = ((IItemHighlight) b).onDrawHighlight(player, levelRenderer, camera, target, partialTicks, poseStack, multiBufferSource);
            if (type != InteractionResult.SUCCESS) {
                result = type;
            } else {
                return InteractionResult.FAIL;
            }
        }
        return result;
    }

    default void genericInventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected){
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getBehaviours(stack).entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IInventoryTick<?>)) continue;
            ((IInventoryTick) b).inventoryTick(this, stack, level, entity, slotId, isSelected);
        }
    }

    default boolean hasEnoughDurability(ItemStack stack, int damage, boolean energy) {
        return true;
    }
}
