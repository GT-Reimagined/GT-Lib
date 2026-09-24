package org.gtreimagined.gtlib.client.event

import com.google.common.collect.ImmutableSet
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator
import com.mojang.blaze3d.vertex.VertexConsumer
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.ChatFormatting
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.resources.model.ModelBakery
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.TranslatableContents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.item.UseAnim
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.shapes.CollisionContext
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.block.IInfoProvider
import org.gtreimagined.gtlib.blockentity.BlockEntityBase
import org.gtreimagined.gtlib.client.RenderHelper
import org.gtreimagined.gtlib.cover.CoverReplacements
import org.gtreimagined.gtlib.cover.IHaveCover
import org.gtreimagined.gtlib.data.GTTools
import org.gtreimagined.gtlib.item.ICustomDurability
import org.gtreimagined.gtlib.machine.BlockMachine
import org.gtreimagined.gtlib.mixin.client.LevelRendererAccessor
import org.gtreimagined.gtlib.mixin.client.MultiPlayerGameModeAccessor
import org.gtreimagined.gtlib.pipe.BlockPipe
import org.gtreimagined.gtlib.tool.GTToolType
import org.gtreimagined.gtlib.tool.IBasicGTTool
import org.gtreimagined.gtlib.tool.IGTTool
import org.gtreimagined.gtlib.tool.behaviour.BehaviourAOEBreak
import org.gtreimagined.gtlib.tool.behaviour.BehaviourExtendedHighlight
import org.gtreimagined.gtlib.util.getHarvestableBlocksToBreak
import org.gtreimagined.gtlib.util.getToolType
import org.gtreimagined.gtlib.util.literal

private inline val MC
    get() = Minecraft.getInstance()


fun onBlockHighlight(levelRenderer: LevelRenderer?, camera: Camera, target: BlockHitResult, partialTick: Float, poseStack: PoseStack, bufferSource: MultiBufferSource): Boolean {
    val player: Player = MC.player ?: return false
    val world = player.commandSenderWorld
    val stack = player.mainHandItem
    if (stack.isEmpty || ((stack.item !is IBasicGTTool) && (stack.item !is IHaveCover) && !CoverReplacements.hasReplacement(
            stack.item
        ))
    ) return false
    if (stack.item is IHaveCover || CoverReplacements.hasReplacement(stack.item)) {
        if (player.isCrouching) return false
        RenderHelper.onDrawHighlight(player, levelRenderer, camera, target, partialTick, poseStack, bufferSource,
            { it is BlockMachine || it is BlockPipe<*> }, BehaviourExtendedHighlight.COVER_FUNCTION)
        return true
    }
    val item = stack.item as IBasicGTTool
    val type: GTToolType = getToolType(player) ?: return false
    if (player.isCrouching && type !== GTTools.WRENCH && type !== GTTools.CROWBAR && type !== GTTools.WIRE_CUTTER) return false
    //Perform highlight of wrench
    val res = item.onGenericHighlight(player, levelRenderer, camera, target, partialTick, poseStack, bufferSource)
    if (res == InteractionResult.FAIL) {
        return true
    }
    if (res.shouldSwing()) {
        return false
    }
    val behaviour = type.getBehaviour("aoe_break")
    if (behaviour !is BehaviourAOEBreak) return false

    val currentPos = target.blockPos
    val state = world.getBlockState(currentPos)
    if (state.isAir || !item.genericIsCorrectToolForDrops(
            stack,
            state
        ) || item.getDataTag(stack) == null || !item.getDataTag(stack).getBoolean(Ref.KEY_TOOL_BEHAVIOUR_AOE_BREAK)
    ) return false
    val viewPosition = camera.position
    val entity = camera.entity
    val builderLines = bufferSource.getBuffer(RenderType.LINES)
    val viewX = viewPosition.x
    val viewY = viewPosition.y
    val viewZ = viewPosition.z
    val positions: ImmutableSet<BlockPos> = getHarvestableBlocksToBreak(world, player, item, stack,
        behaviour.getColumn(), behaviour.getRow(), behaviour.getDepth())
    for (nextPos in positions) {
        val modX = nextPos.x - viewX
        val modY = nextPos.y - viewY
        val modZ = nextPos.z - viewZ
        val shape = world.getBlockState(nextPos).getShape(world, nextPos, CollisionContext.of(entity))
        poseStack.pushPose()
        LevelRendererAccessor.renderShape(poseStack, builderLines, shape, modX, modY, modZ,
            0f, 0f, 0f, 0.4f)
        poseStack.popPose()
    }
    if (MC.gameMode!!.isDestroying) {
        for (nextPos in positions) {
            val modX = nextPos.x - viewX
            val modY = nextPos.y - viewY
            val modZ = nextPos.z - viewZ
            //TODO 1.18
            //int partialDamage = 1;
            val partialDamage =
                ((MC.gameMode as MultiPlayerGameModeAccessor).getDestroyProgress() * 10).toInt() - 1 // destroyProgress = curBlockDamageMP
            poseStack.pushPose()
            poseStack.translate(modX, modY, modZ)
            if (partialDamage == -1) return false // Not sure why this happens, but it certainly is an edge-case, if we made it so it returns 0 every time it hit -1, the animation will have a delay

            val builderBreak: VertexConsumer = SheetedDecalTextureGenerator(
                bufferSource.getBuffer(ModelBakery.DESTROY_TYPES[partialDamage]),
                poseStack.last().pose(),
                poseStack.last().normal(),
                1.0f
            )
            MC.blockRenderer
                .renderBreakingTexture(world.getBlockState(nextPos), nextPos, world, poseStack, builderBreak)
            // MC.getBlockRendererDispatcher().renderModel(world.getBlockState(nextPos), nextPos, world, matrix, builderBreak, ModelDataManager.getModelData(world, nextPos));
            poseStack.popPose()
        }
    }
    return false
}

