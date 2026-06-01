package legaming.tech.litematicacenter.client

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.SliderWidget
import net.minecraft.text.Text

class ModConfigScreen(private val parent: Screen) : Screen(Text.literal("LitematicaCenter Config")) {

    private lateinit var slider: SimilaritySlider

    override fun init() {
        super.init()

        val centerX = width / 2
        val centerY = height / 2

        slider = SimilaritySlider(
            centerX - 100,
            centerY - 10,
            200,
            20,
            LitematicaCenterConfig.data.similarityThreshold
        ) { newValue ->
            LitematicaCenterConfig.data.similarityThreshold = newValue
        }
        addDrawableChild(slider)

        // Reset to Default button
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Reset to Default")) { _ ->
                slider.setValue(0.8)
                LitematicaCenterConfig.data.similarityThreshold = 0.8
            }
                .dimensions(centerX - 100, centerY + 20, 200, 20)
                .build()
        )

        // Save and Back button
        addDrawableChild(
            ButtonWidget.builder(Text.literal("Save and Back")) { _ ->
                LitematicaCenterConfig.save()
                client?.setScreen(parent)
            }
                .dimensions(centerX - 100, centerY + 50, 200, 20)
                .build()
        )
    }

    override fun render(drawContext: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(drawContext, mouseX, mouseY, delta)
        super.render(drawContext, mouseX, mouseY, delta)

        // Draw the title
        drawContext.drawCenteredTextWithShadow(
            textRenderer,
            title,
            width / 2,
            height / 2 - 50,
            0xFFFFFF
        )
        
        // Draw description text for user
        drawContext.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("Configure the minimum percentage of blocks matching"),
            width / 2,
            height / 2 - 80,
            0xAAAAAA
        )
        drawContext.drawCenteredTextWithShadow(
            textRenderer,
            Text.literal("to prompt schematic alignment."),
            width / 2,
            height / 2 - 68,
            0xAAAAAA
        )
    }

    override fun close() {
        LitematicaCenterConfig.save()
        client?.setScreen(parent)
    }

    class SimilaritySlider(
        x: Int, y: Int, width: Int, height: Int,
        initialValue: Double,
        private val onChange: (Double) -> Unit
    ) : SliderWidget(
        x, y, width, height,
        Text.literal("Matching Threshold: ${(initialValue * 100).toInt()}%"),
        initialValue
    ) {
        override fun updateMessage() {
            message = Text.literal("Matching Threshold: ${(value * 100).toInt()}%")
        }

        override fun applyValue() {
            onChange(value)
        }

        fun setValue(newValue: Double) {
            this.value = newValue
            updateMessage()
        }
    }
}
