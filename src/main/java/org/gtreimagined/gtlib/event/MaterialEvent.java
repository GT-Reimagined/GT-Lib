package org.gtreimagined.gtlib.event;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.Antimatter;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.IMaterialTag;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialStack;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.material.data.ArmorData;
import org.gtreimagined.gtlib.material.data.HandleData;
import org.gtreimagined.gtlib.material.data.ToolData;
import org.gtreimagined.gtlib.tool.AntimatterToolType;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.gtreimagined.gtlib.data.GTLibMaterials.Wood;
import static org.gtreimagined.gtlib.material.Material.NULL;
import static org.gtreimagined.gtlib.material.MaterialTags.*;

public class MaterialEvent<T extends MaterialEvent<T>> {
    protected Material material = Material.NULL;

    public T setMaterial(Material material){
        this.material = material;
        return (T) this;
    }

    public T setMaterial(String material){
        return setMaterial(Material.get(material));
    }

    public T asDust(IMaterialTag... tags) {
        flags(GTMaterialTypes.DUST, GTMaterialTypes.DUST_SMALL, GTMaterialTypes.DUST_TINY);
        flags(tags);
        return (T) this;
    }

    public T asDust(int meltingPoint, IMaterialTag... tags) {
        MaterialTags.MELTING_POINT.add(material, meltingPoint);
        if (meltingPoint > 295) {
//            asFluid();//TODO disabled due to Sodium having a fluid
        }
        return asDust(tags);
    }

    public T asSolid(IMaterialTag... tags) {
        asDust(tags);
        flags(GTMaterialTypes.INGOT, GTMaterialTypes.NUGGET, GTMaterialTypes.CHUNK, GTMaterialTypes.BLOCK).asFluid();
        return (T) this;
    }

    public T asSolid(int meltingPoint, IMaterialTag... tags){
        MaterialTags.MELTING_POINT.add(material, meltingPoint);
        return asSolid(tags);
    }

    public T asMetal(IMaterialTag... tags) {
        flags(METAL);
        return asSolid(tags);
    }

    public T asMetal(int meltingPoint, IMaterialTag... tags) {
        flags(METAL);
        return asSolid(meltingPoint, tags);
    }

    public T asOre(int minXp, int maxXp, boolean small, IMaterialTag... tags) {
        EXP_RANGE.add(material, UniformInt.of(minXp, maxXp));
        return asOre(small, tags);
    }

    public T asOre(IMaterialTag... tags) {
        return asOre(true, tags);
    }

    public T asOre(boolean small, IMaterialTag... tags) {
        asDust(GTMaterialTypes.ORE, GTMaterialTypes.BEARING_ROCK, GTMaterialTypes.CRUSHED, GTMaterialTypes.CRUSHED_PURIFIED, GTMaterialTypes.CRUSHED_REFINED, GTMaterialTypes.DUST_IMPURE, GTMaterialTypes.DUST_PURE, GTMaterialTypes.RAW_ORE, GTMaterialTypes.RAW_ORE_BLOCK);
        if (small) flags(GTMaterialTypes.ORE_SMALL);
        if (!has(EXP_RANGE)) EXP_RANGE.add(material, UniformInt.of(1, 5));
        flags(tags);
        return (T) this;
    }

    public T asOreStone(int minXp, int maxXp, IMaterialTag... tags) {
        asOre(minXp, maxXp, false, tags);
        flags(GTMaterialTypes.ORE_STONE);
        return (T) this;
    }

    public T asOreStone(IMaterialTag... tags) {
        asOre(tags);
        asDust(GTMaterialTypes.ORE_STONE, GTMaterialTypes.ORE, GTMaterialTypes.BEARING_ROCK, GTMaterialTypes.CRUSHED, GTMaterialTypes.CRUSHED_PURIFIED, GTMaterialTypes.CRUSHED_REFINED, GTMaterialTypes.DUST_IMPURE, GTMaterialTypes.DUST_PURE);
        flags(tags);
        return (T) this;
    }

    public T asGemBasic(boolean transparent, IMaterialTag... tags) {
        asDust(tags);
        flags(GTMaterialTypes.GEM, GTMaterialTypes.BLOCK);
        if (transparent) {
            flags(MaterialTags.TRANSPARENT, GTMaterialTypes.PLATE, GTMaterialTypes.LENS);
        }
        return (T) this;
    }

    public T asGem(boolean transparent, IMaterialTag... tags) {
        asGemBasic(transparent, tags);
        flags(GTMaterialTypes.GEM_EXQUISITE);
        return (T) this;
    }

    public T asFluid() {
        flags(GTMaterialTypes.LIQUID);
        return (T) this;
    }

    public T asFluid(int fuelPower) {
        MaterialTags.FUEL_POWER.add(this.material, fuelPower);
        return asFluid();
    }

    public T asFluid(int fuelPower, int temp) {
        MaterialTags.LIQUID_TEMPERATURE.add(this.material, temp);
        if (temp >= 400 && material.has(METAL)){
            flags(MOLTEN);
        }
        return asFluid(fuelPower);
    }

