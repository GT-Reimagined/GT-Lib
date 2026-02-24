package org.gtreimagined.gtlib.data;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.TextureSet;

import static org.gtreimagined.gtlib.material.Element.*;
import static org.gtreimagined.gtlib.material.TextureSet.*;

public class GTLibMaterials {
    //Vanilla Stone Materials
    public static Material Stone = GTAPI.register(Material.class, new Material(Ref.ID, "stone", 0xcdcdcd, NONE));
    public static Material Granite = GTAPI.register(Material.class, new Material(Ref.ID, "granite", 0xa07882, NONE));
    public static Material Diorite = GTAPI.register(Material.class, new Material(Ref.ID, "diorite", 0xf0f0f0, NONE));
    public static Material Andesite = GTAPI.register(Material.class, new Material(Ref.ID, "andesite", 0xbfbfbf, NONE));
    public static Material Deepslate = GTAPI.register(Material.class, new Material(Ref.ID, "deepslate", 0x1e1414, NONE));
    public static Material Tuff = GTAPI.register(Material.class, new Material(Ref.ID, "tuff", 0x392923, NONE));
    public static Material Dirt = GTAPI.register(Material.class, new Material(Ref.ID, "dirt", 0x976d4d, NONE));
    public static Material Sand = GTAPI.register(Material.class, new Material(Ref.ID, "sand", 0xfafac8, NONE));
    public static Material RedSand = GTAPI.register(Material.class, new Material(Ref.ID, "red_sand", 0xff8438, NONE));
    public static Material Blackstone = GTAPI.register(Material.class, new Material(Ref.ID, "blackstone", 0x2c272d, NONE));
    public static Material Endstone = GTAPI.register(Material.class, new Material(Ref.ID, "endstone", 0xd9de9e, NONE));
    public static Material Netherrack = GTAPI.register(Material.class, new Material(Ref.ID, "netherrack", 0xc80000, NONE));
    public static Material Bedrock = GTAPI.register(Material.class, new Material(Ref.ID, "bedrock", 0x404040, NONE));
    public static Material Prismarine = GTAPI.register(Material.class, new Material(Ref.ID, "prismarine", 0x6eb2a5, NONE));
    public static Material DarkPrismarine = GTAPI.register(Material.class, new Material(Ref.ID, "dark_prismarine", 0x587d6c, NONE));
    public static Material Wood = GTAPI.register(Material.class, new Material(Ref.ID, "wood", 0x643200, TextureSet.WOOD));
    //Vanilla Metal/Gem Materials
    public static Material Iron = GTAPI.register(Material.class, new Material(Ref.ID, "iron", 0xc8c8c8, METALLIC, Fe));
    public static Material Gold = GTAPI.register(Material.class, new Material(Ref.ID, "gold", 0xffe650, SHINY, Au));
    //cause 1.18
    public static Material Copper = GTAPI.register(Material.class, new Material(Ref.ID, "copper", 0xff6400, SHINY, Cu));
    public static Material Glowstone = GTAPI.register(Material.class, new Material(Ref.ID, "glowstone", 0xffff00, SHINY));
    public static Material Sugar = GTAPI.register(Material.class, new Material(Ref.ID, "sugar", 0xfafafa, DULL));
    public static Material Bone = GTAPI.register(Material.class, new Material(Ref.ID, "bone", 0xb3b3b3, DULL));
    public static Material Blaze = GTAPI.register(Material.class, new Material(Ref.ID, "blaze", 0xffc800, NONE)).setMassMultiplierAndDivider(3,2);
    public static Material Flint = GTAPI.register(Material.class, new Material(Ref.ID, "flint", 0x002040, FLINT));
    public static Material Charcoal = GTAPI.register(Material.class, new Material(Ref.ID, "charcoal", 0x644646, LIGNITE));
    public static Material Coal = GTAPI.register(Material.class, new Material(Ref.ID, "coal", 0x464646, LIGNITE)).setMassMultiplierAndDivider(2, 1);
    public static Material Diamond = GTAPI.register(Material.class, new Material(Ref.ID, "diamond", /*0x3de0e5*/0xc8ffff, DIAMOND)).setMassMultiplierAndDivider(64, 1);
    public static Material Emerald = GTAPI.register(Material.class, new Material(Ref.ID, "emerald", 0x50ff50, GEM_V));
    public static Material EnderPearl = GTAPI.register(Material.class, new Material(Ref.ID, "enderpearl", 0x6cdcc8, SHINY));
    public static Material EnderEye = GTAPI.register(Material.class, new Material(Ref.ID, "endereye", 0xa0fae6, SHINY)).setMassMultiplierAndDivider(2, 1);
    public static Material Lapis = GTAPI.register(Material.class, new Material(Ref.ID, "lapis", 0x4646dc, LAPIS));
    public static Material Redstone = GTAPI.register(Material.class, new Material(Ref.ID, "redstone", 0xc80000, REDSTONE));
    public static Material Quartz = GTAPI.register(Material.class, new Material(Ref.ID, "quartz", 0xe6d2d2, NONE));
    public static Material Netherite = GTAPI.register(Material.class, new Material(Ref.ID, "netherite", 0x504650, DULL));
    public static Material NetherizedDiamond = GTAPI.register(Material.class, new Material(Ref.ID, "netherized_diamond", 0x5a505a, DIAMOND));
    public static Material NetheriteScrap = GTAPI.register(Material.class, new Material(Ref.ID, "netherite_scrap", 0x6e505a, ROUGH));
    public static Material Lava = GTAPI.register(Material.class, new Material(Ref.ID, "lava", 0xff4000, NONE));
    public static Material Water = GTAPI.register(Material.class, new Material(Ref.ID, "water", 0x0000ff, NONE));

    public static void init(){
        Material.init();
    }
}
