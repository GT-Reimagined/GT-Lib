package org.gtreimagined.gtlib.client.baked;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.capability.pipe.PipeCoverHandler;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.cover.BaseCover;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.texture.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PipeFullBakedModel extends GTBakedModel<PipeFullBakedModel>{

    final BakedModel open;
    final BakedModel closed;
    public PipeFullBakedModel(TextureAtlasSprite p, BakedModel open, BakedModel closed) {
        super(p);
        this.open = open;
        this.closed = closed;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlockEntityPipe<?> pipe)) return Collections.emptyList();
        List<BakedQuad> quads = new ArrayList<>();
        if (side == null) return List.of();
        BakedModel sideModel = pipe.connects(side) ? open : closed;
        quads.addAll(ModelUtils.getQuadsFromBaked(sideModel, state, side, rand, level, pos));
        PipeCoverHandler<?> covers = pipe.coverHandler.orElse(null);
        if (covers == null) return quads;
        List<BakedQuad> coverQuads = new LinkedList<>();
        ICover c = covers.get(side);
        if (c.isEmpty()) return quads;
        Texture tex = pipe.connects(side) ? ((BlockPipe<?>) state.getBlock()).getFace() : ((BlockPipe<?>) state.getBlock()).getSide();
        coverQuads = covers.getTexturer(side).getQuads("pipe_full", coverQuads, state, c,
                new BaseCover.DynamicKey(side, tex, c.getId()), side.get3DDataValue(), level, pos);
        return coverQuads;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
