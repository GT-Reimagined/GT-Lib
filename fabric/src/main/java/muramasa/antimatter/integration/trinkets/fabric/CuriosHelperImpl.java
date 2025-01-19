package muramasa.antimatter.integration.trinkets.fabric;

import muramasa.antimatter.integration.curios.ICuriosHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public class CuriosHelperImpl implements ICuriosHelper {

    @Override //TODO trinkets implementation
    public Stream<ItemStack> getCuriosItems(String slotType, Player player) {
        return Stream.empty();
    }
}
