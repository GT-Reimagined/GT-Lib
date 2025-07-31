package org.gtreimagined.gtlib.pipe.types;

import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.pipe.BlockCable;
import net.minecraft.world.level.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class Wire<T extends Wire<T>> extends Cable<T> {

    public Wire(String domain, Material material, int loss, Tier tier) {
        this(domain, material, (double) loss, tier);
    }

    public Wire(String domain, Material material, double loss, Tier tier) {
        super(domain, material, loss, tier);
        material.flags(MaterialTags.WIRE);
    }

    @Override
    public String getType() {
        return "wire";
    }

    @Override
    public Set<Block> getBlocks() {
        return sizes.stream().map(s -> new BlockCable(this, s, false)).collect(Collectors.toSet());
    }
}
