package org.gtreimagined.gtlib.datagen.providers;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.data.GTLibTags;
import org.gtreimagined.gtlib.data.ForgeTags;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.datagen.builder.GTTagBuilder;
import org.gtreimagined.gtlib.item.ItemFluidCell;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialItem;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.SubTag;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.pipe.PipeSize;
import org.gtreimagined.gtlib.pipe.types.Cable;
import org.gtreimagined.gtlib.pipe.types.Wire;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.util.TagUtils;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.data.GTLibMaterials.Quartz;
import static org.gtreimagined.gtlib.material.MaterialTags.*;
import static org.gtreimagined.gtlib.util.TagUtils.*;
import static org.gtreimagined.gtlib.util.Utils.getConventionalMaterialType;
import static org.gtreimagined.gtlib.util.Utils.getConventionalStoneType;

public class GTItemTagProvider extends GTTagProvider<Item> implements IGTLibProvider {
    private final boolean replace;
    private final Function<TagKey<Block>, GTTagBuilder<Block>> blockTags;

    public GTItemTagProvider(String providerDomain, String providerName, boolean replace, GTBlockTagProvider p) {
        super(Registry.ITEM, providerDomain, providerName, "items");
        Objects.requireNonNull(p);
        this.blockTags = p::getOrCreateRawBuilder;
        this.replace = replace;
    }

    private void antimatterTags() {
        this.tag(TagUtils.getForgelikeItemTag("pistons")).add(Items.PISTON, Items.STICKY_PISTON);
        this.tag(ForgeTags.GEMS_QUARTZ_ALL).addTag(GEM.getMaterialTag(Quartz));
        this.tag(TagUtils.getForgelikeItemTag("stone")).add(Items.BASALT);
    }

