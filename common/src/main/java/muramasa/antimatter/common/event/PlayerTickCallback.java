package muramasa.antimatter.common.event;

import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface PlayerTickCallback {
    List<PlayerTickCallback> PLAYER_TICK_CALLBACKS = new ArrayList<>();

    void onTick(boolean end, boolean logicalServer, Player player);
}
