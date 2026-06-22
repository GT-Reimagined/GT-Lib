package org.gtreimagined.gtlib.cover;

import net.minecraft.util.RandomSource;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityMultiMachine;
import org.gtreimagined.gtlib.capability.ComponentHandler;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.structure.StructureCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Random;

public class CoverMuffler extends BaseCover {

    public CoverMuffler(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        if (type.equals("pipe"))
            return PIPE_COVER_MODEL;
        return getBasicModel();
    }

    @Override
    public void onBlockUpdate() {
        super.onBlockUpdate();
        BlockState neighbor = this.handler.getTile().getLevel().getBlockState(this.handler.getTile().getBlockPos().relative(this.side));
        boolean isAir = neighbor.isAir();
        if (this.handler.getTile() instanceof BlockEntityHatch<?> machine){
            machine.getComponentHandler().map(ComponentHandler::getControllers).orElse(Collections.emptyList()).forEach(controller -> {
                controller.recipeHandler.ifPresent(r -> {
                    if (isAir && r.isProcessingBlocked()){
                        r.setProcessingBlocked(false);
                    } else if (!isAir && !r.isProcessingBlocked()){
                        r.setProcessingBlocked(true);
                    }
                });
            });
        }
    }

    @Override
    public boolean ticks() {
        return true;
    }

    @Override
    public void onClientTick() {
        BlockPos pos = handler.getTile().getBlockPos();
        Level world = this.handler.getTile().getLevel();
        BlockEntityMultiMachine<?> tile = StructureCache.getAnyMulti(world, pos, BlockEntityMultiMachine.class);
        if (tile == null || tile.getMachineState() != MachineState.ACTIVE) return;
        RandomSource rand = world.random;
        Direction dir = this.side;
        double d0 = (double) pos.getX() + 0.5D + (dir.getAxis() == Direction.Axis.X ? 0f : (rand.nextDouble() - 0.5f) / 2);
        double d1 = (double) pos.getY() + 0.5D + (dir.getAxis() == Direction.Axis.Y ? 0f : (rand.nextDouble() - 0.5f) / 2);
        double d2 = (double) pos.getZ() + 0.5D + (dir.getAxis() == Direction.Axis.Z ? 0f : (rand.nextDouble() - 0.5f) / 2);
        if (rand.nextDouble() < 0.1D) {
            //world.playSound(d0, d1, d2, SoundEvents.FIRE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
        }
        world.addParticle(ParticleTypes.SMOKE, d0 + 0.6f * this.side.getStepX(), d1 + 0.6 * this.side.getStepY(), d2 + 0.6f * this.side.getStepZ(),
                this.side.getStepX(), this.side.getStepY(), this.side.getStepZ());
    }
}
