package org.gtreimagined.gtlib.worldgen.stonelayer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.gtreimagined.gtlib.ore.StoneType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StoneLayerBuilder {
    @Nullable
    private final ResourceLocation id;
    @Nullable
    private StoneType stoneType;
    @Nullable
    private Block stoneState;
    @Nullable
    private Integer weight;
    @Nullable
    private Integer minY;
    @Nullable
    private Integer maxY;
    @Nullable
    private Block topReplacementBlock;
    @Nullable
    private Block bottomReplacementBlock;

    private final ArrayList<ResourceKey<Level>> dimensions;
    private StoneLayerOre[] ores = new StoneLayerOre[0];

    public StoneLayerBuilder(ResourceLocation id) {
        this.id = id;
        this.dimensions = new ArrayList<>();
    }

    public final StoneLayerBuilder withWeight(int weight) {
        this.weight = weight;
        return this;
    }

    public final StoneLayerBuilder withStone(StoneType type) {
        this.stoneType = type;
        this.stoneState = type.getState().getBlock();
        return this;
    }

    public final StoneLayerBuilder withStone(BlockState state) {
        this.stoneState = state.getBlock();
        return this;
    }

    public final StoneLayerBuilder minY(int minY, Block replacementBlock) {
        this.minY = minY;
        this.bottomReplacementBlock = replacementBlock;
        return this;
    }

    public final StoneLayerBuilder maxY(int maxY, Block replacementBlock) {
        this.maxY = maxY;
        this.topReplacementBlock = replacementBlock;
        return this;
    }

    public final StoneLayerBuilder inDimension(ResourceKey<Level> dimension) {
        this.dimensions.add(dimension);
        return this;
    }

    public final StoneLayerBuilder inDimensions(List<ResourceKey<Level>> dimensions) {
        this.dimensions.addAll(dimensions);
        return this;
    }

    public StoneLayerBuilder addOres(StoneLayerOre... ores) {
        if (stoneType == null){
            throw new IllegalStateException("Stone type must not be null before adding ores!");
        }
        this.ores = ores;
        return this;
    }

    public final StoneLayer buildVein() {
        if (this.id == null) {
            throw new RuntimeException("id is required");
        }
        if (this.stoneState == null && this.stoneType == null) {
            throw new RuntimeException("either stone state or stone type is required");
        }
        if (this.weight == null) {
            throw new RuntimeException("weight is required");
        }
        if (this.dimensions.isEmpty()) {
            this.dimensions.add(Level.OVERWORLD);
        }

        return new StoneLayer(
                this.id,
                this.stoneState,
                this.weight,
                new StoneLayerRestrictions(
                        this.minY != null ? this.minY : Integer.MIN_VALUE,
                        this.maxY != null ? this.maxY : Integer.MAX_VALUE,
                        this.topReplacementBlock != null ? topReplacementBlock : Blocks.AIR,
                        this.bottomReplacementBlock != null ? this.bottomReplacementBlock : Blocks.AIR
                ),
                this.dimensions,
                List.of(ores));
    }
}
