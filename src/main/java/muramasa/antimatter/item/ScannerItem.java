package muramasa.antimatter.item;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Data;
import muramasa.antimatter.Ref;
import muramasa.antimatter.block.BlockFrame;
import muramasa.antimatter.block.BlockStone;
import muramasa.antimatter.block.BlockStorage;
import muramasa.antimatter.block.IInfoProvider;
import muramasa.antimatter.blockentity.BlockEntityBase;
import muramasa.antimatter.data.AntimatterMaterialTypes;
import muramasa.antimatter.machine.MachineFlag;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.ore.BlockOre;
import muramasa.antimatter.pipe.BlockPipe;
import muramasa.antimatter.util.Utils;
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
