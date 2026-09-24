package org.gtreimagined.gtlib.data

import net.ccbluex.fastutil.invoke
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.material.Fluids
import net.minecraftforge.fluids.FluidStack
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.block.BlockFrame
import org.gtreimagined.gtlib.block.BlockStorage
import org.gtreimagined.gtlib.block.BlockSurfaceRock
import org.gtreimagined.gtlib.cover.CoverFactory
import org.gtreimagined.gtlib.cover.CoverPlate
import org.gtreimagined.gtlib.fluid.GTFluid
import org.gtreimagined.gtlib.fluid.GTMaterialFluid
import org.gtreimagined.gtlib.item.CoverMaterialItem
import org.gtreimagined.gtlib.material.*
import org.gtreimagined.gtlib.ore.BlockOre
import org.gtreimagined.gtlib.ore.BlockOreStone
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.texture.Texture
import org.gtreimagined.gtlib.util.lowerUnderscoreToUpperSpaced
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Function

object GTMaterialTypes {
    @JvmField
    val UNSPLIT_FUNCTION: BiFunction<MaterialType<*>, Material, String> = BiFunction { t, m ->
        "${m.displayNameString} ${lowerUnderscoreToUpperSpaced(t.getId())}" }
    private val d: Function<Material, String> = Function { m -> if (m.has(MaterialTags.RUBBERTOOLS)) "Pulp" else "Dust" }
    private val n: Function<Material, String> =
        Function { m -> if (m.element != null) "Native " else "" }
    private val p: Function<Material, String> = Function { m ->
        if (m === GTLibMaterials.Wood) "Plank" else if (m.has(MaterialTags.RUBBERTOOLS)) "Sheet" else "Plate"
    }
    //Item Types
    @JvmField
    val DUST: MaterialTypeItem<*> = MaterialTypeItem<Any?>("dust", true, Ref.U).apply {
        lang = Function { m -> "${m.displayNameString} ${d(m)}" }
    }
    @JvmField
    val SMALL_DUST: MaterialTypeItem<*> = MaterialTypeItem<Any?>("small_dust", true, Ref.U4).apply {
        lang = Function { m -> "Small ${m.displayNameString} ${d(m)}" }
        isHidden = true
    }
    @JvmField
    val TINY_DUST: MaterialTypeItem<*> = MaterialTypeItem<Any?>("tiny_dust", true, Ref.U9).apply {
        lang = Function { m -> "Tiny ${m.displayNameString} ${d(m)}" }
        isHidden = true
    }
    @JvmField
    val IMPURE_DUST: MaterialTypeItem<*> = MaterialTypeItem<Any?>("impure_dust", true, -1).apply {
        lang = Function { m -> "Impure ${m.displayNameString} ${d(m)}" }
    }
    @JvmField
    val PURE_DUST: MaterialTypeItem<*> = MaterialTypeItem<Any?>("pure_dust", true, -1).apply {
        lang = Function { m -> "Pure ${m.displayNameString} ${d(m)}" }
    }
    @JvmField
    val CRUSHED_ORE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("crushed_ore", true, -1).apply {
        lang = Function { m -> "Crushed ${n(m)}${m.displayNameString} Ore" }
    }
    @JvmField
    val PURIFIED_ORE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("purified_ore", true, -1).apply {
        lang = Function { m -> "Purified ${n(m)}${m.displayNameString} Ore" }
    }
    @JvmField
    val REFINED_ORE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("refined_ore", true, -1).apply {
        lang = Function { m -> "Refined ${n(m)}${m.displayNameString} Ore" }
    }
    @JvmField
    val CHIPPED_GEM: MaterialTypeItem<*> = MaterialTypeItem<Any?>("chipped_gem", true, Ref.U4).apply {
        lang = Function{ m -> "Chipped ${m.displayNameString}" }
    }
    @JvmField
    val FLAWED_GEM: MaterialTypeItem<*> = MaterialTypeItem<Any?>("flawed_gem", true, Ref.U2).apply {
        lang = Function { m -> "Flawed ${m.displayNameString}" }
    }
    @JvmField
    val GEM: MaterialTypeItem<*> = MaterialTypeItem<Any?>("gem", true, Ref.U).apply {
        lang = Function { it.displayNameString }
    }
    @JvmField
    val FLAWLESS_GEM: MaterialTypeItem<*> = MaterialTypeItem<Any?>("flawless_gem", true, Ref.U * 2).apply {
        lang = Function { m -> "Flawless ${m.displayNameString}" }
    }
    @JvmField
    val EXQUISITE_GEM: MaterialTypeItem<*> = MaterialTypeItem<Any?>("exquisite_gem", true, Ref.U * 4).apply {
        lang = Function { m -> "Exquisite ${m.displayNameString}" }
    }
    @JvmField
    val NUGGET: MaterialTypeItem<*> = MaterialTypeItem<Any?>("nugget", true, Ref.U9).apply {
        lang = Function { m -> "${m.displayNameString} ${if (m.has(MaterialTags.RUBBERTOOLS)) "Chip" else "Nugget"}" }
    }
    @JvmField
    val CHUNK: MaterialTypeItem<*> = MaterialTypeItem<Any?>("chunk", true, Ref.U4)
    @JvmField
    val INGOT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("ingot", true, Ref.U).apply {
        lang = Function { m -> "${m.displayNameString} ${if (m.has(MaterialTags.RUBBERTOOLS)) "Bar" else "Ingot"}" }
    }
    @JvmField
    val HOT_INGOT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("hot_ingot", true, Ref.U)
    @JvmField
    val TINY_PLATE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("tiny_plate", true, Ref.U9).apply {
        lang = Function { m -> "Tiny ${m.displayNameString} ${p(m)}" }
    }
    @JvmField
    val PLATE: MaterialTypeItem<Any> = MaterialTypeItem<Any>("plate", true, Ref.U) { _, b, c ->
        CoverFactory.builder { u, v, t, w ->
            CoverPlate(u, v, t, w, b, c)
        }.item { u, _ -> CoverMaterialItem(u.getDomain(), b, u, c) }
            .build(Ref.ID, "plate_${c.id}")
    }.apply {
        lang = Function { m -> "${m.displayNameString} ${p(m)}" }

    }

