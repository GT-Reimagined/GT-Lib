package org.gtreimagined.gtlib.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.model.data.ModelProperty;

public class GTLibModelProperties {
    public static final ModelProperty<BlockAndTintGetter> WORLD = new ModelProperty<>();
    public static final ModelProperty<BlockPos> POS = new ModelProperty<>();
}
