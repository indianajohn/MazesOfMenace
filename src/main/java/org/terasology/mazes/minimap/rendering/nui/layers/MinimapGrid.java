/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.mazes.minimap.rendering.nui.layers;


import java.util.Collection;
import java.util.Optional;
import java.util.function.IntFunction;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.assets.ResourceUrn;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.characters.CharacterComponent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Border;
import org.terasology.math.ChunkMath;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.BasicTextureRegion;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.assets.texture.TextureRegion;
import org.terasology.rendering.nui.Canvas;
import org.terasology.rendering.nui.Color;
import org.terasology.rendering.nui.CoreWidget;
import org.terasology.rendering.nui.ScaleMode;
import org.terasology.rendering.nui.SubRegion;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockAppearance;
import org.terasology.world.block.BlockPart;
import org.terasology.world.chunks.ChunkConstants;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * @author mkienenb
 */
public class MinimapGrid extends CoreWidget {
    private static final Logger logger = LoggerFactory.getLogger(MinimapGrid.class);

    private static final ImmutableVector2i CELL_SIZE = new ImmutableVector2i(4, 4);
    private static final ImmutableVector2i BUFFER_SIZE = new ImmutableVector2i(
            CELL_SIZE.getX() * ChunkConstants.SIZE_X, CELL_SIZE.getY() * ChunkConstants.SIZE_Z);


    private Binding<EntityRef> targetEntityBinding = new DefaultBinding<>(EntityRef.NULL);
    private Binding<Integer> zoomFactorBinding = new DefaultBinding<>(0);

    private Texture textureAtlas;
    private TextureRegion questionMark;

    private Multimap<BaseVector2i, Vector3i> dirtyBlocks = LinkedHashMultimap.create();

    private WorldProvider worldProvider;

    private LoadingCache<Block, TextureRegion> cache = CacheBuilder.newBuilder().build(new CacheLoader<Block, TextureRegion>() {

        @Override
        public TextureRegion load(Block block) {
            BlockAppearance primaryAppearance = block.getPrimaryAppearance();

            BlockPart blockPart = BlockPart.TOP;

            // TODO: security issues
            //                    WorldAtlas worldAtlas = CoreRegistry.get(WorldAtlas.class);
            //                    float tileSize = worldAtlas.getRelativeTileSize();

            float tileSize = 16f / 256f; // 256f could be replaced by textureAtlas.getWidth();

            Vector2f textureAtlasPos = primaryAppearance.getTextureAtlasPos(blockPart);

            TextureRegion textureRegion = new BasicTextureRegion(textureAtlas, textureAtlasPos, new Vector2f(tileSize, tileSize));
            return textureRegion;
        }

    });

    private IntFunction<Float> brightness;

    public MinimapGrid() {
        textureAtlas = Assets.getTexture("engine:terrain").get();
        questionMark = Assets.getTextureRegion("engine:items#questionMark").get();
    }

    public void setHeightRange(int bottom, int top) {
        Preconditions.checkArgument(top > bottom);

        float minBright = 0.5f;
        float fac = (1 - minBright) / (top - bottom);
        brightness = y -> TeraMath.clamp(minBright + (y - bottom) * fac);
    }

