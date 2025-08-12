package org.gtreimagined.gtlib.datagen.providers;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStoneSlab;
import org.gtreimagined.gtlib.block.BlockStoneStair;
import org.gtreimagined.gtlib.block.BlockStoneWall;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.block.BlockSurfaceRock;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.item.ItemBasic;
import org.gtreimagined.gtlib.item.ItemStoneCover;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.BasicMultiMachine;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialItem;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.pipe.BlockItemPipe;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.pipe.types.Cable;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.tool.IGTArmor;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.util.Utils;
import net.devtech.arrp.json.lang.JLang;
import net.minecraft.Util;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import static org.gtreimagined.gtlib.util.Utils.*;

public class GTLanguageProvider implements DataProvider, IGTLibProvider {

    private final String providerDomain, providerName, locale;
    private final Object2ObjectMap<String, String> data = new Object2ObjectRBTreeMap<>();

    private static final Object2ObjectMap<String, Object2ObjectMap<String, Object2ObjectMap<String, String>>> GLOBAL_DATA = new Object2ObjectRBTreeMap<>();

    public GTLanguageProvider(String providerDomain, String providerName, String locale) {
        this.providerDomain = providerDomain;
        this.providerName = providerName;
        this.locale = locale;
    }

    @Override
    public final void run() {
        addTranslations();
        GLOBAL_DATA.computeIfAbsent(providerDomain, d -> new Object2ObjectRBTreeMap<>()).merge(locale, data, (oldV, newV) -> {
            oldV.putAll(newV);
            return oldV;
        });
    }

    @Override
    public final void onCompletion() {
        overrides();
    }

    public static void postCompletion(){
        GLOBAL_DATA.forEach((domain, map) -> {
            map.forEach((locale, data) -> {
                JLang lang = JLang.lang();
                data.forEach(lang::entry);
                GTLibDynamics.DYNAMIC_RESOURCE_PACK.addLang(new ResourceLocation(domain, locale), lang);
            });
        });

    }

    protected void addTranslations() {
        if (locale.startsWith("en")) english(providerDomain, locale);
        if (providerDomain.equals(Ref.ID)) processGTLibTranslations();
    }

    private String tryComponent(String lang, IGTObject object, Supplier<String> otherwise) {
        String component = object.getLang(lang);
        if (component != null) {
            return component;
        }
        return otherwise.get();
    }

    protected void overrides(){

    }

