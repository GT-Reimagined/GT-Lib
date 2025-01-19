package muramasa.antimatter.integration.curios.forge;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.integration.curios.ICuriosHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CuriosHelperImpl implements ICuriosHelper {
    @Override
    public Stream<ItemStack> getCuriosItems(String slotType, Player player) {
        if (!AntimatterAPI.isModLoaded("curios")) {
            return Stream.empty();
        }
        var handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().orElse(null);
        if (handler == null) {
            return Stream.empty();
        }
        List<ItemStack> stacks = new ArrayList<>();
        var curioMap = handler.getCurios();
        var stacksHandler = curioMap.get(slotType).getStacks();
        for (int i = 0; i < stacksHandler.getSlots(); i++) {
            if (!stacksHandler.getStackInSlot(i).isEmpty()) {
                stacks.add(stacksHandler.getStackInSlot(i));
            }
        }
        return stacks.stream();
    }
}
