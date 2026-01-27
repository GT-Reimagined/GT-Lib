package org.gtreimagined.gtlib.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface IGTLibProvider extends DataProvider {

    // Only runs when dynamically generating assets/data
    void run();

    default boolean async() {
        return true;
    }

    default void onCompletion() {

    }

    @Override
    default CompletableFuture<?> run(CachedOutput hashCache) {
        //NOOP
        return null;
    }
}
