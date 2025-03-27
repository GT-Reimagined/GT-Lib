package org.gtreimagined.gtlib.client.baked;

import org.gtreimagined.gtlib.client.AntimatterModelProperties;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface IAntimatterBakedModel extends IDynamicBakedModel {
    List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos);

     default List<BakedQuad> getQuads(BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos){
         try {
             if (hasOnlyGeneralQuads() && side != null) return Collections.emptyList();
             return state != null ? getBlockQuads(state, side, rand, level, pos) : Collections.emptyList(); //todo figure out item quads if necessary
         } catch (Exception e) {
             e.printStackTrace();
             return Collections.emptyList();
         }
     }

    boolean hasOnlyGeneralQuads();
    @Override
    default List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, Random rand) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull Random rand, @NotNull IModelData data){
        BlockAndTintGetter world = data.getData(AntimatterModelProperties.WORLD);
        BlockPos pos = data.getData(AntimatterModelProperties.POS);
        if (world == null || pos == null) return Collections.emptyList();
        return getQuads(state, side, rand, world, pos);
    }

    @NotNull
    @Override
    default IModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull IModelData modelData) {
        IModelData d = IDynamicBakedModel.super.getModelData(level, pos, state, modelData);
        if (d == EmptyModelData.INSTANCE) d = new ModelDataMap.Builder().build();
        d.setData(AntimatterModelProperties.WORLD, level);
        d.setData(AntimatterModelProperties.POS, pos);
        return d;
    }

    @Override
    default TextureAtlasSprite getParticleIcon(@NotNull IModelData data) {
        BlockAndTintGetter world = data.getData(AntimatterModelProperties.WORLD);
        BlockPos pos = data.getData(AntimatterModelProperties.POS);
        if (world == null || pos == null) return IDynamicBakedModel.super.getParticleIcon(data);
        return getParticleIcon(world, pos);
    }

    TextureAtlasSprite getParticleIcon(BlockAndTintGetter level, BlockPos pos);
}
