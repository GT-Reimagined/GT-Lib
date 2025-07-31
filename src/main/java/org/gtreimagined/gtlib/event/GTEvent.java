package org.gtreimagined.gtlib.event;

import org.gtreimagined.gtlib.registration.IGTRegistrar;
import net.minecraftforge.eventbus.api.Event;

public abstract class GTEvent extends Event {
    public final IGTRegistrar sender;

    public GTEvent(IGTRegistrar registrar) {
        this.sender = registrar;
    }
}

