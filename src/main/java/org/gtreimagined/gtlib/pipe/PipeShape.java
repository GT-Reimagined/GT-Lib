package org.gtreimagined.gtlib.pipe;

import org.gtreimagined.gtlib.registration.IGTObject;

import java.util.Locale;

public enum PipeShape implements IGTObject {

    ALL,
    ARROW,
    BASE,
    CORNER,
    CROSS,
    ELBOW,
    FIVE,
    LINE,
    SIDE,
    SINGLE;

    public static final PipeShape[] VALUES = values();

    @Override
    public String getId() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}
