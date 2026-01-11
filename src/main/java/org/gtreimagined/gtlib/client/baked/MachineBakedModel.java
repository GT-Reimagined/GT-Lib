package org.gtreimagined.gtlib.client.baked;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import org.gtreimagined.gtlib.GTLibProperties.MachineProperties;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.CoverHandler;
import org.gtreimagined.gtlib.client.DirectionalQuadTransformer;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.client.RenderHelper;
import org.gtreimagined.gtlib.client.dynamic.DynamicTexturer;
import org.gtreimagined.gtlib.client.quad.ITextureReferenceBakedQuad;
import org.gtreimagined.gtlib.client.quad.RetexturedBakedQuad;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class MachineBakedModel extends GTBakedModel<MachineBakedModel> {

    private final ImmutableMap<MachineState, BakedModel[]> sides;
    public MachineBakedModel(TextureAtlasSprite particle, ImmutableMap<MachineState, BakedModel[]> sides) {
        super(particle);
        this.sides = sides;
    }    


    protected List<BakedQuad> getCoverQuads(BlockState state, Direction side, RandomSource rand, MachineProperties data, BlockEntityMachine<?> machine, BlockAndTintGetter level, BlockPos pos) {
   
        ICover cover = data.covers[side.get3DDataValue()];
        if (cover.isEmpty()) return Collections.emptyList();
        Texture tex = machine.getMultiTexture() != null
                ? machine.getMultiTexture().apply(side)
                : data.machTexture.apply(side);
        List<BakedQuad> list = new ObjectArrayList<>();
        list = data.coverTexturer.apply(side).getQuads("cover", list, state, cover,
                new ICover.DynamicKey(state, tex, cover.getId()), side.get3DDataValue(),
                level, pos);
        /*for (Direction s : Ref.DIRS) {

        }*/
        return list;
    }
    @Override
    public List<BakedQuad> getBlockQuads(BlockState state, Direction side, RandomSource rand, BlockAndTintGetter level, @NotNull BlockPos pos) {
        if (side == null) {
            return Collections.emptyList();
        }
        BlockEntity tile = level.getBlockEntity(pos);
        if (!(tile instanceof BlockEntityMachine<?> machine)) return Collections.emptyList();
        MachineProperties props = getMachineProperty(machine);
        if (props == null) return Collections.emptyList();
        List<BakedQuad> quads = new ObjectArrayList<>(20);
        List<BakedQuad> coverQuads = getCoverQuads(state, side, rand, props, machine, level, pos);
        if (!coverQuads.isEmpty()) return coverQuads;

        BakedModel model = getModel(state, side, props.state, props.type);
        for (Direction dir : Ref.DIRS) {
            quads.addAll(ModelUtils.getQuadsFromBaked(model, state, dir, rand, level, pos));
        }
        quads.addAll(ModelUtils.getQuadsFromBaked(model, state, null, rand, level, pos));
        if (machine.getMultiTexture() != null) {
            for (int i = 0; i < quads.size(); i++) {
                var quad  = quads.get(i);
                if (quad instanceof ITextureReferenceBakedQuad textureReferenceBakedQuad){
                    if (textureReferenceBakedQuad.gtLib$getTextureId().equals("#base")){
                        var sprite = ModelUtils.getDefaultTextureGetter().apply(ModelUtils.getBlockMaterial(machine.getMultiTexture().apply(side)));
                        quads.set(i, new RetexturedBakedQuad(quad, sprite));
                    }
                }
            }
        }
        if (props.type.isNoFacing() || props.type.isNoTextureRotation()) return quads;
        Matrix4f f = new Matrix4f();
        f.setIdentity();
        Transformation mat = new Transformation(f);
        mat = mat.blockCornerToCenter();
        mat = mat.compose(RenderHelper.faceRotation(state));
        mat = mat.blockCenterToCorner();
        DirectionalQuadTransformer transformer = new DirectionalQuadTransformer(mat);
        return transformer.processMany(quads, side);
    }

    public BakedModel getModel(BlockState state, Direction dir, MachineState m, Machine<?> type) {
        if (type.isNoFacing() || type.isNoTextureRotation()) return sides.get(m)[dir.get3DDataValue()];
        Vec3i vector3i = dir.getNormal();
        Vector4f vector4f = new Vector4f((float) vector3i.getX(), (float) vector3i.getY(), (float) vector3i.getZ(), 0.0F);
        vector4f.transform(RenderHelper.faceRotation(state).inverse().getMatrix());
        Direction side = Direction.getNearest(vector4f.x(), vector4f.y(), vector4f.z());
        return sides.get(m)[side.get3DDataValue()];
    }

    public MachineProperties getMachineProperty(BlockEntityMachine<?> machine) {
        ICover[] covers = machine.coverHandler.map(CoverHandler::getAllRendered).orElse(new ICover[]{ICover.empty, ICover.empty, ICover.empty, ICover.empty, ICover.empty, ICover.empty});
        Machine<?> m = machine.getMachineType();
        Function<Direction, Texture> mText = a -> {
            Texture[] tex = machine.getMachineType().getBaseTexture(machine.getMachineTier(), machine.getMachineState().getTextureState());
            if (tex.length == 1) return tex[0];
            return tex[a.get3DDataValue()];
        };
        MachineState st = machine.getMachineState().getTextureState();
        Function<Direction, DynamicTexturer<ICover, ICover.DynamicKey>> tx = a -> machine.coverHandler.map(t -> t.getTexturer(a)).orElse(null);
        MachineProperties mh = new MachineProperties(m, machine.getMachineTier(), covers, st, mText, tx);
        return mh;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
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
        return true;
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

}
