package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.Ref;

public interface ISharedGTObject extends IGTObject {
    default String getDomain() {
        return Ref.SHARED_ID;
    }
}
