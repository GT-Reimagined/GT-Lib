package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.machine.Tier;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import tesseract.api.hu.IHeatHandler;

public class CoverHeat extends BaseCover{
    public CoverHeat(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    @Override
    public boolean ticks() {
        return false;
    }

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        if (type.equals("pipe"))
            return PIPE_COVER_MODEL;
        return getBasicDepthModel();
    }

    @Override
    public void onPlace() {
        super.onPlace();
        ((BlockEntityMachine<?>) handler.getTile()).invalidateCap(IHeatHandler.class);
    }
}
