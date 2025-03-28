package org.gtreimagined.gtlib.datagen.providers;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTLibTags;
import org.gtreimagined.gtlib.datagen.IAntimatterProvider;
import org.gtreimagined.gtlib.fluid.AntimatterFluid;
import org.gtreimagined.gtlib.fluid.AntimatterMaterialFluid;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import net.minecraft.core.Registry;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import static org.gtreimagined.gtlib.util.TagUtils.getForgelikeFluidTag;

public class AntimatterFluidTagProvider extends AntimatterTagProvider<Fluid> implements IAntimatterProvider {

    private final boolean replace;

    public AntimatterFluidTagProvider(String providerDomain, String providerName, boolean replace) {
        super(Registry.FLUID, providerDomain, providerName, "fluids");
        this.replace = replace;
    }

    protected void processTags(String domain) {
        AntimatterAPI.all(AntimatterFluid.class, domain).forEach(f -> {
            tag(getForgelikeFluidTag(f.getId()))
                    .add(f.getFluid())
                    .replace(replace);
            if (f instanceof AntimatterMaterialFluid) {
                Material m = ((AntimatterMaterialFluid) f).getMaterial();
                tag(getForgelikeFluidTag(m.getId()))
                        .add(f.getFluid())
                        .replace(replace);
                if (m.has(MaterialTags.ACID)){
                    tag(GTLibTags.ACID).add(f.getFluid());
                }
            }
        });
        if (domain.equals(Ref.SHARED_ID)){
            tag(getForgelikeFluidTag("water")).add(Fluids.WATER);
        }
    }
}
