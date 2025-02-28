package muramasa.antimatter.data;

import muramasa.antimatter.util.TagUtils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

public class ForgeTags {
    public static final TagKey<Item> GEMS_QUARTZ_ALL = tag("gems/quartz_all");

    public static final TagKey<Item> DYES = tag("dyes");
    public static final TagKey<Item> DYES_BLACK = DyeColor.BLACK.getTag();
    public static final TagKey<Item> DYES_RED = DyeColor.RED.getTag();
    public static final TagKey<Item> DYES_GREEN = DyeColor.GREEN.getTag();
    public static final TagKey<Item> DYES_BROWN = DyeColor.BROWN.getTag();
    public static final TagKey<Item> DYES_BLUE = DyeColor.BLUE.getTag();
    public static final TagKey<Item> DYES_PURPLE = DyeColor.PURPLE.getTag();
    public static final TagKey<Item> DYES_CYAN = DyeColor.CYAN.getTag();
    public static final TagKey<Item> DYES_LIGHT_GRAY = DyeColor.LIGHT_GRAY.getTag();
    public static final TagKey<Item> DYES_GRAY = DyeColor.GRAY.getTag();
    public static final TagKey<Item> DYES_PINK = DyeColor.PINK.getTag();
    public static final TagKey<Item> DYES_LIME = DyeColor.LIME.getTag();
    public static final TagKey<Item> DYES_YELLOW = DyeColor.YELLOW.getTag();
    public static final TagKey<Item> DYES_LIGHT_BLUE = DyeColor.LIGHT_BLUE.getTag();
    public static final TagKey<Item> DYES_MAGENTA = DyeColor.MAGENTA.getTag();
    public static final TagKey<Item> DYES_ORANGE = DyeColor.ORANGE.getTag();
    public static final TagKey<Item> DYES_WHITE = DyeColor.WHITE.getTag();

    private static TagKey<Item> tag(String id){
        return TagUtils.getForgelikeItemTag(id);
    }

    public static void init(){
        AntimatterTags.init();
    }
}
