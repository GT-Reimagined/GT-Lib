package org.gtreimagined.gtlib.integration.jade;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import mcp.mobius.waila.api.ui.IElementHelper;
import mcp.mobius.waila.api.ui.IProgressStyle;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityBasicMultiMachine;
import org.gtreimagined.gtlib.capability.machine.MachineRecipeHandler;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.system.CallbackI.P;

import static org.gtreimagined.gtlib.machine.MachineFlag.EU;
import static org.gtreimagined.gtlib.machine.MachineFlag.FE;

public class MachineProvider implements IComponentProvider, IServerDataProvider<BlockEntity> {
    public static MachineProvider INSTANCE = new MachineProvider();
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (accessor.getBlockEntity() instanceof BlockEntityMachine<?> machine) {
            if (config.get(JadePlugin.PROGRESS)){
                MachineRecipeHandler<?> recipeHandler = machine.recipeHandler.orElse(null);
                if (recipeHandler != null && (!accessor.isServerConnected() || accessor.getServerData().contains("jadeProgress"))) {
                    IElementHelper helper = tooltip.getElementHelper();
                    long cur, max;
                    boolean active;
                    boolean isGenerator = recipeHandler.isGenerator();
                    if (accessor.isServerConnected()) {
                        cur = accessor.getServerData().getLong("jadeProgress");
                        max = accessor.getServerData().getLong("jadeMaxProgress");
                        active = accessor.getServerData().getBoolean("jadeActive");
                    } else {
                        cur = isGenerator ? recipeHandler.getPowerGenerated() : recipeHandler.getCurrentProgress();
                        max = isGenerator ? recipeHandler.getTotalPowerToGenerate() : recipeHandler.getMaxProgress();
                        active = machine.getMachineState() == MachineState.ACTIVE;
                    }
                    if (max > 0 && active){
                        String curText, maxText;
                        if (isGenerator){
                           curText = ChatFormatting.WHITE + String.valueOf(cur) + ChatFormatting.GRAY;
                           maxText = max + (machine.has(EU) ? " EU" : machine.has(FE) ? " FE" : "") + " Generated";
                        } else {
                            curText = ChatFormatting.WHITE + String.valueOf(max >= 20 ? Math.round(cur / 20.0) : cur) + ChatFormatting.GRAY;
                            maxText = (max >= 20 ? Math.round(max / 20.0) : max) + " " + (max >= 20 ? "s" : "t");
                        }
                        MutableComponent text = Utils.translatable("jade.fe", curText, maxText).withStyle(ChatFormatting.WHITE);
                        IProgressStyle progressStyle = helper.progressStyle().color(0xFF4CBB17, 0xFF4CBB17);
                        tooltip.add(helper.progress((float) cur / max, text, progressStyle, helper.borderStyle()).tag(JadePlugin.PROGRESS));
                    }
                }
            }
            if (machine instanceof BlockEntityBasicMultiMachine<?> multiMachine && config.get(JadePlugin.STRUCTURE)){
                if (!accessor.isServerConnected() || accessor.getServerData().contains("jadeStructureValid")){
                    boolean validStructure;
                    if (accessor.isServerConnected()) {
                        validStructure = accessor.getServerData().getBoolean("jadeStructureValid");
                    } else {
                        validStructure = multiMachine.isStructureValid();
                    }
                    if (validStructure) {
                        tooltip.add(tooltip.getElementHelper().text(Utils.translatable("gtlib.tooltip.valid_structure").withStyle(ChatFormatting.GREEN)).tag(JadePlugin.STRUCTURE));
                    } else {
                        tooltip.add(tooltip.getElementHelper().text(Utils.translatable("gtlib.tooltip.invalid_structure").withStyle(ChatFormatting.RED)).tag(JadePlugin.STRUCTURE));
                    }
                }

            }
        }
    }

    @Override
    public void appendServerData(CompoundTag compoundTag, ServerPlayer serverPlayer, Level level, BlockEntity blockEntity, boolean b) {
        if (blockEntity instanceof BlockEntityMachine<?> machine){
            machine.recipeHandler.ifPresent(r -> {
                if (r.isGenerator()){
                    compoundTag.putLong("jadeProgress", r.getPowerGenerated());
                    compoundTag.putLong("jadeMaxProgress", r.getTotalPowerToGenerate());
                } else {
                    compoundTag.putLong("jadeProgress", r.getCurrentProgress());
                    compoundTag.putLong("jadeMaxProgress", r.getMaxProgress());
                }
                compoundTag.putBoolean("jadeActive", machine.getMachineState() == MachineState.ACTIVE);
            });
            if (machine instanceof BlockEntityBasicMultiMachine<?> multiMachine){
                compoundTag.putBoolean("jadeStructureValid", multiMachine.isStructureValid());
            }
        }
    }
}
