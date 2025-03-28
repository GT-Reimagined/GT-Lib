package org.gtreimagined.gtlib.event;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class AntimatterProvidersEvent extends AntimatterEvent implements IModBusEvent {
    private final List<IGTLibProvider> providers = new ObjectArrayList<>(10);

    public AntimatterProvidersEvent(IAntimatterRegistrar registrar) {
        super(registrar);
    }

    public void addProvider(Supplier<IGTLibProvider> provider) {
        providers.add(provider.get());
    }

    public Collection<IGTLibProvider> getProviders() {
        return providers;
    }
}
