package org.gtreimagined.gtlib.ore;

import org.gtreimagined.gtlib.block.BlockBasic;
import org.gtreimagined.gtlib.material.IMaterialObject;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialColorChanger;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.registration.IItemBlockProvider;
import org.gtreimagined.gtlib.registration.IModelProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class BlockMaterialStone extends BlockBasic implements IGTObject, IItemBlockProvider, IColorHandler, IModelProvider, IMaterialObject {

    protected Material material;
    protected StoneType stoneType;

    public BlockMaterialStone(String domain, String id, Material material, StoneType stoneType, Properties properties) {
        super(domain, id, properties);
        this.material = material;
        this.stoneType = stoneType;
    }

    public Material getMaterial() {
        return material;
    }

    public StoneType getStoneType() {
        return stoneType;
    }

    @Override
    public int getBlockColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int i) {
        return i == 1 ? MaterialColorChanger.getMaterialRgb(material) : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return i == 1 ? MaterialColorChanger.getMaterialRgb(material) : -1;
    }

}
