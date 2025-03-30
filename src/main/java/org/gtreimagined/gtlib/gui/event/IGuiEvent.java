package org.gtreimagined.gtlib.gui.event;

import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.machine.event.IMachineEvent;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.function.BiFunction;

/**
 * A more powerful extension to regular machine events,
 * that is sent between server and client.
 * Can be simple enums handle in the tile
 * or like a packet that executes itself.
 */
public interface IGuiEvent extends IMachineEvent {

    /**
     * The underlying factory object. Can be compared using reference equality.
     * @return the factory.
     */
    IGuiEventFactory getFactory();

    /**
     * If this packet should be forwarded to the gui handler. Otherwise IGuiEvent#handle is caled.
     *
     * @return if this packet is forwarded to the handler.
     */
    default boolean forward() {
        return true;
    }

    /**
     * If IGuiEvent#forward is false, this is called instead.
     *
     * @param player   the player causing this Gui event.
     * @param instance the gui instance.
     */
    default void handle(Player player, GuiInstance instance) {

    }

    /**
     * Write this gui event to the packet buffer.
     * @param buffer the buffer to write to.
     */
    default void write(FriendlyByteBuf buffer) {

    }

    interface IGuiEventFactory extends ISharedGTObject {
        /**
         * Default method for simplicity.
         */
        default void register() {
            GTAPI.register(IGuiEventFactory.class, this);
        }

        /**
         * Helper method, to write a gui event with the factory.
         * @param event the gui instance.
         * @param buffer the buffer to write to.
         */
        default void write(IGuiEvent event, FriendlyByteBuf buffer) {
            buffer.writeUtf(this.getId());
            event.write(buffer);
        }

        /**
         * The supplier to handle server side instantiation.
         * @return
         */
        BiFunction<IGuiEventFactory, FriendlyByteBuf, IGuiEvent> factory();

        /**
         * Reads an IGuiEvent from a packetbuffer.
         * @param buffer to consume.
         * @return an IGuiEvent instance.
         */
        static IGuiEvent read(FriendlyByteBuf buffer) {
            IGuiEventFactory ev = GTAPI.get(IGuiEventFactory.class, buffer.readUtf(32767));
            return ev.factory().apply(ev, buffer);
        }
    }
}
