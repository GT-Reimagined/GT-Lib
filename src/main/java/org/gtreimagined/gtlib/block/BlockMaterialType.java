package org.gtreimagined.gtlib.block;

import lombok.Getter;
import org.gtreimagined.gtlib.material.IMaterialObject;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BlockMaterialType extends BlockBasic implements IColorHandler, IMaterialObject {

    @Getter
    protected Material material;
    @Getter
    protected MaterialType<?> type;
    protected String textureFolder = "";

    public BlockMaterialType(String domain, Material material, MaterialType<?> type, Properties properties) {
        super(domain, type.getIdGetter().apply(material), properties);
        this.material = material;
        this.type = type;
    }

    public BlockMaterialType instancedTextures(String folder) {
        this.textureFolder = folder;
        return this;
    }

    @Override
    public int getBlockColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int i) {
        return i == 0 ? material.getRGB() : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return i == 0 ? material.getRGB() : -1;
    }

    @Override
    public Texture[] getTextures() {
        return !textureFolder.isEmpty() ? new Texture[]{new Texture(domain, "block/" + textureFolder + "/" + material.getId())} : material.getSet().getTextures(type);
    }
}
