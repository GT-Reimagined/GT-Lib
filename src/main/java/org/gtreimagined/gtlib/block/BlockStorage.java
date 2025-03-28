package org.gtreimagined.gtlib.block;

import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.registration.ISharedAntimatterObject;
import net.minecraft.world.level.block.SoundType;

public class BlockStorage extends BlockMaterialType implements ISharedAntimatterObject {

    public BlockStorage(String domain,  MaterialType<?> type, Material material) {
        super(domain, material, type, Properties.of(material == GTLibMaterials.Wood ? net.minecraft.world.level.material.Material.WOOD : net.minecraft.world.level.material.Material.METAL).strength(type == GTMaterialTypes.FRAME ? 2.0f : 8.0f).sound(material == GTLibMaterials.Wood ? SoundType.WOOD : SoundType.METAL).requiresCorrectToolForDrops().isValidSpawn((blockState, blockGetter, blockPos, object) -> false));
    }
}
