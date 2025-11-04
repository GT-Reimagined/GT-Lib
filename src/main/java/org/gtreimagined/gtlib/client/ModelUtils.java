package org.gtreimagined.gtlib.client;

import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.QuadTransformers;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.common.util.TransformationHelper;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.baked.CoverBakedModel;
import org.gtreimagined.gtlib.client.baked.IGTBakedModel;
import org.gtreimagined.gtlib.mixin.client.SimpleBakedModel$BuilderAccessor;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class ModelUtils {
    private static ModelBakery MODEL_BAKERY;
    //Assumes from North.
    public static Transformation transform(Direction side) {
        switch (side) {
            case DOWN:
                return new Transformation(null, new Quaternionf(new Quaternionf(new Vector3f(1.0F, 0.0F, 0.0F), 90.0F, true)), null, null);
            case UP:
                return new Transformation(null, new Quaternionf(new Quaternionf(new Vector3f(1.0F, 0.0F, 0.0F), -90.0F, true)), null, null);
            case NORTH:
                return Transformation.identity();
            case SOUTH:
                return new Transformation(null, new Quaternionf(new Vector3f(0.0F, 1.0F, 0.0F), 180.0F, true), null, null);
            case WEST:
                return new Transformation(null, new Quaternionf(new Vector3f(0.0F, 1.0F, 0.0F), 90.0f, true), null, null);
            case EAST:
                return new Transformation(null, new Quaternionf(new Vector3f(0.0F, 1.0F, 0.0F), -90.0f, true), null, null);
            default:
                throw new RuntimeException("Invalid direction/null sent to transform.");
        }
    }

    public static UnbakedModel getMissingModel() {
        return getModelBakery().getModel(new ModelResourceLocation(new ResourceLocation("builtin/missing"), "missing"));
    }


    public static UnbakedModel getModel(ResourceLocation resourceLocation){
        return getModelBakery().getModel(resourceLocation);
    }


    public static SimpleBakedModel.Builder createSimpleModelBuilder(boolean smoothLighting, boolean sideLit, boolean isShadedInGui, ItemTransforms transforms, ItemOverrides overrides){
        return SimpleBakedModel$BuilderAccessor.gtlib$create(smoothLighting, sideLit, isShadedInGui, transforms, overrides);
    }

    public static Function<ResourceLocation, UnbakedModel> getDefaultModelGetter(){
        return ModelUtils::getModelOrMissing;
    }

    private static UnbakedModel getModelOrMissing(ResourceLocation location){
        try {
            return ModelUtils.getModel(location);
        }
        catch(Exception e) {
            return ModelUtils.getMissingModel();
        }
    }

    public static Function<Material, TextureAtlasSprite> getDefaultTextureGetter(){
        return Material::sprite;
    }

    public static ModelBakery getModelBakery(){
        return MODEL_BAKERY;
    }

    public static void setModelBakery(ModelBakery modelBakery){
        MODEL_BAKERY = modelBakery;
    }

    public static List<BakedQuad> getQuadsFromBaked(BakedModel model, BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos){
        if (model instanceof IGTBakedModel gtBaked){
            return gtBaked.getQuads(state, side, rand, level, pos);
        } else {
            ModelData data = model.getModelData(level, pos, state, ModelData.EMPTY);
            return model.getQuads(state, side, rand, data, null);
        }
    }

    public static List<BakedQuad> getQuadsFromBakedCover(BakedModel model, BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos, Predicate<Map.Entry<String, BakedModel>> coverPredicate){
        if (model instanceof CoverBakedModel coverBakedModel){
            return coverBakedModel.getBlockQuads(state, side, rand, level, pos, coverPredicate);
        }
        return getQuadsFromBaked(model, state, side, rand, level, pos);
    }

    public static BakedModel getBakedFromQuads(BlockModel model, List<BakedQuad> quads, Function<Material, TextureAtlasSprite> getter) {
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(model, ItemOverrides.EMPTY, true).particle(getter.apply(model.getMaterial("particle")));
        quads.forEach(builder::addUnculledFace);
        return builder.build();
    }

    public static BakedModel getBakedFromModel(BlockModel model, ModelBaker bakery, Function<Material, TextureAtlasSprite> getter, ModelState transform, ResourceLocation loc) {
        List<BakedQuad> generalQuads = model.bake(bakery, model, getter, transform, loc, true).getQuads(null, null, Ref.RNG, ModelData.EMPTY, null);
        SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(model, ItemOverrides.EMPTY, true).particle(getter.apply(model.getMaterial("particle")));
        generalQuads.forEach(builder::addUnculledFace);
        return builder.build();
    }

    public static BakedModel getSimpleBakedModel(BakedModel baked) {
        Map<Direction, List<BakedQuad>> faceQuads = new Object2ObjectOpenHashMap<>();
        Arrays.stream(Ref.DIRS).forEach(d -> faceQuads.put(d, baked.getQuads(null, d, Ref.RNG, ModelData.EMPTY, null)));
        return new SimpleBakedModel(baked.getQuads(null, null, Ref.RNG, ModelData.EMPTY, null), faceQuads, baked.useAmbientOcclusion(), baked.usesBlockLight(), baked.isGui3d(), baked.getParticleIcon(), baked.getTransforms(), baked.getOverrides());
    }

    public static BakedModel getBaked(ResourceLocation loc) {
        return getModelBakery().getBakedTopLevelModels().get(loc);// SimpleModelState.IDENTITY, ForgeModelBakery.defaultTextureGetter());
    }

    public static BakedModel getBakedFromState(BlockState state) {
        return Minecraft.getInstance().getModelManager().getModel(BlockModelShaper.stateToModelLocation(state));
    }

    public static BakedModel getBakedFromItem(Item item) {
        return Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(new ModelResourceLocation(RegistryUtils.getIdFromItem(item), "inventory"));
    }

    public static TextureAtlasSprite getSprite(ResourceLocation loc) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(loc);
    }

    public static Material getBlockMaterial(ResourceLocation loc) {
        return new Material(InventoryMenu.BLOCK_ATLAS, loc);
    }

    public static List<BakedQuad> trans(List<BakedQuad> quads, Vector3f rotationL, Vector3f rotationR) {
        Quaternionf rotL = rotationL == null ? null : quatFromXYZ(rotationL, true);
        Quaternionf rotR = rotationR == null ? null : quatFromXYZ(rotationR, true);
        return trans(quads, new Transformation(new Vector3f(0, 0, 0), rotL, null, rotR));
    }

    public static Quaternionf quatFromXYZ(Vector3f xyz, boolean degrees){
        return TransformationHelper.quatFromXYZ(xyz, degrees);
    }

    public static List<BakedQuad> trans(List<BakedQuad> quads, Transformation transform) {
        return QuadTransformers.applying(transform.blockCenterToCorner()).process(quads);
    }

    public static void setRenderLayer(Block block, RenderType renderType){
        ItemBlockRenderTypes.setRenderLayer(block, renderType);
    }

    public static void setRenderLayer(Fluid fluid, RenderType renderType){
        ItemBlockRenderTypes.setRenderLayer(fluid, renderType);
    }

    public static void registerProperty(Item item, ResourceLocation location, ClampedItemPropertyFunction function){
        ItemProperties.register(item, location, function);
    }
}
