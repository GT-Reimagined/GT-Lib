package org.gtreimagined.gtlib.client.baked;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.gtreimagined.gtlib.client.GTLibModelProperties;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public interface IGTBakedModel extends IDynamicBakedModel {
    List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos);

     default List<BakedQuad> getQuads(BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos){
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
    default List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState state, @org.jetbrains.annotations.Nullable Direction side, RandomSource rand) {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    default List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, RenderType renderType){
        BlockAndTintGetter world = data.get(GTLibModelProperties.WORLD);
        BlockPos pos = data.get(GTLibModelProperties.POS);
        if (world == null || pos == null) return Collections.emptyList();
        return getQuads(state, side, rand, world, pos);
    }

    @NotNull
    @Override
    default ModelData getModelData(@NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData modelData) {
        ModelData d = IDynamicBakedModel.super.getModelData(level, pos, state, modelData);
        ModelData.Builder builder = null;
        if (d == ModelData.EMPTY) builder = ModelData.builder();
        if (builder != null){
            builder.with(GTLibModelProperties.WORLD, level);
            builder.with(GTLibModelProperties.POS, pos);
            d = builder.build();
        }
        return d;
    }

    @Override
    default TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        BlockAndTintGetter world = data.get(GTLibModelProperties.WORLD);
        BlockPos pos = data.get(GTLibModelProperties.POS);
        if (world == null || pos == null) return IDynamicBakedModel.super.getParticleIcon(data);
        return getParticleIcon(world, pos);
    }

    TextureAtlasSprite getParticleIcon(BlockAndTintGetter level, BlockPos pos);
}
