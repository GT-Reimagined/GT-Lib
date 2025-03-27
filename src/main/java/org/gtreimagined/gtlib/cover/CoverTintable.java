package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.machine.Tier;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public abstract class CoverTintable extends BaseCover {

    public CoverTintable(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    public abstract int getRGB();
}
