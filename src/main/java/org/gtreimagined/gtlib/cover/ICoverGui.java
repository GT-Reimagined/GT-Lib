package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;

public interface ICoverGui extends ICover, MenuProvider, IGuiHandler {

    default Component getDisplayName() {
        return Utils.literal(Utils.underscoreToUpperCamel(this.getId()));
    }

    default String getDomain() {
        return Ref.ID;
    }
}
