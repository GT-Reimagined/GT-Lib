package org.gtreimagined.gtlib.mui;

import brachy.modularui.drawable.progress.CircularProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable.Direction;
import lombok.Getter;
import org.gtreimagined.gtlib.util.int2;

public enum BarDir {

    UP(false, new int2(72, 18)),
    DOWN(false, new int2(72, 18)),
    LEFT(false, new int2(72, 18)),
    RIGHT(false, new int2(72, 18)),
    CW(true, new int2(72, 18)),
    CCW(true, new int2(72, 18));

    @Getter
    private final int2 pos;
    @Getter
    private final boolean circular;

    BarDir(boolean circular, int2 pos) {
        this.pos = pos;
        this.circular = circular;
    }

    public Direction toRegularDirection(){
        return switch (this) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case RIGHT -> Direction.RIGHT;
            default -> Direction.LEFT;
        };
    }

    public CircularProgressDrawable.Direction toCircularDirection(){
        return this == CCW ? CircularProgressDrawable.Direction.CCW : CircularProgressDrawable.Direction.CW;
    }

}
