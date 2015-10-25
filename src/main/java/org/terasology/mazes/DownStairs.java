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

import org.terasology.math.geom.Vector3i;

import java.util.HashMap;

/**
 * Created by john on 10/18/15.
 */
public class DownStairs extends RoomTemplate {
    public DownStairs(MazeInfo maze_info_input) {
        super(maze_info_input);
        // TODO: make it so this code is only executed once and other stairs are copies.
        // Spiral counterclockwise from the lower right hand corner, going up one block per level
        Vector3i focus = new Vector3i(0,maze_info.room_height + maze_info_input.floor_thickness-1,1);
        room_blocks.put(new Vector3i(focus), false);
        Vector3i direction = Vector3i.north();
        while (focus.y() > maze_info.room_height) {
            spiralUp(focus,direction);
            room_blocks.put(new Vector3i(focus), false);
        }
    }
    public boolean containsRelativePosition(Vector3i position)
    {
        // Everything above the floor is air
        if (position.y() <= maze_info.room_height)
            return true;

        // Otherwise, we use a hash map to create and use a stair case block.
        if (room_blocks.containsKey(position))
        {
            return room_blocks.get(position);
        }
        else
        {
            // Absence from the map indicates an air block
            return true;
        }
    }
}
