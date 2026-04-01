package org.gtreimagined.gtlib.material;

import net.minecraft.resources.ResourceKey;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTCreativeTabs;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.item.ItemBasic;
import org.gtreimagined.gtlib.material.data.ToolData;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.registration.IColorHandler;
import org.gtreimagined.gtlib.registration.IModelProvider;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.registration.ITextureProvider;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.util.CodeUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.WorldGenHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.material.MaterialTags.RAINBOW_RGB;
import static org.gtreimagined.gtlib.material.MaterialTags.TOOLS;

public class MaterialItem extends ItemBasic<MaterialItem> implements ISharedGTObject, IColorHandler, ITextureProvider, IModelProvider, IMaterialObject {

    protected Material material;
    protected MaterialType<?> type;

    public MaterialItem(String domain, MaterialType<?> type, Material material, Properties properties) {
        super(domain, type.getIdGetter().apply(material), MaterialItem.class, properties);
        this.material = material;
        this.type = type;
    }

    public MaterialItem(String domain, MaterialType<?> type, Material material) {
        this(domain, type, material, new Properties());
        tab(GTCreativeTabs.MATERIALS.getKey());
    }

    public MaterialType<?> getType() {
        return type;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public void fillItemCategory(ResourceKey<CreativeModeTab> group, NonNullList<ItemStack> items) {
        if (allowedIn(group) && getType().isVisible()) items.add(new ItemStack(this));
    }

    @SuppressWarnings("NoTranslation")
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        //Here only add specific types, events are handled below.
        if (type == GTMaterialTypes.BEARING_ROCK) {
            tooltip.add(Utils.translatable("gtlib.tooltip.occurrence").append(Utils.literal(material.getDisplayName().getString()).withStyle(ChatFormatting.YELLOW)));
        }
    }

    @SuppressWarnings("NoTranslation")
    public static void addTooltipsForMaterialItems(ItemStack stack, Material mat, MaterialType<?> type, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        if (mat.has(TOOLS)){
            ToolData toolData = TOOLS.get(mat);
            tooltip.add(Utils.literal("Q: " + toolData.toolQuality() + " - S: " + toolData.toolSpeed() + " - D: " + toolData.toolDurability()).withStyle(ChatFormatting.BLUE));
        }
        if (!mat.getChemicalFormula().isEmpty()) {
            if (Screen.hasShiftDown()) {
                tooltip.add(Utils.translatable("gtlib.tooltip.chemical_formula", Utils.literal(mat.getChemicalFormula()).withStyle(ChatFormatting.DARK_AQUA)));
                tooltip.add(Utils.translatable("gtlib.tooltip.mass", Utils.literal(mat.getMass() + "").withStyle(ChatFormatting.DARK_AQUA)));
                tooltip.add(Utils.translatable("gtlib.tooltip.atomic_number", Utils.literal(mat.getProtons() + "").withStyle(ChatFormatting.DARK_AQUA)));
            } else {
                tooltip.add(Utils.translatable("gtlib.tooltip.formula").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.ITALIC));
            }
        }
        if (mat.getElement() != null){
            tooltip.add(Utils.literal("Is Element"));
        }
        if (stack.getItem() instanceof MaterialItem) {
            tooltip.add(Utils.translatable("gtlib.tooltip.material_modid", Utils.getModName(mat.materialDomain())));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (type == GTMaterialTypes.BEARING_ROCK || type == ROCK){
            return tryPlace(new BlockPlaceContext(context));
        }
        return super.useOn(context);
    }

    public InteractionResult tryPlace(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        } else {
            BlockState blockstate;
            if (this.type == BEARING_ROCK){
                BlockState existing = WorldGenHelper.getStoneStateForRock(context.getClickedPos().getY() - 1, context.getClickedPos(), context.getLevel());
                StoneType type = WorldGenHelper.STONE_MAP.get(existing) != null ? WorldGenHelper.STONE_MAP.get(existing) : VanillaStoneTypes.STONE;
                blockstate = GTMaterialTypes.BEARING_ROCK.get().get(material, type).asState();
            } else {
                blockstate = ROCK.get().get(material).asState();
            }
            if (blockstate == null) {
                return InteractionResult.FAIL;
            } else if (!context.getLevel().setBlock(context.getClickedPos(), blockstate, 11)) {
                return InteractionResult.FAIL;
            } else {
                BlockPos blockpos = context.getClickedPos();
                Level world = context.getLevel();
                Player playerentity = context.getPlayer();
                ItemStack itemstack = context.getItemInHand();
                BlockState blockstate1 = world.getBlockState(blockpos);
                Block block = blockstate1.getBlock();
                if (block == blockstate.getBlock()) {
                    blockstate1 = this.updateBlockStateFromTag(blockpos, world, itemstack, blockstate1);
                    this.onBlockPlaced(blockpos, world, playerentity, itemstack, blockstate1);
                    block.setPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
                    if (playerentity instanceof ServerPlayer) {
                        CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer) playerentity, blockpos, itemstack);
                    }
                }