    protected void english(String domain, String locale) {
        GTAPI.all(ItemBasic.class, domain).forEach(i -> {
            add(i, lowerUnderscoreToUpperSpaced(i.getId()));
            if (!i.getTooltip().isEmpty()){
                add("tooltip." + i.getDomain() + "." + i.getId().replace("/", "."), i.getTooltip());
            }
        });
        GTAPI.all(Machine.class, domain).forEach(i -> {
            String lang = i.getLang(locale);
            if (!(i instanceof BasicMultiMachine<?>) || i.getTiers().size() > 1){
                lang = lang.concat(" (%s)");
            }
            if (!i.hasTierSpecificLang()){
                add("machine." + i.getId(), lang);
            }
            Collection<Tier> tiers = i.getTiers();
            String finalLang = lang;
            tiers.forEach(t -> {
                String langCopy = finalLang;
                if (t == Tier.NONE) langCopy = langCopy.replace(" (%s)", "");
                if (i.hasTierSpecificLang()) {
                    add("machine." + i.getId() + "." + t.getId(), langCopy);
                }
                add(i.getBlockState(t), langCopy.replace("%s", t.getId().toUpperCase(Locale.ROOT)));
                if (i instanceof BasicMultiMachine<?>) {
                    add(i.getDomain() + ".ponder." + i.getIdFromTier(t) + ".header", langCopy.replace("%s", t.getId().toUpperCase(Locale.ROOT)).concat(" Multiblock"));
                }
            });
        });
        GTAPI.all(CoverFactory.class, domain).forEach(c -> {
            add("cover." + domain + "." + c.getId(), Utils.lowerUnderscoreToUpperSpaced(c.getId()));
        });
        GTAPI.all(Enchantment.class, domain, (en, d, i) -> {
            add("enchantment." + d + "." + i, lowerUnderscoreToUpperSpaced(i));
        });
        GTAPI.all(BlockDimensionMarker.class, domain).forEach(i -> this.add(i, Utils.lowerUnderscoreToUpperSpaced(i.getDimension())));

        if (domain.equals(Ref.ID)) {
            GTAPI.all(IGTTool.class, t -> {
                String customName = t.getGTToolType().getCustomName().isEmpty() ? Utils.getLocalizedType(t.getGTToolType()) : t.getGTToolType().getCustomName();
                if (t.getGTToolType().isPowered()) {
                    String defaultName = Utils.getLocalizedType(t.getGTToolType());
                    add(t.getItem().getDescriptionId(), Utils.lowerUnderscoreToUpperSpacedRotated(t.getId()).replace(defaultName, customName));
                } else {
                    if (t.getGTToolType().isSimple()){
                        add(t.getItem().getDescriptionId(), Utils.getLocalizedType(t.getPrimaryMaterial(t.getItem().getDefaultInstance())) + " " + customName);
                    } else {
                        add(t.getItem().getDescriptionId(), customName);
                    }
                }

            });
            GTAPI.all(BlockPipe.class).forEach(s -> {
                String str = s.getSize().getId();
                //hmmmm
                if (str.equals("vtiny")) str = "very_tiny";
                if (s.getType() instanceof Cable) {
                    str = s.getSize().getCableThickness() + "x";
                }
                if (s instanceof BlockItemPipe<?> itemPipe && itemPipe.isRestricted()){
                    str = "restrictive_" + str;
                }
                String prefix = str.contains("_") ? Utils.lowerUnderscoreToUpperSpaced(str) : str.substring(0, 1).toUpperCase() + str.substring(1);
                add(s, StringUtils.join(prefix, " ", Utils.getLocalizedType(s.getType().getMaterial()), " ", Utils.lowerUnderscoreToUpperSpaced(s.getType().getType())));
            });
            GTAPI.all(Material.class).forEach(m -> add("material.".concat(m.getId()), getLocalizedType(m)));
            GTAPI.all(BlockOre.class, o -> {
                String nativeSuffix = o.getMaterial().getElement() != null ? "Native " : "";
                String stoneType = o.getStoneType() == VanillaStoneTypes.STONE ? "" : getLocalizeStoneType(o.getStoneType()) + " ";
                if (o.getOreType() == GTMaterialTypes.ORE)
                    add(o, String.join("", stoneType, nativeSuffix, getLocalizedType(o.getMaterial()), " Ore"));
                else
                    add(o, String.join("", "Small ", stoneType, nativeSuffix, getLocalizedType(o.getMaterial()), " Ore"));
            });

            GTAPI.all(BlockOreStone.class, o -> {
                add(o, getLocalizedType(o.getMaterial()));
            });
            GTAPI.all(BlockStone.class).forEach(s -> {
                String localized = getLocalizedType(s);
                if (s.getSuffix().contains("mossy")) localized = "Mossy " + localized.replace(" Mossy", "");
                if (s.getSuffix().contains("chiseled")) localized = "Chiseled " + localized.replace(" Chiseled", "");
                if (s.getSuffix().contains("cracked")) localized = "Cracked " + localized.replace(" Cracked", "");
                if (s.getSuffix().contains("smooth")) localized = "Polished " + localized.replace(" Smooth", "");
                add(s, localized);
            });
            GTAPI.all(ItemStoneCover.class).forEach(i -> {
                String localized = getLocalizedType(i);
                if (i.getSuffix().contains("mossy")) localized = "Mossy " + localized.replace(" Mossy", "");
                if (i.getSuffix().contains("chiseled")) localized = "Chiseled " + localized.replace(" Chiseled", "");
                if (i.getSuffix().contains("cracked")) localized = "Cracked " + localized.replace(" Cracked", "");
                if (i.getSuffix().contains("smooth")) localized = "Polished " + localized.replace(" Smooth", "");
                override(i.getDescriptionId(), localized);
            });
            GTAPI.all(BlockStoneSlab.class).forEach(s -> {
                String localized = getLocalizedType(s);
                if (s.getSuffix().contains("mossy")) localized = "Mossy " + localized.replace(" Mossy", "");
                if (s.getSuffix().contains("chiseled")) localized = "Chiseled " + localized.replace(" Chiseled", "");
                if (s.getSuffix().contains("cracked")) localized = "Cracked " + localized.replace(" Cracked", "");
                if (s.getSuffix().contains("smooth")) localized = "Polished " + localized.replace(" Smooth", "");
                add(s, localized);
            });
            GTAPI.all(BlockStoneStair.class).forEach(s -> {
                String localized = getLocalizedType(s);
                if (s.getSuffix().contains("mossy")) localized = "Mossy " + localized.replace(" Mossy", "");
                if (s.getSuffix().contains("chiseled")) localized = "Chiseled " + localized.replace(" Chiseled", "");
                if (s.getSuffix().contains("cracked")) localized = "Cracked " + localized.replace(" Cracked", "");
                if (s.getSuffix().contains("smooth")) localized = "Polished " + localized.replace(" Smooth", "");
                add(s, localized);
            });
            GTAPI.all(BlockStoneWall.class).forEach(s -> {
                String localized = getLocalizedType(s);
                if (s.getSuffix().contains("mossy")) localized = "Mossy " + localized.replace(" Mossy", "");
                if (s.getSuffix().contains("chiseled")) localized = "Chiseled " + localized.replace(" Chiseled", "");
                if (s.getSuffix().contains("cracked")) localized = "Cracked " + localized.replace(" Cracked", "");
                if (s.getSuffix().contains("smooth")) localized = "Polished " + localized.replace(" Smooth", "");
                add(s, localized);
            });
            GTAPI.all(GTFluid.class).forEach((GTFluid s) -> {
                add(Util.makeDescriptionId("fluid_type", s.getLoc()), tryComponent(locale, s, () -> lowerUnderscoreToUpperSpaced(s.getId())));
                Item bucket = GTAPI.get(Item.class, s.getId() + "_bucket", Ref.SHARED_ID);
                if (bucket != null) add(bucket, tryComponent(locale, s, () -> lowerUnderscoreToUpperSpaced(s.getId())) + " Bucket");
            });
            GTAPI.all(BlockStorage.class).forEach(block -> {
                MaterialType<?> type = block.getType();
                add(block, type.getLang().apply(block.getMaterial()));
            });
            GTAPI.all(BlockFrame.class).forEach(block -> {
                add(block, String.join("", getLocalizedType(block.getMaterial()), " ", getLocalizedType(block.getType())));
            });
            GTAPI.all(BlockSurfaceRock.class).forEach(b -> {
                add(b, String.join("", getLocalizeStoneType(b.getStoneType()) + " ", (b.getMaterial() == Material.NULL ? "" : getLocalizedType(b.getMaterial()) + " "), "Surface Rock"));
            });
            GTAPI.all(MaterialType.class).stream().filter(t -> t instanceof MaterialTypeBlock<?> || t instanceof MaterialTypeItem<?>).forEach(t -> {
                if (t.get() instanceof MaterialTypeBlock.IOreGetter){
                    GTAPI.all(StoneType.class, s -> {
                        add(Ref.ID + ".rei.group." + t.getId() + "." + s.getId(), getLocalizedType(s) + " " + getLocalizedType(t) + "s");
                    });
                    if (t != GTMaterialTypes.BEARING_ROCK){
                        return;
                    }
                }
                add(Ref.ID + ".rei.group." + t.getId(), ((String) t.getLang().apply(GTLibMaterials.Iron)).replace(" Iron", "") + "s");
            });
            GTAPI.all(StoneType.class, s -> {
                if (s instanceof CobbleStoneType){
                    add(Ref.ID + ".rei.group." + s.getId(), getLocalizedType(s));
                }
            });
            GTAPI.all(MaterialItem.class).forEach(item -> {
                MaterialType<?> type = item.getType();
                add(item, type.getLang().apply(item.getMaterial()));
            });
            GTAPI.all(IGTArmor.class, t -> {
                add(t.getItem().getDescriptionId(), Utils.getLocalizedType(t.getMat()) + " " + Utils.getLocalizedType(t.getGTArmorType()));
            });
            customTranslations();
            pipeTranslations();
            GTAPI.all(RecipeMap.class, t -> {
                String id = "jei.category." + t.getId();
                String emiID = "emi.category.gt." + t.getId();
                add(id, Utils.lowerUnderscoreToUpperSpaced(t.getId().replace('.', '_'), 0));
                add(emiID, Utils.lowerUnderscoreToUpperSpaced(t.getId().replace('.', '_'), 0));
            });
        }
    }

