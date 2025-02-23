package muramasa.antimatter.data;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.block.BlockFrame;
import muramasa.antimatter.block.BlockStorage;
import muramasa.antimatter.block.BlockSurfaceRock;
import muramasa.antimatter.cover.CoverFactory;
import muramasa.antimatter.cover.CoverPlate;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.fluid.AntimatterMaterialFluid;
import muramasa.antimatter.item.CoverMaterialItem;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.material.MaterialItem;
import muramasa.antimatter.material.MaterialType;
import muramasa.antimatter.material.MaterialTypeBlock;
import muramasa.antimatter.material.MaterialTypeFluid;
import muramasa.antimatter.material.MaterialTypeItem;
import muramasa.antimatter.ore.BlockOre;
import muramasa.antimatter.ore.BlockOreStone;
import muramasa.antimatter.ore.StoneType;
import muramasa.antimatter.texture.Texture;
import muramasa.antimatter.util.FluidPlatformUtils;
import muramasa.antimatter.util.Utils;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import java.util.function.BiFunction;
import java.util.function.Function;

import static muramasa.antimatter.Ref.*;
import static muramasa.antimatter.data.AntimatterMaterials.Wood;
import static muramasa.antimatter.material.MaterialTags.RUBBERTOOLS;
import static muramasa.antimatter.material.MaterialTags.WOOD;

