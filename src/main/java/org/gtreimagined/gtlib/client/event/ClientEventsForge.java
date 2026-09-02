package org.gtreimagined.gtlib.client.event;

import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.level.LevelEvent;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.client.SoundHelper;
import org.gtreimagined.gtlib.material.MaterialColorChanger;
import org.gtreimagined.gtlib.material.MaterialItem;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.tool.IGTTool;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Ref.ID, value = Dist.CLIENT)
public class ClientEventsForge {

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        if (ClientEvents.onBlockHighlight(event.getLevelRenderer(), event.getCamera(), event.getTarget(), event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource()))
            event.setCanceled(true);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    protected static void onTooltipAdd(final ItemTooltipEvent ev) {
        MaterialType.addTooltip(ev.getItemStack(), ev.getToolTip(), ev.getEntity(), ev.getFlags());
        ClientEvents.onItemTooltip(ev.getItemStack(), ev.getToolTip(), ev.getEntity(), ev.getFlags());
    }

    //TODO why is this client only?
    //Needs some work, won't work in 3rd person also, needs special ItemModel properties
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            Player player = e.player;
            if (player == null || player.getMainHandItem().isEmpty()) return;
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof IGTTool)) return;
            IGTTool item = (IGTTool) stack.getItem();
            if (item.getGTToolType().getUseAction() != UseAnim.NONE && player.swinging) {
                item.getItem().onUseTick(player.level(), player, stack, stack.getCount());
                //player.swingProgress = player.prevSwingProgress;
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent event){
        if (event.phase == Phase.END){
            MaterialColorChanger.RGB_CHANGING_MAP.forEach((m, t) -> t.tick());
            MaterialColorChanger.incrementTime();
        }
    }

    /*@SubscribeEvent
    public static void onRenderDebugInfo(RenderGameOverlayEvent.Text e) {
        ClientEvents.onRenderDebugInfo(e.getLeft());
    }*/

    @SubscribeEvent
    public static void onGuiMouseScrollPre(ScreenEvent.MouseScrolled e) {
        ClientEvents.onGuiMouseScrollPre(e.getScrollDelta());
    }
    @SubscribeEvent
    public static void onGuiMouseClickPre(ScreenEvent.MouseButtonPressed e) {
        ClientEvents.onGuiMouseClickPre(e.getButton());
    }

    @SubscribeEvent
    public static void onGuiMouseReleasedPre(ScreenEvent.MouseButtonReleased e) {
        ClientEvents.onGuiMouseReleasedPre(e.getButton());
    }

    @SubscribeEvent
    public static void worldUnload(LevelEvent.Unload ev) {
        SoundHelper.worldUnload(ev.getLevel());
    }
}
