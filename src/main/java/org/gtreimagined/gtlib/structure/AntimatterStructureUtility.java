package org.gtreimagined.gtlib.structure;

import com.gtnewhorizon.structurelib.structure.IStructureElement;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import org.gtreimagined.gtlib.capability.IComponentHandler;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.HatchMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AntimatterStructureUtility {
    public static <T extends BlockEntityBasicMultiMachine<T>> IStructureElement<T> ofHatch(HatchMachine machine){
        return ofHatch(machine, (t, world, pos, machine1, handler) -> {
            t.addComponent(machine1.getId(), handler);
            return true;
        });
    }

    public static <T extends BlockEntityBasicMultiMachine<T>> IStructureElement<T> ofHatchMinTier(HatchMachine machine, Tier minTier){
        return ofHatch(machine, (t, world, pos, machine1, handler) -> {
            if (!(handler.getTile() instanceof BlockEntityMachine<?> machineTile)) return false;
            if (machineTile.getMachineTier().getVoltage() < minTier.getVoltage()){
                return false;
            }
            t.addComponent(machine1.getIdForHandlers(), handler);
            return true;
        });
    }

    public static <T extends BlockEntityBasicMultiMachine<T>> IStructureElement<T> ofHatch(HatchMachine machine, IHatchStructurePredicate<T> callback){
        return new IStructureElement<T>() {
            @Override
            public boolean check(T t, Level world, int x, int y, int z) {
                BlockPos pos = new BlockPos(x, y, z);
                BlockEntity tile = world.getBlockEntity(pos);
                if (tile instanceof IComponent component) {
                    if (component.getComponentHandler().isPresent()) {
                        IComponentHandler componentHandler = component.getComponentHandler().orElse(null);
                        if (machine.getIdForHandlers().equals(componentHandler.getIdForHandlers())) {
                            boolean test = callback.test(t, world, pos, machine, componentHandler);
                            if (test && world.getBlockEntity(pos) instanceof BlockEntityHatch<?> hatch) {
                                hatch.setTextureBlock(t.getHatchBlock(pos));
                            }
                            return test;
                        }
                        return false;
                    }
                }
                return false;
            }

            @Override
            public boolean spawnHint(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return false;
            }

            @Override
            public boolean placeBlock(T t, Level world, int x, int y, int z, ItemStack trigger) {
                return false;
            }

            @Override
            public void onStructureFail(T t, Level world, int x, int y, int z) {
                if (world.getBlockEntity(new BlockPos(x,y,z)) instanceof BlockEntityHatch<?> hatch) {
                    hatch.setTextureBlock(null);
                }
            }
        };
    }

    public interface IHatchStructurePredicate<T> {
        boolean test(T t, Level world, BlockPos pos, HatchMachine machine, IComponentHandler handler);
    }
}
