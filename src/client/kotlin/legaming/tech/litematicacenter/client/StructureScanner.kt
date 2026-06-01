package legaming.tech.litematicacenter.client

import net.minecraft.client.MinecraftClient
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import java.util.concurrent.Executors

object StructureScanner {

    private val executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "LitematicaCenter-ScannerThread").apply {
            isDaemon = true
        }
    }

    data class ScanResult(
        val matchedOrigin: BlockPos,
        val matchedRotation: BlockRotation
    )

    /**
     * Rotates a relative offset according to a BlockRotation.
     */
    fun rotateOffset(offset: BlockPos, rotation: BlockRotation): BlockPos {
        return when (rotation) {
            BlockRotation.NONE -> offset
            BlockRotation.CLOCKWISE_90 -> BlockPos(-offset.z, offset.y, offset.x)
            BlockRotation.CLOCKWISE_180 -> BlockPos(-offset.x, offset.y, -offset.z)
            BlockRotation.COUNTERCLOCKWISE_90 -> BlockPos(offset.z, offset.y, -offset.x)
        }
    }

    /**
     * Asynchronously scans the world around the player for existing structures matching the fingerprint.
     */
    fun scanAsync(
        world: ClientWorld,
        fingerprint: StructureFingerprint,
        onMatchFound: (ScanResult) -> Unit
    ) {
        val player = MinecraftClient.getInstance().player ?: return
        val playerPos = player.blockPos
        
        executor.submit {
            try {
                val renderDistance = MinecraftClient.getInstance().options.viewDistance.value
                val playerChunkX = playerPos.x shr 4
                val playerChunkZ = playerPos.z shr 4

                // Scan chunks in a spiral or grid around the player (up to renderDistance)
                val radius = minOf(renderDistance, 12) 
                
                for (dx in -radius..radius) {
                    for (dz in -radius..radius) {
                        val chunkX = playerChunkX + dx
                        val chunkZ = playerChunkZ + dz

                        val chunk = world.getChunk(chunkX, chunkZ) ?: continue
                        
                        // Scan chunk sections (16x16x16 blocks)
                        val sections = chunk.sectionArray
                        for (sectionIndex in sections.indices) {
                            val section = sections[sectionIndex]
                            if (section.isEmpty) continue

                            val sectionY = chunk.bottomY + (sectionIndex shl 4)

                            // Check block states in the section
                            for (x in 0..15) {
                                for (y in 0..15) {
                                    for (z in 0..15) {
                                        val worldX = (chunkX shl 4) + x
                                        val worldY = sectionY + y
                                        val worldZ = (chunkZ shl 4) + z

                                        val blockPos = BlockPos(worldX, worldY, worldZ)
                                        val state = section.getBlockState(x, y, z)

                                        if (state.block == fingerprint.anchorBlock) {
                                            // Candidate found! Verify rotations.
                                            val matchedRotation = verifyCandidate(world, blockPos, fingerprint)
                                            if (matchedRotation != null) {
                                                // Calculate matching placement origin
                                                val rotatedLocalAnchor = rotateOffset(fingerprint.anchorLocalPos, matchedRotation)
                                                val matchedOrigin = blockPos.subtract(rotatedLocalAnchor)

                                                val result = ScanResult(matchedOrigin, matchedRotation)
                                                // Run callback on main thread
                                                MinecraftClient.getInstance().execute {
                                                    onMatchFound(result)
                                                }
                                                return@submit // Stop scanning after first strong match
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Verifies if the candidate anchor block matches the verify offsets in any rotation.
     * Returns the matching BlockRotation if found, or null otherwise.
     */
    private fun verifyCandidate(
        world: ClientWorld,
        candidatePos: BlockPos,
        fingerprint: StructureFingerprint
    ): BlockRotation? {
        val similarityThreshold = LitematicaCenterConfig.data.similarityThreshold

        for (rotation in BlockRotation.entries) {
            var matchCount = 0
            val totalOffsets = fingerprint.verifyOffsets.size

            if (totalOffsets == 0) continue

            for (verifyOffset in fingerprint.verifyOffsets) {
                val rotatedOffset = rotateOffset(verifyOffset.offset, rotation)
                val targetPos = candidatePos.add(rotatedOffset)

                if (world.isInBuildLimit(targetPos)) {
                    val state = world.getBlockState(targetPos)
                    if (state.block == verifyOffset.block) {
                        matchCount++
                    }
                }
            }

            val similarity = matchCount.toDouble() / totalOffsets
            if (similarity >= similarityThreshold) {
                // Secondary check: verify a few more random blocks if match is close to avoid false positives
                return rotation
            }
        }
        return null
    }
}
