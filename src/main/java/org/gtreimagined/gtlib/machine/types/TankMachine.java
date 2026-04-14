package org.gtreimagined.gtlib.machine.types;

import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.blockentity.BlockEntityTank;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.ICanSyncData;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import org.gtreimagined.gtlib.gui.widget.WidgetSupplier;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.Function;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class TankMachine extends Machine<TankMachine> {
    final Function<Tier, Integer> capacityPerTier;

    public TankMachine(String domain, String name) {
        this(domain, name, t -> 8000 * (1 + t.getIntegerId()));
    }

    public TankMachine(String domain, String name, Function<Tier, Integer> capacityPerTier) {
        super(domain, name);
        this.capacityPerTier = capacityPerTier;
        setTile(BlockEntityTank::new);
        addTooltipInfo((machine, stack, world, tooltip, flag) -> {
            tooltip.add(Utils.translatable("machine.tank.capacity", capacityPerTier.apply(machine.getTier())));
        });
        addFlags(ITEM, FLUID, COVERABLE);
        setAllowsFrontCovers();
        setAllowsFrontIO();
    }

    public Function<Tier, Integer> getCapacityPerTier() {
        return capacityPerTier;
    }

    @Override
    protected void setupGui() {
        super.setupGui();
        addGuiCallback(t -> t.addWidget(TankRenderWidget.build().onlyIf(h -> h.handler instanceof BlockEntityTank)));
    }

    public static class TankRenderWidget extends InfoRenderWidget<TankRenderWidget> {

        public FluidStack stack = FluidStack.EMPTY;

        protected TankRenderWidget(GuiInstance gui, IGuiElement parent, IInfoRenderer<TankRenderWidget> renderer) {
            super(gui, parent, renderer);
        }

        @Override
        public void init() {
            super.init();
            BlockEntityTank<?> tank = (BlockEntityTank<?>) gui.handler;
            gui.syncFluidStack(() -> tank.fluidHandler.map(t -> t.getFluidInTank(0)).orElse(FluidStack.EMPTY), f -> this.stack = f, ICanSyncData.SyncDirection.SERVER_TO_CLIENT);
        }

        public static WidgetSupplier build() {
            return builder((a, b) -> new TankRenderWidget(a, b, (IInfoRenderer<TankRenderWidget>) a.handler));
        }
    }
}
