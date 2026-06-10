package org.gtreimagined.gtlib.blockentity;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.mui.IInfoRenderer;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.machine.types.TankMachine;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.util.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.util.int2;
import org.jetbrains.annotations.Nullable;

import static org.gtreimagined.gtlib.machine.MachineFlag.FLUID;
import static org.gtreimagined.gtlib.machine.MachineFlag.ITEM;

public class BlockEntityTank<T extends BlockEntityMachine<T>> extends BlockEntityMachine<T> implements IInfoRenderer {

    public BlockEntityTank(Machine<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        int capacity = type instanceof TankMachine tankMachine ? tankMachine.getCapacityPerTier().apply(tier) : 8000 * (1 + tier.getIntegerId());
        fluidHandler.set(() -> new MachineFluidHandler<T>((T) this, capacity) {
            @Nullable
            @Override
            public FluidTanks getOutputTanks() {
                return super.getInputTanks();
            }

            @Override
            protected FluidTank getTank(int tank) {
                return getInputTanks().getTank(tank);
            }

            @Override
            public FluidTanks getTanks(int tank) {
                return getInputTanks();
            }
        });
    }

    @Override
    public void drawInfo(GTInfoRenderWidget widget, ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        FluidStack stack = widget.getSyncedValue("fluid", FluidStack.class).orElse(FluidStack.EMPTY);
        if (stack.isEmpty()){
            widget.drawText(context, widgetTheme, 0, 0, Utils.literal("Empty"), 0xFAFAFF);
            return;
        }
        widget.drawText(context, widgetTheme, 0, 0, FluidUtils.getFluidDisplayName(stack), 0xFAFAFF);
        String fluidAmount = String.valueOf(stack.getAmount());
        widget.drawText(context, widgetTheme, 0, 8, Utils.literal(fluidAmount + "L"), 0xFAFAFF);
    }

    @Override
    public void registerSyncHandlers(PanelSyncManager manager) {
        manager.syncValue("fluid", GenericSyncValue.forFluid(() -> this.fluidHandler.map(f -> f.getFluidInTank(0)).orElse(FluidStack.EMPTY), null));
    }

    @Override
    public int2 getPos() {
        return new int2(56, 24);
    }

    @Override
    public int2 getSize() {
        return new int2(90, 16);
    }

    private String intToSuperScript(int i){
        String intString = String.valueOf(i);
        StringBuilder builder = new StringBuilder();
        for (char c : intString.toCharArray()) {
            builder.append(charToSuperScript(c));
        }
        return builder.toString();
    }

    private String charToSuperScript(char c){
        return switch (c){
            case '0' -> "⁰";
            case '1' -> "¹";
            case '2' -> "²";
            case '3' -> "³";
            case '4' -> "⁴";
            case '5' -> "⁵";
            case '6' -> "⁶";
            case '7' -> "⁷";
            case '8' -> "⁸";
            case '9' -> "⁹";
            default -> String.valueOf(c);
        };
    }

    @Override
    public void onFirstTickServer(Level level, BlockPos pos, BlockState state) {
        super.onFirstTickServer(level, pos, state);
        setAutoOutput();
    }

    protected void setAutoOutput(){
        coverHandler.ifPresent(t -> {
            ICover cover = t.getOutputCover();
            if (!(cover instanceof CoverOutput output))
                return;
            output.setEjects(has(FLUID), has(ITEM));
        });
    }
}
