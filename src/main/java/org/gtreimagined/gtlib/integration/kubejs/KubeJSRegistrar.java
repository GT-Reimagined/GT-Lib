package org.gtreimagined.gtlib.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import org.gtreimagined.gtlib.AntimatterMod;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.GTBlockTagProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemTagProvider;
import org.gtreimagined.gtlib.datagen.providers.GTLanguageProvider;
import org.gtreimagined.gtlib.event.AntimatterProvidersEvent;
import org.gtreimagined.gtlib.registration.RegistrationEvent;
import net.minecraftforge.api.distmarker.Dist;

import java.nio.file.Files;
import java.nio.file.LinkOption;

;

public class KubeJSRegistrar extends AntimatterMod {
    public KubeJSRegistrar() {
        super();
        GTLibDynamics.clientProvider(Ref.MOD_KJS, () -> new GTBlockStateProvider(Ref.MOD_KJS, "KubeJS BlockStates"));
        GTLibDynamics.clientProvider(Ref.MOD_KJS, () -> new GTItemModelProvider(Ref.MOD_KJS, "KubeJS Item Models"));
        GTLibDynamics.clientProvider(Ref.MOD_KJS, () -> new GTLanguageProvider(Ref.MOD_KJS, "KubeJS en_us Localization", "en_us"));
    }

    public static void providerEvent(AntimatterProvidersEvent ev) {
        final GTBlockTagProvider[] p = new GTBlockTagProvider[1];
        ev.addProvider(() -> {
            p[0] = new GTBlockTagProvider(Ref.MOD_KJS, "KubeJS Block Tags", false);
            return p[0];
        });
        ev.addProvider(() ->
                new GTItemTagProvider(Ref.MOD_KJS, "KubeJS Item Tags", false, p[0]));
        ev.addProvider(() -> new GTBlockLootProvider(Ref.MOD_KJS, "KubeJS Loot generator"));
    }

    @Override
    public String getId() {
        return Ref.MOD_KJS;
    }

    @Override
    public void onRegistrationEvent(RegistrationEvent event, Dist side) {
        if (event == RegistrationEvent.DATA_INIT){
            AntimatterKubeJS.loadStartup();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    public static void checkKubeJSServerScriptManager(){
        if (ServerScriptManager.instance == null){
            ServerScriptManager.instance = new ServerScriptManager();
            try {
                if (Files.notExists(KubeJSPaths.DATA, new LinkOption[0])) {
                    Files.createDirectories(KubeJSPaths.DATA);
                }

            } catch (Throwable var3) {
                throw new RuntimeException("KubeJS failed to register it's script loader!", var3);
            }
        }
    }
}
