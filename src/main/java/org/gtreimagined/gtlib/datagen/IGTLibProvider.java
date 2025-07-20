package org.gtreimagined.gtlib.datagen;

import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;

import java.io.IOException;

public interface IGTLibProvider extends DataProvider {

    // Only runs when dynamically generating assets/data
    void run();

    default boolean async() {
        return true;
    }

    default void onCompletion() {

    }

    @Override
    default void run(HashCache hashCache) throws IOException {
        //NOOP
    }
}
