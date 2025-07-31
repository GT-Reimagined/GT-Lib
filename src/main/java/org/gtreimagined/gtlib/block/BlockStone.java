package org.gtreimagined.gtlib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.texture.Texture;

public class BlockStone extends BlockBasic implements ISharedGTObject {

    protected StoneType type;
    protected String suffix;

    public BlockStone(StoneType type) {
        super(type.getDomain(), type.getId(), getProps(type));
        this.type = type;
        this.suffix = "";
    }

    public BlockStone(StoneType type, String suffix) {
        super(type.getDomain(), type.getId() + "_" + suffix, getProps(type).isValidSpawn(BlockStone::notValidSpawn));
        this.type = type;
        this.suffix = suffix;
    }

    private static Properties getProps(StoneType type) {
        Properties props = Properties.of(type.getBlockMaterial()).sound(type.getSoundType()).strength(type.getHardness(), type.getResistence());
        if (type.doesRequireTool()) {
            props.requiresCorrectToolForDrops();
        }
        return props;
    }

    static boolean notValidSpawn(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, EntityType<?> object) {
        return false;
    }

    public StoneType getType() {
        return type;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public Texture[] getTextures() {
        if (type instanceof CobbleStoneType c && !suffix.isEmpty()) {
            return new Texture[]{new Texture(type.getDomain(), c.getBeginningPath() + type.getId() + "/" + suffix)};
        }
        return type.getTextures();
    }
}
