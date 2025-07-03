package org.gtreimagined.gtlib.pipe;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityCable;
import org.gtreimagined.gtlib.blockentity.pipe.BlockEntityPipe;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.pipe.types.Cable;
import org.gtreimagined.gtlib.texture.Texture;
import org.gtreimagined.gtlib.tool.GTToolType;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockCable<T extends Cable<T>> extends BlockPipe<T> {

    public static final int INSULATION_COLOR = 0x404040;
    public final boolean insulated;

    public BlockCable(T type, PipeSize size, boolean insulated) {
        super(type.getId(), type, size, 2);
        this.insulated = insulated;
        String prefix = insulated ? "cable" : "wire";
        this.side = new Texture(Ref.ID, "block/pipe/" + prefix + "_side");
        this.faces = new Texture[]{
                new Texture(Ref.ID, "block/pipe/" + "wire_vtiny"),
                new Texture(Ref.ID, "block/pipe/" + "wire_tiny"),
                new Texture(Ref.ID, "block/pipe/" + "wire_small"),
                new Texture(Ref.ID, "block/pipe/" + "wire_normal"),
                new Texture(Ref.ID, "block/pipe/" + "wire_large"),
                new Texture(Ref.ID, "block/pipe/" + "wire_huge")
        };
    }

    public int getFlammability(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return 300;
    }

    public boolean isFlammable(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public GTToolType getToolType() {
        return GTTools.WIRE_CUTTER;
    }

    public boolean isFireSource(BlockState state, LevelReader world, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public int getBlockColor(BlockState state, @Nullable BlockGetter world, @Nullable BlockPos pos, int i) {
        if (!(state.getBlock() instanceof BlockCable) && world == null || pos == null) return -1;
        BlockEntityPipe<?> pipe = getTilePipe(world, pos);
        if (insulated && pipe != null && pipe.getPipeColor() != -1 && i == 0) return pipe.getPipeColor();
        if (insulated) return i == 1 ? getRGB() : i == 0 ? INSULATION_COLOR : -1;
        return i == 0 || i == 1 ? getRGB() : -1;
    }

    @Override
    public int getItemColor(ItemStack stack, @Nullable Block block, int i) {
        if (insulated && stack.getTag() != null && stack.getTag().contains(Ref.KEY_PIPE_TILE_COLOR) && i == 0){
            return stack.getTag().getInt(Ref.KEY_PIPE_TILE_COLOR);
        }
        return insulated ? i == 1 ? getRGB() : i == 0 ? INSULATION_COLOR : -1 : getRGB();
    }

    @Override
    public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
        super.entityInside(state, worldIn, pos, entityIn);
        if (worldIn.isClientSide) return;
        if (this.insulated) return;
        if (entityIn instanceof LivingEntity entity) {
            if (worldIn.getBlockEntity(pos) instanceof BlockEntityCable<?> cable) {
                if (cable.getNetwork().cableIsActive.containsKey(worldIn.dimension().location()) && cable.getNetwork().cableIsActive.get(worldIn.dimension().location()).contains(pos.asLong())){
                    entity.hurt(DamageSource.GENERIC, this.getType().getTier().getIntegerId());
                }
            }
        }
    }

    @Override
    public List<String> getInfo(List<String> info, Level world, BlockState state, BlockPos pos, boolean simple) {
        return info;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        tooltip.add(Utils.translatable("generic.amp").append(": ").append(Utils.literal(String.valueOf(this.type.getAmps(this.size))).withStyle(ChatFormatting.GREEN)));
        tooltip.add(Utils.translatable("generic.voltage").append(": ").append(Utils.literal(String.valueOf(this.type.getTier().getVoltage())).withStyle(ChatFormatting.BLUE)));
        tooltip.add(Utils.translatable("generic.loss").append(": ").append(Utils.literal(String.valueOf(this.type.getLoss())).withStyle(ChatFormatting.BLUE)));

        if (!Screen.hasShiftDown()) {
            tooltip.add(Utils.translatable("gtlib.tooltip.more").withStyle(ChatFormatting.DARK_AQUA));
        } else {
            tooltip.add(Utils.literal("----------"));
            tooltip.add(Utils.translatable("gtlib.pipe.cable.info").withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Utils.literal("----------"));
        }
    }
}