package org.gtreimagined.gtlib.blockentity.multi;

import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import net.minecraft.world.level.Level.ExplosionInteraction;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.IComponentHandler;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.capability.machine.MultiMachineEnergyHandler;
import org.gtreimagined.gtlib.capability.machine.MultiMachineFluidHandler;
import org.gtreimagined.gtlib.capability.machine.MultiMachineItemHandler;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.mui.IInfoRenderer;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.mui.widgets.GTInfoRenderWidget;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.util.int2;
import org.gtreimagined.tesseract.api.hu.IHeatHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.gtreimagined.gtlib.machine.MachineFlag.*;

public class BlockEntityMultiMachine<T extends BlockEntityMultiMachine<T>> extends BlockEntityBasicMultiMachine<T> implements IInfoRenderer {

    protected long EUt;
    protected List<IHeatHandler> heatHandlers = Collections.emptyList();

    //TODO: Sync multiblock state(if it is formed), otherwise the textures might bug out. Not a big deal.
    public BlockEntityMultiMachine(Machine<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        if (type.has(ITEM) || type.has(CELL)) {
            itemHandler.set(() -> new MultiMachineItemHandler<>((T) this));
        }
        if (type.has(EU)) {
            energyHandler.set(() -> new MultiMachineEnergyHandler<>((T) this));
        }
        if (type.has(FLUID)) {
            fluidHandler.set(() -> new MultiMachineFluidHandler<>((T) this));
        }
    }

    public List<IHeatHandler> getHeatHandlers() {
        return heatHandlers;
    }

    @Override
    public Tier getPowerLevel() {
        return energyHandler.map(t -> ((MultiMachineEnergyHandler<T>) t).getAccumulatedPower()).orElse(super.getPowerLevel());
    }

    @Override
    public void afterStructureFormed() {
        this.components.forEach((k, v) -> v.forEach(c -> {
            c.onStructureFormed(this);
        }));
        if (isClientSide()) return;
        //Handlers.
        this.itemHandler.ifPresent(handle -> {
            ((MultiMachineItemHandler<T>) handle).onStructureBuild();
        });
        this.energyHandler.ifPresent(handle -> {
            ((MultiMachineEnergyHandler<T>) handle).onStructureBuild();
        });
        this.fluidHandler.ifPresent(handle -> {
            ((MultiMachineFluidHandler<T>) handle).onStructureBuild();
        });
        var heats = this.components.get("components");
        if (heats != null) {
            this.heatHandlers = heats.stream().map(IComponentHandler::getHeatHandler).filter(Optional::isPresent).map(Optional::get).toList();
        } else {
            this.heatHandlers = Collections.emptyList();
        }
    }

    @Override
    public void onStructureInvalidated() {
        this.components.forEach((k, v) -> v.forEach(c -> c.onStructureInvalidated(this)));
        if (isClientSide()) return;
        this.itemHandler.ifPresent(handle -> ((MultiMachineItemHandler<T>) handle).invalidate());
        this.energyHandler.ifPresent(handle -> ((MultiMachineEnergyHandler<T>) handle).invalidate());
        this.fluidHandler.ifPresent(handle -> ((MultiMachineFluidHandler<T>) handle).invalidate());
        this.heatHandlers = Collections.emptyList();
    }

    @Override
    public void onGuiEvent(IGuiEvent event, Player playerEntity) {
        super.onGuiEvent(event, playerEntity);
        /*if (event == GuiEvent.MULTI_ACTIVATE) {
            checkStructure();
            recipeHandler.ifPresent(MachineRecipeHandler::checkRecipe);
        }*/
    }

    @Override
    public long getMaxInputVoltage() {
        return getPowerLevel().getVoltage();
        //List<IComponentHandler> hatches = getComponentsByHandlerId("energy");
        //return !hatches.isEmpty() ? hatches.stream().mapToLong(t -> t.getEnergyHandler().map(EnergyHandler::getInputVoltage).orElse(0L)).sum() : Ref.V[0];
    }

    @Override
    public void drawInfo(GTInfoRenderWidget widget, ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        widget.drawText(context, widgetTheme, 0, 0, this.getDisplayName(), 0xFAFAFF);
        if (getMachineState() != MachineState.ACTIVE) {
            widget.drawText(context, widgetTheme, 0, 8, Utils.literal("Inactive."), 0xFAFAFF);
        } else {
            widget.drawText(context, widgetTheme, 0, 8, Utils.literal("Progress: " +
                    widget.getSyncedValue("progress", Integer.class).orElse(0) + "/" +
                    widget.getSyncedValue("maxProgress", Integer.class)), 0xFAFAFF);
            widget.drawText(context, widgetTheme, 0, 16, Utils.literal("Overclock: " +
                    widget.getSyncedValue("overclock", Integer.class)), 0xFAFAFF);
            widget.drawText(context, widgetTheme, 0, 24, Utils.literal("EU/t: " +
                    widget.getSyncedValue("eut", Long.class)), 0xFAFAFF);
        }
    }

    @Override
    public void registerSyncHandlers(PanelSyncManager manager) {
        manager.syncValue("progress", new IntSyncValue(() -> recipeHandler.map(MachineRecipeHandler::getCurrentProgress).orElse(0)));
        manager.syncValue("maxProgress", new IntSyncValue(() -> recipeHandler.map(MachineRecipeHandler::getMaxProgress).orElse(0)));
        manager.syncValue("overclock", new IntSyncValue(() -> recipeHandler.map(MachineRecipeHandler::getOverclock).orElse(0)));
        manager.syncValue("eut", new LongSyncValue(() -> recipeHandler.map(MachineRecipeHandler::getPower).orElse(0L)));
    }

    @Override
    public int2 getSize() {
        return new int2(90, 90);
    }

    @Override
    public int2 getPos() {
        return new int2(3, 3);
    }

    public void explodeMultiblock() {
        this.components.forEach((s, l) -> {
            l.forEach(c -> {
                if (c.getTile() instanceof BlockEntityMachine<?> machine){
                    Utils.createExplosion(this.level, machine.getBlockPos(), 6.0F, ExplosionInteraction.BLOCK);
                }
            });
        });
        Utils.createExplosion(this.level, this.getBlockPos(), 6.0F, ExplosionInteraction.BLOCK);
    }
}
