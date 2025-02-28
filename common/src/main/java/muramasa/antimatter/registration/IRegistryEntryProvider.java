package muramasa.antimatter.registration;

import net.minecraft.core.Registry;
import net.minecraftforge.registries.IForgeRegistry;

public interface IRegistryEntryProvider extends IAntimatterObject {

    void onRegistryBuild(IForgeRegistry<?> registry);
}
