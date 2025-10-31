package org.gtreimagined.gtlib.data;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.ore.VanillaStoneType;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;

public class VanillaStoneTypes {
    public static StoneType STONE = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "stone", GTLibMaterials.Stone, new Texture("minecraft", "block/stone"), SoundType.STONE, false).setState(Blocks.STONE));
    public static StoneType GRANITE = GTAPI.register(StoneType.class, new VanillaStoneType(Ref.ID, "granite", GTLibMaterials.Granite, "block/stone/", new Texture("minecraft", "block/granite"), SoundType.STONE, false).setState(Blocks.GRANITE));
    public static StoneType DIORITE = GTAPI.register(StoneType.class, new VanillaStoneType(Ref.ID, "diorite", GTLibMaterials.Diorite, "block/stone/", new Texture("minecraft", "block/diorite"), SoundType.STONE, false).setState(Blocks.DIORITE));
    public static StoneType ANDESITE = GTAPI.register(StoneType.class, new VanillaStoneType(Ref.ID, "andesite", GTLibMaterials.Andesite, "block/stone/", new Texture("minecraft", "block/andesite"), SoundType.STONE, false).setState(Blocks.ANDESITE));
    public static StoneType DEEPSLATE = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "deepslate", GTLibMaterials.Deepslate, new Texture("minecraft", "block/deepslate"), SoundType.STONE, false).setState(Blocks.DEEPSLATE));
    public static StoneType TUFF = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "tuff", GTLibMaterials.Tuff, new Texture("minecraft", "block/tuff"), SoundType.STONE, false).setState(Blocks.TUFF));
    public static StoneType GRAVEL = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "gravel", GTLibMaterials.Stone, new Texture("minecraft", "block/gravel"), SoundType.GRAVEL, false).setState(Blocks.GRAVEL)).setHardnessAndResistance(0.6F).setSandLike(true);
    public static StoneType DIRT = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "dirt", GTLibMaterials.Dirt, new Texture("minecraft", "block/dirt"), SoundType.GRAVEL, false).setState(Blocks.DIRT).setHardnessAndResistance(0.5F).setRequiresTool(false).setType(BlockTags.MINEABLE_WITH_SHOVEL).setGenerateOre(false));
    public static StoneType SAND = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "sand", GTLibMaterials.Sand, new Texture("minecraft", "block/sand"), SoundType.SAND, false).setState(Blocks.SAND)).setSandLike(true);
    public static StoneType SAND_RED = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "red_sand", GTLibMaterials.RedSand, new Texture("minecraft", "block/red_sand"), SoundType.SAND, false).setStateSupplier(Blocks.RED_SAND::defaultBlockState)).setSandLike(true);
    public static StoneType SANDSTONE = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "sandstone", GTLibMaterials.Sand, new Texture("minecraft", "block/sandstone"), SoundType.STONE, false).setState(Blocks.SANDSTONE)
            .setTextures(new Texture("minecraft", "block/sandstone_bottom"), new Texture("minecraft", "block/sandstone_top"), new Texture("minecraft", "block/sandstone"), new Texture("minecraft", "block/sandstone"), new Texture("minecraft", "block/sandstone"), new Texture("minecraft", "block/sandstone")));
    public static StoneType BLACKSTONE = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "blackstone", GTLibMaterials.Blackstone, new Texture("minecraft", "block/blackstone"), SoundType.STONE, false).setState(Blocks.BLACKSTONE));
    public static StoneType NETHERRACK = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "netherrack", GTLibMaterials.Netherrack, new Texture("minecraft", "block/netherrack"), SoundType.NETHERRACK, false).setState(Blocks.NETHERRACK).setHardnessAndResistance(0.4F));
    public static StoneType ENDSTONE = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "endstone", GTLibMaterials.Endstone, new Texture("minecraft", "block/end_stone"), SoundType.STONE, false).setState(Blocks.END_STONE).setHardnessAndResistance(3.0F, 9.0F));
    public static StoneType BEDROCK = GTAPI.register(StoneType.class, new StoneType(Ref.ID, "bedrock", Material.NULL, new Texture("minecraft", "block/bedrock"), SoundType.STONE, false)).setState(Blocks.BEDROCK).setHardnessAndResistance(-1.0f, 3600000.0F);

    public static void init(){}
}
