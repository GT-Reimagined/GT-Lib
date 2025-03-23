package muramasa.antimatter.block;

import muramasa.antimatter.Ref;
import muramasa.antimatter.texture.Texture;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class BlockDimensionMarker extends BlockBasic {
    final String dimension;
    public BlockDimensionMarker(String dimension) {
        super(Ref.ID, dimension + "_marker", Properties.of(Material.STONE).sound(SoundType.STONE).instabreak());
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
