package org.gtreimagined.gtlib.registration;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;

@Mod.EventBusSubscriber(modid = Ref.ID)
public class GTBiomeModifier implements BiomeModifier {
    public static final Codec<GTBiomeModifier> CODEC = Codec.unit(GTBiomeModifier::new);

    public static RegistryAccess REGISTRY_ACCESS;


    @SubscribeEvent
    public static void getRegistryAccess(ServerStartingEvent event) {
        REGISTRY_ACCESS = event.getServer().registryAccess();
    }

    @Override
    public void modify(Holder<Biome> arg, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD){
            try {
                Registry<PlacedFeature> placedFeatureRegistry = REGISTRY_ACCESS.registryOrThrow(Registries.PLACED_FEATURE);
                GTLibWorldGenerator.reloadEvent(arg.unwrapKey().get().location(), builder.getClimateSettings().build(), builder.getSpecialEffects().build(), builder.getGenerationSettings(), builder.getMobSpawnSettings(), placedFeatureRegistry);
            } catch (IllegalStateException ignored){}
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return CODEC;
    }
}