// Needs some work, won't work in 3rd person also, needs special ItemModel properties
fun onPlayerTickEnd(player: Player?) {
    if (player == null || player.mainHandItem.isEmpty) return
    val stack = player.mainHandItem
    if (stack.item !is IGTTool) return
    val item = stack.item as IGTTool
    if (item.getGTToolType().useAction != UseAnim.NONE && player.swinging) {
        //todo abstract this
        //item.getItem().onUsingTick(stack, player, stack.getCount());
        //player.swingProgress = player.prevSwingProgress;
    }
}

fun onRenderDebugInfo(left: ArrayList<String?>) {
    if (!MC.options.renderDebug || MC.hitResult == null || MC.hitResult!!.type !== HitResult.Type.BLOCK) return
    val world: ClientLevel = Minecraft.getInstance().level ?: return
    val pos = BlockPos.containing(MC.hitResult!!.getLocation())
    val state = world.getBlockState(pos)
    val block = state.block
    if (block is IInfoProvider) {
        left.add("")
        left.add(ChatFormatting.AQUA.toString() + "[GTLib Debug Server]")
        left.addAll(block.getInfo(ObjectArrayList(), world, state, pos, false))
    }
    val tile = world.getBlockEntity(pos)
    if (tile is BlockEntityBase<*>) {
        left.addAll(tile.getInfo(false))
    }
    if (MC.player!!.isCrouching) {
        left.add("")
        left.add(ChatFormatting.AQUA.toString() + "[GTLib Debug Client]")
    }
}

//TODO still needed?
fun onItemTooltip(stack: ItemStack, tooltips: MutableList<Component>, player: Player?, flag: TooltipFlag) {
    if (stack.item is ICustomDurability) {
        var j = -1
        for (i in tooltips.indices) {
            val component = tooltips[i]
            val contents = component.contents
            if (contents is TranslatableContents) {
                if (contents.key == "item.durability") {
                    j = i
                    break
                }
            }
        }
        if (j != -1) {
            tooltips.removeAt(j)
        }
    }
    if (flag.isAdvanced && Ref.SHOW_ITEM_TAGS) {
        val tags: MutableCollection<ResourceLocation> = mutableListOf() //ItemTags.getAllTags().getMatchingTags(e.getItemStack().getItem());
        if (!tags.isEmpty()) {
            tooltips.add(literal("Tags:").withStyle(ChatFormatting.DARK_GRAY))
            for (loc in tags) {
                tooltips.add(literal(loc.toString()).withStyle(ChatFormatting.DARK_GRAY))
            }
        }
    }
}

var lastDelta = 0.0
fun onGuiMouseScrollPre(lastScrollDelta: Double) {
    lastDelta = lastScrollDelta
}

var leftDown = false
var rightDown = false
var middleDown = false
fun onGuiMouseClickPre(button: Int) {
    when (button) {
        0 -> leftDown = true
        1 -> rightDown = true
        2 -> middleDown = true
    }
}

fun onGuiMouseReleasedPre(button: Int) {
    when (button) {
        0 -> leftDown = false
        1 -> rightDown = false
        2 -> middleDown = false
    }
}