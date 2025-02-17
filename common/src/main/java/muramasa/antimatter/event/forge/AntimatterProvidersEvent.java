package muramasa.antimatter.event.forge;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muramasa.antimatter.datagen.IAntimatterProvider;
import muramasa.antimatter.registration.IAntimatterRegistrar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AntimatterProvidersEvent extends AntimatterEvent implements IModBusEvent {
    private final List<IAntimatterProvider> providers = new ObjectArrayList<>(10);

    public AntimatterProvidersEvent(IAntimatterRegistrar registrar) {
        super(registrar);
    }

    public void addProvider(Supplier<IAntimatterProvider> provider) {
        providers.add(provider.get());
    }

    public Collection<IAntimatterProvider> getProviders() {
        return providers;
    }
}
