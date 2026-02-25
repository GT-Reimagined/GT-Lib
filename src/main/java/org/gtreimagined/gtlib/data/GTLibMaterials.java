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
    public static Material Lava = GTAPI.register(Material.class, new Material(Ref.ID, "lava", 0xff4000, NONE));
    public static Material Water = GTAPI.register(Material.class, new Material(Ref.ID, "water", 0x0000ff, NONE));

    public static void init(){
        Material.init();
    }
}
