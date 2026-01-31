package org.gtreimagined.gtlib.item;

import lombok.Getter;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.energy.ItemEnergyHandler;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.gtreimagined.tesseract.api.context.TesseractItemContext;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;
import org.gtreimagined.tesseract.api.eu.IEnergyHandlerItem;
import org.gtreimagined.tesseract.api.eu.IEnergyItem;

import java.util.List;
import java.util.Optional;

public class ItemBattery extends ItemBasic<ItemBattery> implements IEnergyItem {

    @Getter
    protected Tier tier;
    protected final long cap;
    protected final int amps;
    @Getter
    protected final boolean reusable;

    public ItemBattery(String domain, String id, Tier tier, long cap, boolean reusable) {
        this(domain, id, tier, cap, 1, reusable);
    }

    public ItemBattery(String domain, String id, Tier tier, long cap, int amps, boolean reusable) {
        super(domain, id);
        this.tier = tier;
        this.cap = cap;
        this.amps = amps;
        this.reusable = reusable;
    }

    public long getCapacity() {
        return cap;
    }

    @Override
    public void fillItemCategory(ResourceKey<CreativeModeTab> group, NonNullList<ItemStack> items) {
        if (this.allowedIn(group)) {
            ItemStack stack = new ItemStack(this);
            items.add(stack.copy());
            items.add(getFilledBattery(this));
        }
    }

    public static ItemStack getFilledBattery(ItemBasic<?> item) {
        ItemStack stack = item.getDefaultInstance();
        if (!(item instanceof ItemBattery battery)) return stack;
        stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA).putLong(Ref.KEY_ITEM_ENERGY, battery.cap);
        return stack;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        long energy = stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA).getLong(Ref.KEY_ITEM_ENERGY);
        if (energy <= 0) return super.getBarColor(stack);
        return 0x00BFFF;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return (int)(13.0f* (stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA).getLong(Ref.KEY_ITEM_ENERGY) / (double) cap));
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!world.isClientSide() && player.isCrouching()) {
            boolean newMode = chargeModeSwitch(stack);
            player.displayClientMessage(Utils.translatable(newMode ? "message.discharge.on" : "message.discharge.off"), false);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    private boolean canDischarge(ItemStack stack) {
        CompoundTag nbt = stack.getTag();
        if (nbt == null || !nbt.contains(Ref.TAG_ITEM_ENERGY_DATA)) return true;
        CompoundTag energyTag = nbt.getCompound(Ref.TAG_ITEM_ENERGY_DATA);
        if (!energyTag.contains(Ref.KEY_ITEM_DISCHARGE_MODE)) return true;
        return energyTag.getBoolean(Ref.KEY_ITEM_DISCHARGE_MODE);
    }

    public boolean chargeModeSwitch(ItemStack stack) {
        boolean discharge = !canDischarge(stack);
        CompoundTag energyTag = stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA);
        energyTag.putBoolean(Ref.KEY_ITEM_DISCHARGE_MODE, discharge);
        return discharge;
    }

    private static Optional<ItemEnergyHandler> getCastedHandler(ItemStack stack) {
        Optional<IEnergyHandlerItem> itemHandler = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).resolve();
        return itemHandler.map(e -> (ItemEnergyHandler)e);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Utils.translatable("gtlib.tooltip.battery.tier", Utils.literal(tier.getId().toUpperCase()).withStyle(tier.getRarityFormatting())));
        if (reusable) {
            tooltip.add(Utils.translatable("item.reusable"));
        }
        if (amps > 1){
            boolean red = worldIn == null || worldIn.getGameTime() % 20 < 10;
            tooltip.add(Utils.translatable("item.amps", amps).withStyle(red ? ChatFormatting.DARK_RED : ChatFormatting.WHITE));
        }
        long energy = stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(IEnergyHandler::getEnergy).orElse(0L);
        tooltip.add(Utils.translatable("item.charge").append(": ").append(Utils.literal(energy + "/" + cap).withStyle(energy == 0 ? ChatFormatting.RED : ChatFormatting.GREEN)).append(" (" + tier.getId().toUpperCase() + ")"));
        super.appendHoverText(stack, worldIn, tooltip, flag);
    }

    @Override
    public IEnergyHandlerItem createEnergyHandler(TesseractItemContext context) {
        return new ItemEnergyHandler(context, cap, isReusable() ? tier.getVoltage() : 0, tier.getVoltage(), reusable ? 2 : 0, amps);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (stack.getTag() != null){
            long energy = stack.getOrCreateTagElement(Ref.TAG_ITEM_ENERGY_DATA).getLong(Ref.KEY_ITEM_ENERGY);
            if (energy > 0) return 1;
        }
        return super.getMaxStackSize(stack);
    }
}