    protected void processTags(String domain) {
        if (domain.equals(Ref.ID)) antimatterTags();
        TagKey<Block> blockTag = BLOCK.getTag(), frameTag = FRAME.getTag();
        this.copy(TagUtils.getForgelikeBlockTag("ores"), TagUtils.getForgelikeItemTag("ores"));
        this.copy(TagUtils.getForgelikeBlockTag("stone"), TagUtils.getForgelikeItemTag("stone"));
        this.copy(TagUtils.getForgelikeBlockTag("storage_blocks"), TagUtils.getForgelikeItemTag("storage_blocks"));
        this.copy(blockTag, blockToItemTag(blockTag));
        this.copy(frameTag, blockToItemTag(frameTag));
        if (domain.equals(Ref.ID)) {
            GTAPI.all(BlockOre.class, o -> {
                //if (o.getOreType() == ORE_SMALL) return;
                this.tag(getForgelikeItemTag(String.join("", getConventionalStoneType(o.getStoneType()), "_", getConventionalMaterialType(o.getOreType()), "/", o.getMaterial().getId()))).add(o.asItem());
                this.tag(getForgelikeItemTag(String.join("", getConventionalMaterialType(o.getOreType()), "/", o.getMaterial().getId()))).add(o.asItem());
                this.tag(getForgelikeItemTag(getConventionalMaterialType(o.getOreType()))).add(o.asItem());
                this.tag(getForgelikeItemTag(getConventionalStoneType(o.getStoneType()) + "_" + getConventionalMaterialType(o.getOreType()))).add(o.asItem());
            });

            GTAPI.all(BlockStone.class, s -> {
                String id = "blocks/".concat(s.getId());
                if (s.getSuffix().isEmpty()) {
                    this.tag(TagUtils.getForgelikeItemTag("stone")).add(s.asItem());
                } else if (s.getSuffix().equals("cobble")) {
                    this.tag(TagUtils.getForgelikeItemTag("cobblestone")).add(s.asItem());
                } else if (s.getSuffix().contains("bricks")) {
                    this.tag(ItemTags.STONE_BRICKS).add(s.asItem());
                }
                this.copy(getBlockTag(new ResourceLocation(Ref.ID, id)), getItemTag(new ResourceLocation(Ref.ID, id)));
            });
            GTAPI.all(StoneType.class, s -> {
                if (s instanceof CobbleStoneType c){
                    this.tag(ItemTags.STONE_TOOL_MATERIALS).add(c.getBlock("cobble").asItem());
                    this.tag(ItemTags.STONE_CRAFTING_MATERIALS).add(c.getBlock("cobble").asItem());
                }
            });
            GTAPI.all(BlockOreStone.class, domain, s -> {
                String id = "ore_stones/" + s.getMaterial().getId();
                this.copy(getBlockTag(new ResourceLocation(domain, id)), getItemTag(new ResourceLocation(domain, id)));
            });
            GTAPI.all(BlockStorage.class, storage -> {
                MaterialType<?> type = storage.getType();
                String name = String.join("", getConventionalMaterialType(type), "/", (type == RAW_ORE_BLOCK ? "raw_" : ""), storage.getMaterial().getId());
                this.copy(getForgelikeBlockTag(name), getForgelikeItemTag(name));
            });
            GTAPI.all(BlockFrame.class, storage -> {
                MaterialType<?> type = storage.getType();
                String name = String.join("", getConventionalMaterialType(type), "/", storage.getMaterial().getId());
                this.copy(getForgelikeBlockTag(name), getForgelikeItemTag(name));
            });
            GTAPI.all(MaterialItem.class, item -> {
                TagKey<Item> type = item.getType().getTag();
                GTTagBuilder<Item> provider = this.tag(type);
                provider.add(item).replace(replace);
                this.tag(item.getTag()).add(item).replace(replace);
                //if (item.getType() == INGOT || item.getType() == GEM) this.getBuilder(Tags.Items.BEACON_PAYMENT).add(item);
                if (item.getType() == PLATE && item.getMaterial().has(RUBBERTOOLS) && item.getMaterial().has(TOOLS)){
                    this.tag(GTLibTags.PLATE_PLUNGER).add(item);
                }
            });
            GTAPI.all(MaterialType.class, t -> {
                t.getReplacements().forEach((m, i) -> {
                    this.tag(t.getMaterialTag((Material) m)).add(((Supplier<Item>)i).get()).replace(replace);
                    this.tag(t.getTag()).add(((Supplier<Item>)i).get()).replace(replace);
                });
                if (t instanceof MaterialTypeBlock<?> block){
                   block.getOreReplacements().forEach((m, map) -> {
                        map.forEach((s, i) -> {
                            this.tag(block.getMaterialTag(m, s)).add(i.get());
                            this.tag(getForgelikeItemTag(String.join("", getConventionalStoneType(s), "_", getConventionalMaterialType(t)))).add(i.get());
                        });
                   });
                }
            });
            processSubtags();
            GTAPI.all(IGTTool.class, tool -> {
                this.tag(tool.getAntimatterToolType().getTag()).add(tool.getItem()).replace(replace);
                this.tag(tool.getAntimatterToolType().getForgeTag()).add(tool.getItem()).replace(replace);
            });
        }


        this.copy(TagUtils.getBlockTag(new ResourceLocation(Ref.ID, "item_pipe")), TagUtils.getItemTag(new ResourceLocation(Ref.ID, "item_pipe")));
        this.tag(ItemFluidCell.getTag()).add(GTAPI.all(ItemFluidCell.class, domain).toArray(new Item[0]));
    }

    protected void processSubtags() {
        for (PipeSize value : PipeSize.values()) {
            Set<Material> mats = WIRE.allSub(SubTag.COPPER_WIRE);
            if (mats.size() > 0) {
                this.tag(TagUtils.getItemTag(new ResourceLocation(Ref.ID, SubTag.COPPER_WIRE.getId() + "_" + value.getId()))).add(mats.stream().map(t ->
                        GTAPI.get(Wire.class, "wire_" + t.getId())).filter(Objects::nonNull).map(t -> t.getBlockItem(value)).toArray(Item[]::new));
            }
            mats = CABLE.allSub(SubTag.COPPER_CABLE);
            if (mats.size() > 0) {
                this.tag(TagUtils.getItemTag(new ResourceLocation(Ref.ID, SubTag.COPPER_CABLE.getId() + "_" + value.getId()))).add(mats.stream().map(t ->
                        GTAPI.get(Cable.class, "cable_" + t.getId())).filter(Objects::nonNull).map(t -> t.getBlockItem(value)).toArray(Item[]::new));
            }
        }
    }

    protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        GTTagBuilder<Item> builder = this.getOrCreateRawBuilder(itemTag);
        GTTagBuilder<Block> builder2 = this.blockTags.apply(blockTag);
        Stream<Tag.BuilderEntry> stream = builder2.builder.getEntries();
        Objects.requireNonNull(builder);
        stream.forEach(builder::add);
    }
}