package muramasa.antimatter.blockentity.multi;

import lombok.Getter;
import lombok.Setter;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.AntimatterConfig;
import muramasa.antimatter.block.BlockBasic;
import muramasa.antimatter.blockentity.BlockEntityMachine;
import muramasa.antimatter.capability.ComponentHandler;
import muramasa.antimatter.capability.machine.HatchComponentHandler;
import muramasa.antimatter.capability.machine.MachineCoverHandler;
import muramasa.antimatter.capability.machine.MachineEnergyHandler;
import muramasa.antimatter.capability.machine.MachineFluidHandler;
import muramasa.antimatter.cover.CoverDynamo;
import muramasa.antimatter.cover.CoverEnergy;
import muramasa.antimatter.cover.CoverOutput;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.machine.event.IMachineEvent;
import muramasa.antimatter.machine.event.MachineEvent;
import muramasa.antimatter.machine.types.HatchMachine;
import muramasa.antimatter.registration.ITextureProvider;
import muramasa.antimatter.structure.IComponent;
import muramasa.antimatter.texture.Texture;
import muramasa.antimatter.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;

import static muramasa.antimatter.machine.MachineFlag.*;

public class BlockEntityHatch<T extends BlockEntityHatch<T>> extends BlockEntityMachine<T> implements IComponent {

    public final Optional<HatchComponentHandler<T>> componentHandler;
    public final HatchMachine hatchMachine;
    @Getter
    private BlockBasic textureBlock = null;

    public BlockEntityHatch(HatchMachine type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.hatchMachine = type;
        componentHandler = Optional
                .of(new HatchComponentHandler<>((T)this));
        if (type.has(FLUID)) {
            fluidHandler.set(() -> new MachineFluidHandler<>((T) this,8000 * (getMachineTier().getIntegerId() + 1)));
        }
        if (type.has(EU)) {
            energyHandler.set(() -> new MachineEnergyHandler<T>((T) this, 0, getMachineTier().getVoltage() * 66L,
                    type.getOutputCover().getId().contains("energy") ? tier.getVoltage() : 0,
                    type.getOutputCover().getId().contains("dynamo") ? tier.getVoltage() : 0,
                    type.getOutputCover().getId().contains("energy") ? 2 : 0, type.getOutputCover().getId().contains("dynamo") ? 1 : 0) {
                @Override
                public boolean canInput(Direction direction) {
                    ICover out = tile.coverHandler.map(MachineCoverHandler::getOutputCover).orElse(null);
                    if (out == null)
                        return false;
                    return out instanceof CoverEnergy && direction == out.side();
                }

                @Override
                protected boolean checkVoltage(long voltage) {
                    boolean flag = true;
                    if (type.getOutputCover().getId().contains("energy")) {
                        flag = voltage <= getInputVoltage();
                    }
                    if (!flag && AntimatterConfig.MACHINES_EXPLODE.get()) {
                        Utils.createExplosion(tile.getLevel(), tile.getBlockPos(), 4.0F, Explosion.BlockInteraction.BREAK);
                    }
                    return flag;
                }

                @Override
                public boolean canOutput(Direction direction) {
                    ICover out = tile.coverHandler.map(MachineCoverHandler::getOutputCover).orElse(null);
                    if (out == null)
                        return false;
                    return out instanceof CoverDynamo && direction == out.side();
                }
            });
        }
    }

    public void setTextureBlock(BlockBasic textureBlock) {
        this.textureBlock = textureBlock;
        this.sidedSync(true);
    }

    @Override
    public boolean wrenchMachine(Player player, BlockHitResult res, boolean crouch) {
        return setFacing(player, Utils.getInteractSide(res));
    }

    @Override
    protected boolean setFacing(Player player, Direction side) {
        boolean setFacing = super.setFacing(player, side);
        if (setFacing){
            setOutputFacing(player, side);
        }
        return setFacing;
    }

    @Override
    public Optional<HatchComponentHandler<T>> getComponentHandler() {
        return componentHandler;
    }

    public Texture getBaseTexture(Direction side){
        if (textureBlock == null || textureBlock.getTextures().length == 0) return null;
        if (textureBlock.getTextures().length >= 6){
            return textureBlock.getTextures()[side.get3DDataValue()];
        }
        return textureBlock.getTextures()[0];
    }

    @Override
    public Function<Direction, Texture> getMultiTexture() {
        if (textureBlock == null || textureBlock.getTextures().length == 0) return null;
        return this::getBaseTexture;
    }

    @Override
    public void onMachineEvent(IMachineEvent event, Object... data) {
        if (isClientSide())
            return;
        super.onMachineEvent(event, data);
        if (event instanceof SlotType<?>) {
            componentHandler.map(ComponentHandler::getControllers).orElse(Collections.emptyList())
                    .forEach(controller -> {
                        if (event == SlotType.IT_IN || event == SlotType.IT_OUT || event == SlotType.CELL_IN || event == SlotType.CELL_OUT || event == SlotType.FL_IN || event == SlotType.FL_OUT) {
                            controller.onMachineEvent(event, data);
                        }
                    });
        } else if (event instanceof MachineEvent) {
            componentHandler.map(ComponentHandler::getControllers).orElse(Collections.emptyList())
                    .forEach(controller -> {
                        switch ((MachineEvent) event) {
                            // Forward energy event to controller.
                            case ENERGY_DRAINED, ENERGY_INPUTTED, HEAT_INPUTTED, HEAT_DRAINED -> controller.onMachineEvent(event, data);
                            default -> {
                            }
                        }
                    });
        }
    }

    @Override
    public void onFirstTickServer(Level level, BlockPos pos, BlockState state) {
        super.onFirstTickServer(level, pos, state);
        coverHandler.ifPresent(t -> {
            ICover cover = t.getOutputCover();
            if (!(cover instanceof CoverOutput output))
                return;
            output.setEjects(has(FLUID), has(ITEM));
        });
    }

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag updateTag = super.getUpdateTag();
        if (textureBlock != null) {
            updateTag.putString("textureBlock", textureBlock.getLoc().toString());
        } else {
            updateTag.putBoolean("noTextureBlock", true);
        }
        return updateTag;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("textureBlock")) {
            Block block = AntimatterAPI.get(Block.class, new ResourceLocation(tag.getString("textureBlock")));
            if (block instanceof BlockBasic blockBasic){
                textureBlock = blockBasic;
            } else textureBlock = null;
        }
        if (tag.getBoolean("noTextureBlock")) {
            textureBlock = null;
        }
        if (level != null) {
            sidedSync(true);
        }
    }

    @Override
    public ResourceLocation getGuiTexture() {
        return new ResourceLocation(getMachineType().getDomain(), "textures/gui/machine/hatch.png");
    }
}
