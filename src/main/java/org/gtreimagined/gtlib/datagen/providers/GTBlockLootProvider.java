package org.gtreimagined.gtlib.datagen.providers;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.data.loot.BlockLootSubProvider;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockFrame;
import org.gtreimagined.gtlib.block.BlockStone;
import org.gtreimagined.gtlib.block.BlockStoneSlab;
import org.gtreimagined.gtlib.block.BlockStoneStair;
import org.gtreimagined.gtlib.block.BlockStoneWall;
import org.gtreimagined.gtlib.block.BlockStorage;
import org.gtreimagined.gtlib.block.BlockSurfaceRock;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.BlockMultiMachine;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.BlockOreStone;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.pipe.BlockPipe;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;

public class GTBlockLootProvider extends BlockLootSubProvider implements DataProvider, IGTLibProvider {
    protected final String providerDomain, providerName;
    protected final Map<Block, Function<Block, LootTable.Builder>> tables = new Object2ObjectOpenHashMap<>();
    protected static final Map<Block, Function<Block, LootTable.Builder>> GLOBAL_TABLES = new Object2ObjectOpenHashMap<>();

    public static final LootItemCondition.Builder BRANCH_CUTTER = MatchTool.toolMatches(ItemPredicate.Builder.item().of(GTTools.BRANCH_CUTTER.getTag()));
    public static final LootItemCondition.Builder HAMMER = MatchTool.toolMatches(ItemPredicate.Builder.item().of(GTTools.HAMMER.getTag()));
    public static final LootItemCondition.Builder SAW = MatchTool.toolMatches(ItemPredicate.Builder.item().of(GTTools.SAW.getTag()).hasEnchantment(new EnchantmentPredicate() {
        @Override
        public boolean containedIn(Map<Enchantment, Integer> enchantmentsIn) {
            return !enchantmentsIn.containsKey(Enchantments.SILK_TOUCH);
        }
    }));

    //public static final ILootCondition.IBuilder BRANCH_CUTTER_SHEARS_SILK_TOUCH = BlockLootTablesAccessor.getSilkTouchOrShears().alternative(BRANCH_CUTTER);

    //public static final ILootCondition.IBuilder BRANCH_CUTTER_SHEARS_SILK_TOUCH_INVERTED = BRANCH_CUTTER_SHEARS_SILK_TOUCH.inverted();


    public GTBlockLootProvider(String providerDomain, String providerName) {
        this.providerDomain = providerDomain;
        this.providerName = providerName;
    }

    public static void init(){}

    @Override
    public void run() {
        loot();
    }

    protected void loot() {
        GTAPI.all(BlockMachine.class, providerDomain, this::add);
        GTAPI.all(BlockMultiMachine.class, providerDomain, this::add);
        if (providerDomain.equals(Ref.ID)) {
            GTAPI.all(BlockPipe.class, this::add);
            GTAPI.all(BlockStorage.class, block -> {
                if (block.getType() == RAW_ORE_BLOCK && block.getMaterial().has(CRUSHED_ORE)){
                    tables.put(block, b -> createOreDropWithHammer(block, block.asItem(), CRUSHED_ORE.get(block.getMaterial()), 9 * MaterialTags.ORE_MULTI.get(block.getMaterial())));
                } else {
                    add(block);
                }
            });
            GTAPI.all(BlockFrame.class, this::add);
            GTAPI.all(BlockStone.class, b -> {
                if (b.getType() instanceof CobbleStoneType && b.getSuffix().isEmpty()) {
                    tables.put(b, b2 -> createSingleItemTableWithSilkTouch(b, ((CobbleStoneType) b.getType()).getBlock("cobble")));
                    return;
                }
                this.add(b);
            });
            GTAPI.all(BlockStoneSlab.class, b -> tables.put(b, BlockLoot::createSlabItemTable));
            GTAPI.all(BlockStoneStair.class, this::add);
            GTAPI.all(BlockStoneWall.class, this::add);
            GTAPI.all(BlockOre.class, this::addToFortune);
            GTAPI.all(BlockOreStone.class, this::addToStone);
            GTAPI.all(BlockSurfaceRock.class, b -> {
                ItemStack drop = b.getMaterial() != Material.NULL && b.getMaterial().has(BEARING_ROCK) ? BEARING_ROCK.get(b.getMaterial(), 1) : b.getStoneType().getMaterial().has(ROCK) ? ROCK.get(b.getStoneType().getMaterial(), 1) : ItemStack.EMPTY;
                if (!drop.isEmpty()) {
                    tables.put(b, b2 -> BlockLootSubProvider.createSingleItemTable(drop.getItem()));
                }
            });
        }
    }

    @Override
    public void onCompletion() {
        for (var e : tables.entrySet()) {
            if (!GLOBAL_TABLES.containsKey(e.getKey())){
                GLOBAL_TABLES.put(e.getKey(), e.getValue());
            }
        }
    }

