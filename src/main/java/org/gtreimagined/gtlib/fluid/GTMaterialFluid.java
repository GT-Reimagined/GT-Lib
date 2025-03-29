package org.gtreimagined.gtlib.fluid;

import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.fluids.FluidAttributes;

import static org.gtreimagined.gtlib.material.MaterialTags.MOLTEN;

/**
 * GTMaterialFluid is an extension of GTFluid that includes both {@link Material} and {@link MaterialType} parameters
 * <p>
 * This allows for straightforward fluid generation derived from base Material values, these are of course overridable with the different constructors still.
 */
public class GTMaterialFluid extends GTFluid {

    protected Material material;
    protected MaterialType<?> type;

    public GTMaterialFluid(String domain, Material material, MaterialType<?> type, FluidAttributes.Builder builder, Block.Properties blockProperties) {
        super(domain, type.getId() + "_" + material.getId(), builder, blockProperties);
        this.material = material;
        this.type = type;
    }

    public GTMaterialFluid(String domain, Material material, MaterialType<?> type, ResourceLocation stillLoc, ResourceLocation flowLoc) {
        super(domain, type.getId() + "_" + material.getId(), stillLoc, flowLoc);
        this.material = material;
        this.type = type;
    }

    public GTMaterialFluid(String domain, Material material, MaterialType<?> type) {
        this(domain, material, type, prepareAttributes(domain, material, type), prepareProperties(material));
    }

    public Material getMaterial() {
        return material;
    }

    public MaterialType<?> getType() {
        return type;
    }

    private static FluidAttributes.Builder prepareAttributes(String domain, Material material, MaterialType<?> type) {
        int density = material.has(MaterialTags.FLUID_DENSITY) ? MaterialTags.FLUID_DENSITY.getInt(material) : type == GTMaterialTypes.GAS ? 1 : material.has(MOLTEN) ? 3000 : 1000;
        if (type == GTMaterialTypes.GAS) {
            return FluidAttributes.builder(GAS_TEXTURE, GAS_FLOW_TEXTURE).overlay(OVERLAY_TEXTURE).color((70 << 24) | (material.getRGB() & 0x00ffffff))
                    .viscosity(200).density(density).gaseous().temperature(MaterialTags.GAS_TEMPERATURE.getInt(material))
                    .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY);
        } else {
            FluidAttributes.Builder b = getDefaultAttributesBuilder(material.has(MOLTEN)).density(density);
            if (material.has(MOLTEN)){
                b.viscosity(6000).luminosity(15);
            }
            int alpha = material.has(MOLTEN) ? 0xFF000000 : (155 << 24);
            return b.color(alpha | (material.getRGB() & 0x00FFFFFF))
                    .temperature(MaterialTags.LIQUID_TEMPERATURE.getInt(material));
        }
    }

    private static Block.Properties prepareProperties(Material material) {
        return getDefaultBlockProperties().lightLevel(s -> material.has(MOLTEN)? 15 : 0);
    }


    @Override
    public String getLang(String lang) {
        if (lang.equals(Language.DEFAULT)) {
            String display = material.getDisplayNameString();
            if (isGasType()) {
                return display;
            }
            String liquid = material.has(MOLTEN) ? "Molten " : "";
            return liquid + display;
        }
        return super.getLang(lang);
    }

    private boolean isGasType(){
        return type == GTMaterialTypes.GAS || this.getAttributes().isGaseous();
    }
}
