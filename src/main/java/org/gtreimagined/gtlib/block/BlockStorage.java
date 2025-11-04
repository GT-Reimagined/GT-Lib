package org.gtreimagined.gtlib.block;

import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import net.minecraft.world.level.block.SoundType;

public class BlockStorage extends BlockMaterialType implements ISharedGTObject {

    public BlockStorage(String domain,  MaterialType<?> type, Material material) {
        super(domain, material, type, createProperties(material, type));
    }

    private static Properties createProperties(Material material, MaterialType<?> type) {
        Properties properties = Properties.of().mapColor(material == GTLibMaterials.Wood ? MapColor.WOOD : MapColor.METAL)
                .strength(type == GTMaterialTypes.FRAME ? 2.0f : 8.0f).sound(material == GTLibMaterials.Wood ? SoundType.WOOD : SoundType.METAL)
                .requiresCorrectToolForDrops().isValidSpawn((blockState, blockGetter, blockPos, object) -> false);
        if (material == GTLibMaterials.Wood){
            properties.ignitedByLava().instrument(NoteBlockInstrument.BASS);
        }
        return properties;
    }
}
