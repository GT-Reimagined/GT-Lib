package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.dynamic.DynamicHandler;
import brachy.modularui.widgets.layout.Flow;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.integration.recipeviewer.MultiMachineInfoPage;
import org.gtreimagined.gtlib.structure.BlockInfo;
import org.gtreimagined.gtlib.structure.Pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiblockPatternWidget extends ParentWidget<MultiblockPatternWidget> {
    private final DynamicHandler partsHandler = new DynamicHandler();
    int currentPattern = 0;
    int currentLevel = -1;
    public MultiblockPatternWidget(MultiMachineInfoPage page){
        if (!GTAPI.isClientThread()) return;
        this.size(176, 150);
        List<List<ItemStack>> parts = new ArrayList<>();
        page.patterns().forEach(p -> {
            Map<Block, Integer> map = new Object2IntArrayMap<>();
            for (int i = 0; i < p.getBlockInfos().length; i++) {
                BlockInfo[][] info2 = p.getBlockInfos()[i];
                for (BlockInfo[] info3 : info2) {
                    for (BlockInfo info : info3) {
                        int amount = map.computeIfAbsent(info.getBlockState().getBlock(), b -> 0);
                        map.put(info.getBlockState().getBlock(), ++amount);
                    }
                }
            }
            List<ItemStack> stacks = new ArrayList<>();
            map.forEach((b, i) -> stacks.add(new ItemStack(b, i)));
            parts.add(stacks);
        });

        this.partsHandler.widgetProvider(() -> Flow.row()
                .name("wrapping_parts_col")
                // NOTE wrapped flows require a fixed size in their axis, relative/coverChildren does not work
                .wrap()
                .coverChildrenHeight(20)
                .width(176)
                .children(parts.get(currentPattern), e -> {
                    return RecipeViewerSlotWidget.create(ItemStack.class)
                            .recipeSlotRole(RecipeSlotRole.OUTPUT)
                            .value(e)
                            .background(IDrawable.EMPTY)
                            .size(16)
                            //.tooltip(r -> r.addFromItem(stack))
                            .margin(1);
                }));
    }
}
