package org.gtreimagined.gtlib.event;

import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;
import net.minecraftforge.eventbus.api.Event;

public abstract class AntimatterEvent extends Event {
    public final IAntimatterRegistrar sender;

    public AntimatterEvent(IAntimatterRegistrar registrar) {
        this.sender = registrar;
    }
}

