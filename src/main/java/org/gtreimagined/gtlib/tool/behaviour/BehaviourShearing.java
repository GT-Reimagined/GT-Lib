package org.gtreimagined.gtlib.tool.behaviour;

import org.gtreimagined.gtlib.behaviour.IInteractEntity;
import org.gtreimagined.gtlib.tool.IBasicGTTool;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;

public class BehaviourShearing implements IInteractEntity<IBasicGTTool> {
    public static final BehaviourShearing INSTANCE = new BehaviourShearing();
    @Override
    public String getId() {
        return "shearing";
    }

    @Override
    public InteractionResult interactLivingEntity(IBasicGTTool instance, ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (!player.getLevel().isClientSide && interactionTarget instanceof Sheep sheep && sheep.readyForShearing()){
            sheep.shear(SoundSource.PLAYERS);
            sheep.gameEvent(GameEvent.SHEAR, player);
            stack.hurtAndBreak(1, player, (playerx) -> {
                playerx.broadcastBreakEvent(usedHand);
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
