package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.TextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;

public class StoneVeinWidget extends ParentWidget<StoneVeinWidget> {
    public StoneVeinWidget(StoneVein stoneVein){
        this.size(170, 120);
        ParentWidget<?> itemOutputs = new ParentWidget<>();
        ResourceLocation id = new ResourceLocation(stoneVein.stoneLayer().getDomain(), stoneVein.stoneLayer().getId() + "_with_" + stoneVein.ore().material().getId());
        this.child(itemOutputs);
        ItemStack ore = new ItemStack(ORE.get().get(stoneVein.ore().material(), VanillaStoneTypes.STONE).asItem());
        ItemStack stone = new ItemStack(stoneVein.stoneLayer().block());
        itemOutputs.child(RecipeViewerSlotWidget.create(ItemStack.class)
                .recipeSlotRole(RecipeSlotRole.OUTPUT).pos(0, 0)
                .value(ore).background(IDrawable.NONE));
        itemOutputs.child(RecipeViewerSlotWidget.create(ItemStack.class)
                .recipeSlotRole(RecipeSlotRole.OUTPUT).pos(18, 0)
                .value(stone).background(IDrawable.NONE));
        this.child(WidgetUtils.getDimensionsWidget(stoneVein.stoneLayer().dimensions()));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.stone_layer_name", Utils.lowerUnderscoreToUpperSpaced(stoneVein.stoneLayer().getId())))
                .pos(0, 18));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.stone", Utils.translatable(stoneVein.stoneLayer().block().getDescriptionId())))
                .pos(0, 28));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.ore", stoneVein.ore().material().getDisplayName()))
                .pos(0, 38));
        DecimalFormat format = new DecimalFormat("###.####");
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.stone_layer_chance", format.format(((double) stoneVein.stoneLayer().weight() / stoneVein.totalWeight()) * 100) + "%"))
                .pos(0, 58));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.height_range", stoneVein.ore().minY(), stoneVein.ore().maxY()))
                .pos(0, 68));
        this.child(new TextWidget<>(Utils.translatable("recipe_info.gtlib.worldgen.stone_layer.ore_chance", format.format(((double)stoneVein.ore().chance() / Ref.U) * 100) + "%"))
                .pos(0, 78));
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
