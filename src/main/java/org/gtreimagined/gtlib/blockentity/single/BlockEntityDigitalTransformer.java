package org.gtreimagined.gtlib.blockentity.single;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.capability.EnergyHandler;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.util.int2;

public class BlockEntityDigitalTransformer<T extends BlockEntityDigitalTransformer<T>> extends BlockEntityTransformer<T> implements IInfoRenderer {

    public BlockEntityDigitalTransformer(Machine<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, 0, (v) -> (8192L + v * 64L));
    }

    @Override
    public void onGuiEvent(IGuiEvent event, Player playerEntity) {
        if (event.getFactory() == GuiEvents.EXTRA_BUTTON) {
            energyHandler.ifPresent(h -> {
                GuiEvents.GuiEvent ev = (GuiEvents.GuiEvent) event;
                int[] data = ev.data;
                boolean shiftHold = data[0] != 0;
                switch (data[1]) {
                    case 0:
                        voltage /= shiftHold ? 512 : 64;
                        break;
                    case 1:
                        voltage -= shiftHold ? 512 : 64;
                        break;
                    case 2:
                        amperage /= shiftHold ? 512 : 64;
                        break;
                    case 3:
                        amperage -= shiftHold ? 512 : 64;
                        break;
                    case 4:
                        voltage /= shiftHold ? 16 : 2;
                        break;
                    case 5:
                        voltage -= shiftHold ? 16 : 1;
                        break;
                    case 6:
                        amperage /= shiftHold ? 16 : 2;
                        break;
                    case 7:
                        amperage -= shiftHold ? 16 : 1;
                        break;
                    case 8:
                        voltage += shiftHold ? 512 : 64;
                        break;
                    case 9:
                        voltage *= shiftHold ? 512 : 64;
                        break;
                    case 10:
                        amperage += shiftHold ? 512 : 64;
                        break;
                    case 11:
                        amperage *= shiftHold ? 512 : 64;
                        break;
                    case 12:
                        voltage += shiftHold ? 16 : 1;
                        break;
                    case 13:
                        voltage *= shiftHold ? 16 : 2;
                        break;
                    case 14:
                        amperage += shiftHold ? 16 : 1;
                        break;
                    case 15:
                        amperage *= shiftHold ? 16 : 2;
                        break;
                }
                amperage = Math.max(amperage, 0);
                voltage = Math.max(voltage, 0);
                setMachineState((long) (amperage * voltage) >= 0L ? getDefaultMachineState() : MachineState.DISABLED);

                if (isDefaultMachineState()) {
                    h.setInputVoltage(getMachineTier().getVoltage());
                    h.setOutputVoltage(voltage);
                    h.setOutputAmperage(amperage);
                    h.setInputAmperage(1);
                } else {
                    h.setInputVoltage(voltage);
                    h.setOutputVoltage(getMachineTier().getVoltage());
                    h.setOutputAmperage(1);
                    h.setInputAmperage(amperage);
                }
            });
        }
    }

    @Override
    public void drawInfo(GTInfoRenderWidget widget, ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        widget.drawText(context, widgetTheme, 0, 0, Component.literal("Control Panel"), 0xFAFAFF);
        long voltage = widget.getSyncedValue("volts", Long.class).orElse(32L);
        long amps = widget.getSyncedValue("amps", Long.class).orElse(4L);
        widget.drawText(context, widgetTheme, 0, 19, Component.literal("VOLT: " + voltage), 0xFAFAFF);
        widget.drawText(context, widgetTheme, 0, 27, Component.literal("TIER: " + Tier.getTier(voltage < 0 ? -voltage : voltage).getId().toUpperCase()), 0xFAFAFF);
        widget.drawText(context, widgetTheme, 0, 35, Component.literal("AMP: " + amps), 0xFAFAFF);
        widget.drawText(context, widgetTheme, 0, 43, Component.literal("SUM: " + (voltage * amps)), 0xFAFAFF);
    }

    @Override
    public void registerSyncHandlers(PanelSyncManager manager) {
        manager.syncValue("volts", new LongSyncValue(() -> this.energyHandler.map(EnergyHandler::getOutputVoltage).orElse(0L)));
        manager.syncValue("amps", new LongSyncValue(() -> this.energyHandler.map(EnergyHandler::getOutputAmperage).orElse(0L)));
    }

    @Override
    public int2 getPos() {
        return new int2(43, 21);
    }

    @Override
    public int2 getSize() {
        return new int2(90, 53);
    }
}
