package org.gtreimagined.gtlib.ore;

import org.gtreimagined.gtlib.block.BlockMaterialType;
import org.gtreimagined.gtlib.data.AntimatterMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;

public class BlockOreStone extends BlockMaterialType implements ISharedAntimatterObject {

    public BlockOreStone(String domain, Material material) {
        super(domain, material, AntimatterMaterialTypes.ORE_STONE, Properties.of(net.minecraft.world.level.material.Material.STONE).strength(1.5f, 3.0f).requiresCorrectToolForDrops());
        instancedTextures("stone");
    }

    @Override
    public boolean registerColorHandlers() {
        return false;
    }
}
