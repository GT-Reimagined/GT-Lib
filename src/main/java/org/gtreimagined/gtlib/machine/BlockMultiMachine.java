package org.gtreimagined.gtlib.machine;

import org.gtreimagined.gtlib.client.AntimatterModelManager;
import org.gtreimagined.gtlib.datagen.builder.AntimatterBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.providers.AntimatterBlockStateProvider;
import org.gtreimagined.gtlib.machine.types.Machine;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class BlockMultiMachine extends BlockMachine {
    public BlockMultiMachine(Machine<?> type, Tier tier) {
        super(type, tier);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (type == null) return; // means this is the first run
        if (type.isVerticalFacingAllowed()) {
            builder.add(BlockStateProperties.FACING);
        } else {
            builder.add(BlockStateProperties.HORIZONTAL_FACING);
        }
    }

    @Override
    public void onBlockModelBuild(Block block, AntimatterBlockStateProvider prov) {
        AntimatterBlockModelBuilder builder = prov.getBuilder(block);
        buildModelsForState(builder, MachineState.IDLE);
        buildModelsForState(builder, MachineState.ACTIVE);
        buildModelsForState(builder, MachineState.INVALID_STRUCTURE);
        builder.loader(AntimatterModelManager.LOADER_MACHINE);
        builder.property("particle", getType().getBaseTexture(tier, MachineState.IDLE)[0].toString());
        prov.state(block, builder);
    }
}
