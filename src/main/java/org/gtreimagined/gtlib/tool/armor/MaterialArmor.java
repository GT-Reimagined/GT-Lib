package org.gtreimagined.gtlib.tool.armor;

import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTCreativeTabs;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.RenderHelper;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.tool.IGTArmor;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.gtreimagined.gtlib.material.Material.NULL;

public class MaterialArmor extends ArmorItem implements IGTArmor, DyeableLeatherItem {
    private static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
    protected String domain;
    protected GTArmorType type;
    protected Material material;

    public MaterialArmor(String domain, GTArmorType type, Material materialIn, ArmorItem.Type armorType, Properties builderIn) {
        super(new MatArmorMaterial(type, materialIn), armorType, builderIn);
        this.domain = domain;
        this.material = materialIn;
        this.type = type;
        GTAPI.register(IGTArmor.class, this);
        if (type.getArmorType() == Type.HELMET && FMLEnvironment.dist.isClient()) {
            RenderHelper.registerProbePropertyOverrides(this);
        }
    }

    @Override
    public String getId() {
        return material.getId() + "_" + type.getId();
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public GTArmorType getGTArmorType() {
        return type;
    }

    @Override
    public Material getMat() {
        return material;
    }

    @Override
    public ItemStack asItemStack() {
        return new ItemStack(this);
    }

    @Override
    public <T extends LivingEntity> int damageItem(ItemStack stack, int amount, T entity, Consumer<T> onBroken) {
        return amount;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return getMaxDamage();
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
        Map<Enchantment, Integer> mainEnchants = MaterialTags.ARMOR.get(getMat()).toolEnchantment();
        Map<Enchantment, Integer> enchants = new HashMap<>();
        if (!mainEnchants.isEmpty()) {
            mainEnchants.entrySet().stream().filter(e -> e.getKey().canEnchant(stack)).forEach(e -> enchants.put(e.getKey(), e.getValue()));
        }
        return enchants;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment.category.canEnchant(stack.getItem());
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        onGenericAddInformation(stack, tooltip, flag);
        super.appendHoverText(stack, world, tooltip, flag);
        appendEnchantmentNames(tooltip, getAllEnchantments(stack));
    }

    @Override
    public void fillItemCategory(ResourceKey<CreativeModeTab> category, NonNullList<ItemStack> items) {
        if (category != GTCreativeTabs.TOOLS.getKey()) return;
        items.add(asItemStack());
    }

    @Nullable
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        String extra = "";
        if (slot == EquipmentSlot.HEAD && type != null) {
            CompoundTag nbt = stack.getTag();
            if (nbt != null && nbt.contains("theoneprobe") && nbt.getBoolean("theoneprobe")) extra = "_probe";
        }
        return Ref.ID + ":textures/models/armor_layer_" + (slot == EquipmentSlot.LEGS ? 2 : 1) + (type == null ? "" : "_" + type + extra) + ".png";
    }

    @Override
    public int getColor(ItemStack stack) {
        return getItemColor(stack, null, 0);
    }

    @Override
    public boolean hasCustomColor(ItemStack stack) {
        return material != NULL;
    }

    @Override
    public void setColor(ItemStack stack, int color) {

    }

    @Override
    public void clearColor(ItemStack stack) {

    }
}
