package org.gtreimagined.gtlib.client;

import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.container.ContainerBasicMachine;
import org.gtreimagined.gtlib.gui.container.ContainerCover;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;
import org.gtreimagined.gtlib.gui.container.ContainerMultiMachine;
import org.gtreimagined.gtlib.gui.screen.GTContainerScreen;
import org.gtreimagined.gtlib.gui.screen.ScreenBasicMachine;
import org.gtreimagined.gtlib.gui.screen.ScreenCover;
import org.gtreimagined.gtlib.gui.screen.ScreenMachine;
import org.gtreimagined.gtlib.gui.screen.ScreenMultiMachine;
import net.minecraft.client.gui.screens.MenuScreens;

public class ClientData {
    public final static MenuScreens.ScreenConstructor SCREEN_BASIC = AntimatterAPI.register(MenuScreens.ScreenConstructor.class, "basic", Ref.ID, (MenuScreens.ScreenConstructor)(a, b, c) -> new ScreenBasicMachine((ContainerBasicMachine) a, b, c));
    public final static MenuScreens.ScreenConstructor SCREEN_MACHINE = AntimatterAPI.register(MenuScreens.ScreenConstructor.class, "machine", Ref.ID, (MenuScreens.ScreenConstructor)(a, b, c) -> new ScreenMachine((ContainerMachine) a, b, c));
    public final static MenuScreens.ScreenConstructor SCREEN_MULTI = AntimatterAPI.register(MenuScreens.ScreenConstructor.class, "multi", Ref.ID, (MenuScreens.ScreenConstructor)(a, b, c) -> new ScreenMultiMachine((ContainerMultiMachine) a, b, c));
    public final static MenuScreens.ScreenConstructor SCREEN_COVER = AntimatterAPI.register(MenuScreens.ScreenConstructor.class, "cover", Ref.ID, (MenuScreens.ScreenConstructor)(a, b, c) -> new ScreenCover((ContainerCover) a, b, c));
    public final static MenuScreens.ScreenConstructor SCREEN_DEFAULT = AntimatterAPI.register(MenuScreens.ScreenConstructor.class, "default", Ref.ID, (MenuScreens.ScreenConstructor)(a, b, c) -> new GTContainerScreen(a, b, c));

    public static void init(){
    }
}
