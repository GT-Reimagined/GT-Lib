package org.gtreimagined.gtlib.data;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.block.BlockSurfaceRock;
import org.gtreimagined.gtlib.cover.CoverFactory;
import org.gtreimagined.gtlib.cover.CoverPlate;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.fluid.GTMaterialFluid;
import org.gtreimagined.gtlib.item.CoverMaterialItem;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialItem;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeFluid;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.gtreimagined.gtlib.Ref.*;
import static org.gtreimagined.gtlib.data.GTLibMaterials.Wood;
import static org.gtreimagined.gtlib.material.MaterialTags.RUBBERTOOLS;

public class GTMaterialTypes {
    //Item Types
    public static MaterialTypeItem<?> DUST = new MaterialTypeItem<>("dust", 2, true, U);
    public static MaterialTypeItem<?> SMALL_DUST = new MaterialTypeItem<>("small_dust", 2, true, Ref.U4);
    public static MaterialTypeItem<?> TINY_DUST = new MaterialTypeItem<>("tiny_dust", 2, true, Ref.U9);
    public static MaterialTypeItem<?> IMPURE_DUST = new MaterialTypeItem<>("impure_dust", 2, true, -1);
    public static MaterialTypeItem<?> PURE_DUST = new MaterialTypeItem<>("pure_dust", 2, true, -1);
    public static MaterialTypeItem<?> CRUSHED_ORE = new MaterialTypeItem<>("crushed_ore", 2, true, -1);
    public static MaterialTypeItem<?> PURIFIED_ORE = new MaterialTypeItem<>("purified_ore", 2, true, -1);
    public static MaterialTypeItem<?> REFINED_ORE = new MaterialTypeItem<>("refined_ore", 2, true, -1);
    public static MaterialTypeItem<?> CHIPPED_GEM = new MaterialTypeItem<>("chipped_gem", 2, true, U4);
    public static MaterialTypeItem<?> FLAWED_GEM = new MaterialTypeItem<>("flawed_gem", 2, true, U2);
    public static MaterialTypeItem<?> GEM = new MaterialTypeItem<>("gem", 2, true, U);
    public static MaterialTypeItem<?> FLAWLESS_GEM = new MaterialTypeItem<>("flawless_gem", 2, true, U * 2);
    public static MaterialTypeItem<?> EXQUISITE_GEM = new MaterialTypeItem<>("exquisite_gem", 2, true, U * 4);
    public static MaterialTypeItem<?> NUGGET = new MaterialTypeItem<>("nugget", 2, true, Ref.U9);
    public static MaterialTypeItem<?> CHUNK = new MaterialTypeItem<>("chunk", 2, true, U4);
    public static MaterialTypeItem<?> INGOT = new MaterialTypeItem<>("ingot", 2, true, U);
    public static MaterialTypeItem<?> HOT_INGOT = new MaterialTypeItem<>("hot_ingot", 2, true, U);
    public static MaterialTypeItem<?> TINY_PLATE = new MaterialTypeItem<>("tiny_plate", 2, true, U9);
    public static MaterialTypeItem<?> PLATE = new MaterialTypeItem<>("plate", 2, true, U, (a, b, c) -> CoverFactory.builder((u, v, t, w) -> new CoverPlate(u, v, t, w, b, c)).item((u, v) -> new CoverMaterialItem(u.getDomain(), b, u, c)).build(Ref.ID, "plate_" + c.getId()));
    public static MaterialTypeItem<?> DENSE_PLATE = new MaterialTypeItem<>("dense_plate", 2, true, U * 9);
    public static MaterialTypeItem<MaterialTypeBlock.IOreGetter> BEARING_ROCK = new MaterialTypeItem<>("bearing_rock", 2, false, Ref.U4, (domain, type, mat) -> {
        GTAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).forEach(s -> GTAPI.register(BlockSurfaceRock.class, new BlockSurfaceRock(domain, mat, s)));
        new MaterialItem(domain, type, mat);
    });
    public static MaterialTypeItem<MaterialTypeBlock.IBlockGetter> ROCK = new MaterialTypeItem<>("rock", 2, false, Ref.U4, (domain, type, mat) -> {
        StoneType type1 = GTAPI.get(StoneType.class, mat.getId());
        if (type1 != null){
            GTAPI.register(BlockSurfaceRock.class, new BlockSurfaceRock(domain, Material.NULL, type1));
        }
        new MaterialItem(domain, type, mat);
    });
    public static MaterialTypeItem<?> RAW_ORE = new MaterialTypeItem<>("raw_ore", 2, true, -1);
    public static MaterialTypeItem<?> SMALL_GEAR = new MaterialTypeItem<>("small_gear", 2, true, U);
    public static MaterialTypeItem<?> GEAR = new MaterialTypeItem<>("gear", 2, true, U * 4);
    public static MaterialTypeItem<?> ROTOR = new MaterialTypeItem<>("rotor", 2, true, (U * 4) + Ref.U4);
    public static MaterialTypeItem<?> ROD = new MaterialTypeItem<>("rod", 2, true, U2);
    public static MaterialTypeItem<?> LONG_ROD = new MaterialTypeItem<>("long_rod", 2, true, U);
    public static MaterialTypeItem<?> SPRING = new MaterialTypeItem<>("spring", 2, true, U);
    public static MaterialTypeItem<?> LENS = new MaterialTypeItem<>("lens", 2, true, U * 3 / 4);
    public static MaterialTypeItem<?> BOLT = new MaterialTypeItem<>("bolt", 2, true, U8);
    public static MaterialTypeItem<?> SCREW = new MaterialTypeItem<>("screw", 2, true, Ref.U8);
    public static MaterialTypeItem<?> RING = new MaterialTypeItem<>("ring", 2, true, Ref.U4);
    public static MaterialTypeItem<?> FOIL = new MaterialTypeItem<>("foil", 2, true, U4);
    public static MaterialTypeItem<?> ITEM_CASING = new MaterialTypeItem<>("item_casing", 1, true, U2);
    public static MaterialTypeItem<?> FINE_WIRE = new MaterialTypeItem<>("fine_wire", 2, true, U8);
    public static final MaterialTypeItem<?> SWORD_BLADE = new MaterialTypeItem<>("sword_blade", 2, true, U * 2);
    public static final MaterialTypeItem<?> PICKAXE_HEAD = new MaterialTypeItem<>("pickaxe_head", 2, true, U * 3);
    public static final MaterialTypeItem<?> SHOVEL_HEAD = new MaterialTypeItem<>("shovel_head", 2, true, U);
    public static final MaterialTypeItem<?> AXE_HEAD = new MaterialTypeItem<>("axe_head", 2, true, U * 3);
    public static final MaterialTypeItem<?> HOE_HEAD = new MaterialTypeItem<>("hoe_head", 2, true, U * 2);
    public static final MaterialTypeItem<?> HAMMER_HEAD = new MaterialTypeItem<>("hammer_head", 2, true, U * 6);
    public static final MaterialTypeItem<?> FILE_HEAD = new MaterialTypeItem<>("file_head", 2, true, U * 2);
    public static final MaterialTypeItem<?> SAW_BLADE = new MaterialTypeItem<>("saw_blade", 2, true, U * 2);
    public static MaterialTypeItem<?> DRILL_BIT = new MaterialTypeItem<>("drill_bit", 2, true, U * 4);
    public static MaterialTypeItem<?> CHAINSAW_BIT = new MaterialTypeItem<>("chainsaw_bit", 2, true, U * 2);
    public static MaterialTypeItem<?> WRENCH_BIT = new MaterialTypeItem<>("wrench_bit", 2, true, U * 4);
    public static final MaterialTypeItem<?> SCREWDRIVER_TIP = new MaterialTypeItem<>("screwdriver_tip", 2, true, U);
    public static final MaterialTypeItem<?> SCYTHE_BLADE = new MaterialTypeItem<>("scythe_blade", 2, true, U * 3);
    public static MaterialTypeItem<?> BUZZSAW_BLADE = new MaterialTypeItem<>("buzzsaw_blade", 2, true, U * 4);
    //Block Types
    public static MaterialTypeBlock<MaterialTypeBlock.IOreGetter> ORE = new MaterialTypeBlock<>("ore", 1, true, -1, (domain, type, mat) -> GTAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).filter(s -> !GTAPI.hasReplacement(((MaterialTypeBlock<?>)type).getMaterialTag(mat, s))).forEach(s -> new BlockOre(domain, mat, s, type)));
    public static MaterialTypeBlock<MaterialTypeBlock.IOreGetter> SMALL_ORE = new MaterialTypeBlock<>("small_ore", 1, false, -1, (domain, type, mat) -> GTAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).filter(s -> !GTAPI.hasReplacement(((MaterialTypeBlock<?>)type).getMaterialTag(mat, s))).forEach(s -> new BlockOre(domain, mat, s, type)));
    public static MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> ORE_STONE = new MaterialTypeBlock<>("dense_ore", 1, true, -1,(domain, type, mat) -> new BlockOreStone(domain, mat));
    public static MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> BLOCK = new MaterialTypeBlock<>("block", 1, false, U * 9, BlockStorage::new);
    public static MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> RAW_ORE_BLOCK = new MaterialTypeBlock<>("raw_ore_block", 2, false, -1, BlockStorage::new);
    public static MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> FRAME = new MaterialTypeBlock<>("frame", 1, true, U * 2, BlockFrame::new);
    //Fluid Types
    public static MaterialTypeFluid<MaterialTypeFluid.IFluidGetter> LIQUID = new MaterialTypeFluid<>("liquid", 1, true, -1);
    public static MaterialTypeFluid<MaterialTypeFluid.IFluidGetter> GAS = new MaterialTypeFluid<>("gas", 1, true, -1);

    public static final BiFunction<MaterialType<?>, Material, String> UNSPLIT_FUNCTION = (t, m) -> m.getDisplayNameString() + " " + Utils.lowerUnderscoreToUpperSpaced(t.getId());

    static {
        BEARING_ROCK.set((m, s) -> {
            if (m == null || s == null || !s.doesGenerateOre() || !BEARING_ROCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(BEARING_ROCK, m, s);
            BlockSurfaceRock rock = GTAPI.get(BlockSurfaceRock.class, s.getId() + "_" + m.getId() + "_surface_rock");
            return new MaterialTypeBlock.Container(rock != null ? rock.defaultBlockState() : Blocks.AIR.defaultBlockState());
        });
        ROCK.set((m) -> {
            StoneType s = GTAPI.get(StoneType.class, m.getId());
            if (s == null || !ROCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ROCK, m, s);
            BlockSurfaceRock rock = GTAPI.get(BlockSurfaceRock.class, s.getId() + "_surface_rock");
            return new MaterialTypeBlock.Container(rock != null ? rock.defaultBlockState() : Blocks.AIR.defaultBlockState());
        });
        ORE.set((m, s) -> {
            if (m != null && s != null) {
                Item item = GTAPI.getReplacement(ORE, m, s);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || s == null || !s.doesGenerateOre() || !ORE.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ORE, m, s);
            BlockOre block = GTAPI.get(BlockOre.class, s.getId() + "_" + ORE.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        SMALL_ORE.set((m, s) -> {
            if (m != null && s != null) {
                Item item = GTAPI.getReplacement(SMALL_ORE, m, s);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || s == null || !SMALL_ORE.allowGen(m))
                return MaterialTypeBlock.getEmptyBlockAndLog(SMALL_ORE, m, s);
            BlockOre block = GTAPI.get(BlockOre.class, s.getId() + "_" + SMALL_ORE.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        ORE_STONE.set(m -> {
            if (m == null || !ORE_STONE.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ORE_STONE, m);
            BlockOreStone block = GTAPI.get(BlockOreStone.class, ORE_STONE.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        BLOCK.set(m -> {
            if (m != null) {
                Item item = GTAPI.getReplacement(BLOCK, m);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || !BLOCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(BLOCK, m);
            BlockStorage block = GTAPI.get(BlockStorage.class, BLOCK.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        RAW_ORE_BLOCK.set(m -> {
            if (m != null) {
                Item item = GTAPI.getReplacement(RAW_ORE_BLOCK, m);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || !RAW_ORE_BLOCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(RAW_ORE_BLOCK, m);
            BlockStorage block = GTAPI.get(BlockStorage.class, RAW_ORE_BLOCK.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        FRAME.set(m -> {
            if (m == null || !FRAME.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(FRAME, m);
            BlockFrame block = GTAPI.get(BlockFrame.class, FRAME.getIdGetter().apply(m));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();

        LIQUID.set((m, i) -> {
            if (m == null || !LIQUID.allowGen(m)) return MaterialTypeFluid.getEmptyFluidAndLog(LIQUID, m);
            if (m.getId().equals("water")) return new FluidStack(Fluids.WATER, i);
            else if (m.getId().equals("lava")) return new FluidStack(Fluids.LAVA, i);
            GTFluid fluid = GTAPI.get(GTFluid.class, LIQUID.getId() + "_" + m.getId());
            if (fluid == null) throw new IllegalStateException("Tried to get null fluid");
            return new FluidStack(fluid.getFluid(), i);
        });
        GAS.set((m, i) -> {
            if (m == null || !GAS.allowGen(m)) return MaterialTypeFluid.getEmptyFluidAndLog(GAS, m);
            GTFluid fluid = GTAPI.get(GTFluid.class, GAS.getId() + "_" + m.getId());
            if (fluid == null) throw new IllegalStateException("Tried to get null fluid");
            return new FluidStack(fluid.getFluid(), i);
        });
    }

    public static void init() {
        replacements();
        dependents();
        defaults();
    }

    private static void defaults(){
        Function<Material, String> d = m -> m.has(RUBBERTOOLS) ? "Pulp" : "Dust";
        DUST.setLang(m -> m.getDisplayNameString() + " " + d.apply(m));
        TINY_DUST.setLang(m -> "Tiny " + m.getDisplayNameString() +  " " + d.apply(m)).setHidden();
        SMALL_DUST.setLang(m -> "Small " + m.getDisplayNameString() + " " + d.apply(m)).setHidden();
        IMPURE_DUST.setLang(m -> "Impure " + m.getDisplayNameString() +  " " + d.apply(m));
        PURE_DUST.setLang(m -> "Pure " + m.getDisplayNameString() + " " + d.apply(m));
        Function<Material, String> n = m -> m.getElement() != null ? "Native " : "";
        BEARING_ROCK.unSplitName().setLang(m -> n.apply(m) + m.getDisplayNameString() + " Bearing Rock").setIgnoreTextureSets();
        ROCK.unSplitName().setIgnoreTextureSets();
        CRUSHED_ORE.setLang(m -> "Crushed " + n.apply(m) + m.getDisplayNameString() + " Ore");
        PURIFIED_ORE.setLang(m -> "Purified " + n.apply(m) + m.getDisplayNameString() + " Ore");
        REFINED_ORE.setLang(m -> "Refined " + n.apply(m) + m.getDisplayNameString() + " Ore");
        RAW_ORE.unSplitName().setLang(m -> "Raw " + n.apply(m) + m.getDisplayNameString()).setIdGetter(m -> "raw_" + m.getId()).setTagPrefix("raw_materials");
        INGOT.setLang(m -> m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Bar" : "Ingot"));
        NUGGET.setLang(m -> m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Chip" : "Nugget"));
        GEM.setLang(Material::getDisplayNameString);
        EXQUISITE_GEM.setLang(m -> "Exquisite " + m.getDisplayNameString());
        FLAWLESS_GEM.setLang(m -> "Flawless " + m.getDisplayNameString());
        FLAWED_GEM.setLang(m -> "Flawed " + m.getDisplayNameString());
        CHIPPED_GEM.setLang(m -> "Chipped " + m.getDisplayNameString());
        Function<Material, String> p = m -> m == Wood ? "Plank" : m.has(RUBBERTOOLS) ? "Sheet" : "Plate";
        PLATE.setLang(m -> m.getDisplayNameString() + " " + p.apply(m));
        DENSE_PLATE.setLang(m -> "Dense " + m.getDisplayNameString() + " " + p.apply(m));
        TINY_PLATE.setLang(m -> "Tiny " + m.getDisplayNameString() + " " + p.apply(m));
        ITEM_CASING.unSplitName().setLang(m -> m.getDisplayNameString() + " Item Casings");
        FOIL.setLang(m -> (m.has(RUBBERTOOLS) ? "Thin " : "") + m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Sheet" : "Foil"));
        FINE_WIRE.setIgnoreTextureSets();
        DRILL_BIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        CHAINSAW_BIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        WRENCH_BIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        BUZZSAW_BLADE.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        PICKAXE_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        SHOVEL_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        SWORD_BLADE.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        AXE_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        HOE_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        HAMMER_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        FILE_HEAD.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        SAW_BLADE.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        SCREWDRIVER_TIP.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        SCYTHE_BLADE.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        RAW_ORE_BLOCK.unSplitName().setLang(m -> "Block of Raw " + n.apply(m) + m.getDisplayNameString()).setIdGetter(m -> "raw_" + m.getId() + "_block").setTagPrefix("storage_blocks");
        BLOCK.setLang(m -> "Block of " + m.getDisplayNameString()).setTagPrefix("storage_blocks");
    }

    private static void replacements(){
        NUGGET.replacement(GTLibMaterials.Iron, () -> Items.IRON_NUGGET);
        NUGGET.replacement(GTLibMaterials.Gold, () -> Items.GOLD_NUGGET);
        INGOT.replacement(GTLibMaterials.Iron, () -> Items.IRON_INGOT);
        INGOT.replacement(GTLibMaterials.Gold, () -> Items.GOLD_INGOT);
        INGOT.replacement(GTLibMaterials.Netherite, () -> Items.NETHERITE_INGOT);
        INGOT.replacement(GTLibMaterials.Copper, () -> Items.COPPER_INGOT);
        INGOT.replacement(GTLibMaterials.NetheriteScrap, () -> Items.NETHERITE_SCRAP);


        DUST.replacement(GTLibMaterials.Redstone, () -> Items.REDSTONE);
        DUST.replacement(GTLibMaterials.Glowstone, () -> Items.GLOWSTONE_DUST);
        DUST.replacement(GTLibMaterials.Blaze, () -> Items.BLAZE_POWDER);
        DUST.replacement(GTLibMaterials.Sugar, () -> Items.SUGAR);
        RAW_ORE.replacement(GTLibMaterials.Iron, () -> Items.RAW_IRON);
        RAW_ORE.replacement(GTLibMaterials.Copper, () -> Items.RAW_COPPER);
        RAW_ORE.replacement(GTLibMaterials.Gold, () -> Items.RAW_GOLD);
        GEM.replacement(GTLibMaterials.Flint, () -> Items.FLINT);
        GEM.replacement(GTLibMaterials.Diamond, () -> Items.DIAMOND);
        GEM.replacement(GTLibMaterials.Emerald, () -> Items.EMERALD);
        GEM.replacement(GTLibMaterials.Lapis, () -> Items.LAPIS_LAZULI);
        GEM.replacement(GTLibMaterials.Quartz, () -> Items.QUARTZ);
        GEM.replacement(GTLibMaterials.Coal, () -> Items.COAL);
        GEM.replacement(GTLibMaterials.Charcoal, () -> Items.CHARCOAL);
        GEM.replacement(GTLibMaterials.EnderEye, () -> Items.ENDER_EYE);
        GEM.replacement(GTLibMaterials.EnderPearl, () -> Items.ENDER_PEARL);

        ROD.replacement(GTLibMaterials.Blaze, () -> Items.BLAZE_ROD);
        ROD.replacement(GTLibMaterials.Bone, () -> Items.BONE);
        ROD.replacement(Wood, () -> Items.STICK);

        BLOCK.replacement(GTLibMaterials.Coal, () -> Items.COAL_BLOCK);
        BLOCK.replacement(GTLibMaterials.Iron, () -> Items.IRON_BLOCK);
        BLOCK.replacement(GTLibMaterials.Copper, () -> Items.COPPER_BLOCK);
        BLOCK.replacement(GTLibMaterials.Gold, () -> Items.GOLD_BLOCK);
        BLOCK.replacement(GTLibMaterials.Diamond, () -> Items.DIAMOND_BLOCK);
        BLOCK.replacement(GTLibMaterials.Emerald, () -> Items.EMERALD_BLOCK);
        BLOCK.replacement(GTLibMaterials.Lapis, () -> Items.LAPIS_BLOCK);
        BLOCK.replacement(GTLibMaterials.Netherite, () -> Items.NETHERITE_BLOCK);
        RAW_ORE_BLOCK.replacement(GTLibMaterials.Iron, () -> Items.RAW_IRON_BLOCK);
        RAW_ORE_BLOCK.replacement(GTLibMaterials.Copper, () -> Items.RAW_COPPER_BLOCK);
        RAW_ORE_BLOCK.replacement(GTLibMaterials.Gold, () -> Items.RAW_GOLD_BLOCK);
        ORE.replacement(GTLibMaterials.Coal, VanillaStoneTypes.STONE, () -> Items.COAL_ORE);
        ORE.replacement(GTLibMaterials.Coal, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_COAL_ORE);
        ORE.replacement(GTLibMaterials.Iron, VanillaStoneTypes.STONE, () -> Items.IRON_ORE);
        ORE.replacement(GTLibMaterials.Iron, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_IRON_ORE);
        ORE.replacement(GTLibMaterials.Copper, VanillaStoneTypes.STONE, () -> Items.COPPER_ORE);
        ORE.replacement(GTLibMaterials.Copper, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_COPPER_ORE);
        ORE.replacement(GTLibMaterials.Gold, VanillaStoneTypes.STONE, () -> Items.GOLD_ORE);
        ORE.replacement(GTLibMaterials.Gold, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_GOLD_ORE);
        ORE.replacement(GTLibMaterials.Redstone, VanillaStoneTypes.STONE, () -> Items.REDSTONE_ORE);
        ORE.replacement(GTLibMaterials.Redstone, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_REDSTONE_ORE);
        ORE.replacement(GTLibMaterials.Emerald, VanillaStoneTypes.STONE, () -> Items.EMERALD_ORE);
        ORE.replacement(GTLibMaterials.Emerald, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_EMERALD_ORE);
        ORE.replacement(GTLibMaterials.Lapis, VanillaStoneTypes.STONE, () -> Items.LAPIS_ORE);
        ORE.replacement(GTLibMaterials.Lapis, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_LAPIS_ORE);
        ORE.replacement(GTLibMaterials.Diamond, VanillaStoneTypes.STONE, () -> Items.DIAMOND_ORE);
        ORE.replacement(GTLibMaterials.Diamond, VanillaStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_DIAMOND_ORE);
        ORE.replacement(GTLibMaterials.Quartz, VanillaStoneTypes.NETHERRACK, () -> Items.NETHER_QUARTZ_ORE);
    }

    private static void dependents() {
        ROTOR.dependents(PLATE, SCREW, RING);
        SCREW.dependents(BOLT);
        BOLT.dependents(ROD);
        RING.dependents(ROD);
        LONG_ROD.dependents(ROD);
        CRUSHED_ORE.dependents(PURIFIED_ORE, REFINED_ORE, IMPURE_DUST);
        PURE_DUST.dependents(DUST);
        IMPURE_DUST.dependents(PURE_DUST);
        DUST.dependents(SMALL_DUST, TINY_DUST);
        SMALL_GEAR.dependents(PLATE);
        GEAR.dependents(PLATE, ROD);
        EXQUISITE_GEM.dependents(FLAWLESS_GEM, FLAWED_GEM, CHIPPED_GEM,  GEM);
    }

    public static void postInit() {
        LIQUID.all().stream().filter(l -> !l.getId().equals("water") && !l.getId().equals("lava")).forEach(m -> GTAPI.register(GTFluid.class, new GTMaterialFluid(Ref.SHARED_ID, m, LIQUID)));
        GAS.all().forEach(m -> GTAPI.register(GTFluid.class, new GTMaterialFluid(Ref.SHARED_ID, m, GAS)));
        ORE_STONE.all().forEach(m -> GTAPI.register(StoneType.class, new StoneType(ID, m.getId(), m, new Texture(m.materialDomain(), "block/stone/" + m.getId()), SoundType.STONE, false).setGenerateOre(false).setStateSupplier(() -> ORE_STONE.get().get(m).asState())));
    }
}
