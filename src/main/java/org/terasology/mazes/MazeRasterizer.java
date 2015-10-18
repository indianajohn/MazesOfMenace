package org.terasology.mazes;

import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizerPlugin;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
public class MazeRasterizer implements WorldRasterizerPlugin {

    String blockUri;

    public MazeRasterizer() {
    }

    public MazeRasterizer(String blockUri) {
        this.blockUri = blockUri;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        MazeFacet mazeFacet = chunkRegion.getFacet(MazeFacet.class);
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block mazeBlock = blockManager.getBlock(BlockManager.AIR_ID);
        if (blockUri != null) {
            mazeBlock = blockManager.getBlock(blockUri);
        }
        for (Vector3i position : ChunkConstants.CHUNK_REGION) {
            if (mazeFacet.get(position))
            {
                Vector3i vectorWorld = chunk.chunkToWorldPosition(position);
                chunk.setBlock(position, mazeBlock);
            }
        }
    }
}
