package org.gtreimagined.gtlib.integration.recipeviewer;

import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayer;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerOre;

public record StoneVein(StoneLayer stoneLayer, StoneLayerOre ore, int totalWeight) {
}
