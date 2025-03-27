package org.gtreimagined.gtlib.util;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface NonNullSupplier<T> {
    @NotNull
    T get();
}
