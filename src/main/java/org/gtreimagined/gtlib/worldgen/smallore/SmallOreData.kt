package org.gtreimagined.gtlib.worldgen.smallore;

import com.mojang.serialization.Codec;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

public class SmallOreData extends BaseWorldGenData<SmallOre> {
    public static final SmallOreData INSTANCE = new SmallOreData();
    private SmallOreData() {
        super("small_ores", "small_ore");
    }

    @Override
    protected Codec<SmallOre> getCodec() {
        return SmallOre.CODEC;
    }
}
