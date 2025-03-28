package org.gtreimagined.gtlib.block;

import lombok.Getter;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.builder.VariantBlockStateBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.registration.IAntimatterObject;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.material.Material;

import static net.minecraft.core.Direction.*;
import static net.minecraft.world.level.block.state.properties.StairsShape.STRAIGHT;

public class BlockBasicStair extends StairBlock implements IAntimatterObject, ITextureProvider, IModelProvider {
    protected final String domain, id;
    @Getter
    protected final Block base;

    public BlockBasicStair(String domain, String id, Block base, Properties properties) {
        super(base.defaultBlockState(), properties);
        this.domain = domain;
        this.id = id;
        this.base = base;
        AntimatterAPI.register(getClass(), this);
    }

    public BlockBasicStair(String domain, String id, Block base) {
        this(domain, id, base, Properties.of(Material.METAL).strength(1.0f, 1.0f).sound(SoundType.STONE));
    }

    public String getDomain() {
        return this instanceof ISharedAntimatterObject ? Ref.SHARED_ID : domain;
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
        GTBlockModelBuilder outer = prov.models().getBuilder(getId() + "_outer").parent(prov.existing(Ref.ID, "block/outer_stairs")).texture("bottom", texture).texture("top", texture).texture("side", texture);
        GTBlockModelBuilder inner = prov.models().getBuilder(getId() + "_inner").parent(prov.existing(Ref.ID, "block/inner_stairs")).texture("bottom", texture).texture("top", texture).texture("side", texture);
        GTBlockModelBuilder regular = prov.models().getBuilder(getId()).parent(prov.existing(Ref.ID, "block/stairs")).texture("bottom", texture).texture("top", texture).texture("side", texture);
        prov.getVariantBuilder(block).forAllStates(s -> {
            VariantBlockStateBuilder.VariantBuilder builder = new VariantBlockStateBuilder.VariantBuilder();
            GTBlockModelBuilder b = regular;
            StairsShape shape = s.getValue(SHAPE);
            Half half = s.getValue(HALF);
            Direction facing = s.getValue(FACING);
            boolean left = shape.getSerializedName().contains("left"), right = shape.getSerializedName().contains("right");
            if (shape.getSerializedName().contains("outer")) b = outer;
            if (shape.getSerializedName().contains("inner")) b = inner;
            builder.modelFile(b);
            if (half == Half.TOP) {
                builder.rotationX(180);
                if ((facing == EAST && right) || (facing == SOUTH && (shape == STRAIGHT || left))) {
                    builder.rotationY(90);
                }
                if ((facing == SOUTH && right) || (facing == WEST && (left || shape == STRAIGHT))) {
                    builder.rotationY(180);
                }
                if ((facing == WEST && right) || (facing == NORTH && (left || shape == STRAIGHT))) {
                    builder.rotationY(270);
                }
                builder.uvLock(true);
            } else {
                if ((facing == SOUTH && (shape == STRAIGHT || right)) || (facing == WEST && left)) {
                    builder.rotationY(90);
                }
                if ((facing == NORTH && left) || (facing == WEST && (right || shape == STRAIGHT))) {
                    builder.rotationY(180);
                }
                if ((facing == EAST && left) || (facing == NORTH && (right || shape == STRAIGHT))) {
                    builder.rotationY(270);
                }
                if (!((facing == EAST && (right || shape == STRAIGHT)) || (facing == SOUTH && left))) {
                    builder.uvLock(true);
                }
            }
            return builder;
        });
    }
}
