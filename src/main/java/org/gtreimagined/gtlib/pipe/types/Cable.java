package org.gtreimagined.gtlib.pipe.types;

import lombok.Getter;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityCable;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.pipe.BlockCable;
import org.gtreimagined.gtlib.pipe.PipeSize;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class Cable<T extends Cable<T>> extends PipeType<T> {

    @Getter
    protected double loss;
    @Getter
    protected Tier tier;
    protected int[] amps;

    public Cable(String domain, Material material, double loss, Tier tier) {
        super(domain, material, BlockEntityCable::new);
        this.loss = loss;
        this.tier = tier;
        material.flags(MaterialTags.CABLE);
    }

    public Cable(String domain, Material material, int loss, Tier tier) {
        this(domain, material, (double) loss, tier);
    }

    @Override
    public String getType() {
        return "cable";
    }

    @Override
    public String getTypeName() {
        return "energy";
    }

    @Override
    public Set<Block> getBlocks() {
        return sizes.stream().map(s -> new BlockCable(this, s, true)).collect(Collectors.toSet());
    }

    public int getAmps(PipeSize size) {
        return amps[size.ordinal()];
    }

    public T amps(int baseAmps) {
        this.amps = new int[]{baseAmps, baseAmps * 2, baseAmps * 4, baseAmps * 8, baseAmps * 12, baseAmps * 16};
        return (T) this;
    }

    public T amps(int... amps) {
        this.amps = amps;
        return (T) this;
    }

    public T loss(double loss){
        this.loss = loss;
        return (T) this;
    }

    @Override
    public String getSizeId(PipeSize size) {
        return size.getCableThickness() + "x";
    }

    @Override
    public String getModelPath(PipeSize size){
        if (this instanceof Wire<?>) return super.getModelPath(size);
        return "pipe/" +  switch (size){
            case HUGE -> "14x14";
            case LARGE -> "12x12";
            case NORMAL -> "10x10";
            case SMALL -> "8x8";
            case TINY -> "6x6";
            case VTINY -> "4x4";
            default -> "";
        };
    }

    @Override
    public AABB getCenterShape(PipeSize size) {
        if (this instanceof Wire<?>) return super.getCenterShape(size);
        if (size == PipeSize.HUGE){
            float offset = getOffset(size);
            return new AABB(0.4375 - offset, 0.4375 - offset, 0.4375 - offset, 0.5625 + offset, 0.5625 + offset, 0.5625 + offset);
        }
        return PipeSize.values()[size.ordinal() + 1].getAABB();
    }

    @Override
    public float getOffset(PipeSize size) {
        if (this instanceof Wire<?>) return super.getOffset(size);
        return 0.0625f * (size.ordinal() + 1);
    }
}