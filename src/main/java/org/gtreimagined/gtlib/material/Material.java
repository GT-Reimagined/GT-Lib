package org.gtreimagined.gtlib.material;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.item.ItemFluidCell;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.gtreimagined.gtlib.Ref.U;
import static org.gtreimagined.gtlib.material.TextureSet.NONE;

@Accessors(chain = true)
public class Material implements ISharedGTObject {

    public static final Material NULL = GTAPI.register(Material.class, new Material(Ref.ID, "null", 0xffffff, NONE));
    public static final Codec<Material> CODEC = Codec.STRING.xmap(Material::get, Material::getId);

    @Getter
    final Set<MaterialType<?>> types = new HashSet<>();
    /**
     * Basic Members
     **/
    private final String domain;
    private final String id;
    private Component displayName;
    @Getter
    @Setter
    private String displayNameString;
    @Getter
    @Setter
    private int rgb;
    @Getter
    private final TextureSet set;

    /**
     * Element Members
     *
     * -- GETTER --
     *  Element Getters
     */
    @Getter
    private Element element;
    private String chemicalFormula = null;
    private int massMultiplier = 1, massDivider = 1;

    public final boolean enabled;

    public Material(String domain, String id, int rgb, TextureSet set, String... modIds) {
        this.domain = domain;
        this.id = id;
        this.rgb = rgb;
        this.set = set;
        this.displayNameString = Utils.lowerUnderscoreToUpperSpaced(id);
        if (modIds != null && modIds.length > 0) {
            for (String modId : modIds) {
                if (!GTAPI.isModLoaded(modId)) {
                    enabled = false;
                    return;
                }
            }
        }
        enabled = true;
        MaterialTags.ORE_MULTI.add(this, 1);
        MaterialTags.SMELTING_MULTI.add(this, 1);
        MaterialTags.BY_PRODUCT_MULTI.add(this, 1);
        MaterialTags.SMELT_INTO.add(this, this);
        MaterialTags.DIRECT_SMELT_INTO.add(this, this);
        MaterialTags.ARC_SMELT_INTO.add(this, this);
        MaterialTags.MACERATE_INTO.add(this, this);
        MaterialTags.MELT_INTO.add(this, this);
        MaterialTags.PROCESS_INTO.add(this, Pair.of(new ObjectArrayList<>(), -1));
        MaterialTags.BYPRODUCTS.add(this, new ObjectArrayList<>());
    }

    public static void init(){}

    public String materialDomain() {
        return domain;
    }

    @Override
    public boolean shouldRegister() {
        return enabled;
    }

    public Material(String domain, String id, int rgb, TextureSet set, Element element, String... modIds) {
        this(domain, id, rgb, set, modIds);
        this.element = element;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return Utils.lowerUnderscoreToUpperSpaced(getId());
    }

    public boolean has(IMaterialTag... tags) {
        for (IMaterialTag t : tags) {
            if (!t.all().contains(this)) return false;
        }
        return true;
    }

    public Material setMassMultiplierAndDivider(int massMultiplier, int massDivider){
        this.massMultiplier = massMultiplier;
        this.massDivider = massDivider;
        return this;
    }

    public Material flags(IMaterialTag... tags) {
        if (!enabled) return this;
        for (IMaterialTag t : tags) {
            if (!this.has(t)) {
                t.add(this);
            }
            flags(t.dependents().stream().filter(d -> !this.has(d)).toArray(IMaterialTag[]::new));
        }
        return this;
    }

    public void remove(IMaterialTag... tags) {
        if (!enabled) return;
        for (IMaterialTag t : tags) {
            t.remove(this);
        }
    }

    public void setChemicalFormula() {
        if (!enabled) return;
        if (chemicalFormula != null && element != null) return;
        if (element != null) chemicalFormula = element.getElement();
        else if (!MaterialTags.PROCESS_INTO.get(this).getLeft().isEmpty()) {
            MaterialTags.PROCESS_INTO.get(this).getLeft().forEach(t -> t.m.setChemicalFormula());
            chemicalFormula = String.join("", MaterialTags.PROCESS_INTO.get(this).getLeft().stream().map(MaterialStack::toString).collect(Collectors.joining()));
        }
    }

    /**
     * Basic Getters
     **/
    public Component getDisplayName() {
        return displayName == null ? displayName = Utils.translatable("material." + getId()) : displayName;
    }

    public int getRGB() {
        return rgb;
    }