    public T asGas() {
        flags(GTMaterialTypes.GAS);
        return (T) this;
    }

    public T asGas(int fuelPower) {
        MaterialTags.FUEL_POWER.add(this.material, fuelPower);
        return asGas();
    }

    public T asGas(int fuelPower,int temp) {
        MaterialTags.GAS_TEMPERATURE.add(this.material, temp);
        return asGas(fuelPower);
    }

    public T fluidDensity(int density){
        FLUID_DENSITY.add(this.material, density);
        return (T) this;
    }

    public T harvestLevel(int harvestLevel) {
        MaterialTags.MINING_LEVEL.add(this.material, harvestLevel);
        return (T) this;
    }

    public T setAllowedTypes(AntimatterToolType... toolTypes) {
        if (!has(MaterialTags.TOOLS)) return (T) this;
        ToolData data = MaterialTags.TOOLS.get(this.material);
        List<AntimatterToolType> toolTypesList = toolTypes.length > 0 ? Arrays.asList(toolTypes) : AntimatterAPI.all(AntimatterToolType.class);
        MaterialTags.TOOLS.add(this.material, new ToolData(data.toolDamage(), data.toolSpeed(), data.toolDurability(), data.toolQuality(), data.handleMaterial(), data.toolEnchantment(), toolTypesList));
        return (T) this;
    }

    public T addArmor(int[] armor, float toughness, float knockbackResistance, int armorDurabilityFactor) {
        return addArmor(armor, toughness, knockbackResistance, armorDurabilityFactor, ImmutableMap.of());
    }

    public T addArmor(int[] armor, float toughness, float knockbackResistance, int armorDurabilityFactor, ImmutableMap<Enchantment, Integer> toolEnchantment) {
        if (armor.length != 4) {
            Antimatter.LOGGER.info("Material " + this.material.getId() + " unable to add armor, protection array must have exactly 4 values");
            return (T) this;
        }
        if (has(GTMaterialTypes.INGOT)) flags(GTMaterialTypes.PLATE);
        MaterialTags.ARMOR.add(this.material, new ArmorData(armor, toughness, knockbackResistance, armorDurabilityFactor, toolEnchantment));
        return (T) this;
    }

    public T addArmor(Material material, ImmutableMap<Enchantment, Integer> toolEnchantment) {
        if (!material.has(ARMOR)) return (T) this;
        ArmorData data = ARMOR.get(material);
        return addArmor(data.armor(), data.toughness(), data.knockbackResistance(), data.armorDurabilityFactor(), toolEnchantment);
    }

    public T addArmor(Material material) {
        if (!material.has(ARMOR)) return (T) this;
        ArmorData data = ARMOR.get(material);
        return addArmor(data.armor(), data.toughness(), data.knockbackResistance(), data.armorDurabilityFactor());
    }

    public T addHandleStat(int durability, float speed) {
        return addHandleStat(durability, speed, ImmutableMap.of());
    }

    public T addHandleStat(int durability, float speed, ImmutableMap<Enchantment, Integer> toolEnchantment) {
        if (!has(GTMaterialTypes.ROD)) flags(GTMaterialTypes.ROD);
        HANDLE.add(this.material, new HandleData(durability, speed, toolEnchantment));
        return (T) this;
    }

    public boolean has(IMaterialTag... tags) {
        for (IMaterialTag t : tags) {
            if (!t.all().contains(this.material)) return false;
        }
        return true;
    }

    public T flags(IMaterialTag... tags) {
        if (!this.material.enabled) return (T) this;
        for (IMaterialTag t : tags) {
            if (!this.has(t)) {
                t.add(this.material);
            }
            flags(t.dependents().stream().filter(d -> !this.has(d)).toArray(IMaterialTag[]::new));
        }
        return (T) this;
    }

    public T setExpRange(UniformInt expRange) {
        MaterialTags.EXP_RANGE.add(this.material, expRange);
        return (T) this;
    }

    public T setExpRange(int min, int max) {
        return this.setExpRange(UniformInt.of(min, max));
    }

    public void remove(IMaterialTag... tags) {
        if (!this.material.enabled) return;
        for (IMaterialTag t : tags) {
            t.remove(this.material);
        }
    }

    public T mats(Function<ImmutableMap.Builder<Material, Integer>, ImmutableMap.Builder<Material, Integer>> func) {
        if (!this.material.enabled) return (T) this;
        return mats(func.apply(new ImmutableMap.Builder<>()).build());
    }

    public T mats(ImmutableMap<Material, Integer> stacks) {
        return mats(stacks, -1);
    }

    public T mats(ImmutableMap<Material, Integer> stacks, int inputAmount) {
        if (!this.material.enabled) return (T) this;
        ImmutableList.Builder<MaterialStack> builder = new ImmutableList.Builder<>();
        stacks.forEach((k, v) -> builder.add(new MaterialStack(k, v)));
        PROCESS_INTO.add(material, Pair.of(builder.build(), inputAmount));
        return (T) this;
    }

