package org.gtreimagined.gtlib.worldgen.smallore;

import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import net.minecraft.resources.ResourceLocation;
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
    @Nullable String id;
    List<ResourceLocation> dimensions = new ArrayList<>();
    List<String> biomes = new ArrayList<>();
    boolean dimensionBlacklist = false, biomeBlacklist = true;

    public SmallOreBuilder() {
    }

    final public SmallOre buildMaterial() {
        if (this.amountPerChunk == null) {
            throw new RuntimeException("weight is required");
        }
        if (this.material == null) {
            throw new RuntimeException("material is required");
        }
        if (this.dimensions.isEmpty()) {
            this.dimensions.add(new ResourceLocation("overworld"));
        }
        SmallOre smallOre =  new SmallOre(
                id != null ? id : material.getId(),
                this.material,
                this.minY != null ? this.minY : Integer.MIN_VALUE,
                this.maxY != null ? this.maxY : Integer.MAX_VALUE,
                amountPerChunk,
                this.dimensions,
                this.biomes,
                this.biomeBlacklist
        );
        GTLibWorldGenerator.writeJson(smallOre.toJson(), smallOre.getId(), "small_ore");
        return GTLibWorldGenerator.readJson(SmallOre.class, smallOre, SmallOre::fromJson, "small_ore");
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

    final public SmallOreBuilder withCustomId(String id){
        this.id = id;
        return this;
    }

    final public SmallOreBuilder withBiomes(String... biomes) {
        Collections.addAll(this.biomes, biomes);
        return this;
    }

    final public SmallOreBuilder withDimensions(ResourceLocation... dimensions) {
        Collections.addAll(this.dimensions, dimensions);
        return this;
    }

    final public SmallOreBuilder setBiomeBlacklist(boolean blacklist) {
        this.biomeBlacklist = blacklist;
        return this;
    }
}
