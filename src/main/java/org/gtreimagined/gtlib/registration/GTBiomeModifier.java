package org.gtreimagined.gtlib.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;

public class GTBiomeModifier implements BiomeModifier {
    public static final Codec<GTBiomeModifier> CODEC = Codec.unit(GTBiomeModifier::new);
    @Override
    public void modify(Holder<Biome> arg, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD){
            GTLibWorldGenerator.reloadEvent(arg.unwrapKey().get().location(), builder.getClimateSettings().build(), builder.getSpecialEffects().build(), builder.getGenerationSettings(), builder.getMobSpawnSettings());
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
