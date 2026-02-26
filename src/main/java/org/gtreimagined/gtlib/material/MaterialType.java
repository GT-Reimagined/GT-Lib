package org.gtreimagined.gtlib.material;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.registration.IRegistryEntryProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.gtreimagined.gtlib.util.Utils.*;

public class MaterialType<T> implements IMaterialTag, ISharedGTObject, IRegistryEntryProvider {

    public static final Codec<MaterialType<?>> CODEC = Codec.STRING.xmap((String t) -> GTAPI.get(MaterialType.class, t), MaterialType::getId);

    protected final String id;
    @Getter
    protected int layers;
    @Getter
    @Setter
    protected long unitValue;
    @Getter
    protected boolean generating = true, blockType, visible, splitName;
    protected final Set<Material> materials = new ObjectLinkedOpenHashSet<>(); //Linked to preserve insertion order for JEI
    protected final Map<MaterialType<?>, TagKey<?>> tagMap = new Object2ObjectOpenHashMap<>();
    @Getter
    protected String tagPrefix;
    @Getter
    protected Function<Material, String> lang, idGetter;
    protected T getter;
    private boolean hidden = false;
    @Getter
    protected final BiMap<Material, Supplier<Item>> replacements = HashBiMap.create();
    protected final Set<IMaterialTag> dependents = new ObjectLinkedOpenHashSet<>();
    //since we have two instances stored in gt lib.
    protected boolean hasRegistered;
    protected boolean ignoreTextureSets = false;

    public MaterialType(String id, int layers, boolean visible, long unitValue) {
        this.id = id;
        this.visible = visible;
        this.unitValue = unitValue;
        this.layers = layers;
        this.splitName = id.contains("_");
        this.tagPrefix = Utils.getConventionalMaterialType(this);
        this.tagMap.put(this, tagFromString(this.tagPrefix));
        this.lang = m -> {
            String[] split = getLocalizedMaterialType(this);
            if (split.length > 1) {
                return String.join("", split[0], " ", getLocalizedType(m), " ", split[1]);
            } else {
                return String.join("", getLocalizedType(m), " ", split[0]);
            }
        };
        this.idGetter = m -> {
            String[] split = getLocalizedMaterialType(this);
            if (split.length > 1) {
                return String.join("", split[0].toLowerCase().replace(" ", "_"), "_", m.getId(), "_", split[1].toLowerCase().replace(" ", "_"));
            } else {
                return String.join("", m.getId(), "_", split[0].toLowerCase().replace(" ", "_"));
            }
        };
        register(MaterialType.class, getId());
    }

    protected TagKey<?> tagFromString(String name) {
        return TagUtils.getForgelikeItemTag(name);
    }

    public MaterialType<T> nonGen() {
        generating = false;
        return this;
    }

    /**
     * Adds a list of dependent flags, that is all of these flags are added as well.
     *
     * @param tags the list of tags.
     * @return this
     */
    public void dependents(IMaterialTag... tags) {
        dependents.addAll(Arrays.asList(tags));
    }

    /**
     * Forces these tags to not generate, assuming they have a replacement.
     */
    public void replacement(Material mat, Supplier<Item> replacement) {
        if (!mat.enabled) return;
        replacements.put(mat, replacement);
        this.add(mat);
        GTAPI.addReplacement(getMaterialTag(mat), replacement);
    }

    public boolean hasReplacement(Material mat) {
        return replacements.containsKey(mat);
    }

    public Material getMaterialFromStack(ItemStack stack) {
        if (stack.getItem() instanceof MaterialItem item) {
            if (item.getType() == this) return item.getMaterial();
            return null;
        }

        //TODO better fix
        //return replacements.inverse().get(stack.getItem());
        for (Map.Entry<Material, Supplier<Item>> entry : replacements.entrySet()) {
            Item item = entry.getValue().get();
            if (item == stack.getItem()){
                return entry.getKey();
            }
        }
        // gets material from other mod items using the tags
        for (TagKey<Item> tagKey : stack.getItem().builtInRegistryHolder().tags().toList()){
            String prefix = this.getTag().location().getPath() + "/";
            if (tagKey.location().getNamespace().equals(this.getTag().location().getNamespace()) && tagKey.location().getPath().contains(prefix)){
                Material material = Material.get(tagKey.location().getPath().replace(prefix, ""));
                if (material != Material.NULL){
                    return material;
                }
            }
        }
        return null;
    }

    public boolean hidden() {
        return hidden;
    }

