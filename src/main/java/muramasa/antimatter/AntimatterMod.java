package muramasa.antimatter;

import muramasa.antimatter.registration.IAntimatterRegistrar;

public abstract class AntimatterMod implements IAntimatterRegistrar {
    public AntimatterMod() {
        AntimatterAPI.addRegistrar(this);
    }
}
