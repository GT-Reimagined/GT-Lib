package org.gtreimagined.gtlib.blockentity;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IExtendingBlockEntity {
    BlockEntity getExtendedBlockEntity(Direction side);
}
