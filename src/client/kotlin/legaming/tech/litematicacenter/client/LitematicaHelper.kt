package legaming.tech.litematicacenter.client

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos

object LitematicaHelper {

    /**
     * Programmatically aligns a Litematica schematic placement to the target coordinates and rotation.
     */
    fun alignPlacement(placement: SchematicPlacement, matchPos: BlockPos, rotation: BlockRotation) {
        // Set the new rotation (passing null for the message feedback consumer)
        placement.setRotation(rotation, null)
        
        // Set the new origin position in the world (passing null for the string feedback consumer)
        placement.setOrigin(matchPos, null)
    }
}