    protected void customTranslations() {
        add("machine.voltage.in", "Voltage IN");
        add("machine.voltage.out", "Voltage OUT");
        add("machine.power.capacity", "Capacity");
        add("machine.tank.capacity", "Stores %sMb of fluid");
        add("machine.structure.form", "Right click structure to form it after placing blocks");
        add("machine.generator.efficiency", "Efficiency: %s");
        add("machine.overclock.no_cost", "Can be overclocked without additional Energy Loss");
        add("generic.amp", "Amperage");
        add("gtlib.tooltip.formula", "Hold Shift to show formula.");
        add("gtlib.tooltip.chemical_formula", "Formula: %s");
        add("gtlib.tooltip.mass", "Mass: %s");
        add("gtlib.tooltip.atomic_number", "Atomic Number: %s");
        add("gtlib.tooltip.more", "Hold Shift to show more information.");
        add("gtlib.tooltip.stacks", "Stacks");
        add("gtlib.tooltip.fluid.amount", "Amount: %s");
        add("gtlib.tooltip.fluid.temp", "Temperature: %s K");
        add("gtlib.tooltip.fluid.liquid", "State: Liquid");
        add("gtlib.tooltip.fluid.gas", "State: Gas");
        add("gtlib.tooltip.cover.output.no_input", "Inputs blocked");
        add("gtlib.tooltip.cover.output.allow_input", "Inputs allowed");
        add("gtlib.tooltip.cover.covers_on_item", "Covers");
        add("gtlib.tooltip.cover.stack", "%s: %s");
        add("gtlib.tooltip.valid_structure", "Structure Formed");
        add("gtlib.tooltip.invalid_structure", "Structure Invalid");
        add("generic.tier", "Tier");
        add("generic.voltage", "Voltage");
        //Is this loss?
        add("generic.loss", "Loss (per block)");
        add("message.discharge.on", "Discharge enabled");
        add("message.discharge.off", "Discharge disabled");
        add("item.charge", "Energy");
        add("item.reusable", "Reusable");
        add("item.amps", "Warning: outputs %s amps");
        add("gtlib.tooltip.battery.tier", "%s Battery");
        add("gtlib.tooltip.material_primary", "Primary Material: %s");
        add("gtlib.tooltip.material_secondary", "Secondary Material: %s");
        add("gtlib.tooltip.dye_color", "Handle Color: %s");
        add("gtlib.tooltip.tool_speed", "Mining Speed: %s");
        add("gtlib.tooltip.crafting_uses", "Crafting Uses: %s");
        add("gtlib.tooltip.mining_level", "Mining Level %s");
        add("gtlib.tooltip.durability", "Durability: %s");
        add("gtlib.gui.show_recipes", "Show Recipes");
        add("gtlib.tooltip.bandwidth", "Bandwidth: %s");
        add("gtlib.tooltip.capacity", "Capacity: %s");
        add("gtlib.tooltip.stepsize", "Stepsize: %s");
        add("gtlib.tooltip.gas_proof", "Can handle Gases");
        add("gtlib.tooltip.acid_proof", "Can handle Acids");
        add("gtlib.tooltip.max_temperature", "Max Temperature");
        add("gtlib.tooltip.energy", "Energy");
        add("gtlib.tooltip.heat_capacity", "Heat capacity");
        add("gtlib.tooltip.heat_capacity_total", "Heat capacity (total)");
        add("gtlib.tooltip.material_modid", "Material added by: %s");
        add("gtlib.tooltip.occurrence", "Indicates occurrence of %s");
        add("gtlib.tooltip.behaviour.aoe_enabled", "%s Enabled");
        add("gtlib.tooltip.behaviour.aoe_disabled", "%s Disabled");
        add("gtlib.tooltip.behaviour.aoe_right_click", "Sneak right click to Enable/Disable %s");
        add("gtlib.tooltip.io_widget.fluid", "Fluid Auto-Output");
        add("gtlib.tooltip.io_widget.item", "Item Auto-Output");
        add("gtlib.behaviour.3x3", "3x3 Mining");
        add("gtlib.behaviour.1x0x2", "1x2 Mining");
        add("jei.category.gtlib.veins", "Vein Stats");
        add("jei.category.gtlib.small_ores", "Small Ore Stats");
        add("jei.category.gtlib.stone_veins", "Stone Layer Vein Stats");
        add("config.jade.plugin_gtlib.eu_hu", "EU and HU Plugin for GT Lib");
        add("config.jade.plugin_gtlib.machine", "Machine Plugin for GT Lib");
    }

