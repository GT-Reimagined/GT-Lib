package org.gtreimagined.gtlib.event;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.datagen.IGTLibProvider;
import org.gtreimagined.gtlib.registration.IGTRegistrar;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GTProvidersEvent extends GTEvent implements IModBusEvent {
    private final List<IGTLibProvider> providers = new ObjectArrayList<>(10);

    public GTProvidersEvent(IGTRegistrar registrar) {
        super(registrar);
    }

    public void addProvider(Supplier<IGTLibProvider> provider) {
        providers.add(provider.get());
    }

    public Collection<IGTLibProvider> getProviders() {
        return providers;
    }
}
