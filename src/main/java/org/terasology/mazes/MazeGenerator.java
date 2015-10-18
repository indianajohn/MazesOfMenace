/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.mazes;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;

/*
+x is defined left of initial character spawn
+y is defined as up
+z is defined as forawrd
 */

/**
 * Created by john on 10/17/15.
 */
public class MazeGenerator {
    public MazeGenerator(MazeInfo maze_info) {
        m_maze_info = maze_info;
    }
    public boolean containsWorldPosition(Vector3i position)
    {
        Vector3i room_id = m_maze_info.getRoomID(position);
        Vector3i relative_position = m_maze_info.getPositionInRoom(position);
        return (room_id != null &&
                m_maze_info.maze_map.containsKey(room_id) &&
                m_maze_info.maze_map.get(room_id).containsRelativePosition(relative_position));
    }
    public boolean intersectsWithRegion(Region3i region) {
        // Catch the tunnel leading down
        if (
                region.minX() <= m_maze_info.entrance.getX() + m_maze_info.room_diameter &&
                region.maxX() >= m_maze_info.entrance.getX() &&
                region.minZ() <= m_maze_info.entrance.getZ() + m_maze_info.room_diameter &&
                region.maxZ() >= m_maze_info.entrance.getZ()
                )
        {
            return true;
        }
        // Main dungeon body
        return (region.maxX() > m_maze_info.minTileX()&&
                region.maxY() > m_maze_info.minTileY() &&
                region.maxZ() > m_maze_info.minTileZ() &&
                region.minX() < m_maze_info.maxTileX() &&
                region.minY() < m_maze_info.maxTileY() &&
                region.minZ() < m_maze_info.maxTileZ()
        );
    }
    private MazeInfo m_maze_info;
}