    private final void pipeTranslations() {
        add("gtlib.pipe.cable.info", "Transmits amperages between machines. \nFor each cable the cable loss is subtracted \nfrom the total energy.");
        add("gtlib.pipe.item.info", "Transfers up to capacity item stacks per tick. \nThis capacity is per stack and not per item transferred.");
        add("gtlib.pipe.fluid.info", "Transfers up to capacity per tick, with a buffer of 20 times the capacity. \nEvery tick the capacity of the pipe is replenished, up to 20 times. \nThis allows large transfers at once, but \n" +
                "continuous transfers is limited by capacity");
        add("gtlib.pipe.item.input_side.enabled", "Accepting from selected Side enabled");
        add("gtlib.pipe.item.output_side.enabled", "Emitting to selected Side enabled");
        add("gtlib.pipe.item.input_side.disabled", "Accepting from selected Side disabled");
        add("gtlib.pipe.item.output_side.disabled", "Emitting to selected Side disabled");
    }

    private void processGTLibTranslations() {
        add(Ref.TAB_BLOCKS, "GT Blocks");
        add(Ref.TAB_ITEMS, "GT Items");
        add(Ref.TAB_MACHINES, "GT Machines");
        add(Ref.TAB_MATERIALS, "GT Material Items");
        add(Ref.TAB_TOOLS, "GT Tools");
    }