    /**
     * Processing Getters/Setters
     **/

    public T setOreMulti(int multi) {
        MaterialTags.ORE_MULTI.add(this.material, multi);
        return (T) this;
    }

    public T setSmeltingMulti(int multi) {
        MaterialTags.SMELTING_MULTI.add(this.material, multi);
        return (T) this;
    }

    public T setByProductMulti(int multi) {
        MaterialTags.BY_PRODUCT_MULTI.add(this.material, multi);
        return (T) this;
    }

    public T setSmeltInto(Material m) {
        MaterialTags.SMELT_INTO.add(this.material, m);
        return (T) this;
    }

    public T setDirectSmeltInto(Material m) {
        MaterialTags.DIRECT_SMELT_INTO.add(this.material, m);
        return (T) this;
    }

    public T setArcSmeltInto(Material m) {
        MaterialTags.ARC_SMELT_INTO.add(this.material, m);
        return (T) this;
    }

    public T setMacerateInto(Material m) {
        MaterialTags.MACERATE_INTO.add(this.material, m);
        return (T) this;
    }

    public T addByProduct(Material... mats) {
        MaterialTags.BYPRODUCTS.add(this.material, new ObjectArrayList<>());
        MaterialTags.BYPRODUCTS.getList(this.material).addAll(Arrays.asList(mats));
        return (T) this;
    }

    public T replaceItem(MaterialTypeItem<?> type, Item toReplace){
        type.replacement(this.material, () -> toReplace);
        return (T) this;
    }

    public T replaceBlock(MaterialTypeBlock<?> type, Item toReplace){
        type.replacement(this.material, () -> toReplace);
        return (T) this;
    }

    public T replaceItem(MaterialTypeItem<?> type, Supplier<Item> toReplace){
        type.replacement(this.material, toReplace);
        return (T) this;
    }

    public T replaceBlock(MaterialTypeBlock<?> type, Supplier<Item> toReplace){
        type.replacement(this.material, toReplace);
        return (T) this;
    }

    public ToolBuiler tool(){
        return new ToolBuiler();
    }

    public ToolBuiler tool(Material derivedMaterial){
        ToolData data = MaterialTags.TOOLS.get(derivedMaterial);
        return tool().toolDamage(data.toolDamage()).toolDurability(data.toolDurability()).toolQuality(data.toolQuality()).toolSpeed(data.toolSpeed());
    }

    @Accessors(fluent = true)
    @Setter
    public class ToolBuiler {
        List<AntimatterToolType> allowedToolTypes;
        float toolDamage;
        float toolSpeed;
        int toolDurability;
        int toolQuality;
        ImmutableMap<Enchantment, Integer> toolEnchantments = ImmutableMap.of();
        Material handleMaterial;
        public ToolBuiler(){
            allowedToolTypes = AntimatterAPI.all(AntimatterToolType.class);
            handleMaterial = Wood;
        }

        public ToolBuiler blacklistToolTypes(AntimatterToolType... types){
            allowedToolTypes.removeAll(List.of(types));
            return this;
        }

        public T build(){
            List<AntimatterToolType> toolTypes = new ArrayList<>(allowedToolTypes);
            for (AntimatterToolType allowedToolType : allowedToolTypes) {
                if (allowedToolType.getPrimaryMaterialRequirement() != null && !material.has(allowedToolType.getPrimaryMaterialRequirement())){
                    toolTypes.remove(allowedToolType);
                }
            }
            if (toolTypes.contains(GTTools.WRENCH) && !toolTypes.contains(GTTools.WRENCH_ALT)) toolTypes.add(GTTools.WRENCH_ALT);
            allowedToolTypes = ImmutableList.copyOf(toolTypes);
            int toolDurability = AntimatterAPI.isModLoaded(Ref.MOD_TFC) ? this.toolDurability * 4 : this.toolDurability;
            return MaterialEvent.this.buildTool(new ToolData(toolDamage, toolSpeed, toolDurability, toolQuality, handleMaterial, toolEnchantments, allowedToolTypes));
        }
    }

    protected T buildTool(ToolData builder){
        if (has(GTMaterialTypes.INGOT))
            flags(GTMaterialTypes.PLATE, GTMaterialTypes.ROD, GTMaterialTypes.SCREW, GTMaterialTypes.BOLT); //TODO: We need to add bolt for now since screws depends on bolt, need to find time to change it
        else flags(GTMaterialTypes.ROD);
        List<AntimatterToolType> toolTypesList = builder.toolTypes();
        MaterialTags.TOOLS.add(this.material, builder);
        MaterialTags.MINING_LEVEL.add(this.material, builder.toolQuality() - 1);
        for (AntimatterToolType type : toolTypesList){
            if (type.getMaterialTypeItem() != null && !material.has(FLINT) && material != NULL && !material.has(RUBBERTOOLS) && !material.has(WOOD)){
                flags(type.getMaterialTypeItem());
            }
        }
        return (T) this;
    }
}
