package org.gtreimagined.gtlib.tool;

import com.google.common.collect.Streams;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.energy.ItemEnergyHandler;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.integration.curios.CuriosHelper;
import org.gtreimagined.gtlib.item.ICustomDurability;
import org.gtreimagined.gtlib.item.ItemBattery;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.DigDurabilityEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import tesseract.api.context.TesseractItemContext;
import tesseract.api.forge.TesseractCaps;
import tesseract.api.gt.IEnergyHandlerItem;
import tesseract.api.gt.IEnergyItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.gtreimagined.gtlib.material.Material.NULL;

public interface IGTTool extends IGTObject, IBasicGTTool, IEnergyItem, ICustomDurability {

    GTItemTier getGTItemTier();

    @Override
    default Tier getItemTier(){
        return getGTItemTier();
    }

    @Override
    default String getTextureDomain(){
        return Ref.ID;
    }

    default Material getPrimaryMaterial(ItemStack stack) {
        if (getGTToolType().isSimple()) return getGTItemTier().getPrimary();
        return Material.get(getOrCreateDataTag(stack).getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL));
    }

    default Material getSecondaryMaterial(ItemStack stack) {
        if (getGTToolType().isSimple()) return getGTItemTier().getSecondary();
        return Material.get(getOrCreateDataTag(stack).getString(Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL));
    }

    default DyeColor getDyeColor(ItemStack stack){
        CompoundTag data = getOrCreateDataTag(stack);
        if (data.contains(Ref.KEY_TOOL_DATA_SECONDARY_COLOUR)){
            Optional<DyeColor> color = Arrays.stream(DyeColor.values()).filter(t -> t.getMaterialColor().col == data.getInt(Ref.KEY_TOOL_DATA_SECONDARY_COLOUR)).findFirst();
            return color.orElse(DyeColor.WHITE);
        }
        return null;
    }

    default int getPoweredBarColor(ItemStack stack){
        return 0x00BFFF;
    }

    default int getPoweredBarWidth(ItemStack stack){
        long currentEnergy = getCurrentEnergy(stack);
        if (currentEnergy > 0) {
            double maxAmount = getMaxEnergy(stack);
            return (int)( 13*(currentEnergy / maxAmount));
        }
        return 0;
    }

    default boolean isPoweredBarVisible(ItemStack stack) {
        return getCurrentEnergy(stack) > 0;
    }

    default Material[] getMaterials(ItemStack stack) {
        return new Material[]{getPrimaryMaterial(stack), getSecondaryMaterial(stack)};
    }

    default int getSubColour(ItemStack stack) {
        return getDyeColor(stack) == null ? 0 : getDyeColor(stack).getMaterialColor().col;
    }

    default long getCurrentEnergy(ItemStack stack) {
        return getEnergyTag(stack).getLong(Ref.KEY_ITEM_ENERGY);
    }

    default long getMaxEnergy(ItemStack stack) {
        return getEnergyTag(stack).getLong(Ref.KEY_ITEM_MAX_ENERGY);
    }

    @Override
    default boolean canCreate(TesseractItemContext context) {
        return getGTToolType().isPowered();
    }

    ItemStack asItemStack(Material primary, Material secondary);

    int getEnergyTier();

    default CompoundTag getEnergyTag(ItemStack stack){
        CompoundTag dataTag = stack.getTagElement(Ref.TAG_ITEM_ENERGY_DATA);
        return dataTag != null ? dataTag : validateEnergyTag(stack, 0, 10000);
    }

    @Override
    default CompoundTag getOrCreateDataTag(ItemStack stack) {
        CompoundTag dataTag = stack.getTagElement(Ref.TAG_TOOL_DATA);
        return dataTag != null ? dataTag : validateTag(stack, getGTItemTier().getPrimary(), getGTItemTier().getSecondary(), 0, 10000);
    }

    @Override
    default Tier getTier(ItemStack stack) {
        if (getGTToolType().isSimple()) return getGTItemTier();
        CompoundTag dataTag = getOrCreateDataTag(stack);
        Optional<GTItemTier> tier = GTItemTier.get(dataTag.getInt(Ref.KEY_TOOL_DATA_TIER));
        return tier.orElseGet(() -> resolveTierTag(dataTag));
    }

    default ItemStack resolveStack(Material primary, Material secondary, long startingEnergy, long maxEnergy) {
        Item item = getItem();
        ItemStack stack = new ItemStack(item);
        if (!getGTToolType().isSimple() || getGTToolType().isPowered()) validateTag(stack, primary, secondary, startingEnergy, maxEnergy);
        if (!primary.has(MaterialTags.TOOLS)){
            return stack;
        }
        Map<Enchantment, Integer> mainEnchants = MaterialTags.TOOLS.get(primary).toolEnchantment();
        if (!mainEnchants.isEmpty()) {
            mainEnchants.entrySet().stream().filter(e -> e.getKey().canEnchant(stack)).forEach(e -> stack.enchant(e.getKey(), e.getValue()));
            //return stack;
        }
        /*if (!handleEnchants.isEmpty()) {
            handleEnchants.entrySet().stream().filter(e -> e.getKey().canEnchant(stack) && !mainEnchants.containsKey(e.getKey())).forEach(e -> stack.enchant(e.getKey(), e.getValue()));
        }*/
        return stack;
    }

    default CompoundTag validateTag(ItemStack stack, Material primary, Material secondary, long startingEnergy, long maxEnergy) {
        CompoundTag dataTag = stack.getOrCreateTagElement(Ref.TAG_TOOL_DATA);
        if (!getGTToolType().isSimple()){
            dataTag.putString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL, primary.getId());
            dataTag.putString(Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL, secondary.getId());
        }
        if (!getGTToolType().isPowered()) return dataTag;
        validateEnergyTag(stack, startingEnergy, maxEnergy);
        return dataTag;
    }

    default CompoundTag validateEnergyTag(ItemStack stack, long startingEnergy, long maxEnergy){
        IEnergyHandlerItem h = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve().orElse(null);
        if (h != null){
            h.setEnergy(startingEnergy);
            h.setCapacity(maxEnergy);
            stack.setTag(h.getContainer().getTag());
        }
        return stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA);
    }

    default GTItemTier resolveTierTag(CompoundTag dataTag) {
        GTItemTier tier = GTItemTier.getOrCreate(dataTag.getString(Ref.KEY_TOOL_DATA_PRIMARY_MATERIAL), dataTag.getString(Ref.KEY_TOOL_DATA_SECONDARY_MATERIAL));
        dataTag.putInt(Ref.KEY_TOOL_DATA_TIER, tier.hashCode());
        return tier;
    }

    default void onGenericFillItemGroup(CreativeModeTab group, NonNullList<ItemStack> list, long maxEnergy) {
        if (group != Ref.TAB_TOOLS) return;
        if (getGTToolType().isPowered()) {
            ItemStack stack = asItemStack(NULL, NULL);
            IEnergyHandlerItem h = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve().orElse(null);
            if (h != null){
                list.add(stack.copy());
                h.setCapacity(maxEnergy);
                h.setEnergy(maxEnergy);
                stack.setTag(h.getContainer().getTag());
                list.add(stack);
            }
        } else list.add(asItemStack(getGTItemTier().getPrimary(), getGTItemTier().getSecondary()));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void onGenericAddInformation(ItemStack stack, List<Component> tooltip, TooltipFlag flag) {
        Material primary = getPrimaryMaterial(stack);
        Material secondary = getSecondaryMaterial(stack);
        if (!getGTToolType().isSimple())
            tooltip.add(Utils.translatable("gtlib.tooltip.material_primary", primary.getDisplayName().getString()));
        if (secondary != NULL)
            tooltip.add(Utils.translatable("gtlib.tooltip.material_secondary", secondary.getDisplayName().getString()));
        DyeColor color = getDyeColor(stack);
        if (color != null){
            tooltip.add(Utils.translatable("gtlib.tooltip.dye_color", color.getName()));
        }
        if (flag.isAdvanced() && getGTToolType().isPowered())
            tooltip.add(Utils.translatable("gtlib.tooltip.energy").append(": " + getCurrentEnergy(stack) + " / " + getMaxEnergy(stack)));
        tooltip.add(Utils.translatable("gtlib.tooltip.durability", Utils.literal((stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage()).withStyle(ChatFormatting.GREEN)));
        IBasicGTTool.super.onGenericAddInformation(stack, tooltip, flag);
    }

    default void refillTool(ItemStack stack, Player player){
        if (this.getGTToolType().isPowered()) {
            Streams.concat(player.getInventory().items.stream(), player.getInventory().offhand.stream(), CuriosHelper.getCuriosItems(player, "belt", "back")).forEach(s -> {
                if (this.getCurrentEnergy(stack) < getMaxEnergy(stack)){
                    if (s.getItem() instanceof ItemBattery battery && battery.getTier().getIntegerId() >= this.getEnergyTier()){
                        IEnergyHandlerItem batteryHandler = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve().orElse(null);
                        IEnergyHandlerItem toolHandler = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve().orElse(null);
                        if (batteryHandler != null && toolHandler != null){
                            long extracted = batteryHandler.extractEu(battery.getCapacity(), true);
                            if (extracted > 0){
                                long inserted = toolHandler.insertEu(extracted, true);
                                if (inserted > 0){
                                    toolHandler.insertEu(batteryHandler.extractEu(inserted, false), false);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    default ItemStack getGenericContainerItem(final ItemStack oldStack) {
        ItemStack stack = oldStack.copy();
        int amount = damage(stack, getGTToolType().getCraftingDurability());
        if (!getGTToolType().isPowered()) { // Powered items can't enchant with Unbreaking
            int level = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, stack), j = 0;
            for (int k = 0; level > 0 && k < amount; k++) {
                if (DigDurabilityEnchantment.shouldIgnoreDurabilityDrop(stack, level, Ref.RNG)) j++;
            }
            amount -= j;
        }
        boolean empty = false;
        if (amount > 0) {
            int l = stack.getDamageValue() + amount;
            stack.setDamageValue(l);
            empty = l >= stack.getMaxDamage();
        }
        if (empty) {
            if (!getGTToolType().getBrokenItems().containsKey(this.getId())) {
                return ItemStack.EMPTY;
            }
            ItemStack item = getGTToolType().getBrokenItems().get(this.getId()).apply(oldStack);
            return item;
        }
        return stack;
    }

    default int getDefaultEnergyUse(){
        return 100;
    }

    default int damage(ItemStack stack, int amount) {
        if (!getGTToolType().isPowered()) return amount;
        IEnergyHandlerItem h = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve().orElse(null);
        if (!(h instanceof ItemEnergyHandler)) {
            return amount;
        }
        long currentEnergy = h.getEnergy();
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        int energyEfficiency = enchants.getOrDefault(Data.ENERGY_EFFICIENCY, 0);
        int energyUse = Math.max(1, getDefaultEnergyUse() - (int)((energyEfficiency * 0.1f) * getDefaultEnergyUse()));
        int multipliedDamage = amount * energyUse;
        if (Ref.RNG.nextInt(20) == 0) return amount; // 1/20 chance of taking durability off the tool
        else if (currentEnergy >= multipliedDamage) {
            h.extractEu(multipliedDamage, false);
            stack.setTag(h.getContainer().getTag());
            //tag.putLong(Ref.KEY_TOOL_DATA_ENERGY, currentEnergy - multipliedDamage); // Otherwise take energy off of tool if energy is larger than multiplied damage
            return 0; // Nothing is taken away from main durability
        } else { // Lastly, set energy to 0 and take leftovers off of tool durability itself
            int leftOver = (int) (multipliedDamage - currentEnergy);
            h.extractEu(currentEnergy, false);
            stack.setTag(h.getContainer().getTag());
            //tag.putLong(Ref.KEY_TOOL_DATA_ENERGY, 0);
            return Math.max(1, leftOver / getDefaultEnergyUse());
        }
    }

    default boolean hasEnoughDurability(ItemStack stack, int damage, boolean energy) {
        if (energy && getCurrentEnergy(stack) >= damage * 100) return true;
        return stack.getDamageValue() >= damage;
    }

    default void onItemBreak(ItemStack stack, Player entity) {
        String name = this.getId();
        GTToolType type = getGTToolType();
        if (!type.getBrokenItems().containsKey(name)) {
            return;
        }
        ItemStack item = type.getBrokenItems().get(name).apply(stack);
        if (!item.isEmpty() && !entity.addItem(item)) {
            entity.drop(item, true);
        }
    }

    @Override
    default int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return i >= 2 ? -1 : i == 0 ? getPrimaryMaterial(stack).getRGB() : getSubColour(stack) == 0 ? getSecondaryMaterial(stack).getRGB() : getSubColour(stack);
    }

    @Override
    default Texture[] getTextures() {
        List<Texture> textures = new ObjectArrayList<>();
        int layers = getGTToolType().getOverlayLayers();
        textures.add(new Texture(getTextureDomain(), "item/tool/".concat(getGTToolType().getId())));
        if (layers == 1)
            textures.add(new Texture(getTextureDomain(), "item/tool/overlay/".concat(getGTToolType().getId())));
        if (layers > 1) {
            for (int i = 1; i <= layers; i++) {
                textures.add(new Texture(getTextureDomain(), String.join("", "item/tool/overlay/", getGTToolType().getId(), "_", Integer.toString(i))));
            }
        }
        return textures.toArray(new Texture[textures.size()]);
    }

    @Override
    default void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        prov.tex(item, "minecraft:item/handheld", getTextures());
    }

    boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker);
}
