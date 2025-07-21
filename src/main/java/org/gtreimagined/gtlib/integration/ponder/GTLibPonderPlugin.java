package org.gtreimagined.gtlib.integration.ponder;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.gtreimagined.gtlib.Ref;

public class GTLibPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return Ref.ID;
    }

}
