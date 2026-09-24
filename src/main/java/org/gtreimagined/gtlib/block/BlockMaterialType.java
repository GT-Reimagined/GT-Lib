package org.gtreimagined.gtlib.block;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.material.IMaterialObject;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialColorChanger;
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
        return i == 0 ? MaterialColorChanger.getMaterialRgb(material) : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        return i == 0 ? MaterialColorChanger.getMaterialRgb(material) : -1;
    }

    @Override
    public void onBlockModelBuild(Block block, GTBlockStateProvider prov) {
        GTBlockModelBuilder b = prov.getBuilder(block);
        if (!textureFolder.isEmpty()){
            b.parent(new ResourceLocation(Ref.ID, "block/preset/simple"));
            b.texture("all", new Texture(domain, "block/" + textureFolder + "/" + material.getId()));
        } else {
            b.loader(GTLibModelManager.LOADER_FALLBACK);
            b.property("base", getMaterial().getSet().getDomain() + ":block/material/" + getMaterial().getSet().getId() + "/" + type.getId())
                    .property("fallback", Ref.ID + ":block/material/none/" + type.getId());
        }
        prov.simpleBlock(block, b);
    }
}
