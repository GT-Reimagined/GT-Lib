package org.gtreimagined.gtlib.gui.event;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public class SlotClickEvent implements IGuiEvent {

    public static final IGuiEventFactory SLOT_CLICKED = GTAPI.register(IGuiEventFactory.class, new IGuiEventFactory() {
        @Override
        public BiFunction<IGuiEventFactory, FriendlyByteBuf, IGuiEvent> factory() {
            return SlotClickEvent::new;
        }

        @Override
        public String getId() {
            return "slot_click";
        }
    });

    public final SlotType<?> type;
    public final int index;

    public SlotClickEvent(IGuiEventFactory factory, FriendlyByteBuf buffer) {
        this.index = buffer.readVarInt();
        ResourceLocation loc = buffer.readResourceLocation();
        this.type = GTAPI.get(SlotType.class, loc.getPath(), loc.getNamespace());
    }

    public SlotClickEvent(int slotIndex, SlotType<?> type) {
        this.index = slotIndex;
        this.type = type;
    }

    @Override
    public boolean forward() {
        return false;
    }

    private IFluidHandler tryGetCap(IGuiHandler handler) {
        if (handler instanceof BlockEntityMachine) {
            BlockEntityMachine<?> machine = (BlockEntityMachine<?>) handler;
            return machine.fluidHandler.map(f -> type == SlotType.FL_IN && f.getInputTanks() != null ? f.getInputTanks().getTank(index) : type == SlotType.FL_OUT && f.getOutputTanks() != null ? f.getOutputTanks().getTank(f.offsetTank(index)) : f.getGuiHandler()).orElse(null);
        }
        if (handler instanceof BlockEntity be) {
            return be.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).resolve().orElse(null);
        }
        return null;
    }

    @Override
    public void handle(Player player, GuiInstance instance) {
        IFluidHandler sink = tryGetCap(instance.handler);
        if (sink == null) return;
        ItemStack stack = player.containerMenu.getCarried();
        if (stack.isEmpty()) return;
        if (type == SlotType.FL_IN || type == SlotType.FL_OUT) {
            Consumer<ItemStack> consumer = s -> {
                if (player.isCreative()) return;
                boolean single = stack.getCount() == 1;
                stack.shrink(1);
                if (single){
                    player.containerMenu.setCarried(s);
                } else {
                    if (!player.addItem(s)){
                        player.drop(s, true);
                    }
                }
            };
            if (type == SlotType.FL_IN){
                if (!FluidUtils.emptyItemIntoContainer(-1, Utils.ca(1, stack), sink, consumer)){
                    FluidUtils.fillItemFromContainer(-1, Utils.ca(1, stack), sink, consumer);
                }
            } else {
                FluidUtils.fillItemFromContainer(-1, Utils.ca(1, stack), sink, consumer);
            }
        }
    }

    @Override
    public IGuiEventFactory getFactory() {
        return SLOT_CLICKED;
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(this.index);
        buffer.writeResourceLocation(new ResourceLocation(type.getDomain(), type.getId()));
    }

    public static void init() {

    }
}
