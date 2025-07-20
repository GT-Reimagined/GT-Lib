package org.gtreimagined.gtlib.datagen.providers;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class GTAdvancementProvider implements DataProvider, IGTLibProvider {

    private static final Logger LOGGER = LogManager.getLogger();
    private final List<Consumer<Consumer<Advancement>>> advancements;
    private final String providerDomain, providerName;

    @SafeVarargs
    public GTAdvancementProvider(String providerDomain, String providerName, Consumer<Consumer<Advancement>>... advancements) {
        this.providerDomain = providerDomain;
        this.providerName = providerName;
        if (advancements.length == 0)
            throw new IllegalArgumentException("GTLibAdvancementProvider requires at least one Advancement class.");
        this.advancements = Arrays.asList(advancements);
    }

    @Override
    public void run() {

    }

    @Override
    public void onCompletion() {
        Set<ResourceLocation> locs = new ObjectOpenHashSet<>();
        Consumer<Advancement> consumer = a -> {
            if (!locs.add(a.getId())) throw new IllegalStateException("Duplicate advancement " + a.getId());
            else {
                GTLibDynamics.RUNTIME_DATA_PACK.addData(GTLibDynamics.fix(a.getId(), "advancements", "json"), GTLibDynamics.serialize(a.deconstruct()));
            }
        };
        advancements.forEach(a -> a.accept(consumer));
    }

    private Path getPath(Path path, Advancement advancement) {
        return path.resolve(String.join("", "data/", providerDomain, "/advancements/", advancement.getId().getPath(), ".json"));
    }

    @NotNull
    @Override
    public String getName() {
        return providerName;
    }

    public static Advancement.Builder buildRootAdvancement(ItemLike provider, ResourceLocation backgroundPath, String title, String desc, FrameType type, boolean toast, boolean announce, boolean hide) {
        return Advancement.Builder.advancement().display(provider, Utils.translatable(title), Utils.translatable(desc), backgroundPath, type, toast, announce, hide).rewards(AdvancementRewards.Builder.experience(10));
    }

    public static Advancement.Builder buildAdvancement(Advancement parent, ItemLike provider, String title, String desc, FrameType type, boolean toast, boolean announce, boolean hide) {
        return Advancement.Builder.advancement().parent(parent).display(provider, Utils.translatable(title), Utils.translatable(desc), null, type, toast, announce, hide).rewards(AdvancementRewards.Builder.experience(10));
    }

    public static String getLoc(String domain, String id) {
        return String.join(":", domain, id);
    }

}