    public MaterialType<T> setHidden() {
        this.hidden = true;
        return this;
    }

    public boolean ignoreTextureSets(){
        return ignoreTextureSets;
    }

    public MaterialType<T> setIgnoreTextureSets() {
        ignoreTextureSets = true;
        return this;
    }

    @SuppressWarnings("unchecked")
    public TagKey<Item> getMaterialTag(Material m) {
        return (TagKey<Item>) tagFromString(String.join("", this.tagPrefix, "/", (getId().equals("raw_ore_block") ? "raw_" : ""), m.getId()));
    }

    public RecipeIngredient getMaterialIngredient(Material m, int count) {
        return RecipeIngredient.of(getMaterialTag(m), count);
    }

    public MaterialType<T> blockType() {
        blockType = true;
        this.tagMap.put(this, TagUtils.getForgelikeBlockTag(Utils.getConventionalMaterialType(this)));
        return this;
    }

    public MaterialType<T> unSplitName() {
        splitName = false;
        this.tagPrefix = Utils.getConventionalMaterialType(this);
        this.tagMap.put(this, tagFromString(tagPrefix));
        return this;
    }

    public MaterialType<T> setLang(Function<Material, String> lang) {
        this.lang = lang;
        return this;
    }

    public MaterialType<T> setLang(BiFunction<MaterialType<?>, Material, String> lang){
        return setLang(m -> lang.apply(this, m));
    }

    public MaterialType<T> setIdGetter(Function<Material, String> idGetter){
        this.idGetter = idGetter;
        return this;
    }

    public MaterialType<T> setTagPrefix(String prefix){
        this.tagPrefix = prefix;
        tagMap.put(this, tagFromString(prefix));
        return this;
    }

    @Override
    public void add(Material... m) {
        for (Material m2 : m) {
            if (m2.enabled) {
                all().add(m2);
                m2.types.add(this);
            }
        }
    }

    @Override
    public void remove(Material... m) {
        for (Material m2 : m) {
            all().remove(m2);
            m2.types.remove(this);
        }
    }

    @Override
    public String getId() {
        return id;
    }

    public <T> TagKey<T> getTag() {
        return (TagKey<T>) tagMap.get(this);
    }

    public MaterialType<T> set(T getter) {
        this.getter = getter;
        return this;
    }

    @Override
    public Set<IMaterialTag> dependents() {
        return this.dependents;
    }

    public T get() {
        return getter;
    }

    @Override
    public Set<Material> all() {
        return materials;
    }

    public boolean isVisible() {
        return visible || GTLibConfig.SHOW_ALL_MATERIAL_ITEMS.get();
    }

    public boolean allowGen(Material material) {
        return generating && materials.contains(material) && GTAPI.getReplacement(this, material) == null;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static ImmutableMap<Item, Tuple<MaterialType, Material>> tooltipCache;

    @OnlyIn(Dist.CLIENT)
    public static void buildTooltips() {
        ImmutableMap.Builder<Item, Tuple<MaterialType, Material>> builder = ImmutableMap.builder();
        GTAPI.all(MaterialType.class, t -> {
            BiMap<Supplier<Item>, Material> map = t.getReplacements().inverse();
            for (Map.Entry<Supplier<Item>, Material> entry : map.entrySet()) {
                builder.put(entry.getKey().get(), new Tuple<>(t, entry.getValue()));
            }
        });
        tooltipCache = builder.build();
    }

    public static void addTooltip(ItemStack stack, List<Component> tooltips, Player player, TooltipFlag flag){
        if (player == null) return;
        if (tooltipCache == null) return;
        var mat = tooltipCache.get(stack.getItem());
        if (mat == null) {
            if (stack.getItem() instanceof MaterialItem item) {
                MaterialItem.addTooltipsForMaterialItems(stack, item.material, item.type, player.level(), tooltips, flag);
            }
            return;
        }
        MaterialItem.addTooltipsForMaterialItems(stack, mat.getB(), mat.getA(), player.level(), tooltips, flag);
    }

    public static Material getMaterialFromStackTypeless(ItemStack stack) {
        Material material = null;
        for (MaterialType<?> type : GTAPI.all(MaterialType.class)) {
            material = type.getMaterialFromStack(stack);
            if (material != null) {
                break;
            }
        }
        return material;
    }

    @Override
    public void onRegistryBuild(ResourceKey<? extends Registry<?>> registry) {

    }

    protected boolean doRegister() {
        boolean old = hasRegistered;
        hasRegistered = true;
        return !old;
    }
}
