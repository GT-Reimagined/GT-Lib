package org.gtreimagined.gtlib.client.dynamic;

import com.mojang.datafixers.util.Either;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.GTLibProperties;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.ICoverHandler;
import org.gtreimagined.gtlib.capability.ICoverHandlerProvider;
import org.gtreimagined.gtlib.client.ModelUtils;
import org.gtreimagined.gtlib.client.RenderHelper;
import org.gtreimagined.gtlib.client.SimpleModelState;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.mixin.client.BlockModelAccessor;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class DynamicTexturers {

    /**
     * Dynamic texture implementations.
     **/

    public static final DynamicTextureProvider<ICover, ICover.DynamicKey> COVER_DYNAMIC_TEXTURER = new DynamicTextureProvider<ICover, ICover.DynamicKey>(
            t -> {
                if (t.currentDir == t.source.side()) {
                    BlockModel m;
                    try {
                        m = ModelUtils.getModelBakery().loadBlockModel(t.source.getModel(t.type, Direction.SOUTH));
                    } catch (Exception e) {
                        GTLib.LOGGER.error(e);
                        return Collections.emptyList();
                    }
                    BlockModel model = m;
                    ((BlockModelAccessor)model).getTextureMap().put("base", Either.left(ModelUtils.getBlockMaterial(t.key.machineTexture)));
                    t.source.setTextures(
                            (name, texture) -> ((BlockModelAccessor)model).getTextureMap().put(name, Either.left(ModelUtils.getBlockMaterial(texture))));
                    Transformation base = RenderHelper.faceRotation(t.source.side());
                    List<BakedQuad> ret = new ObjectArrayList<>();
                    ret.addAll(bakeVanilla(model, ModelUtils.getDefaultTextureGetter(),
                            new SimpleModelState(base), t.source.getModel(t.type, Direction.SOUTH), true));
                    return t.source.transformQuads(t.state, ret);
                }
                return Collections.emptyList();
            });

    @Nullable
    private static Predicate<Map.Entry<String, BakedModel>> getEntryPredicate(DynamicTextureProvider<ICover, ICover.DynamicKey>.BuilderData t) {
        ICoverHandler<?> coverHandler = t.getBlockEntity() instanceof ICoverHandlerProvider<?> provider ? provider.getCoverHandler().orElse(null) : null;
        Predicate<Map.Entry<String, BakedModel>> predicate = null;

        if (coverHandler != null){
            predicate = e -> {
                String key = e.getKey();
                if (key.isEmpty()) return true;
                Direction dir = Utils.rotate(t.currentDir, Direction.byName(key));
                if (dir == null) throw new NullPointerException("Dir null in getBlockQuads");
                boolean ok =  coverHandler.get(dir).isEmpty();//(filter & (1 << dir.get3DDataValue())) > 0;
                return ok;
            };
        }
        return predicate;
    }

    private static List<BakedQuad> bakeVanilla(BlockModel model, Function<Material, TextureAtlasSprite> textureGetter, ModelState modelState, ResourceLocation modelLocation, boolean guiLight3d) {
        TextureAtlasSprite textureatlassprite = (TextureAtlasSprite)textureGetter.apply(model.getMaterial("particle"));
        List<BakedQuad> bakedQuads = new ArrayList<>();
        for(BlockElement blockelement : model.getElements()) {
            for(Direction direction : blockelement.faces.keySet()) {
                BlockElementFace blockelementface = blockelement.faces.get(direction);
                TextureAtlasSprite textureatlassprite1 = textureGetter.apply(model.getMaterial(blockelementface.texture));
                bakedQuads.add(BlockModelAccessor.invokeBakeFace(blockelement, blockelementface, textureatlassprite1, direction, modelState, modelLocation));
            }
        }
        return bakedQuads;
    }
}
