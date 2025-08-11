package org.gtreimagined.gtlib.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.behaviour.IBehaviour;
import org.gtreimagined.gtlib.behaviour.IDestroySpeed;
import org.gtreimagined.gtlib.capability.energy.ItemEnergyHandler;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.gtreimagined.tesseract.api.context.TesseractItemContext;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.IEnergyHandlerItem;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.gtreimagined.gtlib.data.GTTools.KNIFE;

//@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MaterialTool extends DiggerItem implements IGTTool {

    protected final String domain;
    protected final GTToolType type;
    protected final GTItemTier itemTier;

    /**
     * -- GETTER --
     *  Returns -1 if its not a powered tool
     */
    @Getter
    protected final int energyTier;
    protected final long maxEnergy;

    public MaterialTool(String domain, GTToolType type, GTItemTier tier, Properties properties) {
        super(type.getBaseAttackDamage(), type.getBaseAttackSpeed(), tier, type.getToolType(), properties);
        this.domain = domain;
        this.type = type;
        this.itemTier = tier;
        this.energyTier = -1;
        this.maxEnergy = -1;
        GTAPI.register(IGTTool.class, this);
    }

    public MaterialTool(String domain, GTToolType type, GTItemTier tier, Properties properties, int energyTier) {
        super(type.getBaseAttackDamage(), type.getBaseAttackSpeed(), tier, type.getToolType(), properties);
        this.domain = domain;
        this.type = type;
        this.itemTier = tier;
        this.energyTier = energyTier;
        this.maxEnergy = type.getBaseMaxEnergy() * energyTier;
        GTAPI.register(IGTTool.class, this);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getId() {
        if (type.isSimple()) return type.isPowered() ? String.join("_", itemTier.getPrimary().getId(), type.getId(), Ref.VN[energyTier].toLowerCase(Locale.ENGLISH)) : String.join("_", itemTier.getPrimary().getId(),type.getId());;
        return type.isPowered() ? String.join("_", type.getId(), Ref.VN[energyTier].toLowerCase(Locale.ENGLISH)) : type.getId();
    }

    @NotNull
    @Override
    public GTToolType getGTToolType() {
        return type;
    }

    @Override
    public GTItemTier getGTItemTier() {
        return itemTier;
    }

    @NotNull
    @Override
    public ItemStack asItemStack(@NotNull Material primary, @NotNull Material secondary) {
        return resolveStack(primary, secondary, 0, maxEnergy);
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> list) {
        onGenericFillItemGroup(group, list, maxEnergy);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return Utils.doesStackHaveToolTypes(stack, GTTools.WRENCH, GTTools.SCREWDRIVER, GTTools.CROWBAR, GTTools.WIRE_CUTTER); // ???
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state){
        return genericIsCorrectToolForDrops(stack, state);
    }

    @Override
    public void onUseTick(Level p_41428_, LivingEntity p_41429_, ItemStack p_41430_, int p_41431_) {
        super.onUseTick(p_41428_, p_41429_, p_41430_, p_41431_);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        onGenericAddInformation(stack, tooltip, flag);
        super.appendHoverText(stack, world, tooltip, flag);
    }

    //TODO figure out why I wrote the below todo
    //TODO figure this out
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return false;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return type.getUseAction();
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return type.getUseAction() == UseAnim.NONE ? super.getUseDuration(stack) : 72000;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return (int) (getTier(stack).getUses() * getGTToolType().getDurabilityMultiplier());
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return onGenericHitEntity(stack, target, attacker, 0.75F, 0.75F);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        float destroySpeed = genericIsCorrectToolForDrops(stack, state) ? getDefaultMiningSpeed(stack) : 1.0F;
        if (type.isPowered() && getCurrentEnergy(stack)  == 0){
            destroySpeed = 0.0f;
        }
        for (Map.Entry<String, IBehaviour<IBasicGTTool>> e : getGTToolType().getBehaviours().entrySet()) {
            IBehaviour<?> b = e.getValue();
            if (!(b instanceof IDestroySpeed destroySpeed1)) continue;
            float i = destroySpeed1.getDestroySpeed(this, destroySpeed, stack, state);
            if (i > 0){
                destroySpeed = i;
                break;
            }
        }
        return destroySpeed;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level world, BlockState state, BlockPos pos, LivingEntity entity) {
        return onGenericBlockDestroyed(stack, world, state, pos, entity);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        return onGenericItemUse(ctx);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return genericInteractLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        InteractionResultHolder<ItemStack> result = onGenericRightclick(level, player, usedHand);
        if (result.getResult().shouldAwardStats()){
            return result;
        }
        return super.use(level, player, usedHand);
    }

    public void handleRenderHighlight(Player entity, LevelRenderer levelRenderer, Camera camera, HitResult target, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        onGenericHighlight(entity, levelRenderer, camera, target, partialTicks, poseStack, multiBufferSource);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
        return type.getBlockBreakability();
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return type.getToolTypes().contains(BlockTags.MINEABLE_WITH_AXE);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slotType, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = HashMultimap.create();
        if (slotType == EquipmentSlot.MAINHAND) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", type.getBaseAttackDamage() + getTier(stack).getAttackDamageBonus(), AttributeModifier.Operation.ADDITION));
            modifiers.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", type.getBaseAttackSpeed(), AttributeModifier.Operation.ADDITION));
        }
        return modifiers;
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        if (!type.isPowered()) {
            return amount;
        }
        if (entity instanceof Player && ((Player) entity).isCreative()) {
            return 0;
        }
        return damage(stack, amount);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return getTier(stack).getEnchantmentValue();
    }

    @Override
    public int getEnchantmentLevel(ItemStack stack, Enchantment enchantment) {
        Map<Enchantment, Integer> enchants = getAllEnchantments(stack);
        if (enchants.containsKey(enchantment)) {
            return enchants.get(enchantment);
        }
        return 0;
    }

    @Override
    public Map<Enchantment, Integer> getAllEnchantments(ItemStack stack) {
        Map<Enchantment, Integer> mainEnchants = MaterialTags.TOOLS.get(getPrimaryMaterial(stack)).toolEnchantment();
        Map<Enchantment, Integer> enchants = new HashMap<>();
        if (!mainEnchants.isEmpty()) {
            mainEnchants.entrySet().stream().filter(e -> e.getKey().canEnchant(stack)).forEach(e -> enchants.put(e.getKey(), e.getValue()));
        }
        return enchants;
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return !type.isPowered() && getTier(toRepair).getRepairIngredient().test(repair);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        if (type.getBlacklistedEnchantments().contains(enchantment)) return false;
        if ((type.getToolTypes().contains(BlockTags.MINEABLE_WITH_AXE) || type == KNIFE) && enchantment.category == EnchantmentCategory.WEAPON) {
            return true;
        }
        return (!type.isPowered() || (enchantment != Enchantments.UNBREAKING && enchantment != Enchantments.MENDING)) && enchantment.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return type.hasContainer();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack oldStack) {
        return getGenericContainerItem(oldStack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        if (type.isPowered()) return true;
        return super.isBarVisible(stack);
    }

    @Override
    public IEnergyHandlerItem createEnergyHandler(TesseractItemContext context) {
        return new ItemEnergyHandler(context, maxEnergy, 8 * (int) Math.pow(4, this.energyTier), 8 * (int) Math.pow(4, this.energyTier), 1, 1);
    }

    private Optional<ItemEnergyHandler> getCastedHandler(ItemStack stack) {
        return stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(e -> (ItemEnergyHandler) e);
    }
}