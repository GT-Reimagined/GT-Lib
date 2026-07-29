package org.gtreimagined.gtlib.integration.recipeviewer.renderer;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.client.gui.Font;
import org.gtreimagined.gtlib.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/*
 Dist cleaning and that annoying stuff means that I have to lazily init info renderers.
 There is most likely a better way but I cba...
 */
public class InfoRenderers {


    public static final IRecipeInfoRenderer BASIC_RENDERER = r -> {
        List<Component> list = new ArrayList<>();
        if (r.getDuration() == 0) return list;
        Component additional;
        if (r.getDuration() < 1200) {
            additional = Component.empty();
        } else if (r.getDuration() < 36000) {
            additional = Utils.translatable("recipe_info.gtlib.duration.seconds", (r.getDuration() / 20.0f));
        } else {
            additional = Utils.translatable("recipe_info.gtlib.duration.minutes", (r.getDuration() / 1200.0f));
        }
        list.add(Utils.translatable("recipe_info.gtlib.duration", r.getDuration(), additional));
        return list;
    };
    public static final IRecipeInfoRenderer EMPTY_RENDERER = r -> List.of();

    public static final IRecipeInfoRenderer DEFAULT_RENDERER = r -> {
        if (r.getDuration() == 0 && r.getPower() == 0) return List.of();
        List<Component> list = new ArrayList<>();
        Component additional;
        if (r.getDuration() < 1200) {
            additional = Component.empty();
        } else if (r.getDuration() < 36000) {
            additional = Utils.translatable("recipe_info.gtlib.duration.seconds", (r.getDuration() / 20.0f));
        } else {
            additional = Utils.translatable("recipe_info.gtlib.duration.minutes", (r.getDuration() / 1200.0f));
        }
        list.add(Utils.translatable("recipe_info.gtlib.duration", r.getDuration(), additional));
        Tier tier = Tier.getTier((r.getPower() / r.getAmps()));
        list.add(Utils.translatable("recipe_info.gtlib.eut", r.getPower(), Utils.translatable("recipe_info.gtlib.eut.tier", tier.getId().toUpperCase(Locale.ROOT)).withStyle(tier.getRarityFormatting())));
        list.add(Utils.translatable("recipe_info.gtlib.amps", r.getAmps()));
        list.add(Utils.translatable("recipe_info.gtlib.total_eu", r.getDuration() * r.getPower() * r.getAmps()));
        return list;
    };

    public static final IRecipeInfoRenderer FE_RENDERER = r ->  {
        if (r.getDuration() == 0 && r.getPower() == 0) return List.of();
        List<Component> list = new ArrayList<>();
        Component additional;
        if (r.getDuration() < 1200) {
            additional = Component.empty();
        } else if (r.getDuration() < 36000) {
            additional = Utils.translatable("recipe_info.gtlib.duration.seconds", (r.getDuration() / 20.0f));
        } else {
            additional = Utils.translatable("recipe_info.gtlib.duration.minutes", (r.getDuration() / 1200.0f));
        }
        list.add(Utils.translatable("recipe_info.gtlib.duration", r.getDuration(), additional));
        list.add(Utils.translatable("recipe_info.gtlib.fet", r.getPower()));
        list.add(Utils.translatable("recipe_info.gtlib.total_fe", r.getDuration() * r.getPower()));
        return list;
    };

    public static final IRecipeInfoRenderer FUEL_RENDERER = r -> {
        List<Component> list = new ArrayList<>();
        list.add(Utils.translatable("recipe_info.gtlib.eul", (double) r.getPower() / (double) Objects.requireNonNull(r.getInputFluids()).get(0).getAmount()));
        list.add(Utils.translatable("recipe_info.gtlib.fluid_per_tick", Objects.requireNonNull(r.getInputFluids()).get(0).getAmount()));
        return list;
    };

}
