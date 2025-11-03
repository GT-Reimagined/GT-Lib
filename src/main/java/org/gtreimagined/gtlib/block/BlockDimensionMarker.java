package org.gtreimagined.gtlib.block;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.world.level.block.SoundType;

public class BlockDimensionMarker extends BlockBasic {
    final String dimension;
    public BlockDimensionMarker(String dimension) {
        super(Ref.ID, dimension + "_marker", Properties.of().sound(SoundType.STONE).instabreak());
        this.dimension = dimension;
    }

    public String getDimension() {
        return dimension;
    }

    @Override
    public Texture[] getTextures() {
        return new Texture[]{
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/bottom"),
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/top"),
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/front"),
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/back"),
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/left"),
                new Texture(Ref.ID, "block/dimension_marker/" + dimension + "/right")
        };
    }
}
