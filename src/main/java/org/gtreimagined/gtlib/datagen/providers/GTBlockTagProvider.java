package org.gtreimagined.gtlib.datagen.providers;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStoneSlab;
import org.gtreimagined.gtlib.block.BlockStoneStair;
import org.gtreimagined.gtlib.block.BlockStoneWall;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.data.GTLibTags;
import org.gtreimagined.gtlib.fluid.AntimatterFluid;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.BlockMultiMachine;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.pipe.BlockItemPipe;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.util.TagUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import static org.gtreimagined.gtlib.util.TagUtils.getBlockTag;
import static org.gtreimagined.gtlib.util.TagUtils.getForgelikeBlockTag;
import static org.gtreimagined.gtlib.util.Utils.getConventionalMaterialType;
import static org.gtreimagined.gtlib.util.Utils.getConventionalStoneType;

public class GTBlockTagProvider extends GTTagProvider<Block> {
    private final boolean replace;

    public GTBlockTagProvider(String providerDomain, String providerName, boolean replace) {
        super(Registry.BLOCK, providerDomain, providerName, "blocks");
        this.replace = replace;
    }

    protected void processTags(String domain) {
        if (domain.equals(Ref.ID)) {
            AntimatterAPI.all(BlockOre.class, o -> {
                this.tag(getForgelikeBlockTag(String.join("", getConventionalStoneType(o.getStoneType()), "_", getConventionalMaterialType(o.getOreType()), "/", o.getMaterial().getId()))).add(o).replace(replace);
                this.tag(getForgelikeBlockTag(String.join("", getConventionalMaterialType(o.getOreType()), "/", o.getMaterial().getId()))).add(o).replace(replace);
                this.tag(getForgelikeBlockTag(getConventionalMaterialType(o.getOreType()))).add(o).replace(replace);
                this.tag(getForgelikeBlockTag(getConventionalStoneType(o.getStoneType()) + "_" + getConventionalMaterialType(o.getOreType()))).add(o).replace(replace);

                if (o.getStoneType() == VanillaStoneTypes.SAND || o.getStoneType() == VanillaStoneTypes.SAND_RED || o.getStoneType() == VanillaStoneTypes.GRAVEL)
                    this.tag(BlockTags.MINEABLE_WITH_SHOVEL).add(o).replace(replace);
                else
                    this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(o).replace(replace);
                int oreMiningLevel = o.getMaterial().has(MaterialTags.MINING_LEVEL) ? MaterialTags.MINING_LEVEL.getInt(o.getMaterial()) : 0;
                if (o.getOreType() == GTMaterialTypes.ORE_SMALL && oreMiningLevel > 0){
                    oreMiningLevel--;
                }
                int stoneMiningLevel = o.getStoneType().getHarvestLevel();
                int maxLevel = Math.max(oreMiningLevel, stoneMiningLevel);
                if (maxLevel > 0){
                    TagKey<Block> tagKey = fromMiningLevel(maxLevel);
                    if (tagKey != null) {
                        this.tag(tagKey).add(o);
                    }
                }
                if (o.getOreType() == GTMaterialTypes.ORE) this.tag(TagUtils.getForgelikeBlockTag("ores")).add(o);
            });
            AntimatterAPI.all(BlockStone.class, s -> {
                if (s.getSuffix().isEmpty()) {
                    this.tag(TagUtils.getForgelikeBlockTag("stone")).add(s);
                } else if (s.getSuffix().equals("cobble")) {
                    this.tag(TagUtils.getForgelikeBlockTag("cobblestone")).add(s);
                } else if (s.getSuffix().contains("bricks")) {
                    this.tag(BlockTags.STONE_BRICKS).add(s);
                }
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(s).replace(replace);
                int stoneMiningLevel = s.getType().getHarvestLevel();
                if (stoneMiningLevel > 0){
                    TagKey<Block> tagKey = fromMiningLevel(stoneMiningLevel);
                    if (tagKey != null) {
                        this.tag(tagKey).add(s);
                    }
                }
                this.tag(getBlockTag(new ResourceLocation(Ref.ID, "blocks/".concat(s.getId())))).add(s).replace(replace);
            });
            AntimatterAPI.all(BlockStoneWall.class, b -> {
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b).replace(replace);
                this.tag(BlockTags.WALLS).add(b);
            });
            AntimatterAPI.all(BlockStoneSlab.class, b -> {
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b).replace(replace);
                this.tag(BlockTags.SLABS).add(b);
            });
            AntimatterAPI.all(BlockStoneStair.class, b -> {
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b).replace(replace);
                this.tag(BlockTags.STAIRS).add(b);
            });
            AntimatterAPI.all(BlockOreStone.class, s -> {
                String id = "ore_stones/" + s.getMaterial().getId();
                this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(s).replace(replace);
                this.tag(TagUtils.getForgelikeBlockTag("ores")).add(s);
                this.tag(getForgelikeBlockTag(id)).add(s);
            });
            AntimatterAPI.all(BlockStorage.class, block -> {
                this.tag(block.getType().getTag()).add(block).replace(replace);
                String name = String.join("", block.getType().getTag().location().getPath(), "/", (block.getType().getId().equals("raw_ore_block") ? "raw_" : ""), block.getMaterial().getId());
                if (block.getMaterial() == GTLibMaterials.Wood){
                    this.tag(GTTools.AXE.getToolType()).add(block);
                } else if (block.getType() == GTMaterialTypes.FRAME){
                    this.tag(GTTools.WRENCH.getToolType()).add(block).replace(replace);
                } else {
                    this.tag(GTTools.PICKAXE.getToolType()).add(block);
                }
                this.tag(getForgelikeBlockTag(name)).add(block);
                // if (block.getType() == FRAME) add climbable tag in 1.16
            });
            AntimatterAPI.all(BlockFrame.class, block -> {
                this.tag(block.getType().getTag()).add(block).replace(replace);
                String name = String.join("", block.getType().getTag().location().getPath(), "/", (block.getType().getId().equals("raw_ore_block") ? "raw_" : ""), block.getMaterial().getId());
                if (block.getMaterial() == GTLibMaterials.Wood){
                    this.tag(GTTools.AXE.getToolType()).add(block);
                } else {
                    this.tag(GTTools.WRENCH.getToolType()).add(block).replace(replace);
                }
                this.tag(getForgelikeBlockTag(name)).add(block);
                // if (block.getType() == FRAME) add climbable tag in 1.16
            });
            AntimatterAPI.all(BlockItemPipe.class, pipe -> {
                this.tag(TagUtils.getBlockTag(new ResourceLocation(Ref.ID, "item_pipe"))).add(pipe);
            });
            AntimatterAPI.all(BlockPipe.class, pipe -> {
                this.tag(pipe.getToolType().getToolType()).add(pipe);
                if (pipe.getType().getMaterial() == GTLibMaterials.Wood){
                    this.tag(GTTools.AXE.getToolType()).add(pipe);
                }
            });
            AntimatterAPI.all(BlockMachine.class, pipe -> {
                this.tag(pipe.getType().getToolTag()).add(pipe);
            });
            AntimatterAPI.all(BlockMultiMachine.class, pipe -> {
                this.tag(pipe.getType().getToolTag()).add(pipe);
            });
            AntimatterAPI.all(AntimatterFluid.class, f -> {
                this.tag(TagUtils.getBlockTag(new ResourceLocation("replaceable"))).add(f.getFluidBlock());
            });
        }
    }

    public TagKey<Block> fromMiningLevel(int miningLevels){
        return switch (miningLevels){
            case 1 -> BlockTags.NEEDS_STONE_TOOL;
            case 2 -> BlockTags.NEEDS_IRON_TOOL;
            case 3 -> BlockTags.NEEDS_DIAMOND_TOOL;
            case 4 -> GTLibTags.NEEDS_NETHERITE_TOOL;
            case 5 -> GTLibTags.NEEDS_ADAMANTIUM_TOOL;
            default -> null;
        };
    }
}