    public void setWorldProvider(WorldProvider worldProvider) {
        this.worldProvider = worldProvider;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void updateLocation(Vector3i worldLocation) {
        Vector3i chunkPos = ChunkMath.calcChunkPos(worldLocation);
        Vector3i blockPos = ChunkMath.calcBlockPos(worldLocation);
        dirtyBlocks.put(new ImmutableVector2i(chunkPos.getX(), chunkPos.getZ()), blockPos);
    }

    @Override
    public void onDraw(Canvas canvas) {

        // Get world position
        EntityRef entity = getTargetEntity();
        LocationComponent locationComponent = entity.getComponent(LocationComponent.class);
        CharacterComponent character = entity.getComponent(CharacterComponent.class);
        float rotation = (float) ((character != null) ? -character.yaw * Math.PI / 180f : 0);
        Vector3f worldPosition = null;
        if (null != locationComponent) {
            worldPosition = locationComponent.getWorldPosition();
        } else {
            return;
        }

        // define zoom factor
        int zoomLevel = -getZoomFactor();
        float zoomDelta = 0.25f;
        float fZoom = (float) Math.pow(2.0, zoomLevel * zoomDelta);

        // Size window
        int width = getPreferredContentSize().getX();
        int height = getPreferredContentSize().getY();

        // Draw minimap
        for (int nU = 0; nU < width; nU+= CELL_SIZE.getX())
        {
            for (int nV = 0; nV < height; nV += CELL_SIZE.getY())
            {
                Rect2i rect = Rect2i.createFromMinAndSize(nU, nV, CELL_SIZE.getX(), CELL_SIZE.getY());
                int nX = Math.round(worldPosition.x()) + Math.round(fZoom*(nU - width/2)/4);
                int nY = Math.round(worldPosition.y());
                int nZ = Math.round(worldPosition.z()) + Math.round(fZoom*(nV - height/2)/4);
                Vector3i relLocation = new Vector3i(nX, nY, nZ);
                drawCell(canvas, rect, relLocation); // the y component of relLocation is modified!
            }
        }

        // draw arrowhead
        Texture arrowhead = Assets.getTexture("MazesOfMenace:arrowhead").get();
        // Drawing textures with rotation is not yet supported, see #1926
        // We therefore use a workaround based on mesh drawing
        // The width of the screenArea is doubled to avoid clipping issues when the texture is rotated
        int arrowWidth = arrowhead.getWidth() * 2;
        int arrowHeight = arrowhead.getHeight() * 2;
        int arrowX = (width - arrowWidth) / 2;
        int arrowY = (height - arrowHeight) / 2;
        Rect2i screenArea = Rect2i.createFromMinAndSize(arrowX, arrowY, arrowWidth, arrowHeight);
//        canvas.drawTexture(arrowhead, arrowX, arrowY, rotation);

        // UITexture should be used here, but it doesn't work
        Material material = Assets.getMaterial("engine:UILitMesh").get();
        material.setTexture("texture", arrowhead);
        Mesh mesh = Assets.getMesh("engine:UIBillboard").get();
        // The scaling seems to be completely wrong - 0.8f looks ok
        canvas.drawMesh(mesh, material, screenArea, new Quat4f(0, 0, rotation), new Vector3f(), 0.8f);
    }

    @Override
    public Vector2i getPreferredContentSize(Canvas canvas, Vector2i sizeHint) {
        Border border = canvas.getCurrentStyle().getBackgroundBorder();
        Vector2i size = getPreferredContentSize();
        int width = size.x + border.getTotalWidth();
        int height = size.y + border.getTotalHeight();
        return new Vector2i(width, height);
    }

    public Vector2i getPreferredContentSize() {
        return new Vector2i(320, 200);
    }

    public void bindTargetEntity(Binding<EntityRef> binding) {
        targetEntityBinding = binding;
    }

    public EntityRef getTargetEntity() {
        return targetEntityBinding.get();
    }

    public void setTargetEntity(EntityRef val) {
        targetEntityBinding.set(val);
    }

    public int getZoomFactor() {
        return zoomFactorBinding.get();
    }

    public void setZoomFactor(int val) {
        zoomFactorBinding.set(val);
    }

    public void bindZoomFactor(ReadOnlyBinding<Integer> offsetBinding) {
        zoomFactorBinding = offsetBinding;
    }

    private void drawCell(Canvas canvas, Rect2i rect, Vector3i pos) {
        Block block = worldProvider.getBlock(pos);
        Block top = block;
        if (block.isTranslucent()) {
            canvas.drawFilledRectangle(rect,Color.GREY);
        } else {
            canvas.drawFilledRectangle(rect,Color.BLACK);
        }

    }

    private static boolean isIgnored(Block block) {
        return block.isPenetrable() && !block.isWater();
    }

}
