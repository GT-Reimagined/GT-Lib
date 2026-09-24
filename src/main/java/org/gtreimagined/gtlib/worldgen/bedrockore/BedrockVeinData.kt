package org.gtreimagined.gtlib.worldgen.bedrockore;

import com.mojang.serialization.Codec;
import org.gtreimagined.gtlib.worldgen.BaseWorldGenData;

public class BedrockVeinData extends BaseWorldGenData<BedrockVein> {
    public static final BedrockVeinData INSTANCE = new BedrockVeinData();
    private BedrockVeinData() {
        super("bedrock_veins", "bedrock_vein");
    }

    @Override
    protected Codec<BedrockVein> getCodec() {
        return BedrockVein.CODEC;
    }
}