public class AntimatterMaterialTypes {
    //Item Types
    public static MaterialTypeItem<?> DUST = new MaterialTypeItem<>("dust", 2, true, U);
    public static MaterialTypeItem<?> DUST_SMALL = new MaterialTypeItem<>("dust_small", 2, true, Ref.U4);
    public static MaterialTypeItem<?> DUST_TINY = new MaterialTypeItem<>("dust_tiny", 2, true, Ref.U9);
    public static MaterialTypeItem<?> DUST_IMPURE = new MaterialTypeItem<>("dust_impure", 2, true, -1);
    public static MaterialTypeItem<?> DUST_PURE = new MaterialTypeItem<>("dust_pure", 2, true, -1);
    public static MaterialTypeItem<MaterialTypeBlock.IOreGetter> BEARING_ROCK = new MaterialTypeItem<>("bearing_rock", 2, false, Ref.U4, (domain, type, mat) -> {
        AntimatterAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).forEach(s -> AntimatterAPI.register(BlockSurfaceRock.class, new BlockSurfaceRock(domain, mat, s)));
        new MaterialItem(domain, type, mat);
    });
    public static MaterialTypeItem<MaterialTypeBlock.IBlockGetter> ROCK = new MaterialTypeItem<>("rock", 2, false, Ref.U4, (domain, type, mat) -> {
        StoneType type1 = AntimatterAPI.get(StoneType.class, mat.getId());
        if (type1 != null){
            AntimatterAPI.register(BlockSurfaceRock.class, new BlockSurfaceRock(domain, Material.NULL, type1));
        }
        new MaterialItem(domain, type, mat);
    });
    public static MaterialTypeItem<?> CRUSHED = new MaterialTypeItem<>("crushed", 2, true, -1);
    public static MaterialTypeItem<?> CRUSHED_REFINED = new MaterialTypeItem<>("crushed_refined", 2, true, -1);
    public static MaterialTypeItem<?> CRUSHED_PURIFIED = new MaterialTypeItem<>("crushed_purified", 2, true, -1);
    public static MaterialTypeItem<?> RAW_ORE = new MaterialTypeItem<>("raw_ore", 2, true, -1);
    public static MaterialTypeItem<?> INGOT = new MaterialTypeItem<>("ingot", 2, true, U);
    public static MaterialTypeItem<?> INGOT_HOT = new MaterialTypeItem<>("ingot_hot", 2, true, U);
    public static MaterialTypeItem<?> NUGGET = new MaterialTypeItem<>("nugget", 2, true, Ref.U9);
    public static MaterialTypeItem<?> CHUNK = new MaterialTypeItem<>("chunk", 2, true, U4);
    public static MaterialTypeItem<?> GEM = new MaterialTypeItem<>("gem", 2, true, U);
    public static MaterialTypeItem<?> GEM_EXQUISITE = new MaterialTypeItem<>("gem_exquisite", 2, true, U * 4);
    public static MaterialTypeItem<?> GEM_FLAWLESS = new MaterialTypeItem<>("gem_flawless", 2, true, U * 2);
    public static MaterialTypeItem<?> GEM_FLAWED = new MaterialTypeItem<>("gem_flawed", 2, true, U2);
    public static MaterialTypeItem<?> GEM_CHIPPED = new MaterialTypeItem<>("gem_chipped", 2, true, U4);
    public static MaterialTypeItem<?> LENS = new MaterialTypeItem<>("lens", 2, true, U * 3 / 4);
    public static MaterialTypeItem<?> PLATE = new MaterialTypeItem<>("plate", 2, true, U, (a, b, c) -> CoverFactory.builder((u, v, t, w) -> new CoverPlate(u, v, t, w, b, c)).setIsValid(be -> true).item((u, v) -> new CoverMaterialItem(u.getDomain(), b, u, c)).build(Ref.ID, "plate_" + c.getId()));
    public static MaterialTypeItem<?> PLATE_DENSE = new MaterialTypeItem<>("plate_dense", 2, true, U * 9);
    public static MaterialTypeItem<?> PLATE_TINY = new MaterialTypeItem<>("plate_tiny", 2, true, U9);
    public static MaterialTypeItem<?> ITEM_CASING = new MaterialTypeItem<>("casing_item", 1, true, U2);
    public static MaterialTypeItem<?> ROD = new MaterialTypeItem<>("rod", 2, true, U2);
    public static MaterialTypeItem<?> ROD_LONG = new MaterialTypeItem<>("rod_long", 2, true, U);
    public static MaterialTypeItem<?> RING = new MaterialTypeItem<>("ring", 2, true, Ref.U4);
    public static MaterialTypeItem<?> FOIL = new MaterialTypeItem<>("foil", 2, true, U4);
    public static MaterialTypeItem<?> BOLT = new MaterialTypeItem<>("bolt", 2, true, U8);
    public static MaterialTypeItem<?> SCREW = new MaterialTypeItem<>("screw", 2, true, Ref.U8);
    public static MaterialTypeItem<?> GEAR = new MaterialTypeItem<>("gear", 2, true, U * 4);
    public static MaterialTypeItem<?> GEAR_SMALL = new MaterialTypeItem<>("gear_small", 2, true, U);
    public static MaterialTypeItem<?> WIRE_FINE = new MaterialTypeItem<>("wire_fine", 2, true, U8);
    public static MaterialTypeItem<?> SPRING = new MaterialTypeItem<>("spring", 2, true, U);
    public static MaterialTypeItem<?> ROTOR = new MaterialTypeItem<>("rotor", 2, true, (U * 4) + Ref.U4);
    public static MaterialTypeItem<?> DRILLBIT = new MaterialTypeItem<>("drill_bit", 2, true, U * 4);
    public static MaterialTypeItem<?> CHAINSAWBIT = new MaterialTypeItem<>("chainsaw_bit", 2, true, U * 2);
    public static MaterialTypeItem<?> WRENCHBIT = new MaterialTypeItem<>("wrench_bit", 2, true, U * 4);
    public static MaterialTypeItem<?> BUZZSAW_BLADE = new MaterialTypeItem<>("buzzsaw_blade", 2, true, U * 4);
    public static final MaterialTypeItem<?> PICKAXE_HEAD = new MaterialTypeItem<>("pickaxe_head", 2, true, U * 3);
    public static final MaterialTypeItem<?> AXE_HEAD = new MaterialTypeItem<>("axe_head", 2, true, U * 3);
    public static final MaterialTypeItem<?> SWORD_BLADE = new MaterialTypeItem<>("sword_blade", 2, true, U * 2);
    public static final MaterialTypeItem<?> SHOVEL_HEAD = new MaterialTypeItem<>("shovel_head", 2, true, U);
    public static final MaterialTypeItem<?> HOE_HEAD = new MaterialTypeItem<>("hoe_head", 2, true, U * 2);
    public static final MaterialTypeItem<?> HAMMER_HEAD = new MaterialTypeItem<>("hammer_head", 2, true, U * 6);
    public static final MaterialTypeItem<?> FILE_HEAD = new MaterialTypeItem<>("file_head", 2, true, U * 2);
    public static final MaterialTypeItem<?> SAW_BLADE = new MaterialTypeItem<>("saw_blade", 2, true, U * 2);
    public static final MaterialTypeItem<?> SCREWDRIVER_TIP = new MaterialTypeItem<>("screwdriver_tip", 2, true, U);
    public static final MaterialTypeItem<?> SCYTHE_BLADE = new MaterialTypeItem<>("scythe_blade", 2, true, U * 3);
    //Block Types
    public static MaterialTypeBlock<MaterialTypeBlock.IOreGetter> ORE = new MaterialTypeBlock<>("ore", 1, true, -1, (domain, type, mat) -> AntimatterAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).filter(s -> !AntimatterAPI.hasReplacement(((MaterialTypeBlock<?>)type).getMaterialTag(mat, s))).forEach(s -> new BlockOre(domain, mat, s, type)));
    public static MaterialTypeBlock<MaterialTypeBlock.IOreGetter> ORE_SMALL = new MaterialTypeBlock<>("ore_small", 1, false, -1, (domain, type, mat) -> AntimatterAPI.all(StoneType.class).stream().filter(StoneType::doesGenerateOre).filter(s -> !AntimatterAPI.hasReplacement(((MaterialTypeBlock<?>)type).getMaterialTag(mat, s))).forEach(s -> new BlockOre(domain, mat, s, type)));
    public static MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> ORE_STONE = new MaterialTypeBlock<>("ore_stone", 1, true, -1,(domain, type, mat) -> new BlockOreStone(domain, mat));
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
            BlockSurfaceRock rock = AntimatterAPI.get(BlockSurfaceRock.class, "surface_rock_" + m.getId() + "_" + s.getId());
            return new MaterialTypeBlock.Container(rock != null ? rock.defaultBlockState() : Blocks.AIR.defaultBlockState());
        });
        ROCK.set((m) -> {
            StoneType s = AntimatterAPI.get(StoneType.class, m.getId());
            if (s == null || !ROCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ROCK, m, s);
            BlockSurfaceRock rock = AntimatterAPI.get(BlockSurfaceRock.class, "surface_rock_" + s.getId());
            return new MaterialTypeBlock.Container(rock != null ? rock.defaultBlockState() : Blocks.AIR.defaultBlockState());
        });
        ORE.set((m, s) -> {
            if (m != null && s != null) {
                Item item = AntimatterAPI.getReplacement(ORE, m, s);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || s == null || !s.doesGenerateOre() || !ORE.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ORE, m, s);
            BlockOre block = AntimatterAPI.get(BlockOre.class, ORE.getId() + "_" + m.getId() + "_" + s.getId());
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        ORE_SMALL.set((m, s) -> {
            if (m != null && s != null) {
                Item item = AntimatterAPI.getReplacement(ORE_SMALL, m, s);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || s == null || !ORE_SMALL.allowGen(m))
                return MaterialTypeBlock.getEmptyBlockAndLog(ORE_SMALL, m, s);
            BlockOre block = AntimatterAPI.get(BlockOre.class, ORE_SMALL.getId() + "_" + m.getId() + "_" + Utils.getConventionalStoneType(s));
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        ORE_STONE.set(m -> {
            if (m == null || !ORE_STONE.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(ORE_STONE, m);
            BlockOreStone block = AntimatterAPI.get(BlockOreStone.class, ORE_STONE.getId() + "_" + m.getId());
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        BLOCK.set(m -> {
            if (m != null) {
                Item item = AntimatterAPI.getReplacement(BLOCK, m);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || !BLOCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(BLOCK, m);
            BlockStorage block = AntimatterAPI.get(BlockStorage.class, BLOCK.getId() + "_" + m.getId());
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        RAW_ORE_BLOCK.set(m -> {
            if (m != null) {
                Item item = AntimatterAPI.getReplacement(RAW_ORE_BLOCK, m);
                if (item instanceof BlockItem) {
                    return new MaterialTypeBlock.Container(((BlockItem) item).getBlock().defaultBlockState());
                }
            }
            if (m == null || !RAW_ORE_BLOCK.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(RAW_ORE_BLOCK, m);
            BlockStorage block = AntimatterAPI.get(BlockStorage.class, RAW_ORE_BLOCK.getId() + "_" + m.getId());
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();
        FRAME.set(m -> {
            if (m == null || !FRAME.allowGen(m)) return MaterialTypeBlock.getEmptyBlockAndLog(FRAME, m);
            BlockFrame block = AntimatterAPI.get(BlockFrame.class, FRAME.getId() + "_" + m.getId());
            return new MaterialTypeBlock.Container(block != null ? block.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }).blockType();

        LIQUID.set((m, i) -> {
            if (m == null || !LIQUID.allowGen(m)) return MaterialTypeFluid.getEmptyFluidAndLog(LIQUID, m);
            if (m.getId().equals("water")) return new FluidStack(Fluids.WATER, i);
            else if (m.getId().equals("lava")) return new FluidStack(Fluids.LAVA, i);
            AntimatterFluid fluid = AntimatterAPI.get(AntimatterFluid.class, LIQUID.getId() + "_" + m.getId());
            if (fluid == null) throw new IllegalStateException("Tried to get null fluid");
            return new FluidStack(fluid.getFluid(), i);
        });
        GAS.set((m, i) -> {
            if (m == null || !GAS.allowGen(m)) return MaterialTypeFluid.getEmptyFluidAndLog(GAS, m);
            AntimatterFluid fluid = AntimatterAPI.get(AntimatterFluid.class, GAS.getId() + "_" + m.getId());
            if (fluid == null) throw new IllegalStateException("Tried to get null fluid");
            return new FluidStack(fluid.getFluid(), i);
        });
    }

    public static void init() {
        replacements();
        dependents();
        Function<Material, String> d = m -> m.has(RUBBERTOOLS) ? "Pulp" : "Dust";
        DUST.setLang(m -> m.getDisplayNameString() + " " + d.apply(m));
        DUST_TINY.setLang(m -> "Tiny " + m.getDisplayNameString() +  " " + d.apply(m)).setHidden();
        DUST_SMALL.setLang(m -> "Small " + m.getDisplayNameString() + " " + d.apply(m)).setHidden();
        DUST_IMPURE.setLang(m -> "Impure " + m.getDisplayNameString() +  " " + d.apply(m));
        DUST_PURE.setLang(m -> "Pure " + m.getDisplayNameString() + " " + d.apply(m));
        Function<Material, String> n = m -> m.getElement() != null ? "Native " : "";
        BEARING_ROCK.unSplitName().setLang(m -> n.apply(m) + m.getDisplayNameString() + " Bearing Rock").setIgnoreTextureSets();
        ROCK.unSplitName().setIgnoreTextureSets();
        CRUSHED.setLang(m -> "Crushed " + n.apply(m) + m.getDisplayNameString() + " Ore");
        CRUSHED_PURIFIED.setLang(m -> "Purified " + n.apply(m) + m.getDisplayNameString() + " Ore");
        CRUSHED_REFINED.setLang(m -> "Refined " + n.apply(m) + m.getDisplayNameString() + " Ore");
        RAW_ORE.unSplitName().setLang(m -> "Raw " + n.apply(m) + m.getDisplayNameString());
        INGOT.setLang(m -> m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Bar" : "Ingot"));
        NUGGET.setLang(m -> m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Chip" : "Nugget"));
        GEM.setLang(Material::getDisplayNameString);
        GEM_EXQUISITE.setLang(m -> "Exquisite " + m.getDisplayNameString());
        GEM_FLAWLESS.setLang(m -> "Flawless " + m.getDisplayNameString());
        GEM_FLAWED.setLang(m -> "Flawed " + m.getDisplayNameString());
        GEM_CHIPPED.setLang(m -> "Chipped " + m.getDisplayNameString());
        Function<Material, String> p = m -> m == Wood ? "Plank" : m.has(RUBBERTOOLS) ? "Sheet" : "Plate";
        PLATE.setLang(m -> m.getDisplayNameString() + " " + p.apply(m));
        PLATE_DENSE.setLang(m -> "Dense " + m.getDisplayNameString() + " " + p.apply(m));
        PLATE_TINY.setLang(m -> "Tiny " + m.getDisplayNameString() + " " + p.apply(m));
        ITEM_CASING.setLang(m -> m.getDisplayNameString() + " Item Casings");
        FOIL.setLang(m -> (m.has(RUBBERTOOLS) ? "Thin " : "") + m.getDisplayNameString() + " " + (m.has(RUBBERTOOLS) ? "Sheet" : "Foil"));
        WIRE_FINE.setIgnoreTextureSets();
        DRILLBIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        CHAINSAWBIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
        WRENCHBIT.unSplitName().setLang(UNSPLIT_FUNCTION).setIgnoreTextureSets();
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
        RAW_ORE_BLOCK.unSplitName().setLang(m -> "Block of Raw " + n.apply(m) + m.getDisplayNameString());
        BLOCK.setLang(m -> "Block of " + m.getDisplayNameString());
    }

    private static void replacements(){
        NUGGET.replacement(AntimatterMaterials.Iron, () -> Items.IRON_NUGGET);
        NUGGET.replacement(AntimatterMaterials.Gold, () -> Items.GOLD_NUGGET);
        INGOT.replacement(AntimatterMaterials.Iron, () -> Items.IRON_INGOT);
        INGOT.replacement(AntimatterMaterials.Gold, () -> Items.GOLD_INGOT);
        INGOT.replacement(AntimatterMaterials.Netherite, () -> Items.NETHERITE_INGOT);
        INGOT.replacement(AntimatterMaterials.Copper, () -> Items.COPPER_INGOT);
        INGOT.replacement(AntimatterMaterials.NetheriteScrap, () -> Items.NETHERITE_SCRAP);


        DUST.replacement(AntimatterMaterials.Redstone, () -> Items.REDSTONE);
        DUST.replacement(AntimatterMaterials.Glowstone, () -> Items.GLOWSTONE_DUST);
        DUST.replacement(AntimatterMaterials.Blaze, () -> Items.BLAZE_POWDER);
        DUST.replacement(AntimatterMaterials.Sugar, () -> Items.SUGAR);
        RAW_ORE.replacement(AntimatterMaterials.Iron, () -> Items.RAW_IRON);
        RAW_ORE.replacement(AntimatterMaterials.Copper, () -> Items.RAW_COPPER);
        RAW_ORE.replacement(AntimatterMaterials.Gold, () -> Items.RAW_GOLD);
        GEM.replacement(AntimatterMaterials.Flint, () -> Items.FLINT);
        GEM.replacement(AntimatterMaterials.Diamond, () -> Items.DIAMOND);
        GEM.replacement(AntimatterMaterials.Emerald, () -> Items.EMERALD);
        GEM.replacement(AntimatterMaterials.Lapis, () -> Items.LAPIS_LAZULI);
        GEM.replacement(AntimatterMaterials.Quartz, () -> Items.QUARTZ);
        GEM.replacement(AntimatterMaterials.Coal, () -> Items.COAL);
        GEM.replacement(AntimatterMaterials.Charcoal, () -> Items.CHARCOAL);
        GEM.replacement(AntimatterMaterials.EnderEye, () -> Items.ENDER_EYE);
        GEM.replacement(AntimatterMaterials.EnderPearl, () -> Items.ENDER_PEARL);

        ROD.replacement(AntimatterMaterials.Blaze, () -> Items.BLAZE_ROD);
        ROD.replacement(AntimatterMaterials.Bone, () -> Items.BONE);
        ROD.replacement(Wood, () -> Items.STICK);

        BLOCK.replacement(AntimatterMaterials.Coal, () -> Items.COAL_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Iron, () -> Items.IRON_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Copper, () -> Items.COPPER_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Gold, () -> Items.GOLD_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Diamond, () -> Items.DIAMOND_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Emerald, () -> Items.EMERALD_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Lapis, () -> Items.LAPIS_BLOCK);
        BLOCK.replacement(AntimatterMaterials.Netherite, () -> Items.NETHERITE_BLOCK);
        RAW_ORE_BLOCK.replacement(AntimatterMaterials.Iron, () -> Items.RAW_IRON_BLOCK);
        RAW_ORE_BLOCK.replacement(AntimatterMaterials.Copper, () -> Items.RAW_COPPER_BLOCK);
        RAW_ORE_BLOCK.replacement(AntimatterMaterials.Gold, () -> Items.RAW_GOLD_BLOCK);
        ORE.replacement(AntimatterMaterials.Coal, AntimatterStoneTypes.STONE, () -> Items.COAL_ORE);
        ORE.replacement(AntimatterMaterials.Coal, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_COAL_ORE);
        ORE.replacement(AntimatterMaterials.Iron, AntimatterStoneTypes.STONE, () -> Items.IRON_ORE);
        ORE.replacement(AntimatterMaterials.Iron, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_IRON_ORE);
        ORE.replacement(AntimatterMaterials.Copper, AntimatterStoneTypes.STONE, () -> Items.COPPER_ORE);
        ORE.replacement(AntimatterMaterials.Copper, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_COPPER_ORE);
        ORE.replacement(AntimatterMaterials.Gold, AntimatterStoneTypes.STONE, () -> Items.GOLD_ORE);
        ORE.replacement(AntimatterMaterials.Gold, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_GOLD_ORE);
        ORE.replacement(AntimatterMaterials.Redstone, AntimatterStoneTypes.STONE, () -> Items.REDSTONE_ORE);
        ORE.replacement(AntimatterMaterials.Redstone, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_REDSTONE_ORE);
        ORE.replacement(AntimatterMaterials.Emerald, AntimatterStoneTypes.STONE, () -> Items.EMERALD_ORE);
        ORE.replacement(AntimatterMaterials.Emerald, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_EMERALD_ORE);
        ORE.replacement(AntimatterMaterials.Lapis, AntimatterStoneTypes.STONE, () -> Items.LAPIS_ORE);
        ORE.replacement(AntimatterMaterials.Lapis, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_LAPIS_ORE);
        ORE.replacement(AntimatterMaterials.Diamond, AntimatterStoneTypes.STONE, () -> Items.DIAMOND_ORE);
        ORE.replacement(AntimatterMaterials.Diamond, AntimatterStoneTypes.DEEPSLATE, () -> Items.DEEPSLATE_DIAMOND_ORE);
        ORE.replacement(AntimatterMaterials.Quartz, AntimatterStoneTypes.NETHERRACK, () -> Items.NETHER_QUARTZ_ORE);
    }

    private static void dependents() {
        ROTOR.dependents(PLATE, SCREW, RING);
        SCREW.dependents(BOLT);
        BOLT.dependents(ROD);
        RING.dependents(ROD);
        ROD_LONG.dependents(ROD);
        ROD.dependents(ROD_LONG);
        CRUSHED.dependents(CRUSHED_PURIFIED, CRUSHED_REFINED, DUST_IMPURE);
        DUST_PURE.dependents(DUST);
        DUST_IMPURE.dependents(DUST_PURE);
        DUST.dependents(DUST_SMALL, DUST_TINY);
        GEAR_SMALL.dependents(PLATE);
        GEAR.dependents(PLATE, ROD);
        GEM_EXQUISITE.dependents(GEM_FLAWLESS, GEM_FLAWED, GEM_CHIPPED,  GEM);
    }

    public static void postInit() {
        LIQUID.all().stream().filter(l -> !l.getId().equals("water") && !l.getId().equals("lava")).forEach(m -> AntimatterAPI.register(AntimatterFluid.class, new AntimatterMaterialFluid(Ref.SHARED_ID, m, LIQUID)));
        GAS.all().forEach(m -> AntimatterAPI.register(AntimatterFluid.class, new AntimatterMaterialFluid(Ref.SHARED_ID, m, GAS)));
        ORE_STONE.all().forEach(m -> AntimatterAPI.register(StoneType.class, new StoneType(ID, m.getId(), m, new Texture(m.materialDomain(), "block/stone/" + m.getId()), SoundType.STONE, false).setGenerateOre(false).setStateSupplier(() -> ORE_STONE.get().get(m).asState())));
    }
}
