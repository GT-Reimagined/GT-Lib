package org.gtreimagined.gtlib.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.capability.fluid.FluidTanks;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.cover.CoverOutput;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.machine.types.TankMachine;
import org.gtreimagined.gtlib.util.FluidUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

import static org.gtreimagined.gtlib.machine.MachineFlag.FLUID;
import static org.gtreimagined.gtlib.machine.MachineFlag.ITEM;

public class BlockEntityTank<T extends BlockEntityMachine<T>> extends BlockEntityMachine<T> implements IInfoRenderer<TankMachine.TankRenderWidget> {

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
    public int drawInfo(TankMachine.TankRenderWidget instance, GuiGraphics graphics, Font font, int left, int top) {
        left = left + 55;
        top = top + 24;
        if (instance.stack.isEmpty()){
            graphics.drawString(font, "Empty", left, top, 0xFAFAFF);
            return 8;
        }
        graphics.drawString(font, FluidUtils.getFluidDisplayName(instance.stack).getString(), left, top, 0xFAFAFF);
        String fluidAmount = String.valueOf(instance.stack.getAmount());
        graphics.drawString(font, fluidAmount + " mb", left, top + 8, 0xFAFAFF);
        return 16;
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
