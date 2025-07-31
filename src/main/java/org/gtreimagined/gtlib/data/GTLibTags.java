package org.gtreimagined.gtlib.data;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.util.TagUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public class GTLibTags {
    public static final TagKey<Fluid> ACID = TagUtils.getFluidTag(new ResourceLocation(Ref.ID, "acid"));
    public static final TagKey<Item> RADIOACTIVE = TagUtils.getItemTag(new ResourceLocation(Ref.ID, "radioactive"));
    public static final TagKey<Item> RADIATION_PROOF = TagUtils.getItemTag(new ResourceLocation(Ref.ID, "radiation_proof"));
    public static final TagKey<Item> PLATE_PLUNGER = TagUtils.getForgelikeItemTag("plates/plunger");
    public static final TagKey<Block> NEEDS_NETHERITE_TOOL = TagUtils.getForgelikeBlockTag("needs_netherite_tool");
    public static final TagKey<Block> NEEDS_ADAMANTIUM_TOOL = TagUtils.getForgelikeBlockTag("needs_adamantium_tool");
    static void init(){

    }
}
