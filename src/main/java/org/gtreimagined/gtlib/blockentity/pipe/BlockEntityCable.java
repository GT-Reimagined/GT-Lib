package org.gtreimagined.gtlib.blockentity.pipe;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gtreimagined.gtlib.capability.Dispatch;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.widget.InfoRenderWidget;
import org.gtreimagined.gtlib.integration.xei.renderer.IInfoRenderer;
import org.gtreimagined.gtlib.pipe.BlockCable;
import org.gtreimagined.gtlib.pipe.types.Cable;
import org.gtreimagined.gtlib.pipe.types.PipeType;
import net.minecraft.client.gui.Font;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import org.gtreimagined.tesseract.api.capability.TesseractEUCapability;
import org.gtreimagined.tesseract.api.eu.EUHolder;
import org.gtreimagined.tesseract.api.eu.IEUCable;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.EUGrid;
import org.gtreimagined.tesseract.api.eu.EUNetwork;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;

import java.util.Collection;

public class BlockEntityCable<T extends PipeType<T>> extends BlockEntityPipe<T> implements IEUCable, Dispatch.Sided<IEnergyHandler>, IInfoRenderer<InfoRenderWidget.TesseractGTWidget> {

    private long holder;
    private EUNetwork network;

    public BlockEntityCable(T type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        pipeCapHolder.set(() -> this);
    }

    @Override
    public void onLoad() {
        this.holder = EUHolder.create(this, 0);
        super.onLoad();
    }

    @Override
    protected void register() {
        EUGrid.INSTANCE.addElement(this);
    }

    @Override
    protected boolean deregister() {
        EUGrid.INSTANCE.removeElement(this);
        return true;
    }

    @Override
    public Class<?> getCapClass() {
        return IEnergyHandler.class;
    }

    @Override
    public long getVoltage() {
        return ((Cable<?>) getPipeType()).getTier().getVoltage();
    }

    @Override
    public boolean insulated() {
        return ((BlockCable<?>) this.getBlockState().getBlock()).insulated;
    }

    @Override
    public long getHolder() {
        return holder;
    }

    @Override
    public void setHolder(long holder) {
        this.holder = holder;
    }

    @Override
    public double getLoss() {
        return ((Cable<?>) getPipeType()).getLoss();
    }

    @Override
    public int getAmps() {
        return ((Cable<?>) getPipeType()).getAmps(getPipeSize());
    }

    @Override
    public boolean connects(Direction direction) {
        return canConnect(direction.get3DDataValue());
    }

    @Override
    public boolean validate(Direction dir) {
        if (!super.validate(dir)) return false;
        BlockEntity tile = this.getCachedBlockEntity(dir);
        if (tile == null) return false;
        return tile.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY, dir.getOpposite()).isPresent();
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    protected void serverTick(Level level, BlockPos pos, BlockState state) {
        super.serverTick(level, pos, state);
        //this.setHolder(EUHolder.create(this, 0));
    }

    @Override
    public LazyOptional<IEnergyHandler> forSide(Direction side) {
        return LazyOptional.of(() -> new TesseractEUCapability<>(this, side, !isConnector(), (stack, dir, input, simulate) ->
        this.coverHandler.map(t -> t.onTransfer(stack, dir, input, simulate)).orElse(false)));
    }

    @Override
    public LazyOptional<IEnergyHandler> forNullSide() {
        return forSide(null);
    }

    @Override
    public void addWidgets(GuiInstance instance, IGuiElement parent) {
        super.addWidgets(instance, parent);
        instance.addWidget(InfoRenderWidget.TesseractGTWidget.build().setPos(10, 10));
    }

    @Override
    public int drawInfo(InfoRenderWidget.TesseractGTWidget instance, PoseStack stack, Font renderer, int left, int top) {
        renderer.draw(stack, "Amp average: " + instance.ampAverage, left, top, 0xFAFAFF);
       // renderer.draw(stack, "Cable average: " + instance.cableAverage, left, top + 8, 0xFAFAFF);
        renderer.draw(stack, "Average extracted: " + ((double) instance.voltAverage) / 20, left, top + 16, 0xFAFAFF);
        renderer.draw(stack, "Average inserted: " + ((double) (instance.voltAverage - instance.loss)) / 20, left, top + 24, 0xFAFAFF);
        renderer.draw(stack, "Loss average: " + (double) instance.loss / 20, left, top + 32, 0xFAFAFF);
        return 40;
    }

    @Override
    public void getNeighbours(Collection<IEUCable> neighbours) {
        for (Direction dir : Direction.values()) {
            BlockEntity pipe = getCachedBlockEntity(dir);
            if (pipe instanceof IEUCable cable) {
                if (cable.connects(dir.getOpposite()) && connects(dir)){
                    neighbours.add(cable);
                }
            }
        }
    }

    @Override
    public EUNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(EUNetwork network) {
        this.network = network;
    }
}
