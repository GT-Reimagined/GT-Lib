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

public class PipeBakedModelNew extends GTBakedModel<PipeBakedModelNew>{
    final BakedModel base;
    final BakedModel baseEnd;
    final BakedModel[] connections;
    final BakedModel[] connectionsEnd;
    public PipeBakedModelNew(TextureAtlasSprite p, BakedModel base, BakedModel baseEnd, BakedModel[] connections, BakedModel[] connectionsEnd) {
        super(p);
        this.base = base;
        this.baseEnd = baseEnd;
        this.connections = connections;
        this.connectionsEnd = connectionsEnd;
    }

    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof BlockEntityPipe<?> pipe)) return Collections.emptyList();
        List<BakedQuad> quads = new ArrayList<>();
        PipeCoverHandler<?> covers = pipe.coverHandler.orElse(null);
        if (side == null){
            quads.addAll(getQuadsFromModel(base, state, rand, level, pos));
            List<BakedQuad> coverQuads = new LinkedList<>();
            boolean connected = false;
            for (Direction d : Direction.values()) {
                if (pipe.connects(d)){
                    connected = true;
                    quads.addAll(getQuadsFromModel(connections[d.get3DDataValue()], state, rand, level, pos));
                }
                if (covers != null && !covers.get(d).isEmpty()){
                    Texture tex = ((BlockPipe<?>) state.getBlock()).getFace();
                    ICover c = covers.get(d);
                    coverQuads = covers.getTexturer(d).getQuads("pipe", coverQuads, state, c,
                            new BaseCover.DynamicKey(d, tex, c.getId()), d.get3DDataValue(), level, pos);
                }
            }
            quads.addAll(coverQuads);
            if (!connected) quads.addAll(getQuadsFromModel(baseEnd, state, rand, level, pos));
        } else {
            if (pipe.connects(side) && covers != null && covers.get(side).isEmpty()){
                quads.addAll(getQuadsFromModel(connectionsEnd[side.get3DDataValue()], state, rand, level, pos));
            }
        }
        return quads;
    }

    private List<BakedQuad> getQuadsFromModel(BakedModel model, BlockState state, RandomSource rand, BlockAndTintGetter level, BlockPos pos) {
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction side : Direction.values()) {
            quads.addAll(ModelUtils.getQuadsFromBaked(model, state, side, rand, level, pos));
        }
        quads.addAll(ModelUtils.getQuadsFromBaked(model, state, null, rand, level, pos));
        return quads;
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