    @JvmField
    val DENSE_PLATE: MaterialTypeItem<*> = MaterialTypeItem<Any>("dense_plate", true, Ref.U * 9).apply {
        lang = Function { m -> "Dense ${m.displayNameString} ${p(m)}" }
    }
    @JvmField
    val BEARING_ROCK: MaterialTypeItem<MaterialTypeBlock.IOreGetter> = MaterialTypeItem<MaterialTypeBlock.IOreGetter>("bearing_rock", false, Ref.U4) { domain, type, mat ->
        GTAPI.all(StoneType::class.java).stream().filter { it.doesGenerateOre() }
            .forEach { s ->
                GTAPI.register(BlockSurfaceRock::class.java, BlockSurfaceRock(domain, mat, s))
            }
        MaterialItem(domain, type, mat)
    }.apply {
        unSplitName()
        lang = Function { m -> "${n(m)}${m.displayNameString} Bearing Rock" }
    }

    @JvmField
    val ROCK: MaterialTypeItem<MaterialTypeBlock.IBlockGetter> = MaterialTypeItem<MaterialTypeBlock.IBlockGetter>("rock", false, Ref.U4) { domain, type, mat ->
        val type1 = GTAPI.get(StoneType::class.java, mat.id)
        if (type1 != null) {
            GTAPI.register(BlockSurfaceRock::class.java, BlockSurfaceRock(domain, Material.NULL, type1))
        }
        MaterialItem(domain, type, mat)
    }.unSplitName()

