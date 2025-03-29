package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.registration.IGTRegistrar;

public abstract class AntimatterMod implements IGTRegistrar {
    public AntimatterMod() {
        AntimatterAPI.addRegistrar(this);
    }
}
