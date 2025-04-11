package org.gtreimagined.gtlib.worldgen.vanillaore;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.worldgen.OreObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VanillaVeinBuilder {
    @Nullable
    private Material material;
    @Nullable
    private Material secondary;
    @Nullable
    private MaterialTypeBlock<?> materialType;
    @Nullable
    private MaterialTypeBlock<?> secondaryType;
    @Nullable
    private Integer weight;
    @Nullable
    private Integer probability;
    @Nullable
    private Integer maxY;
    @Nullable
    private Integer minY;
    @Nullable
    private Integer size;
    @Nullable
    private Integer plateau;
    @Nullable
    private Float secondaryChance;
    @Nullable
    private Float discardOnExposureChance;
    ResourceLocation id;
    List<ResourceKey<Level>> dimensions = new ArrayList<>();
    List<String> biomes = new ArrayList<>();
    boolean biomeBlacklist = true, triangle = false, spawnOnOceanFloor = false;

    public VanillaVeinBuilder(@NotNull ResourceLocation id) {
        this.id = id;
    }

    final public VanillaVein buildMaterial() {
        if (this.id == null) {
            throw new RuntimeException("id required");
        }
        if (this.size == null) {
            throw new RuntimeException("size is required");
        }
        if (this.material == null) {
            throw new RuntimeException("material is required");
        }
        MaterialTypeBlock<?> materialTypeBlock = this.materialType == null ? GTMaterialTypes.ORE : materialType;
        return new VanillaVein(
                id,
                new OreObject(this.material, materialTypeBlock),
                new OreObject(this.secondary == null ? Material.NULL : this.secondary, this.secondaryType == null ? materialTypeBlock : this.secondaryType),
                this.secondaryChance == null ? 0.0f : this.secondaryChance,
                this.discardOnExposureChance == null ? 0.0f : this.discardOnExposureChance,
                this.minY != null ? this.minY : Integer.MIN_VALUE,
                this.maxY != null ? this.maxY : Integer.MAX_VALUE,
                weight == null ? 1 : weight,
                size,
                probability == null ? 1 : probability,
                triangle,
                this.plateau == null ? 0 : this.plateau,
                this.spawnOnOceanFloor,
                this.dimensions,
                this.biomes,
                this.biomeBlacklist
        );
    }

    final public VanillaVeinBuilder withMaterial(Material material) {
        this.material = material;
        return this;
    }

    final public VanillaVeinBuilder withSecondaryMaterial(Material secondary, float secondaryChance){
        this.secondary = secondary;
        this.secondaryChance = secondaryChance;
        return this;
    }

    final public VanillaVeinBuilder withSecondaryType(MaterialTypeBlock<?> materialType){
        this.secondaryType = materialType;
        return this;
    }

    final public VanillaVeinBuilder withMaterialType(MaterialTypeBlock<?> materialType){
        this.materialType = materialType;
        return this;
    }

    final public VanillaVeinBuilder withWeight(int weight) {
        this.weight = weight;
        return this;
    }

    final public VanillaVeinBuilder withSize(int size){
        this.size = size;
        return this;
    }

    final public VanillaVeinBuilder withDiscardOnExposureChance(float discardOnExposureChance){
        this.discardOnExposureChance = discardOnExposureChance;
        return this;
    }

    final public VanillaVeinBuilder atHeight(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        return this;
    }

    final public VanillaVeinBuilder withProbability(int probability){
        this.probability = probability;
        return this;
    }

    final public VanillaVeinBuilder withBiomes(String... biomes) {
        Collections.addAll(this.biomes, biomes);
        return this;
    }

    public final VanillaVeinBuilder inDimension(ResourceKey<Level> dimension) {
        this.dimensions.add(dimension);
        return this;
    }

    public final VanillaVeinBuilder inDimensions(List<ResourceKey<Level>> dimension) {
        this.dimensions.addAll(dimension);
        return this;
    }

    @SafeVarargs
    final public VanillaVeinBuilder inDimensions(ResourceKey<Level>... dimensions) {
        Collections.addAll(this.dimensions, dimensions);
        return this;
    }

    final public VanillaVeinBuilder setBiomeBlacklist(boolean blacklist) {
        this.biomeBlacklist = blacklist;
        return this;
    }

    final public VanillaVeinBuilder setHasTriangleHeight(boolean triangle){
        this.triangle = triangle;
        return this;
    }

    final public VanillaVeinBuilder setHasTriangleHeight(boolean triangle, int plateau){
        this.triangle = triangle;
        this.plateau = plateau;
        return this;
    }

    final public VanillaVeinBuilder setSpawnOnOceanFloor(boolean spawnOnOceanFloor){
        this.spawnOnOceanFloor = spawnOnOceanFloor;
        return this;
    }
}
