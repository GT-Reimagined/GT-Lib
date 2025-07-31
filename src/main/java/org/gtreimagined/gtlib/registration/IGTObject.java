package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.Ref;
import net.minecraft.resources.ResourceLocation;

public interface IGTObject {

    default String getDomain() {
        return Ref.ID;
    }

    String getId();

    default boolean shouldRegister() {
        return true;
    }

    default ResourceLocation getLoc() {
        return new ResourceLocation(getDomain(), getId());
    }

    /**
     * Translates this GTObject. Return null if there is no implementable translation.
     * @param lang the language to provide for.
     * @return a component to render.
     */
    default String getLang(String lang) {
        return null;
    }
}
