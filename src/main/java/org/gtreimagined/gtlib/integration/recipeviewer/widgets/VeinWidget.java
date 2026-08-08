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
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.vein.Vein;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE;

public class VeinWidget extends ParentWidget<VeinWidget> {
    public VeinWidget(Vein vein){
        this.size(170, 120);
        ParentWidget<?> outputs = new ParentWidget<>();
        this.child(outputs);
        this.child(WidgetUtils.getDimensionsWidget(vein.dimensions()));
        List<List<ItemStack>> outputList = getStacks(vein);
        for (int i = 0; i < outputList.size(); i++) {
            outputs.child(RecipeViewerSlotWidget.create(ItemStack.class)
                    .background(IDrawable.NONE).recipeSlotRole(RecipeSlotRole.OUTPUT)
                    .pos(i * 18, 0)
                    .value(ItemStackList.of(outputList.get(i))));
        }
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.vein_name", Utils.lowerUnderscoreToUpperSpaced(vein.getId())))
                .pos(0, 18));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.primary", vein.primary().getDisplayName()))
                .pos(0, 38));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.secondary", vein.secondary().getDisplayName()))
                .pos(0, 48));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.between", vein.between().getDisplayName()))
                .pos(0, 58));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.sporadic", vein.sporadic().getDisplayName()))
                .pos(0, 68));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.height_range", vein.minY(), vein.maxY()))
                .pos(0, 78));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.weight", vein.weight()))
                .pos(100, 78));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.dimensions"))
                .pos(0, 88));

    }

    public static List<List<ItemStack>> getStacks(Vein vein){
        List<List<ItemStack>> list = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Material m = switch (i){
                case 0 -> vein.primary();
                case 1 -> vein.secondary();
                case 2 -> vein.between();
                default -> vein.sporadic();
            };
            list.add(GTAPI.all(StoneType.class).stream()
                            .filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK)
                            .map(s -> ORE.get().get(m, s).asBlock())
                            .map(ItemStack::new).toList());
        }
        return list;
    }
}
