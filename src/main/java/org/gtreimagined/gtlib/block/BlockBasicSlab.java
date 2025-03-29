package org.gtreimagined.gtlib.block;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.builder.VariantBlockStateBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Material;

public class BlockBasicSlab extends SlabBlock implements IGTObject, ITextureProvider, IModelProvider {
    protected final String domain, id;

    public BlockBasicSlab(String domain, String id, Properties properties) {
        super(properties);
        this.domain = domain;
        this.id = id;
        AntimatterAPI.register(getClass(), this);
    }

    public BlockBasicSlab(String domain, String id) {
        this(domain, id, Properties.of(Material.METAL).strength(1.0f, 1.0f).sound(SoundType.STONE));
    }

    public String getDomain() {
        return this instanceof ISharedGTObject ? Ref.SHARED_ID : domain;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[0];
    }

    public void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        Texture texture = getTextures()[0];
        ResourceLocation both = prov.existing(this.getDomain(), "block/" + this.getId().replace("_slab", ""));
        GTBlockModelBuilder top = prov.models().getBuilder(getId() + "_top").parent(prov.existing(Ref.ID, "block/slab_top")).texture("bottom", texture).texture("top", texture).texture("side", texture);
        GTBlockModelBuilder bottom = prov.models().getBuilder(getId()).parent(prov.existing(Ref.ID, "block/slab")).texture("bottom", texture).texture("top", texture).texture("side", texture);
        ResourceLocation finalBoth = both;
        prov.getVariantBuilder(block).forAllStates(s -> {
            if (s.getValue(TYPE) == SlabType.DOUBLE) {
                return new VariantBlockStateBuilder.VariantBuilder().modelFile(finalBoth);
            }
            return new VariantBlockStateBuilder.VariantBuilder().modelFile(s.getValue(TYPE) == SlabType.TOP ? top : bottom);
        });
    }
}
