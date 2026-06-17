package org.gtreimagined.gtlib.mixin.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;
import net.minecraftforge.items.wrapper.PlayerMainInvWrapper;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import yalter.mousetweaks.Main;

@OnlyIn(Dist.CLIENT)
@Debug(export = true)
@Mixin(Main.class)
public class MainMixin {
    @Definition(id = "selectedSlot", local = @Local(argsOnly = true, type = Slot.class))
    @Definition(id = "container", field = "Lnet/minecraft/world/inventory/Slot;container:Lnet/minecraft/world/Container;", remap = true)
    @Definition(id = "mc", field = "Lyalter/mousetweaks/Main;mc:Lnet/minecraft/client/Minecraft;", remap = true)
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", remap = true)
    @Definition(id = "getInventory", method = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;", remap = true)
    @Expression("selectedSlot.container != mc.player.getInventory()")
    @ModifyExpressionValue(method = {"findPushSlots", "findPullSlot"}, at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    private static boolean wrapPlayerSlot(boolean original, @Local(argsOnly = true) Slot selectedSlot){
        return original && !gtlib$isPlayerSlot(selectedSlot);
    }

    @Definition(id = "slot", local = @Local(type = Slot.class, ordinal = 1))
    @Definition(id = "container", field = "Lnet/minecraft/world/inventory/Slot;container:Lnet/minecraft/world/Container;", remap = true)
    @Definition(id = "mc", field = "Lyalter/mousetweaks/Main;mc:Lnet/minecraft/client/Minecraft;", remap = true)
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", remap = true)
    @Definition(id = "getInventory", method = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;", remap = true)
    @Expression("slot.container == mc.player.getInventory()")
    @ModifyExpressionValue(method = "findPushSlots", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    private static boolean wrapPlayerSlot2(boolean original, @Local(type = Slot.class, name = "slot") Slot slot){
        return original || gtlib$isPlayerSlot(slot);
    }
    @Definition(id = "slot", local = @Local(type = Slot.class, ordinal = 1))
    @Definition(id = "container", field = "Lnet/minecraft/world/inventory/Slot;container:Lnet/minecraft/world/Container;", remap = true)
    @Definition(id = "mc", field = "Lyalter/mousetweaks/Main;mc:Lnet/minecraft/client/Minecraft;", remap = true)
    @Definition(id = "player", field = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;", remap = true)
    @Definition(id = "getInventory", method = "Lnet/minecraft/client/player/LocalPlayer;getInventory()Lnet/minecraft/world/entity/player/Inventory;", remap = true)
    @Expression("slot.container == mc.player.getInventory()")
    @ModifyExpressionValue(method = "findPullSlot", at = @At(value = "MIXINEXTRAS:EXPRESSION"), remap = false)
    private static boolean wrapPlayerSlot3(boolean original, @Local(type = Slot.class, name = "slot") Slot slot){
        return original || gtlib$isPlayerSlot(slot);
    }

    @Unique
    private static boolean gtlib$isPlayerSlot(Slot slot){
        if (slot instanceof SlotItemHandler slotItemHandler){
            return (slotItemHandler.getItemHandler() instanceof PlayerInvWrapper && slotItemHandler.getSlotIndex() >=0 && slotItemHandler.getSlotIndex() < 36) ||
                    slotItemHandler.getItemHandler() instanceof PlayerMainInvWrapper;
        }
        return slot.container instanceof Inventory;
    }
}
