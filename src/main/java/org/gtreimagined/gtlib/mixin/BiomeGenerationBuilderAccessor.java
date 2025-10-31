package org.gtreimagined.gtlib.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(BiomeGenerationSettings.PlainBuilder.class)
public interface BiomeGenerationBuilderAccessor {

    @Accessor
    List<List<Holder<PlacedFeature>>> getFeatures();
}
