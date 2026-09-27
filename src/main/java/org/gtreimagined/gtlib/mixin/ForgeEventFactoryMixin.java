package org.gtreimagined.gtlib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent.EntityPlaceEvent;
import org.gtreimagined.gtlib.common.EntityPlaceEventExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeEventFactory.class, remap = false)
public class ForgeEventFactoryMixin {
    @Inject(method = "onBlockPlace", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z"))
    private static void gtlib$injectOnBlockPlace(Entity entity, BlockSnapshot blockSnapshot, Direction direction, CallbackInfoReturnable<Boolean> cir, @Local(name = "event") EntityPlaceEvent event){
        ((EntityPlaceEventExtension)event).gtlib_setPlacedDirection(direction);
    }
}
