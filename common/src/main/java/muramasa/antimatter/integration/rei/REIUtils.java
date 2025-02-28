package muramasa.antimatter.integration.rei;

import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.view.ViewSearchBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class REIUtils {
    static List<Consumer<CategoryRegistry>> EXTRA_CATEGORIES = new ArrayList<>();
    static List<Consumer<DisplayRegistry>> EXTRA_DISPLAYS = new ArrayList<>();

    public static FluidStack fromREIFluidStack(dev.architectury.fluid.FluidStack from){
        return new FluidStack(from.getFluid(), (int) from.getAmount(), from.getTag());
    }

    public static dev.architectury.fluid.FluidStack toREIFLuidStack(FluidStack from){
        return dev.architectury.fluid.FluidStack.create(from.getFluid(), from.getAmount(), from.getTag());
    }

    public static <T> void addModDescriptor(List<Component> tooltip, T t) {

    }

    public static void addExtraDisplay(Consumer<DisplayRegistry> registry){
        EXTRA_DISPLAYS.add(registry);
    }

    public static void addExtraCategory(Consumer<CategoryRegistry> registry){
        EXTRA_CATEGORIES.add(registry);
    }

    public static void uses(FluidStack val, boolean USE) {
        EntryStack<?> stack = EntryStack.of(VanillaEntryTypes.FLUID, toREIFLuidStack(val));
        if (USE) ViewSearchBuilder.builder().addUsagesFor(stack).open();
        else ViewSearchBuilder.builder().addRecipesFor(stack).open();
    }

    public static void showCategories(ResourceLocation... categories) {
        ViewSearchBuilder.builder().addCategories(Arrays.stream(categories).map(CategoryIdentifier::of).collect(Collectors.toList())).open();
    }
}
