package legaming.tech.litematicacenter.client

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.text.Text
import net.minecraft.util.BlockRotation
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

class AlignmentConfirmScreen(
    private val placement: SchematicPlacement,
    private val matchedOrigin: BlockPos,
    private val matchedRotation: BlockRotation
) : Screen(Text.literal("Automatic Alignment")) {

    override fun init() {
        super.init()

        val buttonWidth = 100
        val buttonHeight = 20
        val spacing = 20

        val centerX = width / 2
        val centerY = height / 2

        // Add "Yes" button
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Yes")) { _ ->
                // Apply the alignment
                LitematicaHelper.alignPlacement(placement, matchedOrigin, matchedRotation)
                client?.player?.sendMessage(
                    Text.literal("[LitematicaCenter] Schematic aligned successfully!")
                        .formatted(Formatting.GREEN),
                    false
                )
                close()
            }
                .dimensions(centerX - buttonWidth - spacing / 2, centerY + 20, buttonWidth, buttonHeight)
                .build()
        )

        // Add "No" button
        addDrawableChild(
            ButtonWidget.builder(Text.literal("No")) { _ ->
                client?.player?.sendMessage(
                    Text.literal("[LitematicaCenter] Alignment canceled.")
                        .formatted(Formatting.RED),
                    false
                )
                close()
            }
                .dimensions(centerX + spacing / 2, centerY + 20, buttonWidth, buttonHeight)
                .build()
        )
    }

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        super.render(drawContext, mouseX, mouseY, delta)

        val centerX = width / 2
        val centerY = height / 2

        // Render Title
        drawContext.drawCenteredTextWithShadow(
            textRenderer,
            title,
            centerX,
            centerY - 45,
            0xFFFFFF
        )

        // Render Prompt Message (Wrapped dynamically to fit nicely without overlapping)
        val messageText = Text.literal("A similar structure was found. Do you want to automatically align the schematic to it?")
        val maxTextWidth = minOf(width - 60, 320)
        val wrappedLines = textRenderer.wrapLines(messageText, maxTextWidth)

        val lineHeight = 12
        val totalTextHeight = wrappedLines.size * lineHeight
        val startY = centerY - 10 - (totalTextHeight / 2)

        for (i in wrappedLines.indices) {
            drawContext.drawCenteredTextWithShadow(
                textRenderer,
                wrappedLines[i],
                centerX,
                startY + i * lineHeight,
                0xCCCCCC
            )
        }
    }
}
