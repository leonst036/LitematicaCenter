package legaming.tech.litematicacenter.client

import fi.dy.masa.litematica.schematic.LitematicaSchematic
import fi.dy.masa.litematica.schematic.container.LitematicaBlockStateContainer
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos

class StructureFingerprint(
    val anchorBlock: Block,
    val anchorLocalPos: BlockPos,
    val verifyOffsets: List<VerifyOffset>
) {

    data class VerifyOffset(
        val offset: BlockPos,
        val block: Block
    )

    companion object {
        private val COMMON_BLOCKS = setOf(
            Blocks.AIR, Blocks.CAVE_AIR, Blocks.VOID_AIR,
            Blocks.STONE, Blocks.DIRT, Blocks.GRASS_BLOCK,
            Blocks.COBBLESTONE, Blocks.NETHERRACK, Blocks.END_STONE,
            Blocks.WATER, Blocks.LAVA
        )

        /**
         * Compiles a schematic into a StructureFingerprint.
         */
        fun create(schematic: LitematicaSchematic): StructureFingerprint? {
            val blockCounts = mutableMapOf<Block, Int>()
            val blockPositions = mutableMapOf<Block, MutableList<BlockPos>>()

            // Get sub-region sizes
            val areaSizes = schematic.areaSizes
            if (areaSizes.isEmpty()) return null

            for ((regionName, size) in areaSizes) {
                val container = schematic.getSubRegionContainer(regionName) ?: continue
                val maxX = size.x
                val maxY = size.y
                val maxZ = size.z

                for (x in 0 until maxX) {
                    for (y in 0 until maxY) {
                        for (z in 0 until maxZ) {
                            val relativePos = BlockPos(x, y, z)
                            val state = container.get(x, y, z)
                            val block = state.block

                            if (block != Blocks.AIR && block != Blocks.CAVE_AIR) {
                                blockCounts[block] = blockCounts.getOrDefault(block, 0) + 1
                                blockPositions.computeIfAbsent(block) { mutableListOf() }.add(relativePos)
                            }
                        }
                    }
                }
            }

            if (blockCounts.isEmpty()) return null

            // Find the rarest block (preferring non-common blocks first)
            var chosenAnchorBlock = blockCounts.keys
                .filter { it !in COMMON_BLOCKS }
                .minByOrNull { blockCounts[it] ?: Int.MAX_VALUE }

            // Fallback if the schematic only consists of common blocks
            if (chosenAnchorBlock == null) {
                chosenAnchorBlock = blockCounts.keys.minByOrNull { blockCounts[it] ?: Int.MAX_VALUE }
            }

            if (chosenAnchorBlock == null) return null

            val positions = blockPositions[chosenAnchorBlock] ?: return null
            if (positions.isEmpty()) return null

            // Pick the first occurrence of the anchor block as the reference origin
            val anchorPos = positions[0]

            // Gather up to 10 other blocks for fingerprint verification
            val verifyOffsets = mutableListOf<VerifyOffset>()
            val allOtherPositions = mutableListOf<Pair<BlockPos, Block>>()

            for ((regionName, size) in areaSizes) {
                val container = schematic.getSubRegionContainer(regionName) ?: continue
                for (x in 0 until size.x) {
                    for (y in 0 until size.y) {
                        for (z in 0 until size.z) {
                            val pos = BlockPos(x, y, z)
                            if (pos == anchorPos) continue
                            val state = container.get(x, y, z)
                            val block = state.block
                            if (block != Blocks.AIR && block != Blocks.CAVE_AIR) {
                                allOtherPositions.add(pos to block)
                            }
                        }
                    }
                }
            }

            // Sort by distance to anchor Pos to ensure spatial spread
            allOtherPositions.sortBy { posPair ->
                posPair.first.getSquaredDistance(anchorPos)
            }

            val step = if (allOtherPositions.size > 10) allOtherPositions.size / 10 else 1
            var i = 0
            while (i < allOtherPositions.size && verifyOffsets.size < 10) {
                val pair = allOtherPositions[i]
                val relativeOffset = BlockPos(
                    pair.first.x - anchorPos.x,
                    pair.first.y - anchorPos.y,
                    pair.first.z - anchorPos.z
                )
                verifyOffsets.add(VerifyOffset(relativeOffset, pair.second))
                i += step
            }

            return StructureFingerprint(chosenAnchorBlock, anchorPos, verifyOffsets)
        }
    }
}
