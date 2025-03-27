package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.Ref;

public interface ISharedAntimatterObject extends IAntimatterObject {
    default String getDomain() {
        return Ref.SHARED_ID;
    }
}
