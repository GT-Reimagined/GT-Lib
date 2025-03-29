package org.gtreimagined.gtlib.material;

import com.google.common.collect.HashBiMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import lombok.Getter;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.registration.IGTObject;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Supplier;

public class MaterialTypeBlock<T> extends MaterialType<T> {

    @Getter
    protected final Map<Material, Map<StoneType, Supplier<Item>>> oreReplacements = HashBiMap.create();

    public interface BlockSupplier {
        void createBlocks(String domain, MaterialType<?> type, Material material);
    }

    private final BlockSupplier supplier;

    public MaterialTypeBlock(String id, int layers, boolean visible, long unitValue, BlockSupplier supplier) {
        super(id, layers, visible, unitValue);
        AntimatterAPI.register(MaterialTypeBlock.class, this);
        this.supplier = supplier;
    }

    /**
     * Forces these tags to not generate, assuming they have a replacement.
     */
    public void replacement(Material mat, StoneType type, Supplier<Item> replacement) {
        if (!mat.enabled) return;
        if (get() instanceof IOreGetter){
            Map<StoneType, Supplier<Item>> subMap = oreReplacements.computeIfAbsent(mat, m -> new Object2ObjectArrayMap<>());
            subMap.put(type, replacement);
            this.add(mat);
            AntimatterAPI.addReplacement(getMaterialTag(mat, type), replacement);
        }

    }

    public static Container getEmptyBlockAndLog(MaterialType<?> type, IGTObject... objects) {
        Utils.onInvalidData("Tried to create " + type.getId() + " for objects: " + Arrays.toString(Arrays.stream(objects).map(IGTObject::getId).toArray(String[]::new)));
        return new Container(Blocks.AIR.defaultBlockState());
    }

    public RecipeIngredient getMaterialIngredient(Material m, int count) {
        return RecipeIngredient.of(getMaterialTag(m), count);
    }

    public RecipeIngredient getMaterialIngredient(Material m, StoneType s, int count) {
        return RecipeIngredient.of(getMaterialTag(m, s), count);
    }

    public TagKey<Block> getBlockMaterialTag(Material m){
        return TagUtils.getForgelikeBlockTag(String.join("", Utils.getConventionalMaterialType(this), "/", (getId().equals("raw_ore_block") ? "raw_" : ""), m.getId()));
    }

    public TagKey<Item> getMaterialTag(Material m, StoneType s){
        if (!(this.get() instanceof IOreGetter)) return getMaterialTag(m);
        return TagUtils.getForgelikeItemTag(s.getId() + "_" + Utils.getConventionalMaterialType(this) + "/" + m.getId());
    }

    public boolean allowBlockGen(Material material) {
        return !replacements.containsKey(material) && allowGen(material);
    }

    @Override
    public void onRegistryBuild(IForgeRegistry<?> registry) {
        super.onRegistryBuild(registry);
        if (doRegister()) {
            for (Material material : this.materials) {
                if (!material.enabled) continue;
                if (allowBlockGen(material)) supplier.createBlocks(material.materialDomain(), this, material);
            }
        }
    }

    public interface IBlockGetter {
        Container get(Material m);
    }

    public interface IOreGetter {
        Container get(Material m, StoneType s);
    }

    public static class Container {

        protected BlockState state;

        public Container(BlockState state) {
            this.state = state;
        }

        public BlockState asState() {
            return state;
        }

        public Block asBlock() {
            return state.getBlock();
        }

        public Item asItem() {
            return asBlock().asItem();
        }

        public ItemStack asStack(int count) {
            return new ItemStack(asItem(), count);
        }

        public ItemStack asStack() {
            return asStack(1);
        }

        public RecipeIngredient asIngredient() {
            return RecipeIngredient.of(asStack(1));
        }
    }
}
