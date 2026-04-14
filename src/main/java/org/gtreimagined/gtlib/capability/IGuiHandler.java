package org.gtreimagined.gtlib.capability;

import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.event.IGuiEvent;
import org.gtreimagined.gtlib.network.packets.AbstractGuiEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Consumer;

public interface IGuiHandler {

    default void onGuiEvent(IGuiEvent event, Player player) {
        // NOOP
    }

    GuiProperties getGuiProperties();

    boolean isRemote();

    default void addWidgets(GuiInstance instance, IGuiElement parent) {
        if (this instanceof IHaveWidgets) {
            ((IHaveWidgets) this).getCallbacks().forEach(t -> t.accept(instance));
        }
    }

    ResourceLocation getGuiTexture();

    default int guiSize() {
        return getGuiProperties().getXSize();
    }

    default int guiHeight() {
        return getGuiProperties().getYSize();
    }

    default int guiTextureSize() {
        return getGuiProperties().getTextureXSize();
    }

    default int guiTextureHeight() {
        return getGuiProperties().getTextureYSize();
    }

    /**
     * Creates a gui packet, depending on the type of gui handler.
     *
     * @param event the event container.
     * @return a packet to send.
     */
    AbstractGuiEventPacket createGuiPacket(IGuiEvent event);

    String handlerDomain();

    interface IHaveWidgets {
        List<Consumer<GuiInstance>> getCallbacks();

        @Deprecated(forRemoval = true)
        default IHaveWidgets addGuiCallback(Consumer<GuiInstance> gui) {
            getCallbacks().add(gui);
            return this;
        }
    }
}
