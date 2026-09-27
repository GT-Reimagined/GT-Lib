package org.gtreimagined.gtlib.mixin;

import net.minecraft.core.Direction;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import org.gtreimagined.gtlib.common.EntityPlaceEventExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = EntityPlaceEvent.class, remap = false)
public class EntityPlaceEventMixin implements EntityPlaceEventExtension {
    @Unique
    Direction gtlib$placedDirection;
    @Override
    public @Nullable Direction gtlib_getPlacedDirection() {
        return gtlib$placedDirection;
    }

    @Override
    public void gtlib_setPlacedDirection(@Nullable Direction direction) {
        this.gtlib$placedDirection = direction;
    }
}