    @JvmField
    val RAW_ORE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("raw_ore", true, -1).apply {
        unSplitName()
        lang  = Function { m -> "Raw ${n(m)}${m.displayNameString}" }
        idGetter = Function { m -> "raw_${m.id}" }
        tagPrefix = "raw_materials"
    }
    @JvmField
    val SMALL_GEAR: MaterialTypeItem<*> = MaterialTypeItem<Any?>("small_gear", true, Ref.U)
    @JvmField
    val GEAR: MaterialTypeItem<*> = MaterialTypeItem<Any?>("gear", true, Ref.U * 4)
    @JvmField
    val ROTOR: MaterialTypeItem<*> = MaterialTypeItem<Any?>("rotor", true, (Ref.U * 4) + Ref.U4)
    @JvmField
    val ROD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("rod", true, Ref.U2)
    @JvmField
    val LONG_ROD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("long_rod", true, Ref.U)
    @JvmField
    val SPRING: MaterialTypeItem<*> = MaterialTypeItem<Any?>("spring", true, Ref.U)
    @JvmField
    val LENS: MaterialTypeItem<*> = MaterialTypeItem<Any?>("lens", true, Ref.U * 3 / 4)
    @JvmField
    val BOLT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("bolt", true, Ref.U8)
    @JvmField
    val SCREW: MaterialTypeItem<*> = MaterialTypeItem<Any?>("screw", true, Ref.U8)
    @JvmField
    val RING: MaterialTypeItem<*> = MaterialTypeItem<Any?>("ring", true, Ref.U4)
    @JvmField
    val FOIL: MaterialTypeItem<*> = MaterialTypeItem<Any?>("foil", true, Ref.U4).apply {
        lang = Function{ m ->
            val prefix = if (m.has(MaterialTags.RUBBERTOOLS))"Thin" else ""
            val suffix = if (m.has(MaterialTags.RUBBERTOOLS))"Sheet" else "Foil"
            "$prefix${m.displayNameString} $suffix"
        }
    }
    @JvmField
    val ITEM_CASING: MaterialTypeItem<*> = MaterialTypeItem<Any?>("item_casing", true, Ref.U2).apply {
        unSplitName()
        lang = Function { m -> "${m.displayNameString} Item Casings" }
    }
    @JvmField
    val FINE_WIRE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("fine_wire", true, Ref.U8)
    @JvmField
    val SWORD_BLADE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("sword_blade", true, Ref.U * 2).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val PICKAXE_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("pickaxe_head", true, Ref.U * 3).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val SHOVEL_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("shovel_head", true, Ref.U).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val AXE_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("axe_head", true, Ref.U * 3).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val HOE_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("hoe_head", true, Ref.U * 2).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val HAMMER_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("hammer_head", true, Ref.U * 6).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val FILE_HEAD: MaterialTypeItem<*> = MaterialTypeItem<Any?>("file_head", true, Ref.U * 2).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val KNIFE_BLADE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("knife_blade", true, Ref.U).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val SAW_BLADE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("saw_blade", true, Ref.U * 2).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val DRILL_BIT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("drill_bit", true, Ref.U * 4).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val CHAINSAW_BIT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("chainsaw_bit", true, Ref.U * 2).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val WRENCH_BIT: MaterialTypeItem<*> = MaterialTypeItem<Any?>("wrench_bit", true, Ref.U * 4).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val SCREWDRIVER_TIP: MaterialTypeItem<*> = MaterialTypeItem<Any?>("screwdriver_tip", true, Ref.U).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val SCYTHE_BLADE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("scythe_blade", true, Ref.U * 3).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }
    @JvmField
    val BUZZSAW_BLADE: MaterialTypeItem<*> = MaterialTypeItem<Any?>("buzzsaw_blade", true, Ref.U * 4).apply {
        unSplitName().lang(UNSPLIT_FUNCTION)
    }

    //Block Types
    @JvmField
    val ORE: MaterialTypeBlock<MaterialTypeBlock.IOreGetter> = MaterialTypeBlock("ore", true, -1) { domain, type, mat ->
        GTAPI.all(StoneType::class.java).stream().filter { it.doesGenerateOre() }.filter { s ->
            !GTAPI.hasReplacement((type as MaterialTypeBlock<*>).getMaterialTag(mat, s))
        }.forEach { s -> BlockOre(domain, mat, s, type) }
    }

    @JvmField
    val SMALL_ORE: MaterialTypeBlock<MaterialTypeBlock.IOreGetter> = MaterialTypeBlock("small_ore", false, -1) { domain, type, mat ->
        GTAPI.all(StoneType::class.java).stream().filter { it.doesGenerateOre() }.filter { s ->
            !GTAPI.hasReplacement((type as MaterialTypeBlock<*>).getMaterialTag(mat, s))
        }.forEach { s -> BlockOre(domain, mat, s, type) }
    }

    @JvmField
    val ORE_STONE: MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> = MaterialTypeBlock("dense_ore", true, -1) { domain, _, mat ->
        BlockOreStone(domain, mat)
    }

    @JvmField
    val BLOCK: MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> = MaterialTypeBlock<MaterialTypeBlock.IBlockGetter>("block", false, Ref.U * 9, ::BlockStorage).apply {
        lang = Function { m: Material? -> "Block of " + m!!.displayNameString }
        tagPrefix = "storage_blocks"
    }

    @JvmField
    val RAW_ORE_BLOCK: MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> = MaterialTypeBlock<MaterialTypeBlock.IBlockGetter>("raw_ore_block", false, -1, ::BlockStorage).apply {
        unSplitName()
        lang = Function { m -> "Block of Raw ${n(m)}${m.displayNameString}" }
        idGetter = Function { m -> "raw_${m.id}_block" }
        tagPrefix = "storage_blocks"
    }

    @JvmField
    val FRAME: MaterialTypeBlock<MaterialTypeBlock.IBlockGetter> = MaterialTypeBlock("frame", true, Ref.U * 2, ::BlockFrame)

    //Fluid Types
    @JvmField
    val LIQUID: MaterialTypeFluid<MaterialTypeFluid.IFluidGetter> = MaterialTypeFluid("liquid", true, -1)
    @JvmField
    val GAS: MaterialTypeFluid<MaterialTypeFluid.IFluidGetter> = MaterialTypeFluid("gas", true, -1)


    init {
        BEARING_ROCK.set { m, s ->
            if (!s.doesGenerateOre() || !BEARING_ROCK.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(
                BEARING_ROCK, m, s
            )
            val rock = GTAPI.get(BlockSurfaceRock::class.java, "${s.id}_${m.id}_surface_rock")
            MaterialTypeBlock.Container(if (rock != null) rock.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        ROCK.set { m ->
            val s = GTAPI.get(StoneType::class.java, m.id)
            if (s == null || !ROCK.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(ROCK, m, s!!)
            val rock = GTAPI.get(BlockSurfaceRock::class.java, "${s.id}_surface_rock")
            MaterialTypeBlock.Container(if (rock != null) rock.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        ORE.set { m, s ->
            val item = GTAPI.getReplacement(ORE, m, s)
            if (item is BlockItem) {
                return@set MaterialTypeBlock.Container(item.block.defaultBlockState())
            }
            if (!s.doesGenerateOre() || !ORE.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(ORE, m, s)
            val block = GTAPI.get(BlockOre::class.java, BlockOre.getId(s, ORE, m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        SMALL_ORE.set { m, s ->
            val item = GTAPI.getReplacement(SMALL_ORE, m, s)
            if (item is BlockItem) {
                return@set MaterialTypeBlock.Container(item.block.defaultBlockState())
            }
            if (!SMALL_ORE.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(SMALL_ORE, m, s)
            val block = GTAPI.get(BlockOre::class.java, BlockOre.getId(s, SMALL_ORE, m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        ORE_STONE.set { m ->
            if (!ORE_STONE.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(ORE_STONE, m)
            val block = GTAPI.get(BlockOreStone::class.java, ORE_STONE.idGetter(m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        BLOCK.set { m ->
            val item = GTAPI.getReplacement(BLOCK, m)
            if (item is BlockItem) {
                return@set MaterialTypeBlock.Container(item.block.defaultBlockState())
            }
            if (!BLOCK.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(BLOCK, m)
            val block = GTAPI.get(BlockStorage::class.java, BLOCK.idGetter(m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        RAW_ORE_BLOCK.set { m ->
            val item = GTAPI.getReplacement(RAW_ORE_BLOCK, m)
            if (item is BlockItem) {
                return@set MaterialTypeBlock.Container(item.block.defaultBlockState())
            }
            if (!RAW_ORE_BLOCK.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(RAW_ORE_BLOCK, m)
            val block = GTAPI.get(BlockStorage::class.java, RAW_ORE_BLOCK.idGetter(m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }
        FRAME.set { m ->
            if (!FRAME.allowGen(m)) return@set MaterialTypeBlock.getEmptyBlockAndLog(FRAME, m)
            val block = GTAPI.get(BlockFrame::class.java, FRAME.idGetter(m))
            MaterialTypeBlock.Container(if (block != null) block.defaultBlockState() else Blocks.AIR.defaultBlockState())
        }

        LIQUID.set { m, i ->
            if (m == null || !LIQUID.allowGen(m)) return@set MaterialTypeFluid.getEmptyFluidAndLog(
                LIQUID, m!!
            )
            if (LIQUID.fluidReplacements.containsKey(m)) return@set FluidStack(LIQUID.fluidReplacements[m]!!.get(), i)
            val fluid = GTAPI.get(GTFluid::class.java, "${LIQUID.getId()}_${m.id}")
            checkNotNull(fluid) { "Tried to get null fluid" }
            FluidStack(fluid.fluid, i)
        }
        GAS.set { m, i ->
            if (m == null || !GAS.allowGen(m)) return@set MaterialTypeFluid.getEmptyFluidAndLog(GAS, m!!)
            if (GAS.fluidReplacements.containsKey(m)) return@set FluidStack(GAS.fluidReplacements[m]!!.get(), i)
            val fluid = GTAPI.get(GTFluid::class.java, "${GAS.getId()}_${m.id}")
            checkNotNull(fluid) { "Tried to get null fluid" }
            FluidStack(fluid.fluid, i)
        }
    }

    @JvmStatic
    fun init() {
        dependents()
        replacements()
    }

    private fun replacements() {
        ROD.replacement(GTLibMaterials.Wood) { Items.STICK }
        LIQUID.addReplacement(GTLibMaterials.Water) { Fluids.WATER }
        LIQUID.addReplacement(GTLibMaterials.Lava) { Fluids.LAVA }
    }

    private fun dependents() {
        ROTOR.dependents(PLATE, SCREW, RING)
        SCREW.dependents(BOLT)
        BOLT.dependents(ROD)
        RING.dependents(ROD)
        LONG_ROD.dependents(ROD)
        CRUSHED_ORE.dependents(PURIFIED_ORE, REFINED_ORE, IMPURE_DUST)
        PURE_DUST.dependents(DUST)
        IMPURE_DUST.dependents(PURE_DUST)
        DUST.dependents(SMALL_DUST, TINY_DUST)
        SMALL_GEAR.dependents(PLATE)
        GEAR.dependents(PLATE, ROD)
        EXQUISITE_GEM.dependents(FLAWLESS_GEM, FLAWED_GEM, CHIPPED_GEM, GEM)
    }

    @JvmStatic
    fun postInit() {
        LIQUID.all().stream().filter { l -> !LIQUID.hasReplacement(l) }.forEach { m ->
            GTAPI.register(GTFluid::class.java, GTMaterialFluid(Ref.SHARED_ID, m, LIQUID))
        }
        GAS.all().stream().filter { g -> !GAS.hasReplacement(g) }.forEach { m ->
            GTAPI.register(GTFluid::class.java, GTMaterialFluid(Ref.SHARED_ID, m, GAS))
        }
        ORE_STONE.all().forEach { m ->
            GTAPI.register(
                StoneType::class.java,
                StoneType(Ref.ID, m.id, m, Texture(m.materialDomain(), "block/stone/${m.id}"), SoundType.STONE, false)
                    .setGenerateOre(false).setStateSupplier {
                    ORE_STONE.get()!!.get(m).asState()
                }
            )
        }
    }
}
