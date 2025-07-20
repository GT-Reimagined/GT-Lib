package org.gtreimagined.gtlib.fluid;

import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.fluids.FluidType;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Block;

import static org.gtreimagined.gtlib.material.MaterialTags.MOLTEN;

/**
 * GTMaterialFluid is an extension of GTFluid that includes both {@link Material} and {@link MaterialType} parameters
 * <p>
 * This allows for straightforward fluid generation derived from base Material values, these are of course overridable with the different constructors still.
 */
public class GTMaterialFluid extends GTFluid {

    protected Material material;
    protected MaterialType<?> type;

    public GTMaterialFluid(String domain, Material material, MaterialType<?> type, FluidType.Properties builder, Block.Properties blockProperties, IClientFluidTypeExtensions extensions) {
        super(domain, type.getId() + "_" + material.getId(), modifyProperties(material, builder), blockProperties, extensions);
        this.material = material;
        this.type = type;
    }

    public GTMaterialFluid(String domain, Material material, MaterialType<?> type) {
        this(domain, material, type, prepareFluidTypeProperties(material, type), prepareProperties(material), prepareExtensions(material, type));
    }

    public Material getMaterial() {
        return material;
    }

    public MaterialType<?> getType() {
        return type;
    }

    private static FluidType.Properties modifyProperties(Material material, FluidType.Properties properties){
        if (material.has(MaterialTags.FLUID_TYPE_PROPERTIES)) MaterialTags.FLUID_TYPE_PROPERTIES.get(material).accept(properties);
        return properties;
    }

    private static FluidType.Properties prepareFluidTypeProperties(Material material, MaterialType<?> type){
        int density = material.has(MaterialTags.FLUID_DENSITY) ? MaterialTags.FLUID_DENSITY.getInt(material) : type == GTMaterialTypes.GAS ? 1 : material.has(MOLTEN) ? 3000 : 1000;
        if (type == GTMaterialTypes.GAS){
            return getDefaultFluidTypeProperties(false).viscosity(200).density(density).temperature(MaterialTags.GAS_TEMPERATURE.getInt(material));
        } else {
            FluidType.Properties properties = getDefaultFluidTypeProperties(material.has(MOLTEN)).density(density);
            if (material.has(MOLTEN)){
                properties.viscosity(6000).lightLevel(15);
            }
            return properties.temperature(MaterialTags.LIQUID_TEMPERATURE.getInt(material));
        }
    }

    private static IClientFluidTypeExtensions prepareExtensions(Material material, MaterialType<?> type){
        if (type == GTMaterialTypes.GAS){
            return GTClientFluidTypeExtension.builder().stillTexture(GAS_TEXTURE).flowingTexture(GAS_FLOW_TEXTURE).overlayTexture(OVERLAY_TEXTURE).tintColor((70 << 24) | (material.getRGB() & 0x00ffffff)).build();
        } else {
            GTClientFluidTypeExtension.GTClientFluidTypeExtensionBuilder b = GTClientFluidTypeExtension.builder();
            b.overlayTexture(OVERLAY_TEXTURE);
            if (material.has(MOLTEN)){
                b.stillTexture(LIQUID_HOT_STILL_TEXTURE).flowingTexture(LIQUID_HOT_FLOW_TEXTURE).build();
            } else {
                b.stillTexture(LIQUID_STILL_TEXTURE).flowingTexture(LIQUID_FLOW_TEXTURE).build();
            }
            int alpha = material.has(MOLTEN) ? 0xFF000000 : (155 << 24);
            b.tintColor(alpha | (material.getRGB() & 0x00FFFFFF));
            return b.build();
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
        return type == GTMaterialTypes.GAS || this.getFluid().is(Fluids.GASEOUS);
    }
}
