package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import net.minecraft.world.item.ItemStack;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.CHIPPED_GEM;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.CRUSHED_ORE;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.DUST;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.FLAWED_GEM;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.FLAWLESS_GEM;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.GEM;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.IMPURE_DUST;

public class SmallOreWidget extends ParentWidget<SmallOreWidget> {
    public SmallOreWidget(SmallOre smallOre){
        this.size(170, 120);
        ParentWidget<?> itemOutputs = new ParentWidget<>();
        this.child(itemOutputs);
        List<List<ItemStack>> outputs = getOutputs(smallOre);
        if (!outputs.isEmpty()){
            itemOutputs.child(RecipeViewerSlotWidget.create(ItemStack.class)
                    .recipeSlotRole(RecipeSlotRole.OUTPUT).pos(0, 0)
                    .value(ItemStackList.of(outputs.get(0))).background(IDrawable.NONE));
            for (int i = 1; i < 9 && i < outputs.size(); i++) {
                int x = (i - 1) % 4;
                int y = (i - 1) / 4;
                itemOutputs.child(RecipeViewerSlotWidget.create(ItemStack.class)
                        .recipeSlotRole(RecipeSlotRole.OUTPUT).pos(41 + (x * 18), 54 + (y * 18))
                        .value(ItemStackList.of(outputs.get(i))).background(IDrawable.NONE));
            }

        }
        this.child(WidgetUtils.getDimensionsWidget(smallOre));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.vein_name", Utils.lowerUnderscoreToUpperSpaced(smallOre.getId())))
                .pos(0, 18));
        String minY = smallOre.minY() == Integer.MIN_VALUE ? "N/A" : String.valueOf(smallOre.minY());
        String maxY = smallOre.minY() == Integer.MAX_VALUE ? "N/A" : String.valueOf(smallOre.maxY());
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.height_range", minY, maxY))
                .pos(0, 28));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.amount_per_chunk", smallOre.amountPerChunk()))
                .pos(0, 38));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.drops"))
                .pos(0, 58));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.dimensions"))
                .pos(0, 88));
    }

    public static List<List<ItemStack>> getOutputs(SmallOre smallOre){
        List<List<ItemStack>> outputs = new ArrayList<>();
        outputs.add(GTAPI.all(StoneType.class).stream()
                .filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK)
                .map(s -> SMALL_ORE.get().get(smallOre.material(), s).asBlock())
                .map(ItemStack::new).toList());
        List<ItemStack> stoneDusts = new ArrayList<>();
        GTAPI.all(StoneType.class).stream().filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK).forEach(s -> {
            if (s.getMaterial().has(DUST)){
                stoneDusts.add(DUST.get(s.getMaterial(), 1));
            }
        });
        if (!stoneDusts.isEmpty()) {
            outputs.add(stoneDusts);
        }
        if (smallOre.material().has(EXQUISITE_GEM)){
            outputs.add(List.of(EXQUISITE_GEM.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(FLAWLESS_GEM)){
            outputs.add(List.of(FLAWLESS_GEM.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(GEM)){
            outputs.add(List.of(GEM.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(FLAWED_GEM)){
            outputs.add(List.of(FLAWED_GEM.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(CHIPPED_GEM)){
            outputs.add(List.of(CHIPPED_GEM.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(CRUSHED_ORE)){
            outputs.add(List.of(CRUSHED_ORE.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(IMPURE_DUST)){
            outputs.add(List.of(IMPURE_DUST.get(smallOre.material(), 1)));
        }
        if (smallOre.material().has(DUST) && !smallOre.material().has(CRUSHED_ORE) && !smallOre.material().has(IMPURE_DUST) && !smallOre.material().has(GEM)){
            outputs.add(List.of(DUST.get(smallOre.material(), 1)));
        }
        return outputs;
    }
}
