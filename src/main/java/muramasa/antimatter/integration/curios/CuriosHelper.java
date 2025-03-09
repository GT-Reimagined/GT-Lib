package muramasa.antimatter.integration.curios;

import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.util.ImplLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class CuriosHelper {
    public static Stream<ItemStack> getCuriosItems(Player player, String... slotTypes){
        if (!AntimatterAPI.isModLoaded("curios")) {
            return Stream.empty();
        }
        var handler = CuriosApi.getCuriosHelper().getCuriosHandler(player).resolve().orElse(null);
        if (handler == null) {
            return Stream.empty();
        }
        List<ItemStack> stacks = new ArrayList<>();
        var curioMap = handler.getCurios();
        for (String slotType : slotTypes) {
            if (!curioMap.containsKey(slotType)) continue;
            var stacksHandler = curioMap.get(slotType).getStacks();
            for (int i = 0; i < stacksHandler.getSlots(); i++) {
                if (!stacksHandler.getStackInSlot(i).isEmpty()) {
                    stacks.add(stacksHandler.getStackInSlot(i));
                }
            }
        }
        return stacks.stream();
    }
}
