package org.gtreimagined.gtlib.datagen.providers;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.Tags;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTLibTags;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.fluid.GTMaterialFluid;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import static org.gtreimagined.gtlib.util.TagUtils.getForgelikeFluidTag;

public class GTFluidTagProvider extends GTTagProvider<Fluid> implements IGTLibProvider {

    private final boolean replace;

    public GTFluidTagProvider(String providerDomain, String providerName, boolean replace) {
        super(Registries.FLUID, providerDomain, providerName, "fluids", f -> BuiltInRegistries.FLUID.getResourceKey(f).get());
        this.replace = replace;
    }

    protected void processTags(String domain) {
        GTAPI.all(GTFluid.class, domain).forEach(f -> {
            tag(getForgelikeFluidTag(f.getId()))
                    .add(f.getFluid())
                    .replace(replace);
            if (f instanceof GTMaterialFluid mf) {
                Material m = mf.getMaterial();
                if (mf.getType() == GTMaterialTypes.GAS){
                    tag(Tags.Fluids.GASEOUS).add(f.getFluid());
                }
                tag(getForgelikeFluidTag(m.getId()))
                        .add(f.getFluid())
                        .replace(replace);
                if (m.has(MaterialTags.ACID)){
                    tag(GTLibTags.ACID).add(f.getFluid());
                }
                if (m.has(MaterialTags.MAGIC)){
                    tag(GTLibTags.MAGIC).add(f.getFluid());
                }
            }
        });
        if (domain.equals(Ref.SHARED_ID)){
            tag(getForgelikeFluidTag("water")).add(Fluids.WATER);
        }
    }
}
