package org.gtreimagined.gtlib.datagen.loaders;

import com.google.common.collect.ImmutableMap;
import net.minecraft.data.recipes.RecipeCategory;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.ForgeTags;
import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.ore.VanillaStoneType;
import org.gtreimagined.gtlib.util.TagUtils;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.of;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.data.GTLibMaterials.*;

public class StoneRecipes {
    public static void loadRecipes(Consumer<FinishedRecipe> output, GTRecipeProvider provider){
        provider.addItemRecipe(output, "minecraft", "", "furnaces", Items.FURNACE,
                ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS), "CCC", "C C", "CCC");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.DROPPER,
                ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS, 'R', DUST.getMaterialTag(Redstone)), "CCC", "C C", "CRC");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.OBSERVER,
                ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS, 'R', DUST.getMaterialTag(Redstone), 'Q', ForgeTags.GEMS_QUARTZ_ALL), "CCC", "RRQ", "CCC");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.LEVER,
                ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS, 'R', ROD.getMaterialTag(Wood)), "R", "C");
        //provider.addItemRecipe(output, "minecraft", "", "redstone", Items.PISTON,
        //        ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS, 'R', DUST.getMaterialTag(Redstone), 'I', INGOT.getMaterialTag(Iron), 'W', ItemTags.PLANKS), "WWW", "CIC", "CRC");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.DISPENSER,
                ImmutableMap.of('C', ItemTags.STONE_CRAFTING_MATERIALS, 'R', DUST.getMaterialTag(Redstone), 'B', Items.BOW), "CCC", "CBC", "CRC");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.REPEATER,
                of('T', Items.REDSTONE_TORCH, 'R', DUST.getMaterialTag(Redstone), 'S', TagUtils.getForgelikeItemTag("stone")), "TRT", "SSS");
        provider.addItemRecipe(output, "minecraft", "", "redstone", Items.COMPARATOR,
                of('T', Items.REDSTONE_TORCH, 'Q', ForgeTags.GEMS_QUARTZ_ALL, 'S', TagUtils.getForgelikeItemTag("stone")), " T ", "TQT", "SSS");
        provider.addItemRecipe(output, "stones", Items.SAND, of('S', DUST.getMaterialTag(Sand)), "SS", "SS");
        provider.addItemRecipe(output, "stones", Items.RED_SAND, of('S', DUST.getMaterialTag(RedSand)), "SS", "SS");
        if (GTAPI.isModLoaded(Ref.MOD_AE)){
            provider.removeRecipe(new ResourceLocation(Ref.MOD_AE, "misc/vanilla_comparator"));
        }
        GTAPI.all(StoneType.class).forEach(s -> {
            Material m = s.getMaterial();
            if (s instanceof CobbleStoneType c){
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(c.getBlock("cobble")), RecipeCategory.BUILDING_BLOCKS, c.getBlock(""), 0.1F, 200).unlockedBy("has_cobble", provider.hasSafeItem(c.getBlock("cobble"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_stone"));
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(c.getBlock("bricks")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_cracked"), 0.1F, 200).unlockedBy("has_bricks", provider.hasSafeItem(c.getBlock("bricks"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_cracked"));
                String[] types = new String[]{"bricks_mossy", "cobble_mossy", "bricks", "cobble", "smooth", ""};
                for (String type : types){
                    String i = type.isEmpty() ? "" : "_";
                    provider.addStackRecipe(output, Ref.ID, "slab_" + m.getId() + i + type, "slabs", new ItemStack(c.getBlock(type + i + "slab"), 6), of('S', c.getBlock(type)), "SSS");
                    provider.addStackRecipe(output, Ref.ID, "stairs_" + m.getId() + i + type, "stairs", new ItemStack(c.getBlock(type + i + "stairs"), 4), of('S', c.getBlock(type)), "S  ", "SS ", "SSS");
                    provider.addStackRecipe(output, Ref.ID, "wall_" + m.getId() + i + type, "walls", new ItemStack(c.getBlock(type + i + "wall"), 6), of('S', c.getBlock(type)), "SSS", "SSS");
                    String[] pattern = type.equals("bricks") ? new String[]{"SS"} : new String[]{"S", "S"};
                    provider.addStackRecipe(output, Ref.ID, m.getId() + i + type + "_from_slabs", "slabs", new ItemStack(c.getBlock(type), 1), of('S', c.getBlock(type + i + "slab")), pattern);
                }
                provider.addStackRecipe(output, Ref.ID, "bricks_" + m.getId(), "bricks", new ItemStack(c.getBlock("bricks"), 4), of('S', c.getBlock("")), "SS", "SS");
                provider.addStackRecipe(output, Ref.ID, "polished_" + m.getId(), "bricks", new ItemStack(c.getBlock("smooth"), 2), of('S', c.getBlock("")), "SS");
                provider.addStackRecipe(output, Ref.ID, "bricks_chiseled_" + m.getId(), "bricks", new ItemStack(c.getBlock("bricks_chiseled"), 1), of('S', c.getBlock("bricks_slab")), "S", "S");
                provider.shapeless(output, "bricks_mossy_" + m.getId(), "bricks", new ItemStack(c.getBlock("bricks_mossy")), c.getBlock("bricks"), Items.VINE);
                provider.shapeless(output, "cobble_mossy_" + m.getId(), "bricks", new ItemStack(c.getBlock("cobble_mossy")), c.getBlock("cobble"), Items.VINE);
                types = new String[]{"stairs", "slab", "wall", "bricks_slab", "bricks_stairs", "bricks_chiseled", "bricks_wall", "bricks", "smooth", "smooth_slab", "smooth_stairs", "smooth_wall"};
                for (String type : types){
                    if (s instanceof VanillaStoneType && !type.contains("bricks") && !type.equals("smooth_wall") && (type.contains("slab") || type.contains("stairs") || type.contains("wall"))){
                        continue;
                    }
                    int amount = type.contains("slab") ? 2 : 1;
                    SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("")), RecipeCategory.BUILDING_BLOCKS, c.getBlock(type), amount).unlockedBy("has_stone", provider.hasSafeItem(c.getBlock(""))).save(output, new ResourceLocation(Ref.ID,m.getId() + "_stone_" + type));
                }
                for (String type : CobbleStoneType.SUFFIXES){
                    String id = (type.isEmpty() ? c.getId() : c.getId() + "_" + type) + "_cover";
                    Item cover = GTAPI.get(Item.class, id, Ref.SHARED_ID);
                    if (cover == null) continue;
                    SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock(type)), RecipeCategory.DECORATIONS, cover, 4).unlockedBy("has_stone", provider.hasSafeItem(c.getBlock(type))).save(output, new ResourceLocation(Ref.ID, id));
                    if (type.equals("bricks") || type.equals("smooth") || type.equals("bricks_chiseled")) {
                        Item coverStone = GTAPI.get(Item.class, c.getId() + "_cover", Ref.SHARED_ID);
                        if (coverStone == null) continue;
                        SingleItemRecipeBuilder.stonecutting(Ingredient.of(coverStone), RecipeCategory.DECORATIONS, cover, 1).unlockedBy("has_stone", provider.hasSafeItem(c.getBlock(""))).save(output, new ResourceLocation(Ref.ID, id + "_from_regular_cover"));
                    }
                }
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_slab"), 2).unlockedBy("has_cobble", provider.hasSafeItem(c.getBlock("cobble"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_slab"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_stairs")).unlockedBy("has_cobble", provider.hasSafeItem(c.getBlock("cobble"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_stairs"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_wall")).unlockedBy("has_cobble", provider.hasSafeItem(c.getBlock("cobble"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_wall"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_mossy_slab"), 2).unlockedBy("has_cobble_mossy", provider.hasSafeItem(c.getBlock("cobble_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_mossy_slab"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_mossy_stairs")).unlockedBy("has_cobble_mossy", provider.hasSafeItem(c.getBlock("cobble_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_mossy_stairs"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("cobble_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("cobble_mossy_wall")).unlockedBy("has_cobble_mossy", provider.hasSafeItem(c.getBlock("cobble_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_cobble_mossy_wall"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_slab"), 2).unlockedBy("has_bricks", provider.hasSafeItem(c.getBlock("bricks"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_slab2"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_stairs")).unlockedBy("has_bricks", provider.hasSafeItem(c.getBlock("bricks"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_stairs2"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_wall")).unlockedBy("has_bricks", provider.hasSafeItem(c.getBlock("bricks"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_wall2"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_chiseled")).unlockedBy("has_bricks", provider.hasSafeItem(c.getBlock("bricks"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_chiseled2"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_mossy_slab"), 2).unlockedBy("has_bricks_mossy", provider.hasSafeItem(c.getBlock("bricks_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_mossy_slab"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_mossy_stairs")).unlockedBy("has_bricks_mossy", provider.hasSafeItem(c.getBlock("bricks_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_mossy_stairs"));
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("bricks_mossy")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("bricks_mossy_wall")).unlockedBy("has_bricks_mossy", provider.hasSafeItem(c.getBlock("bricks_mossy"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_bricks_mossy_wall"));
                if (!(s instanceof VanillaStoneType)) {
                    SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("smooth")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("smooth_slab"), 2).unlockedBy("has_smooth", provider.hasSafeItem(c.getBlock("smooth"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_smooth_slab"));
                    SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("smooth")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("smooth_stairs")).unlockedBy("has_smooth", provider.hasSafeItem(c.getBlock("smooth"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_smooth_stairs"));
                }
                SingleItemRecipeBuilder.stonecutting(Ingredient.of(c.getBlock("smooth")), RecipeCategory.BUILDING_BLOCKS, c.getBlock("smooth_wall")).unlockedBy("has_smooth", provider.hasSafeItem(c.getBlock("smooth"))).save(output, new ResourceLocation(Ref.ID, m.getId() + "_smooth_wall"));
            }
        });
    }
}