                if (!context.getPlayer().isCreative()){
                    context.getItemInHand().shrink(1);
                }
                SoundType soundtype = blockstate1.getSoundType();
                world.playSound(playerentity, blockpos, blockstate.getBlock().getSoundType(blockstate1).getPlaceSound(), SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);

                return InteractionResult.sidedSuccess(world.isClientSide);
            }
        }
    }

    protected boolean onBlockPlaced(BlockPos pos, Level worldIn, @Nullable Player player, ItemStack stack, BlockState state) {
        return BlockItem.updateCustomBlockEntityTag(worldIn, player, pos, stack);
    }

    private BlockState updateBlockStateFromTag(BlockPos p_219985_1_, Level p_219985_2_, ItemStack p_219985_3_, BlockState p_219985_4_) {
        BlockState blockstate = p_219985_4_;
        CompoundTag compoundnbt = p_219985_3_.getTag();
        if (compoundnbt != null) {
            CompoundTag compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
            StateDefinition<Block, BlockState> statecontainer = p_219985_4_.getBlock().getStateDefinition();

            for (String s : compoundnbt1.getAllKeys()) {
                Property<?> property = statecontainer.getProperty(s);
                if (property != null) {
                    String s1 = compoundnbt1.get(s).getAsString();
                    blockstate = updateState(blockstate, property, s1);
                }
            }
        }

        if (blockstate != p_219985_4_) {
            p_219985_2_.setBlock(p_219985_1_, blockstate, 2);
        }

        return blockstate;
    }

    private static <T extends Comparable<T>> BlockState updateState(BlockState state, Property<T> property, String value) {
        return property.getValue(value).map((p_219986_2_) -> {
            return state.setValue(property, p_219986_2_);
        }).orElse(state);
    }

    public static InteractionResult interactWithCauldron(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (world.isClientSide()) return InteractionResult.PASS;
        MaterialItem item = (MaterialItem) stack.getItem();
        MaterialType<?> type = item.getType();
        if (state.getBlock() instanceof AbstractCauldronBlock){
            int level = state.getValue(LayeredCauldronBlock.LEVEL);
            if (level > 0){
                Material material = ((MaterialItem) stack.getItem()).getMaterial();
                if (type == GTMaterialTypes.IMPURE_DUST || type == GTMaterialTypes.PURE_DUST) {
                    if (material.has(DUST)) {
                        stack.shrink(1);
                        if (!player.addItem(DUST.get(material, 1))) {
                            player.drop(DUST.get(material, 1), false);
                        }
                        Material oreByProduct = !material.getByProducts().isEmpty() ? material.getByProducts().get(0) : material;
                        Material oreByProduct2 = material.getByProducts().size() > 1 ? material.getByProducts().get(1) : oreByProduct;
                        Material byProduct = type == IMPURE_DUST ? oreByProduct : oreByProduct2;
                        if (byProduct.has(DUST) && world.random.nextInt(100) < 50){
                            if (!player.addItem(TINY_DUST.get(oreByProduct, 1))) {
                                player.drop(TINY_DUST.get(oreByProduct, 1), false);
                            }
                        }
                        LayeredCauldronBlock.lowerFillLevel(state, world, pos);
                        world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                } else if (type == GTMaterialTypes.CRUSHED_ORE) {
                    if (material.has(PURIFIED_ORE)) {
                        stack.shrink(1);
                        if (!player.addItem(PURIFIED_ORE.get(material, 1))) {
                            player.drop(PURIFIED_ORE.get(material, 1), false);
                        }
                        Material oreByProduct = !material.getByProducts().isEmpty() ? material.getByProducts().get(0) : material;
                        if (oreByProduct.has(DUST) && world.random.nextInt(100) < 50){
                            if (!player.addItem(TINY_DUST.get(oreByProduct, 1))) {
                                player.drop(TINY_DUST.get(oreByProduct, 1), false);
                            }
                        }
                        LayeredCauldronBlock.lowerFillLevel(state, world, pos);
                        world.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return InteractionResult.SUCCESS;
                    }
                }
            }

        }
        return InteractionResult.PASS;

    }

    public TagKey<Item> getTag() {
        return type.getMaterialTag(this.material);
    }

    public static boolean hasType(ItemStack stack, MaterialType<?> type) {
        return stack.getItem() instanceof MaterialItem && ((MaterialItem) stack.getItem()).getType() == type;
    }

    public static boolean hasMaterial(ItemStack stack, Material material) {
        return stack.getItem() instanceof MaterialItem && ((MaterialItem) stack.getItem()).getMaterial() == material;
    }

    public static MaterialType<?> getType(ItemStack stack) {
        if (!(stack.getItem() instanceof MaterialItem)) return null;
        return ((MaterialItem) stack.getItem()).getType();
    }

    public static Material getMaterial(ItemStack stack) {
        if (!(stack.getItem() instanceof MaterialItem)) return null;
        return ((MaterialItem) stack.getItem()).getMaterial();
    }

    public static boolean doesShowExtendedHighlight(ItemStack stack) {
        return hasType(stack, GTMaterialTypes.PLATE);
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        if (i == 0) {
            if ((material.has(MaterialTags.NEGATIVE_CHANGING_RGB) || material.has(MaterialTags.POSITIVE_CHANGING_RGB) || material.has(RAINBOW_RGB)) && FMLEnvironment.dist.isClient()){
                return getChangingMaterialColor();
            }
            return material.getRGB();
        }
        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    private int getChangingMaterialColor(){
        long time = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.level().getGameTime() : -1;
        long currentRemainder = time >= 0 ? time % 100 : -1;
        if (currentRemainder >= 0){
            int direction = (int) (currentRemainder < 50 ? currentRemainder : -(currentRemainder - 50));
            int rgb = material.getRGB();
            if (material.has(RAINBOW_RGB)){
                switch((int)(time % 50)) {
                    case   0, 1, 2, 3, 4 -> rgb = ChatFormatting.RED.getColor();
                    case   5, 6, 7, 8, 9 -> rgb = ChatFormatting.GOLD.getColor();
                    case  10, 11, 12, 13, 14 -> rgb = ChatFormatting.YELLOW.getColor();
                    case  15, 16, 17, 18, 19 -> rgb = ChatFormatting.GREEN.getColor();
                    case  20, 21, 22, 23, 24 -> rgb = ChatFormatting.AQUA.getColor();
                    case  25, 26, 27, 28, 29 -> rgb = ChatFormatting.DARK_AQUA.getColor();
                    case  30, 31, 32, 33, 34 -> rgb = ChatFormatting.DARK_BLUE.getColor();
                    case  35, 36, 37, 38, 39 -> rgb = ChatFormatting.BLUE.getColor();
                    case  40, 41, 42, 43, 44 -> rgb = ChatFormatting.DARK_PURPLE.getColor();
                    case  45, 46, 47, 48, 49 -> rgb = ChatFormatting.LIGHT_PURPLE.getColor();
                }
            }

            int r = CodeUtils.getR(rgb);
            int g = CodeUtils.getG(rgb);
            int b = CodeUtils.getB(rgb);
            int newR, newG, newB;
            if (material.has(RAINBOW_RGB)){
                boolean
                        tNR = CodeUtils.inside(  0,  99, (time/2) % 300), tNG = CodeUtils.inside( 50, 149, (time/2) % 300), tNB = CodeUtils.inside(100, 199, (time/2) % 300),
                        tPR = CodeUtils.inside(100, 199, (time/2) % 300), tPG = CodeUtils.inside(150, 249, (time/2) % 300), tPB = CodeUtils.inside(200, 299, (time/2) % 300);
            } else {

            }
            newR = material.has(MaterialTags.POSITIVE_CHANGING_RGB) ? r + direction : r - direction;
            newG = material.has(MaterialTags.POSITIVE_CHANGING_RGB) ? g + direction : g - direction;
            newB = material.has(MaterialTags.POSITIVE_CHANGING_RGB) ? b + direction : b - direction;
            return CodeUtils.getRGB(newR, newG, newB);
        }
        return material.getRGB();
    }

    @Override
    public Texture[] getTextures() {
        return getMaterial().getSet().getTextures(getType());
    }
}
