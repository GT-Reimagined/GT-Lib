package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.machine.Tier;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;

public class CoverDynamo extends BaseCover {

    public CoverDynamo(ICoverHandler<?> source, @Nullable Tier tier, Direction side, CoverFactory factory) {
        super(source, tier, side, factory);
    }

    @Override
    public boolean ticks() {
        return false;
    }

    @Override
    public ResourceLocation getModel(String type, Direction dir) {
        return getBasicModel();
    }

    @Override
    public void onPlace() {
        super.onPlace();
        ((BlockEntityMachine<?>) handler.getTile()).invalidateCap(IEnergyHandler.class);
        ((BlockEntityMachine<?>) handler.getTile()).invalidateCap(IEnergyStorage.class);
    }
}
