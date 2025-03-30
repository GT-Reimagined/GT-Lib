package org.gtreimagined.gtlib.gui;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.container.IGTContainer;
import org.gtreimagined.gtlib.registration.IGTObject;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import org.apache.commons.lang3.function.TriFunction;


//An arbitrary menu handler for e.g. guiclass.
public abstract class MenuHandler<T extends AbstractContainerMenu & IGTContainer> implements IGTObject {

    protected ResourceLocation loc;
    private MenuType<T> containerType;

    public MenuHandler(String domain, String id) {
        loc = new ResourceLocation(domain, id);
        GTAPI.register(MenuHandler.class, this);
        MenuType<?> type = getContainerType();
        GTAPI.register(MenuType.class, id, domain, type);
    }

    @Override
    public String getDomain() {
        return loc.getNamespace();
    }

    @Override
    public String getId() {
        return loc.getPath();
    }

    protected abstract T getMenu(IGuiHandler source, Inventory playerInv, int windowId);

    @MethodsReturnNonnullByDefault
    public final T menu(IGuiHandler source, Inventory playerInv, int windowId) {
        T t = getMenu(source, playerInv, windowId);
        //Gui Entrypoint for server.
        if (!source.isRemote()) t.source().init();
        return t;
    }

    @MethodsReturnNonnullByDefault
    public MenuType<T> getContainerType() {
        if (containerType == null) {
            containerType = create(this::onContainerCreate);
        }
        return containerType;
    }

    static <T extends AbstractContainerMenu> MenuType<T> create(TriFunction<Integer, Inventory, FriendlyByteBuf, T> factory) {
        return IForgeMenuType.create(factory::apply);
    }

    public abstract T onContainerCreate(int windowId, Inventory inv, FriendlyByteBuf data);

    public String screenID(){
        return "default";
    }

    public String screenDomain(){
        return Ref.ID;
    }
}
