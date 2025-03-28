package org.gtreimagined.gtlib.event;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.gtreimagined.gtlib.datagen.ICraftingLoader;
import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.Collection;
import java.util.List;

public class GTCraftingEvent extends GTEvent implements IModBusEvent {


    private final List<ICraftingLoader> loaders = new ObjectArrayList<>();

    public GTCraftingEvent(IAntimatterRegistrar registrar) {
        super(registrar);
    }

    public void addLoader(ICraftingLoader loader) {
        this.loaders.add(loader);
    }

    public Collection<ICraftingLoader> getLoaders() {
        return loaders;
    }
}
