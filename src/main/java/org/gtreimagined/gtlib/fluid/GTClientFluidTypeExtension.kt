package org.gtreimagined.gtlib.fluid

import net.ccbluex.fastutil.invoke
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions
import java.util.function.Consumer
import java.util.function.IntSupplier

@JvmRecord
data class GTClientFluidTypeExtension(
    val stillTexture: ResourceLocation, val flowingTexture: ResourceLocation,
    val tintColorGetter: IntSupplier, val overflowTexture: ResourceLocation?): IClientFluidTypeExtensions{

    override fun getStillTexture(): ResourceLocation {
        return stillTexture
    }

    override fun getFlowingTexture(): ResourceLocation {
        return flowingTexture
    }

    override fun getTintColor(): Int {
        return tintColorGetter()
    }

    companion object {
        @JvmStatic
        fun create(consumer: Consumer<GTClientFluidTypeExtensionBuilder>): GTClientFluidTypeExtension {
            val builder = GTClientFluidTypeExtensionBuilder()
            consumer.accept(builder)
            val stillTexture = requireNotNull(builder.stillTexture){"Still Texture was not set!"}
            val flowingTexture = requireNotNull(builder.flowingTexture){"Flowing Texture was not set!"}
            return GTClientFluidTypeExtension(stillTexture, flowingTexture, builder.tintColorGetter, builder.overlayTexture)
        }


        @JvmStatic
        @Deprecated("", level = DeprecationLevel.HIDDEN)
        fun builder(): GTClientFluidTypeExtensionBuilder {
            return GTClientFluidTypeExtensionBuilder()
        }
    }

    class GTClientFluidTypeExtensionBuilder{
        var stillTexture: ResourceLocation? = null
        var flowingTexture: ResourceLocation? = null
        var tintColorGetter: IntSupplier = IntSupplier {-1}
        var overlayTexture: ResourceLocation? = null

        fun stillTexture(stillTexture: ResourceLocation): GTClientFluidTypeExtensionBuilder = apply { this.stillTexture = stillTexture }
        fun flowingTexture(flowingTexture: ResourceLocation): GTClientFluidTypeExtensionBuilder = apply { this.flowingTexture = flowingTexture }
        fun tintColorGetter(tintColorGetter: IntSupplier): GTClientFluidTypeExtensionBuilder = apply { this.tintColorGetter = tintColorGetter }
        fun tintColor(tintColor: Int): GTClientFluidTypeExtensionBuilder {
            return apply { this.tintColorGetter = IntSupplier { tintColor } }
        }
        fun overlayTexture(overlayTexture: ResourceLocation): GTClientFluidTypeExtensionBuilder = apply { this.overlayTexture = overlayTexture }

        @Deprecated("", level = DeprecationLevel.HIDDEN)
        fun build(): GTClientFluidTypeExtension {
            val stillTexture = requireNotNull(stillTexture){"Still Texture was not set!"}
            val flowingTexture = requireNotNull(flowingTexture){"Flowing Texture was not set!"}
            return GTClientFluidTypeExtension(stillTexture, flowingTexture, tintColorGetter, overlayTexture)
        }
    }
}
