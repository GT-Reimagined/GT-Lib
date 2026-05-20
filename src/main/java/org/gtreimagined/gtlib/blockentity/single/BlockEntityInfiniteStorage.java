package org.gtreimagined.gtlib.blockentity.single;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.LongValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.SyncHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.EnergyHandler;
import org.gtreimagined.gtlib.capability.machine.MachineEnergyHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;

import java.util.List;

import static org.gtreimagined.gtlib.gui.ICanSyncData.SyncDirection.SERVER_TO_CLIENT;

public class BlockEntityInfiniteStorage<T extends BlockEntityInfiniteStorage<T>> extends BlockEntityMachine<T> implements IInfoRenderer<BlockEntityInfiniteStorage.InfiniteStorageWidget> {

    public BlockEntityInfiniteStorage(Machine<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        energyHandler.set(() -> new MachineEnergyHandler<T>((T) this, Long.MAX_VALUE, Long.MAX_VALUE, 0, 32, 0, 4) {

            @Override
            public long extractEu(long voltage, boolean simulate) {
                return Math.min(voltage, getOutputVoltage());
            }

            @Override
            public boolean canOutput(Direction direction) {
                return tile.getFacing() == direction;
            }
        });
    }

    @Override
    protected boolean allowExplosionsInRain() {
        return false;
    }

    @Override
    public void onGuiEvent(IGuiEvent event, Player playerEntity) {
        if (event.getFactory() == GuiEvents.EXTRA_BUTTON) {
            final int[] data = ((GuiEvents.GuiEvent)event).data;
            energyHandler.ifPresent(h -> {
                int voltage = (int) h.getOutputVoltage();
                int amperage = (int) h.getOutputAmperage();
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

                if (voltage < 0){
                    voltage = 0;
                }
                if (amperage < 0){
                    amperage = 0;
                }

                h.setOutputVoltage(voltage);
                h.setOutputAmperage(amperage);
            });
        }
    }

    @Override
    public List<String> getInfo(boolean simple) {
        List<String> info = super.getInfo(simple);
        energyHandler.ifPresent(h -> {
            info.add("Voltage Out: " + h.getOutputVoltage());
            info.add("Amperage Out: " + h.getOutputAmperage());
        });
        return info;
    }

    @Override
    public int drawInfo(InfiniteStorageWidget widget, GuiGraphics graphics, Font font, int left, int top) {
        graphics.drawString(font,"Control Panel", left + 43, top + 21, 0xFAFAFF);
        graphics.drawString(font,"VOLT: " + widget.voltage, left + 43, top + 40, 0xFAFAFF);
        graphics.drawString(font,"TIER: " + Tier.getTier(widget.voltage < 0 ? -widget.voltage : widget.voltage).getId().toUpperCase(), left + 43, top + 48, 0xFAFAFF);
        graphics.drawString(font,"AMP: " + widget.amperage, left + 43, top + 56, 0xFAFAFF);
        graphics.drawString(font,"SUM: " + (long)(widget.amperage * widget.voltage), left + 43, top + 64, 0xFAFAFF);
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
    public void registerSyncHandlers(PanelSyncManager manager) {
        manager.syncValue("volts", new LongSyncValue(() -> this.energyHandler.map(EnergyHandler::getOutputVoltage).orElse(0L)));
        manager.syncValue("amps", new LongSyncValue(() -> this.energyHandler.map(EnergyHandler::getOutputAmperage).orElse(0L)));
    }

    @Override
    public void addWidgets(GuiInstance instance, IGuiElement parent) {
        super.addWidgets(instance, parent);
        instance.addWidget(InfiniteStorageWidget.build());
    }

    public static class InfiniteStorageWidget extends InfoRenderWidget<InfiniteStorageWidget> {
        public int amperage = 0;
        public long voltage = 0;
        protected InfiniteStorageWidget(GuiInstance gui, IGuiElement parent, IInfoRenderer<InfiniteStorageWidget> renderer) {
            super(gui, parent, renderer);
        }

        @Override
        public void init() {
            super.init();
            BlockEntityInfiniteStorage<?> m = (BlockEntityInfiniteStorage<?>) gui.handler;
            gui.syncInt(() -> Math.toIntExact(m.energyHandler.map(EnergyHandler::getOutputAmperage).orElse(0L)), i -> amperage = i, SERVER_TO_CLIENT);
            gui.syncLong(() -> m.energyHandler.map(EnergyHandler::getOutputVoltage).orElse(0L), i -> voltage = i, SERVER_TO_CLIENT);
        }

        public static WidgetSupplier build() {
            return builder((a,b) -> new InfiniteStorageWidget(a,b, (IInfoRenderer) a.handler));
        }
    }
}