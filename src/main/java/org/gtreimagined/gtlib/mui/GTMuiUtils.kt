package org.gtreimagined.gtlib.mui;

import brachy.modularui.api.value.IValue;
import brachy.modularui.value.sync.ModularSyncManager;
import brachy.modularui.value.sync.SyncHandler;

import java.util.Optional;

public class GTMuiUtils {
    public static  <T> Optional<T> getSyncedValue(String id, Class<T> tClass, ModularSyncManager syncManager){
        SyncHandler<?> handler = syncManager.findSyncHandler(id);
        IValue<T> value = handler.castValueNullable(tClass);
        return value == null ? Optional.empty() : Optional.of(value.getValue());
    }
}
