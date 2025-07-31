package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public abstract class CoverMaterial extends BaseCover {


    public CoverMaterial(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    public abstract MaterialType<?> getType();

    public abstract Material getMaterial();

    @Override
    public <T> boolean blocksCapability(Class<T> cap, Direction side) {
        return side != null;
    }
}
