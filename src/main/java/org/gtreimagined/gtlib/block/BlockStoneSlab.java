package org.gtreimagined.gtlib.block;

import lombok.Getter;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.builder.VariantBlockStateBuilder.VariantBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.SlabType;

public class BlockStoneSlab extends BlockBasicSlab implements ISharedAntimatterObject {
    @Getter
    protected String suffix;
    CobbleStoneType type;

    public BlockStoneSlab(CobbleStoneType type, String suffix) {
        super(type.getDomain(), type.getId() + (suffix.isEmpty() ? "" : "_") + suffix + "_slab", getProps(type));
        this.suffix = suffix;
        this.type = type;
    }

    private static Properties getProps(StoneType type) {
        Properties props = Properties.of(type.getBlockMaterial()).sound(type.getSoundType()).strength(type.getHardness(), type.getResistence());
        if (type.doesRequireTool()) {
            props.requiresCorrectToolForDrops();
        }
        return props;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[]{new Texture(type.getDomain(), type.getBeginningPath() + type.getId() + "/" + (suffix.isEmpty() ? "stone" : suffix))};
    }

    public void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        Texture topTexture, bottomTexture, sideTexture;
        topTexture = bottomTexture = sideTexture = getTextures()[0];
        ResourceLocation both = prov.existing(this.getDomain(), "block/" + this.getId().replace("_slab", ""));
        if (type == VanillaStoneTypes.BASALT && (suffix.isEmpty() || suffix.equals("smooth"))){
            if (suffix.isEmpty()) {
                both = prov.existing("minecraft", "block/basalt");
                topTexture = bottomTexture = new Texture("block/basalt_top");
                sideTexture = new Texture("block/basalt_side");
            } else {
                both = prov.existing("minecraft", "block/smooth_basalt");
                topTexture = bottomTexture = sideTexture = new Texture("block/smooth_basalt");
            }
        }
        GTBlockModelBuilder top = prov.models().getBuilder(getId() + "_top").parent(prov.existing("minecraft", "block/slab_top")).texture("bottom", bottomTexture).texture("top", topTexture).texture("side", sideTexture);
        GTBlockModelBuilder bottom = prov.models().getBuilder(getId()).parent(prov.existing("minecraft", "block/slab")).texture("bottom", bottomTexture).texture("top", topTexture).texture("side", sideTexture);
        ResourceLocation finalBoth = both;
        prov.getVariantBuilder(block).forAllStates(s -> {
            if (s.getValue(TYPE) == SlabType.DOUBLE) {
                return new VariantBuilder().modelFile(finalBoth);
            }
            return new VariantBuilder().modelFile(s.getValue(TYPE) == SlabType.TOP ? top : bottom);
        });
    }
}
