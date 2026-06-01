package legaming.tech.litematicacenter.mixin.client

import fi.dy.masa.litematica.schematic.placement.SchematicPlacement
import fi.dy.masa.litematica.schematic.placement.SchematicPlacementManager
import legaming.tech.litematicacenter.client.LitematicaCenterEvents
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo

@Mixin(value = [SchematicPlacementManager::class], remap = false)
class SchematicPlacementManagerMixin {

    @Inject(method = ["addSchematicPlacement"], at = [At("TAIL")], require = 1)
    private fun onAddPlacement(placement: SchematicPlacement, someBooleanFlag: Boolean, ci: CallbackInfo) {
        LitematicaCenterEvents.onSchematicPlacementAdded(placement)
    }
}
