package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.registration.IGTRegistrar;

public abstract class GTMod implements IGTRegistrar {
    public GTMod() {
        GTAPI.addRegistrar(this);
    }
}