    public static void afterCompletion(){
        for (var e : GLOBAL_TABLES.entrySet()) {
            LootTable table = e.getValue().apply(e.getKey()).setParamSet(LootContextParamSets.BLOCK).build();
            GTLibDynamics.RUNTIME_DATA_PACK.addData(GTLibDynamics.fix(RegistryUtils.getIdFromBlock(e.getKey()), "loot_tables/blocks", "json"), GTLibDynamics.serialize(table));
        }
    }

    protected void overrideBlock(Block block, Function<Block, LootTable.Builder> builderFunction){
        GLOBAL_TABLES.put(block, builderFunction);
    }

    protected void overrideOre(Material ore, Function<BlockOre, LootTable.Builder> builderFunction){
        if (ore.has(ORE)){
            GTAPI.all(StoneType.class).stream().filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK).forEach(s -> {
                if (ORE.get().get(ore, s).asBlock() instanceof BlockOre blockOre) {
                    GLOBAL_TABLES.put(blockOre, b -> builderFunction.apply((BlockOre) b));
                }
            });
        }
    }

    protected void overrideSmallOre(Material ore, Function<BlockOre, LootTable.Builder> builderFunction){
        if (ore.has(SMALL_ORE)){
            GTAPI.all(StoneType.class).stream().filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK).forEach(s -> {
                if (SMALL_ORE.get().get(ore, s).asBlock() instanceof BlockOre blockOre) {
                    GLOBAL_TABLES.put(blockOre, b -> builderFunction.apply((BlockOre) b));
                }
            });
        }
    }

    protected void addToFortune(BlockOre block) {
        if (block.getMaterial().has(MaterialTags.CUSTOM_ORE_DROPS)){
            tables.put(block, b -> MaterialTags.CUSTOM_ORE_DROPS.getBuilderFunction(block.getMaterial()).apply(block));
            return;
        }
        if (block.getOreType() == SMALL_ORE) return;
        tables.put(block, addToFortuneWithoutCustomDrops(block));
    }

    public Function<Block, LootTable.Builder> addToFortuneWithoutCustomDrops(BlockOre block) {
        if (block.getOreType() == ORE) {
            Item drop;
            if (block.getMaterial().has(CRUSHED_ORE) || block.getMaterial().has(DUST)){
                drop = block.getMaterial().has(CRUSHED_ORE) ? CRUSHED_ORE.get(block.getMaterial()) : DUST.get(block.getMaterial());
            } else {
                drop = null;
            }
            Item item = block.getStoneType().isSandLike() ? block.asItem() : RAW_ORE.get(block.getMaterial());
            return b -> createOreDropWithHammer(b, item, drop, MaterialTags.ORE_MULTI.get(block.getMaterial()));
        }
        return this::createSingleItemTable;
    }

    public static LootTable.Builder createOreDropWithHammer(Block block, Item primaryDrop, Item hammerDrop, int hammerAmount){
        LootTable.Builder builder = LootTable.lootTable();
        if (block.asItem() == primaryDrop){
            LootPool.Builder loot = LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(primaryDrop));
            if (hammerDrop != null) loot.when(HAMMER.invert());
            builder.withPool(applyExplosionCondition(block, loot));
        } else {
            LootPoolSingletonContainer.Builder<?> pool = LootItem.lootTableItem(primaryDrop).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE));
            if (hammerDrop != null) pool.when(HAMMER.invert());
            builder = createSilkTouchDispatchTable(block, applyExplosionDecay(block, pool));
        }
        if (hammerDrop != null){
            builder.withPool(applyExplosionCondition(hammerDrop, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAMMER).add(LootItem.lootTableItem(hammerDrop).apply(SetItemCountFunction.setCount(ConstantValue.exactly(hammerAmount))))));
        }
        return builder;
    }

    protected void addToStone(BlockOreStone block) {
        if (block.getMaterial().has(MaterialTags.CUSTOM_ORE_STONE_DROPS)){
            tables.put(block, b -> MaterialTags.CUSTOM_ORE_STONE_DROPS.getBuilderFunction(block.getMaterial()).apply(block));
            return;
        }
        if (block.getMaterial().has(RAW_ORE)) {
            Item item = RAW_ORE.get(block.getMaterial());
            tables.put(block, b -> createSilkTouchDispatchTable(block, applyExplosionDecay(block, LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))));
            return;
        }
        add(block);
    }

    protected void add(Block block) {
        tables.put(block, this::build);
    }

    protected LootTable.Builder build(Block block) {
        return createSingleItemTable(block);
    }

    @Override
    public String getName() {
        return providerName;
    }


    protected static LootTable.Builder droppingWithBranchCutters(Block block, Block sapling, float... chances) {
        return createLeavesDrops(block, sapling, chances).withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(GTBlockLootProvider.BRANCH_CUTTER).add(LootItem.lootTableItem(sapling)));
    }

    @Override
    protected void generate() {

    }
}
