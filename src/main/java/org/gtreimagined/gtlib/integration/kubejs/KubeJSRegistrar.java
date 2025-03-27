package org.gtreimagined.gtlib.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPaths;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import org.gtreimagined.gtlib.AntimatterMod;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.datagen.AntimatterDynamics;
import org.gtreimagined.gtlib.datagen.providers.AntimatterBlockLootProvider;
import org.gtreimagined.gtlib.datagen.providers.AntimatterBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.AntimatterBlockTagProvider;
import org.gtreimagined.gtlib.datagen.providers.AntimatterItemModelProvider;
import org.gtreimagined.gtlib.datagen.providers.AntimatterItemTagProvider;
import org.gtreimagined.gtlib.datagen.providers.AntimatterLanguageProvider;
import org.gtreimagined.gtlib.event.AntimatterProvidersEvent;
import org.gtreimagined.gtlib.registration.RegistrationEvent;
import net.minecraftforge.api.distmarker.Dist;

import java.nio.file.Files;
import java.nio.file.LinkOption;

;

public class KubeJSRegistrar extends AntimatterMod {
    public KubeJSRegistrar() {
        super();
        AntimatterDynamics.clientProvider(Ref.MOD_KJS, () -> new AntimatterBlockStateProvider(Ref.MOD_KJS, "KubeJS BlockStates"));
        AntimatterDynamics.clientProvider(Ref.MOD_KJS, () -> new AntimatterItemModelProvider(Ref.MOD_KJS, "KubeJS Item Models"));
        AntimatterDynamics.clientProvider(Ref.MOD_KJS, () -> new AntimatterLanguageProvider(Ref.MOD_KJS, "KubeJS en_us Localization", "en_us"));
    }

    public static void providerEvent(AntimatterProvidersEvent ev) {
        final AntimatterBlockTagProvider[] p = new AntimatterBlockTagProvider[1];
        ev.addProvider(() -> {
            p[0] = new AntimatterBlockTagProvider(Ref.MOD_KJS, "KubeJS Block Tags", false);
            return p[0];
        });
        ev.addProvider(() ->
                new AntimatterItemTagProvider(Ref.MOD_KJS, "KubeJS Item Tags", false, p[0]));
        ev.addProvider(() -> new AntimatterBlockLootProvider(Ref.MOD_KJS, "KubeJS Loot generator"));
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
