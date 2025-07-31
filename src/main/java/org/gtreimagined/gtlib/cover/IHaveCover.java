package org.gtreimagined.gtlib.cover;

import org.gtreimagined.gtlib.machine.Tier;

public interface IHaveCover {
    CoverFactory getCover();

    default Tier getTier() {
        return null;
    }
}