    @Override
    public String getName() {
        return providerName;
    }

    public void addBlock(Supplier<? extends Block> key, String name) {
        add(key.get(), name);
    }

    public void add(Block key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItem(Supplier<? extends Item> key, String name) {
        add(key.get(), name);
    }

    public void add(Item key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItemStack(Supplier<ItemStack> key, String name) {
        add(key.get(), name);
    }

    public void add(ItemStack key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEnchantment(Supplier<? extends Enchantment> key, String name) {
        add(key.get(), name);
    }

    public void add(Enchantment key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEffect(Supplier<? extends MobEffect> key, String name) {
        add(key.get(), name);
    }

    public void add(MobEffect key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addEntityType(Supplier<? extends EntityType<?>> key, String name) {
        add(key.get(), name);
    }

    public void add(EntityType<?> key, String name) {
        add(key.getDescriptionId(), name);
    }

    public void addItemGroup(Supplier<? extends CreativeModeTab> key, String name) {
        add(key.get(), name);
    }

    public void add(CreativeModeTab key, String name) {
        add("itemGroup." + key.getRecipeFolderName(), name);
    }

    public void add(String key, String value) {
        try {
            if (data.containsKey(key)) {
                throw new IllegalStateException("Duplicate translation key " + key + ", Name is " + value);
            }
            data.put(key, value);
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void override(String key, String value) {
        data.put(key, value);
    }

    public void override(Machine<?> i, Tier t, String value){
        if (i.hasTierSpecificLang()) {
            override("machine." + i.getId() + "." + t.getId(), value);
        }
        override(i.getBlockState(t).getDescriptionId(), value);
        if (i instanceof BasicMultiMachine<?>) {
            override(i.getDomain() + ".ponder." + i.getIdFromTier(t) + ".header", value.concat(" Multiblock"));
        }
    }

    public void override(String domain, String key, String value) {
        Map<String, Object2ObjectMap<String, String>> mapMap = GLOBAL_DATA.get(domain);
        if (mapMap != null){
            Map<String, String> map = mapMap.get(locale);
            if (map != null){
                map.put(key, value);
            }
        }
    }

}
