package org.gtreimagined.gtlib.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.block.IInfoProvider;
import org.gtreimagined.gtlib.blockentity.BlockEntityBase;
import org.gtreimagined.gtlib.data.AntimatterMaterialTypes;
import org.gtreimagined.gtlib.machine.MachineFlag;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ScannerItem extends ItemBasic<ScannerItem> {
    final boolean simple;

    public ScannerItem(String domain, String id, boolean simple) {
        this(domain, id, simple, "", new Properties().tab(Ref.TAB_ITEMS));

    }

    public ScannerItem(String domain, String id, boolean simple, String subDir, Properties properties) {
        super(domain, id, subDir, properties);
        this.simple = simple;
    }

    public ScannerItem(String domain, String id, boolean simple, Properties properties) {
        this(domain, id, simple, "", properties);
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Utils.literal(this.tooltip));
        if (Screen.hasShiftDown() && this == Data.DEBUG_SCANNER) {
            tooltip.add(Utils.literal("Blocks: " + AntimatterAPI.all(Block.class).size()));
            tooltip.add(Utils.literal("Machines: " + Machine.getTypes(MachineFlag.BASIC, MachineFlag.MULTI, MachineFlag.HATCH).size()));
            tooltip.add(Utils.literal("Pipes: " + AntimatterAPI.all(BlockPipe.class).size()));
            tooltip.add(Utils.literal("Storage: " + AntimatterAPI.all(BlockStorage.class).size()));
            tooltip.add(Utils.literal("Frame: " + AntimatterAPI.all(BlockFrame.class).size()));
            tooltip.add(Utils.literal("Ores: " + AntimatterAPI.all(BlockOre.class).size()));
            tooltip.add(Utils.literal("Stones: " + AntimatterAPI.all(BlockStone.class).size()));
            tooltip.add(Utils.literal("Data:"));
            tooltip.add(Utils.literal("Ore Materials: " + AntimatterMaterialTypes.ORE.all().size()));
            tooltip.add(Utils.literal("Small Ore Materials: " + AntimatterMaterialTypes.ORE_SMALL.all().size()));
        }
    }

    @NotNull
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.CONSUME;
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        boolean success = false;
        if (tile instanceof BlockEntityBase<?> base) {
            base.getInfo(simple).forEach(s -> context.getPlayer().sendMessage(Utils.literal(s), context.getPlayer().getUUID()));
            success = true;
        }
        if (state.getBlock() instanceof IInfoProvider dynamic && context.getPlayer() != null) {
            dynamic.getInfo(new ObjectArrayList<>(), context.getLevel(), state, context.getClickedPos(), simple).forEach(s -> {
                context.getPlayer().sendMessage(Utils.literal(s), context.getPlayer().getUUID());
            });
            success = true;
        }
        if (success) return InteractionResult.SUCCESS;
        return super.useOn(context);
    }
}
