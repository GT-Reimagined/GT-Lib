package org.gtreimagined.gtlib.block;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;

import static net.minecraft.world.level.block.state.properties.WallSide.LOW;
import static net.minecraft.world.level.block.state.properties.WallSide.TALL;

public class BlockStoneWall extends WallBlock implements ISharedGTObject, ITextureProvider, IModelProvider {
    protected String domain, id, suffix;
    CobbleStoneType type;

    public BlockStoneWall(CobbleStoneType type, String suffix) {
        super(getProps(type));
        domain = type.getDomain();
        String s = suffix.isEmpty() ? "" : "_";
        id = type.getId() + s + suffix + "_wall";
        this.suffix = suffix;
        this.type = type;
        GTAPI.register(getClass(), this);
    }

    private static Properties getProps(StoneType type) {
        Properties props = Properties.of(type.getBlockMaterial()).sound(type.getSoundType()).strength(type.getHardness(), type.getResistence());
        if (type.doesRequireTool()) {
            props.requiresCorrectToolForDrops();
        }
        return props;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[]{new Texture(type.getDomain(), type.getBeginningPath() + type.getId() + "/" + (suffix.isEmpty() ? "stone" : suffix))};
    }

    @Override
    public void onItemModelBuild(ItemLike item, GTItemModelProvider prov) {
        prov.getBuilder(item).parent(new ResourceLocation("minecraft", "block/wall_inventory")).texture("wall", getTextures()[0]);
    }

    @Override
    public void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        GTBlockModelBuilder post = prov.models().getBuilder(getId() + "_post").parent(prov.existing("minecraft", "block/template_wall_post")).texture("wall", getTextures()[0]);
        GTBlockModelBuilder side = prov.models().getBuilder(getId() + "_side").parent(prov.existing("minecraft", "block/template_wall_side")).texture("wall", getTextures()[0]);
        GTBlockModelBuilder side_tall = prov.models().getBuilder(getId() + "_side_tall").parent(prov.existing("minecraft", "block/template_wall_side_tall")).texture("wall", getTextures()[0]);
        prov.getMultipartBuilder(block)
                .part().modelFile(post).addModel().condition(UP, true).end()
                .part().modelFile(side).uvLock().addModel().condition(NORTH_WALL, LOW).end()
                .part().modelFile(side).rotationY(90).uvLock().addModel().condition(EAST_WALL, LOW).end()
                .part().modelFile(side).rotationY(180).uvLock().addModel().condition(SOUTH_WALL, LOW).end()
                .part().modelFile(side).rotationY(270).uvLock().addModel().condition(WEST_WALL, LOW).end()
                .part().modelFile(side_tall).uvLock().addModel().condition(NORTH_WALL, TALL).end()
                .part().modelFile(side_tall).rotationY(90).uvLock().addModel().condition(EAST_WALL, TALL).end()
                .part().modelFile(side_tall).rotationY(180).uvLock().addModel().condition(SOUTH_WALL, TALL).end()
                .part().modelFile(side_tall).rotationY(270).uvLock().addModel().condition(WEST_WALL, TALL).end();

    }
}
