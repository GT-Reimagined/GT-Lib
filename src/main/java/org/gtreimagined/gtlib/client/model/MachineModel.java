package org.gtreimagined.gtlib.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.gtreimagined.gtlib.client.IGTModel;
import org.gtreimagined.gtlib.client.baked.MachineBakedModel;
import org.gtreimagined.gtlib.machine.MachineState;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MachineModel implements IGTModel<MachineModel> {

    protected final Map<MachineState, UnbakedModel[]> models;
    protected final ResourceLocation particle;
    public MachineModel(Map<MachineState, UnbakedModel[]> models, ResourceLocation particle) {
        this.models = models;
        this.particle = particle;
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext configuration, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
            return models.values().stream().flatMap(t -> Arrays.stream(t).flatMap(i -> i.getMaterials(modelGetter, missingTextureErrors).stream())).collect(Collectors.toSet());
    }

    @Override
    public BakedModel bakeModel(IGeometryBakingContext configuration, ModelBakery bakery,
            Function<Material, TextureAtlasSprite> getter, ModelState transform, ItemOverrides overrides,
            ResourceLocation loc) {
                ImmutableMap.Builder<MachineState, BakedModel[]> builder = ImmutableMap.builder();

                for (Map.Entry<MachineState, UnbakedModel[]> pair : this.models.entrySet()) {
                    BakedModel[] mod = new BakedModel[6];
                    for (int i = 0; i < 6; i++) {
                        mod[i] = pair.getValue()[i].bake(bakery, getter, transform, loc);
                    }
                    builder.put(pair.getKey(),mod);
                }
                return new MachineBakedModel(getter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, particle)), builder.build());
            }
    }

