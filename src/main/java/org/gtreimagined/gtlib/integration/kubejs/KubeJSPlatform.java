package org.gtreimagined.gtlib.integration.kubejs;

import org.gtreimagined.gtlib.util.ImplLoader;

public interface KubeJSPlatform {
    KubeJSPlatform INSTANCE = ImplLoader.load(KubeJSPlatform.class);
    void onRegister();
}
