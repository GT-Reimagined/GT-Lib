package org.gtreimagined.gtlib.tool;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.behaviour.IBehaviour;
import org.gtreimagined.gtlib.material.IMaterialTag;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class GTToolType implements IGTObject {

    private final String domain, id;
    @Getter
    private TagKey<Block> toolType;
    @Getter
    private final Set<TagKey<Block>> toolTypes = new ObjectOpenHashSet<>();
    @Getter
    private final Set<Block> effectiveBlocks = new ObjectOpenHashSet<>();
    @Getter
    private final Set<TagKey<Block>> effectiveBlockTags = new ObjectOpenHashSet<>();
    @Getter
    private final Object2ObjectMap<String, IBehaviour<IBasicGTTool>> behaviours = new Object2ObjectOpenHashMap<>();
    @Getter
    @Setter
    private ImmutableMap<String, Function<ItemStack, ItemStack>> brokenItems = ImmutableMap.of();
    @Getter
    private final Map<String, Supplier<Item>> replacements = new Object2ObjectOpenHashMap<>();
    @Getter
    @Setter
    private Set<Enchantment> blacklistedEnchantments = new HashSet<>();
    @Getter
    private final List<Component> tooltip = new ObjectArrayList<>();
    @Getter
    private boolean powered, blockBreakability;
    @Getter
    @Setter
    private boolean hasContainer, hasSecondary, simple, repairable;
    @Getter
    @Setter
    private boolean originalTag = true;
    @Getter
    private long baseMaxEnergy;
    @Getter
    private int[] energyTiers;
    @Getter
    @Setter
    private int useDurability, attackDurability, craftingDurability;
    @Getter
    @Setter
    private float durabilityMultiplier = 1;
    @Getter
    private float miningSpeedMultiplier = 1.0f;
    @Getter
    private int baseQuality, overlayLayers;
    @Getter
    @Setter
    private float baseAttackDamage, baseAttackSpeed;
    @Getter
    @Setter
    private CreativeModeTab itemGroup;
    @Getter
    protected TagKey<Item> tag, forgeTag; // Set?
    @Getter
    @Setter
    private UseAnim useAction;
    @Setter
    private Class<? extends IGTTool> toolClass;
    @Setter
    private IToolSupplier toolSupplier = null;
    @Nullable
    @Getter
    @Setter
    private SoundEvent useSound;
    @Getter
    @Nullable
    private IMaterialTag primaryMaterialRequirement, secondaryMaterialRequirement;
    @Nullable
    @Getter
    @Setter
    private MaterialTypeItem<?> materialTypeItem;
    @Getter
    @Setter
    private Predicate<Material> materialTypeItemPredicate;
    @Getter
    @Setter
    private String customName = "";

    /**
     * Instantiates a GTToolType with its basic values
     *
     * @param domain             unique identifier provided by the mod
     * @param id                 unique identifier
     * @param useDurability      durability that is lost during an 'use' or 'blockDestroyed' operation
     * @param attackDurability   durability that is lost on 'hitEntity'
     * @param craftingDurability durability that is lost on 'getContainerItem' (when item is crafted as well as this tool is in the crafting matrix)
     * @param baseAttackDamage   base attack damage that would be applied to the item's attributes
     * @param baseAttackSpeed    base attack speed that would be applied to the item's attributes
     * @param vanillaType          if the mining type uses vanilla resource location for tool tag.
     */
    public GTToolType(String domain, String id, int useDurability, int attackDurability, int craftingDurability, float baseAttackDamage, float baseAttackSpeed, boolean vanillaType) {
        if (domain.isEmpty()) Utils.onInvalidData("GTToolType registered with no domain name!");
        this.domain = domain;
        if (id.isEmpty()) Utils.onInvalidData("GTToolType registered with an empty ID!");
        this.id = id;
        if (useDurability < 0) Utils.onInvalidData(id + " cannot have a negative use durability value!");
        if (attackDurability < 0) Utils.onInvalidData(id + " cannot have a negative attack durability value!");
        if (craftingDurability < 0) Utils.onInvalidData(id + " cannot have a negative crafting durability value!");
        this.useSound = null;
        this.repairable = true;
        this.blockBreakability = true;
        this.hasContainer = true;
        this.baseQuality = 0;
        this.useDurability = useDurability;
        this.attackDurability = attackDurability;
        this.craftingDurability = craftingDurability;
        this.baseAttackDamage = baseAttackDamage;
        this.baseAttackSpeed = baseAttackSpeed;
        this.overlayLayers = 1;
        this.itemGroup = Ref.TAB_TOOLS;
        String tagString = id.equals("wrench") ? id + "es" : id.endsWith("s") ? id : id + "s";
        this.tag = TagUtils.getItemTag(new ResourceLocation(Ref.ID, tagString));
        this.forgeTag = TagUtils.getForgelikeItemTag("tools/".concat(tagString));
        this.useAction = UseAnim.NONE;
        this.toolClass = MaterialTool.class;
        this.simple = true;
        hasSecondary = true;
        if (vanillaType) {
            this.toolType = TagUtils.getBlockTag(new ResourceLocation("minecraft","mineable/".concat(id)));
        } else {
            this.toolType = TagUtils.getBlockTag(new ResourceLocation(Ref.ID, "mineable/".concat(id)));
        }
        this.materialTypeItemPredicate = m -> true;
        this.toolTypes.add(this.toolType);
        setBrokenItems(ImmutableMap.of(id, (i) -> ItemStack.EMPTY));
    }

    public GTToolType(String domain, String id, GTToolType inheritType) {
        this(domain, id, inheritType.useDurability, inheritType.attackDurability, inheritType.craftingDurability, inheritType.baseAttackDamage, inheritType.baseAttackSpeed, false);
    }

    @Override
    public String getDomain() {
        return domain;
    }

    /* IGTTool Instantiations */

    /**
     * Instantiates powered MaterialTools
     */
    public List<IGTTool> instantiatePoweredTools(String domain) {
        Item.Properties properties = prepareInstantiation(domain);
        return instantiatePoweredTools(domain, () -> properties);
    }

    public List<IGTTool> instantiatePoweredTools(String domain, Supplier<Item.Properties> properties) {
        List<IGTTool> poweredTools = new ObjectArrayList<>();
        for (int energyTier : energyTiers) {
            poweredTools.add(instantiatePoweredTool(domain, GTItemTier.NULL, properties, energyTier));
        }
        return poweredTools;
    }

    /**
     * Instantiates a MaterialTool
     */
    public List<IGTTool> instantiateTools(String domain) {
        return instantiateTools(domain, () -> prepareInstantiation(domain));
    }

    protected IGTTool instantiatePoweredTool(String domain, GTItemTier tier, Supplier<Item.Properties> properties, int energyTier) {
        if (toolSupplier != null) return toolSupplier.create(domain, this, tier, properties.get(), energyTier);
        if (toolClass == MaterialSword.class) return new MaterialSword(domain, this, tier, properties.get(), energyTier);
        return new MaterialTool(domain, this, tier, properties.get(), energyTier);
    }

    protected IGTTool instantiateTool(String domain, GTItemTier tier, Supplier<Item.Properties> properties) {
        if (toolSupplier != null) return toolSupplier.create(domain, this, tier, properties.get());
        if (toolClass == MaterialSword.class) return new MaterialSword(domain, this, tier, properties.get());
        return new MaterialTool(domain, this, tier, properties.get());
    }

    public List<IGTTool> instantiateTools(String domain, Supplier<Item.Properties> properties) {
        List<IGTTool> tools = new ArrayList<>();
        if (simple){
            MaterialTags.TOOLS.getAll().forEach((m, t) -> {
                if (primaryMaterialRequirement != null && !m.has(primaryMaterialRequirement)) return;
                if (replacements.containsKey(m.getId())) return;
                if (t.toolTypes().contains(this)){
                    tools.add(instantiateTool(domain, GTItemTier.getOrCreate(m, hasSecondary ? t.handleMaterial() : Material.NULL), properties));
                }
            });
        } else {
            tools.add(instantiateTool(domain, GTItemTier.NULL, properties));
        }
        return tools;
    }

    protected Item.Properties prepareInstantiation(String domain) {
        if (domain.isEmpty()) Utils.onInvalidData("An GTToolType was instantiated with an empty domain name!");
        Item.Properties properties = new Item.Properties();
        if (!repairable) properties.setNoRepair();
        return properties;
    }

    /* SETTERS */

    public GTToolType addReplacement(Material material, Supplier<Item> item){
        this.replacements.put(material.getId(), item);
        return this;
    }

    public GTToolType setToolTip(Component... tooltip) {
        this.tooltip.addAll(Arrays.asList(tooltip));
        return this;
    }

    public GTToolType setTag(GTToolType tag) {
        this.originalTag = false;
        this.tag = tag.getTag();
        this.forgeTag = tag.getForgeTag();
        return this;
    }

    public GTToolType setType(GTToolType tag) {
        this.toolTypes.remove(this.toolType);
        this.toolType = tag.getToolType();
        this.toolTypes.add(this.toolType);
        return this;
    }
    public GTToolType setTag(ResourceLocation loc) {
        this.originalTag = false;
        this.tag = TagUtils.getItemTag(loc);
        this.forgeTag = TagUtils.getForgelikeItemTag("tools/" + loc.getPath());
        return this;
    }

    public GTToolType setPowered(long baseMaxEnergy, int... energyTiers) {
        this.powered = true;
        this.baseMaxEnergy = baseMaxEnergy;
        this.energyTiers = energyTiers;
        this.simple = false;
        return this;
    }

    public GTToolType addTags(String... types) {
        if (types.length == 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have no additional tool types even when it was explicitly called!");
        Arrays.stream(types).map(t -> {
            String domain = t.equals("pickaxe") || t.equals("axe") || t.equals("shovel") || t.equals("hoe") || t.equals("sword") ? "minecraft" : Ref.ID;
            return TagUtils.getBlockTag(new ResourceLocation(domain, "mineable/" + t));
        }).forEach(t -> this.toolTypes.add(t));
        return this;
    }

    public GTToolType addEffectiveBlocks(Block... blocks) {
        if (blocks.length == 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have no effective blocks even when it was explicitly called!");
        this.effectiveBlocks.addAll(Arrays.asList(blocks));
        return this;
    }

    @SafeVarargs
    public final GTToolType addEffectiveBlockTags(TagKey<Block>... blocks) {
        if (blocks.length == 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have no effective block tags even when it was explicitly called!");
        this.effectiveBlockTags.addAll(Arrays.asList(blocks));
        return this;
    }

    public GTToolType addBlacklistedEnchantments(Enchantment... enchantments){
        blacklistedEnchantments.addAll(Arrays.asList(enchantments));
        return this;
    }

    public GTToolType setPrimaryRequirement(IMaterialTag tag) {
        if (tag == null)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have no primary material requirement even when it was explicitly called!");
        this.primaryMaterialRequirement = tag;
        return this;
    }

    public GTToolType setSecondaryRequirement(IMaterialTag tag) {
        if (tag == null)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have no secondary material requirement even when it was explicitly called!");
        this.secondaryMaterialRequirement = tag;
        return this;
    }

    public boolean getBlockBreakability() {
        return blockBreakability;
    }

    public GTToolType setBlockBreakability(boolean breakable) {
        this.blockBreakability = breakable;
        return this;
    }

    public GTToolType setBaseQuality(int quality) {
        if (quality < 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have negative Base Quality!");
        this.baseQuality = quality;
        return this;
    }

    public GTToolType setToolSpeedMultiplier(float multiplier){
        if (multiplier < 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have negative Speed Multiplier!");
        this.miningSpeedMultiplier = multiplier;
        return this;
    }

    public GTToolType setOverlayLayers(int layers) {
        if (layers < 0)
            Utils.onInvalidData(StringUtils.capitalize(id) + " GTToolType was set to have less than 0 overlayer layers!");
        this.overlayLayers = layers;
        return this;
    }

    public void addBehaviour(IBehaviour<IBasicGTTool>... behaviours) {
        Arrays.stream(behaviours).forEach(b -> this.behaviours.put(b.getId(), b));
    }

    public IBehaviour<IBasicGTTool> getBehaviour(String id) {
        return behaviours.get(id);
    }

    public void removeBehaviour(String... ids) {
        Arrays.stream(ids).forEach(behaviours::remove);
    }

    /* GETTERS */

    public ItemStack getToolStack(Material primary, Material secondary) {
        String id = simple ? primary.getId() + "_" + this.id : this.id;
        return Objects.requireNonNull(GTAPI.get(IGTTool.class, id, domain)).asItemStack(primary, secondary);
    }

    public ItemStack getToolStack(Material primary) {
        if (simple && replacements.containsKey(primary.getId())){
            ItemStack stack = new ItemStack(replacements.get(primary.getId()).get());
            if (!primary.has(MaterialTags.TOOLS)) {
                return stack;
            } else {
                Map<Enchantment, Integer> mainEnchants = MaterialTags.TOOLS.get(primary).toolEnchantment();
                if (!mainEnchants.isEmpty()) {
                    mainEnchants.entrySet().stream().filter((e) -> e.getKey().canEnchant(stack)).forEach((e) -> {
                        stack.enchant(e.getKey(), e.getValue());
                    });
                }
                return stack;
            }
        }
        String id = simple ? primary.getId() + "_" + this.id : this.id;
        return Objects.requireNonNull(GTAPI.get(IGTTool.class, id, domain)).asItemStack(primary, Material.NULL);
    }

    public Item getToolItem(Material material){
        if (simple && replacements.containsKey(material.getId())){
            return replacements.get(material.getId()).get();
        }
        String id = simple ? material.getId() + "_" + this.id : this.id;
        return Objects.requireNonNull(GTAPI.get(IGTTool.class, id, domain)).getItem();
    }

    @Override
    public String getId() {
        return id;
    }

    public boolean hasContainer() {
        return hasContainer;
    }

    public boolean hasOriginalTag(){
        return originalTag;
    }

    public interface IToolSupplier{
        IGTTool create(String domain, GTToolType toolType, GTItemTier tier, Item.Properties properties);

        default IGTTool create(String domain, GTToolType toolType, GTItemTier tier, Item.Properties properties, int energyTier){
            return create(domain, toolType, tier, properties);
        }
    }
}
