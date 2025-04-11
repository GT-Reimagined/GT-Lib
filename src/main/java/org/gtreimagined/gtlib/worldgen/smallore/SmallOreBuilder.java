package org.gtreimagined.gtlib.worldgen.smallore;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.gtreimagined.gtlib.material.Material;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmallOreBuilder {
    @Nullable
    private Material material;
    @Nullable
    private Integer amountPerChunk;
    @Nullable
    private Integer maxY;
    @Nullable
    private Integer minY;
    ResourceLocation id;
    List<ResourceKey<Level>> dimensions = new ArrayList<>();
    List<String> biomes = new ArrayList<>();
    boolean biomeBlacklist = true;

    public SmallOreBuilder(@NotNull ResourceLocation id) {
        this.id = id;
    }

    final public SmallOre buildMaterial() {
        if (id == null){
            throw  new RuntimeException("id is required");
        }
        if (this.amountPerChunk == null) {
            throw new RuntimeException("weight is required");
        }
        if (this.material == null) {
            throw new RuntimeException("material is required");
        }
        return new SmallOre(
                id,
                this.material,
                this.minY != null ? this.minY : Integer.MIN_VALUE,
                this.maxY != null ? this.maxY : Integer.MAX_VALUE,
                amountPerChunk,
                this.dimensions,
                this.biomes,
                this.biomeBlacklist
        );
    }


    final public SmallOreBuilder withMaterial(Material material) {
        this.material = material;
        return this;
    }

    final public SmallOreBuilder withAmountPerChunk(int amountPerChunk) {
        this.amountPerChunk = amountPerChunk;
        return this;
    }

    final public SmallOreBuilder atHeight(int minY, int maxY) {
        this.minY = minY;
        this.maxY = maxY;
        return this;
    }

    final public SmallOreBuilder withBiomes(String... biomes) {
        Collections.addAll(this.biomes, biomes);
        return this;
    }

    public final SmallOreBuilder inDimension(ResourceKey<Level> dimension) {
        this.dimensions.add(dimension);
        return this;
    }

    public final SmallOreBuilder inDimensions(List<ResourceKey<Level>> dimension) {
        this.dimensions.addAll(dimension);
        return this;
    }

    @SafeVarargs
    final public SmallOreBuilder inDimensions(ResourceKey<Level>... dimensions) {
        Collections.addAll(this.dimensions, dimensions);
        return this;
    }

    final public SmallOreBuilder setBiomeBlacklist(boolean blacklist) {
        this.biomeBlacklist = blacklist;
        return this;
    }
}
