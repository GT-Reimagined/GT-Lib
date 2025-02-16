package muramasa.antimatter.registration;

import muramasa.antimatter.event.MaterialEvent;
import net.minecraftforge.api.distmarker.Dist;

public interface IAntimatterRegistrar extends IAntimatterObject, IAntimatterRegistrarInitializer {

    default String getDomain() {
        return getId();
    }

    default boolean isEnabled() {
        return !getId().equals("minecraft");
    }

    void onRegistrationEvent(RegistrationEvent event, Dist side);

    default void onMaterialEvent(MaterialEvent event){}

    default int getPriority() {
        return 1000;
    }

}
