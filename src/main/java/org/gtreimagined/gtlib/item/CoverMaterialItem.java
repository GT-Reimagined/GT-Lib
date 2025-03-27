package org.gtreimagined.gtlib.item;

import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.IHaveCover;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialItem;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CoverMaterialItem extends MaterialItem implements IHaveCover {

    protected final CoverFactory cover;

    public CoverMaterialItem(String domain, MaterialType<?> type, Material material, CoverFactory cover, Properties properties) {
        super(domain, type, material, properties);
        this.cover = cover;
    }

    public CoverMaterialItem(String domain, MaterialType<?> type, CoverFactory cover, Material material) {
        super(domain, type, material);
        this.cover = cover;
    }

    @Override
    public CoverFactory getCover() {
        return cover;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        tooltip.add(Utils.literal("Has cover."));
    }
}
