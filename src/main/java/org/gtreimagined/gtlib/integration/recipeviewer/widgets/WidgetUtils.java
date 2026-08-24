package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.layout.Flow;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.ArrayList;
import java.util.List;

public class WidgetUtils {

    public static List<ItemStack> getDimensionSlotItems(List<ResourceKey<Level>> dimensions){
        List<Block> markers = new ArrayList<>();
        List<ItemStack> markerItems = new ArrayList<>();
        for (ResourceLocation dimension : dimensions.stream().map(ResourceKey::location).toList()) {
            Block dimensionMarker = GTAPI.get(BlockDimensionMarker.class, dimension.getPath() + "_marker", Ref.ID);
            ItemStack world;
            if (dimensionMarker != null){
                if (markers.contains(dimensionMarker)) {
                    continue;
                }
                markers.add(dimensionMarker);
                world = new ItemStack(dimensionMarker);
            } else {
                world = new ItemStack(Items.BARRIER).setHoverName(Utils.literal(dimension.toString()));
            }
            markerItems.add(world);
        }
        return markerItems;
    }

    static IWidget getDimensionsWidget(List<ResourceKey<Level>> dimensions){

        List<ItemStack> stacks = WidgetUtils.getDimensionSlotItems(dimensions);
        IWidget dimensionGroup = new ParentWidget<>();
        if (!stacks.isEmpty()){
            dimensionGroup = new ListWidget<>().scrollDirection(GuiAxis.X).pos(0, 101).width(170).coverChildrenHeight(18).children(stacks, s -> {
                return RecipeViewerSlotWidget.create(ItemStack.class)
                        .recipeSlotRole(RecipeSlotRole.INPUT)
                        .value(s).background(IDrawable.NONE);
            });
        }
        return dimensionGroup;
    }
}
