package org.gtreimagined.gtlib.item.interaction;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.item.ItemFluidCell;
import org.gtreimagined.gtlib.material.MaterialItem;
import net.minecraft.core.cauldron.CauldronInteraction;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;

/**
 * All gt lib cauldron interactions.
 */
public class CauldronInteractions {

    public static void init() {
        IMPURE_DUST.all().stream().filter(t -> t.has(DUST)).map(t -> IMPURE_DUST.get(t)).forEach(stack -> CauldronInteraction.WATER.put(stack, MaterialItem::interactWithCauldron));
        PURE_DUST.all().stream().filter(t -> t.has(DUST)).map(t -> PURE_DUST.get(t)).forEach(stack -> CauldronInteraction.WATER.put(stack, MaterialItem::interactWithCauldron));
        CRUSHED_ORE.all().stream().filter(t -> t.has(PURIFIED_ORE) && t.has(DUST)).map(t -> CRUSHED_ORE.get(t)).forEach(stack -> CauldronInteraction.WATER.put(stack, MaterialItem::interactWithCauldron));
        GTAPI.all(ItemFluidCell.class, t -> CauldronInteraction.WATER.put(t, ItemFluidCell::interactWithCauldron));
    }
}
