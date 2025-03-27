package org.gtreimagined.gtlib;

import org.gtreimagined.gtlib.registration.IAntimatterRegistrar;

public abstract class AntimatterMod implements IAntimatterRegistrar {
    public AntimatterMod() {
        AntimatterAPI.addRegistrar(this);
    }
}
