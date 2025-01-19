package muramasa.antimatter.integration.curios;

import muramasa.antimatter.util.ImplLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.stream.Stream;

public interface ICuriosHelper {
    ICuriosHelper INSTANCE = ImplLoader.load(ICuriosHelper.class);
    Stream<ItemStack> getCuriosItems(String slotType, Player player);
}
