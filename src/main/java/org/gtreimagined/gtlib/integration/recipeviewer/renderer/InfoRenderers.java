package org.gtreimagined.gtlib.integration.recipeviewer.renderer;

import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import net.minecraft.client.gui.Font;

import java.util.Objects;

/*
 Dist cleaning and that annoying stuff means that I have to lazily init info renderers.
 There is most likely a better way but I cba...
 */
public class InfoRenderers {


    public static final IRecipeInfoRenderer BASIC_RENDERER = new IRecipeInfoRenderer() {
        @Override
        public void render(GuiGraphics graphics, IRecipe recipe, Font font, int guiOffsetX, int guiOffsetY) {
            String additional = recipe.getDuration() < 1200 ? "" : recipe.getDuration() < 36000 ? " (" + (recipe.getDuration() / 20.0f) + " secs)" : " (" + (recipe.getDuration() / 1200.0f) + " mins)";
            String duration = "Duration: " + recipe.getDuration() + " ticks" + additional;
            renderString(graphics, duration, font, 5, 0, guiOffsetX, guiOffsetY);
        }

        @Override
        public int getRows() {
            return 1;
        }
    };
    public static final IRecipeInfoRenderer EMPTY_RENDERER = (stack, recipe, fontRenderer, guiOffsetX, guiOffsetY) -> {

    };

    public static final IRecipeInfoRenderer DEFAULT_RENDERER = new IRecipeInfoRenderer() {
        public void render(GuiGraphics graphics, IRecipe recipe, Font font, int guiOffsetX, int guiOffsetY) {
            if (recipe.getDuration() == 0 && recipe.getPower() == 0) return;
            String additional = recipe.getDuration() < 1200 ? "" : recipe.getDuration() < 36000 ? " (" + (recipe.getDuration() / 20.0f) + " secs)" : " (" + (recipe.getDuration() / 1200.0f) + " mins)";
            String power = "Duration: " + recipe.getDuration() + " ticks" + additional;
            String euT = "EU/t: " + recipe.getPower();
            String amps = "Amps: " + recipe.getAmps();
            String total = "Total: " + recipe.getPower() * recipe.getDuration() + " EU";
            Tier tier = Tier.getTier((recipe.getPower() / recipe.getAmps()));
            String formattedText = " (" + tier.getId().toUpperCase() + ")";
            renderString(graphics, power, font, 5, 0, guiOffsetX, guiOffsetY);
            renderString(graphics, euT, font, 5, 10, guiOffsetX, guiOffsetY);
            renderString(graphics, formattedText, font, 5 + stringWidth(euT, font), 10, Tier.EV.getRarityFormatting().getColor(), guiOffsetX, guiOffsetY);
            renderString(graphics, amps, font, 5, 20, guiOffsetX, guiOffsetY);
            renderString(graphics, total, font, 5, 30, guiOffsetX, guiOffsetY);
        }

        @Override
        public int getRows() {
            return 4;
        }
    };

    public static final IRecipeInfoRenderer FE_RENDERER = new IRecipeInfoRenderer() {
        public void render(GuiGraphics graphics, IRecipe recipe, Font font, int guiOffsetX, int guiOffsetY) {
            if (recipe.getDuration() == 0 && recipe.getPower() == 0) return;
            String additional = recipe.getDuration() < 1200 ? "" : recipe.getDuration() < 36000 ? " (" + (recipe.getDuration() / 20.0f) + " secs)" : " (" + (recipe.getDuration() / 1200.0f) + " mins)";
            String power = "Duration: " + recipe.getDuration() + " ticks" + additional;
            String euT = "FE/t: " + recipe.getPower();
            String total = "Total: " + recipe.getPower() * recipe.getDuration() + " FE";
            renderString(graphics, power, font, 5, 0, guiOffsetX, guiOffsetY);
            renderString(graphics, euT, font, 5, 10, guiOffsetX, guiOffsetY);
            renderString(graphics, total, font, 5, 20, guiOffsetX, guiOffsetY);
        }

        @Override
        public int getRows() {
            return 3;
        }
    };

    public static final IRecipeInfoRenderer FUEL_RENDERER = new IRecipeInfoRenderer() {
        @Override
        public void render(GuiGraphics graphics, IRecipe recipe, Font font, int guiOffsetX, int guiOffsetY) {
            String fuelPerMb = "EU/L: " + ((double) recipe.getPower() / (double) Objects.requireNonNull(recipe.getInputFluids()).get(0).getAmount());
            String fuelPerB = "Fluid Amount / tick: " + Objects.requireNonNull(recipe.getInputFluids()).get(0).getAmount();
            renderString(graphics, fuelPerMb, font, 5, 0, guiOffsetX, guiOffsetY);
            renderString(graphics, fuelPerB, font, 5, 10, guiOffsetX, guiOffsetY);
        }

        @Override
        public int getRows() {
            return 2;
        }
    };

}
