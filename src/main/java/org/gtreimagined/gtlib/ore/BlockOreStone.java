package org.gtreimagined.gtlib.ore;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.gtreimagined.gtlib.block.BlockMaterialType;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.registration.ISharedGTObject;

public class BlockOreStone extends BlockMaterialType implements ISharedGTObject {

    public BlockOreStone(String domain, Material material) {
        super(domain, material, GTMaterialTypes.ORE_STONE, Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM).strength(1.5f, 3.0f).requiresCorrectToolForDrops());
        instancedTextures("stone");
    }

    @Override
    public boolean registerColorHandlers() {
        return false;
    }
}
