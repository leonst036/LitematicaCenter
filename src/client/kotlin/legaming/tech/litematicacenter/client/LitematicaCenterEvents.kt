package legaming.tech.litematicacenter.client

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

import net.minecraft.util.Formatting

object LitematicaCenterEvents {

    /**
     * Fired when a Litematica schematic placement is loaded and added to the active placements.
     */
    fun onSchematicPlacementAdded(placement: SchematicPlacement) {
        val client = MinecraftClient.getInstance()
        val world = client.world ?: return
        val schematic = placement.getSchematic() ?: return

        // 1. Compile the schematic's block signature fingerprint
        val fingerprint = StructureFingerprint.create(schematic) ?: return

        // 2. Notify the player that the scanning process has started
        client.player?.sendMessage(
            Text.literal("[LitematicaCenter] Searching for matching structures in the world...")
                .formatted(Formatting.YELLOW),
            false
        )

        // 3. Initiate the non-blocking asynchronous structure search
        StructureScanner.scanAsync(world, fingerprint) { result ->
            // 4. On a strong match, prompt the user with our premium alignment screen
            client.setScreen(
                AlignmentConfirmScreen(placement, result.matchedOrigin, result.matchedRotation)
            )
        }
    }
}