    public long getProtons() {
        if (element != null) return element.getProtons();
        if (MaterialTags.PROCESS_INTO.get(this).getLeft().size() <= 0) return Element.Tc.getProtons();
        long rAmount = 0, tAmount = 0;
        for (MaterialStack stack : MaterialTags.PROCESS_INTO.get(this).getLeft()) {
            tAmount += stack.s;
            rAmount += stack.s * stack.m.getProtons();
        }
        return (getMultiplier() * rAmount) / (tAmount * U);
    }

    public long getNeutrons() {
        if (element != null) return element.getNeutrons();
        if (MaterialTags.PROCESS_INTO.get(this).getLeft().size() <= 0) return Element.Tc.getNeutrons();
        long rAmount = 0, tAmount = 0;
        for (MaterialStack stack : MaterialTags.PROCESS_INTO.get(this).getLeft()) {
            tAmount += stack.s;
            rAmount += stack.s * stack.m.getNeutrons();
        }
        return (getMultiplier() * rAmount) / (tAmount * U);
    }

    public long getMass() {
        if (element != null) return element.getMass();
        if (MaterialTags.PROCESS_INTO.get(this).getLeft().size() <= 0) return Element.Tc.getMass();
        long rAmount = 0, tAmount = 0;
        for (MaterialStack stack : MaterialTags.PROCESS_INTO.get(this).getLeft()) {
            tAmount += stack.s;
            rAmount += stack.s * stack.m.getMass();
        }
        return (getMultiplier() * rAmount) / (tAmount * U);
    }

    public long getDensity() {
        if (element != null) return element.getDensity();
        if (MaterialTags.PROCESS_INTO.get(this).getLeft().size() <= 0) return Element.Tc.getDensity();
        long rAmount = 0, tAmount = 0;
        for (MaterialStack stack : MaterialTags.PROCESS_INTO.get(this).getLeft()) {
            tAmount += stack.s;
            rAmount += stack.s * stack.m.getDensity();
        }
        return (getMultiplier() * rAmount) / (tAmount * U);
    }

    public long getHardness() {
        if (element != null) return element.getHardness();
        if (MaterialTags.PROCESS_INTO.get(this).getLeft().size() <= 0) return Element.Tc.getHardness();
        long rAmount = 0, tAmount = 0;
        for (MaterialStack stack : MaterialTags.PROCESS_INTO.get(this).getLeft()) {
            tAmount += stack.s;
            rAmount += stack.s * stack.m.getHardness();
        }
        return (getMultiplier() * rAmount) / (tAmount * U);
    }

    private long getMultiplier() {
        return (U * massMultiplier) / massDivider;
    }

    public String getChemicalFormula() {
        return chemicalFormula == null ? "" : chemicalFormula;
    }

    /**
     * Fluid/Gas/Plasma Getters
     **/
    public Fluid getLiquid() {
        return GTMaterialTypes.LIQUID.get().get(this, 1).getFluid();
    }

    public Fluid getGas() {
        return GTMaterialTypes.GAS.get().get(this, 1).getFluid();
    }

    public FluidStack getLiquid(int mb) {
        if (!this.has(GTMaterialTypes.LIQUID)){
            throw new RuntimeException("Material: " + this.getId() + " does not have liquid");
        }
        return GTMaterialTypes.LIQUID.get().get(this, mb);
    }

    public FluidStack getGas(int mb) {
        if (!this.has(GTMaterialTypes.GAS)){
            throw new RuntimeException("Material: " + this.getId() + " does not have gas");
        }
        return GTMaterialTypes.GAS.get().get(this, mb);
    }

    public FluidIngredient getFluidIngredient(int mb){
        return FluidIngredient.of(getFluidTag(), mb);
    }

    public TagKey<Fluid> getFluidTag(){
        return TagUtils.getForgelikeFluidTag(this.getId());
    }

    /**
     * Processing Getters/Setters
     **/

    public List<MaterialStack> getProcessInto() {
        return MaterialTags.PROCESS_INTO.get(this).getLeft();
    }

    public List<Material> getByProducts() {
        return MaterialTags.BYPRODUCTS.getList(this);
    }

    public boolean hasByProducts() {
        return MaterialTags.BYPRODUCTS.getList(this).size() > 0;
    }

    public ItemStack getCell(int amount, ItemFluidCell cell) {
        return Utils.ca(amount, cell.fill(getLiquid()));
        // return ItemStack.EMPTY;
    }

    public ItemStack getCellGas(int amount, ItemFluidCell cell) {
        return Utils.ca(amount, cell.fill(getGas()));
        //return ItemStack.EMPTY;
    }

    public static Material get(String id) {
        Material material = GTAPI.get(Material.class, id);
        return material == null ? NULL : material;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
