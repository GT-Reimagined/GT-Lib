package org.gtreimagined.gtlib.client.event

import net.minecraft.world.item.UseAnim
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.client.event.RenderHighlightEvent
import net.minecraftforge.client.event.ScreenEvent
import net.minecraftforge.event.TickEvent
import net.minecraftforge.event.entity.player.ItemTooltipEvent
import net.minecraftforge.event.level.LevelEvent
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.client.SoundHelper
import org.gtreimagined.gtlib.material.Material
import org.gtreimagined.gtlib.material.MaterialColorChanger
import org.gtreimagined.gtlib.material.MaterialColorChanger.Companion.incrementTime
import org.gtreimagined.gtlib.material.MaterialType.Companion.addTooltip
import org.gtreimagined.gtlib.tool.IGTTool

object ClientEventsForge {
    @SubscribeEvent
    fun onBlockHighlight(event: RenderHighlightEvent.Block) {
        if (onBlockHighlight(event.levelRenderer, event.camera, event.target, event.partialTick, event.poseStack, event.multiBufferSource))
            event.setCanceled(true)
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    internal fun onTooltipAdd(ev: ItemTooltipEvent) {
        addTooltip(ev.itemStack, ev.toolTip, ev.entity, ev.flags)
        onItemTooltip(ev.itemStack, ev.toolTip, ev.entity, ev.flags)
    }

    //TODO why is this client only?
    //Needs some work, won't work in 3rd person also, needs special ItemModel properties
    @SubscribeEvent
    fun onPlayerTick(e: TickEvent.PlayerTickEvent) {
        if (e.phase == TickEvent.Phase.END) {
            val player = e.player
            if (player == null || player.mainHandItem.isEmpty) return
            val stack = player.mainHandItem
            if (stack.item !is IGTTool) return
            val item = stack.item as IGTTool
            if (item.getGTToolType().useAction != UseAnim.NONE && player.swinging) {
                item.item.onUseTick(player.level(), player, stack, stack.count)
                //player.swingProgress = player.prevSwingProgress;
            }
        }
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (event.phase == TickEvent.Phase.END) {
            MaterialColorChanger.RGB_CHANGING_MAP.forEach { (m: Material?, t: MaterialColorChanger?) -> t!!.tick() }
            incrementTime()
        }
    }

    /*@SubscribeEvent
    public static void onRenderDebugInfo(RenderGameOverlayEvent.Text e) {
        ClientEvents.onRenderDebugInfo(e.getLeft());
    }*/
    @SubscribeEvent
    fun onGuiMouseScrollPre(e: ScreenEvent.MouseScrolled) {
        onGuiMouseScrollPre(e.scrollDelta)
    }

    @SubscribeEvent
    fun onGuiMouseClickPre(e: ScreenEvent.MouseButtonPressed) {
        onGuiMouseClickPre(e.button)
    }

    @SubscribeEvent
    fun onGuiMouseReleasedPre(e: ScreenEvent.MouseButtonReleased) {
        onGuiMouseReleasedPre(e.button)
    }

    @SubscribeEvent
    fun worldUnload(ev: LevelEvent.Unload) {
        SoundHelper.worldUnload(ev.level)
    }
}
