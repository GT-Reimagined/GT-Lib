package org.gtreimagined.gtlib.mixin;

import net.minecraft.world.level.storage.loot.LootParams;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Collections;
import java.util.List;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> list = super.getDrops(state, builder);
        ItemStack stack = builder.getOptionalParameter(LootContextParams.TOOL);
        if (stack != null && !stack.isEmpty() && stack.getItem() instanceof IGTTool) {
            IGTTool tool = (IGTTool) stack.getItem();
            if (tool.getGTToolType() == GTTools.BRANCH_CUTTER) {
                ResourceLocation resourcelocation = this.getLootTable();
                if (resourcelocation == BuiltInLootTables.EMPTY) {
                    return Collections.emptyList();
                }
                ServerLevel serverworld = builder.getLevel();
                LootTable loottable = serverworld.getServer().getLootData().getLootTable(resourcelocation);
                ItemStack sapling = ItemStack.EMPTY;
                ResourceLocation location = new ResourceLocation(RegistryUtils.getIdFromBlock(this).toString().replace("leaves", "sapling"));
                if (RegistryUtils.blockExists(location)) {
                    sapling = new ItemStack(RegistryUtils.getBlockFromId(location));
                }
                /*for (ItemStack stack1 : list){
                    if (stack1.getItem() instanceof BlockItem && ((BlockItem) stack1.getItem()).getBlock() instanceof SaplingBlock){
                        sapling = stack1.copy();
                        break;
                    }
                }*/
                if (!sapling.isEmpty()) {
                    list.clear();
                    list.add(sapling);
                }
            }
        }
        return list;
    }
}
