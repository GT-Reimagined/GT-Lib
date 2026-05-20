package org.gtreimagined.gtlib.blockentity.single;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;

import static org.gtreimagined.gtlib.gui.ICanSyncData.SyncDirection.SERVER_TO_CLIENT;

public class BlockEntityDigitalTransformer<T extends BlockEntityDigitalTransformer<T>> extends BlockEntityTransformer<T> implements IInfoRenderer<BlockEntityDigitalTransformer.DigitalTransformerWidget> {

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
    public int drawInfo(DigitalTransformerWidget widget, GuiGraphics graphics, Font font, int left, int top) {
        graphics.drawString(font, "Control Panel", left + 43, top + 21, 0xFAFAFF);
        graphics.drawString(font, "VOLT: " + widget.voltage, left + 43, top + 40, 0xFAFAFF);
        graphics.drawString(font, "TIER: " + Tier.getTier(widget.voltage < 0 ? -widget.voltage : widget.voltage).getId().toUpperCase(), left + 43, top + 48, 0xFAFAFF);
        graphics.drawString(font, "AMP: " + widget.amperage, left + 43, top + 56, 0xFAFAFF);
        graphics.drawString(font, "SUM: " + (widget.amperage * widget.voltage), left + 43, top + 64, 0xFAFAFF);
        return 72;
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
    public void addWidgets(GuiInstance instance, IGuiElement parent) {
        super.addWidgets(instance, parent);
        instance.addWidget(DigitalTransformerWidget.build());
    }

    public static class DigitalTransformerWidget extends InfoRenderWidget<DigitalTransformerWidget> {
        public int amperage = 0;
        public long voltage = 0;

        protected DigitalTransformerWidget(GuiInstance gui, IGuiElement parent, IInfoRenderer<DigitalTransformerWidget> renderer) {
            super(gui, parent, renderer);
        }

        @Override
        public void init() {
            super.init();
            BlockEntityDigitalTransformer<?> m = (BlockEntityDigitalTransformer<?>) gui.handler;
            gui.syncInt(() -> m.amperage, i -> amperage = i, SERVER_TO_CLIENT);
            gui.syncLong(() -> m.voltage, i -> voltage = i, SERVER_TO_CLIENT);
        }

        public static WidgetSupplier build() {
            return builder((a, b) -> new DigitalTransformerWidget(a, b, (IInfoRenderer) a.handler));
        }
    }
}
