package org.gtreimagined.gtlib.ore;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext.Builder;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.datagen.builder.GTBlockModelBuilder;
import org.gtreimagined.gtlib.datagen.json.JLoaderModel;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.ticks.ScheduledTick;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.util.Utils.getLocalizedMaterialType;

public class BlockOre extends BlockMaterialStone implements ITextureProvider, IModelProvider, ISharedGTObject, Fallable, ICreativeTabProvider {

    private final MaterialType<?> oreType;

    public BlockOre(String domain, Material material, StoneType stoneType, MaterialType<?> oreType, Properties properties) {
        super(domain, getId(stoneType, oreType, material), material, stoneType, getOreProperties(properties, stoneType));
        this.oreType = oreType;
    }

    public BlockOre(String domain, Material material, StoneType stoneType, MaterialType<?> oreType) {
        this(domain, material, stoneType, oreType, getOreProperties(Properties.of(), stoneType));
    }

    public static String getId(StoneType stoneType, MaterialType<?> materialType, Material material){
        String[] split = getLocalizedMaterialType(materialType);
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].toLowerCase().replace(" ", "_");
        }
        if (split.length > 1) {
            return String.join("", split[0], "_", stoneType.getId(), "_", material.getId(), "_", split[1]);
        } else {
            return String.join("", stoneType.getId(), "_", material.getId(), "_", split[0]);
        }
    }

    @NotNull
    @Override
    public String getDescriptionId() {
        return getId();
    }

    public MaterialType<?> getOreType() {
        return oreType;
    }

    @Override
    public boolean allowedIn(ResourceKey<CreativeModeTab> tab) {
        return stoneType == VanillaStoneTypes.STONE;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        if (this.oreType != GTMaterialTypes.SMALL_ORE) {
            return super.getDrops(state, builder);
        }
        List<ItemStack> drops = new ArrayList<>();
        ItemStack tool = builder.getParameter(LootContextParams.TOOL);
        RandomSource random = builder.getLevel().getRandom();
        boolean silkTouch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) == 1;
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, tool);
        List<ItemStack> selector = new ArrayList<>();
        ItemStack tStack = getGem(EXQUISITE_GEM, 4);
        if (!tStack.isEmpty()) {
            for (int i = 0, j = (silkTouch ? 3:1); i < j; i++) selector.add(tStack);
        }
        tStack = getGem(FLAWLESS_GEM, 2);
        if (!tStack.isEmpty()) {
            for (int i = 0, j = (silkTouch ? 6:2); i < j; i++) selector.add(tStack);
        }
        if (material.has(GEM)){
            for (int i = 0, j = (silkTouch? 6:12); i < j; i++) selector.add(GEM.get(material, 1));
        }
        if (material.has(FLAWED_GEM)){
            for (int i = 0, j = (silkTouch? 10:5); i < j; i++) selector.add(FLAWED_GEM.get(material, 2));
        }
        if (material.has(CHIPPED_GEM)){
            for (int i = 0, j = (silkTouch? 10:5); i < j; i++) selector.add(CHIPPED_GEM.get(material, 4));
        }
        if (material.has(CRUSHED_ORE)){
            int j = (material.has(FLAWED_GEM) || material.has(CHIPPED_GEM)) && silkTouch ? 5 : 10;
            for (int i = 0; i < j; i++) selector.add(CRUSHED_ORE.get(material, 1));
        }
        if (material.has(IMPURE_DUST)){
            for (int i = 0; i < 10; i++) selector.add(IMPURE_DUST.get(material, 1));
        }
        if (!material.has(GEM) && !material.has(CRUSHED_ORE) && !material.has(IMPURE_DUST)) {
            selector.add(DUST.get(material, 1));
        }
        if (!selector.isEmpty()) {
            for (int i = 0, j = Math.max(1, MaterialTags.ORE_MULTI.get(material) + (fortune > 0 ? random.nextInt((1+fortune)*MaterialTags.ORE_MULTI.get(material)):0)/2); i < j; i++) {
                drops.add(selector.get(random.nextInt(selector.size())).copy());
            }
        }
        if (random.nextInt(3 + fortune) > 1){
            if (stoneType.getMaterial().has(DUST)){
                drops.add(DUST.get(stoneType.getMaterial(), 1));
            }
        }
        return drops;
    }

    private ItemStack getGem(MaterialTypeItem<?> betterGem, int replacementAmount){
        if (material.has(betterGem)) return betterGem.get(material, 1);
        return material.has(GEM) ? GEM.get(material, replacementAmount) : ItemStack.EMPTY;
    }

    /**
     * Falling block stuff
     **/
    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (this.stoneType.getGravity()) {
            worldIn.getBlockTicks().schedule(new ScheduledTick<>(this, pos, this.getFallDelay(), 0L));
        }
    }

    /**
     * Update the provided state given the provided neighbor facing and neighbor state, returning a new state.
     * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
     * returns its solidified counterpart.
     * Note that this method should ideally consider only the specific face passed in.
     */
    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (this.stoneType.getGravity()) {
            worldIn.getBlockTicks().schedule(new ScheduledTick<>(this, currentPos, this.getFallDelay(), 0L));
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (this.stoneType.getGravity()) {
            if (worldIn.isEmptyBlock(pos.below()) || canFallThrough(worldIn.getBlockState(pos.below())) && pos.getY() >= worldIn.getMinBuildHeight()) {
                FallingBlockEntity fallingBlockEntity = FallingBlockEntity.fall(worldIn, pos, state);
                this.onStartFalling(fallingBlockEntity);
            }
        }
    }

    protected void onStartFalling(FallingBlockEntity fallingEntity) {
    }

    protected int getFallDelay() {
        return 2;
    }

    public static boolean canFallThrough(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.liquid() || state.canBeReplaced();
    }

    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, Random rand) {
        if (this.stoneType.getGravity()) {
            if (rand.nextInt(16) == 0) {
                BlockPos blockpos = pos.below();
                if (worldIn.isEmptyBlock(blockpos) || canFallThrough(worldIn.getBlockState(blockpos))) {
                    double d0 = (double) pos.getX() + rand.nextDouble();
                    double d1 = (double) pos.getY() - 0.05D;
                    double d2 = (double) pos.getZ() + rand.nextDouble();
                    worldIn.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, stateIn), d0, d1, d2, 0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public int getDustColor(BlockState state, BlockGetter reader, BlockPos pos) {
        return this.stoneType.getFallingDustColor();
    }

    @Override
    public void onBlockModelBuild(Block block, GTBlockStateProvider prov) {

        GTBlockModelBuilder b = prov.getBuilder(block);
        ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
        if (getStoneType().getTextures().length == 6){
            for (Direction direction : Direction.values()){
                mapBuilder.put("base" + direction.getSerializedName(), getStoneType().getTextures()[direction.get3DDataValue()].toString());
            }
        } else {
            mapBuilder.put("base", getStoneType().getTexture().toString());
        }
        b.loader(GTLibModelManager.LOADER_FALLBACK);
        b.property("base", b.addModelObject(JLoaderModel.modelKeepElements(),
                getMaterial().getSet().getDomain() + ":block/material/" + getMaterial().getSet().getId() + "/" + oreType.getId(),
                mapBuilder.build()));
        b.property("fallback", b.addModelObject(JLoaderModel.modelKeepElements(),
                Ref.ID + ":block/material/none/" + oreType.getId(),
                mapBuilder.build()));
        prov.simpleBlock(block, b);
    }

    public static Properties getOreProperties(Properties properties, StoneType type) {
        if (GTLibConfig.ORE_VEIN_SPECTATOR_DEBUG.get()) properties.noOcclusion().lightLevel(b -> 15);
        properties.mapColor(type.getMapColor()).instrument(type.getInstrument()).strength(type.getHardness() * 2, type.getResistence() / 2).sound(type.getSoundType());
        if (type.doesRequireTool()) properties.requiresCorrectToolForDrops();
        return properties;
    }

    @Override
    public int getExpDrop(BlockState state, LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
        if (silkTouchLevel == 0 && material.has(MaterialTags.EXP_RANGE)) {
            if (level instanceof ServerLevel serverLevel) {
                List<ItemStack> self = getDrops(state, serverLevel, pos, level.getBlockEntity(pos));
                if (self.stream().anyMatch(i -> i.getItem() == this.asItem() || i.is(RAW_ORE.getTag()))) {
                    return 0;
                }
            }
            return MaterialTags.EXP_RANGE.get(material).sample(randomSource);
        }
        return 0;
    }

    @Override
    public int getBlockColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int i) {
        //TODO use tags for this instead?
        if (i == 1 && material.has(MaterialTags.ORE_RGB)){
            return MaterialTags.ORE_RGB.getInt(material);
        }
        return super.getBlockColor(state, world, pos, i);
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        if (i == 1 && material.has(MaterialTags.ORE_RGB)){
            return MaterialTags.ORE_RGB.getInt(material);
        }
        return super.getItemColor(stack, block, i);
    }
}
