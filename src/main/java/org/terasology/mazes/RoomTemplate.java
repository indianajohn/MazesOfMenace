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
 * Created by john on 10/25/15.
 */
public abstract class RoomTemplate extends MazeTile {
    public RoomTemplate(MazeInfo maze_info_input)
    {
        super(maze_info_input);
    }
    public boolean containsRelativePosition(Vector3i position) {
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

    protected boolean insideRoom(Vector3i local_position) {
        return (
                local_position.x() >= 0 &&
                        local_position.y() >= 0 &&
                        local_position.z() >= 0 &&
                        local_position.x() < maze_info.room_diameter &&
                        local_position.y() < maze_info.room_height + maze_info.floor_thickness &&
                        local_position.z() < maze_info.room_diameter
        );
    }

    protected HashMap<Vector3i,Boolean> room_blocks = new HashMap<>();
    protected void spiralDown(Vector3i focus, Vector3i direction) {
        focus.add(direction);
        if (!insideRoom(focus)) {
            focus.sub(direction);
            Vector3i new_direction = null;
            if (direction.distance(Vector3i.west()) == 0)
                new_direction = Vector3i.north();
            else if (direction.distance(Vector3i.north()) == 0)
                new_direction = Vector3i.east();
            else if (direction.distance(Vector3i.east()) == 0)
                new_direction = Vector3i.south();
            else if (direction.distance(Vector3i.south()) == 0)
                new_direction = Vector3i.west();
            direction.set(new_direction);
            focus.add(direction);
        }
        focus.addY(1);
    }

    protected void spiralUp(Vector3i focus, Vector3i direction) {
        focus.add(direction);
        if (!insideRoom(focus)) {
            Vector3i new_direction = null;
            focus.sub(direction);
            if (direction.distance(Vector3i.north()) == 0)
                new_direction = Vector3i.west();
            else if (direction.distance(Vector3i.west()) == 0)
                new_direction = Vector3i.south();
            else if (direction.distance(Vector3i.south()) == 0)
                new_direction = Vector3i.east();
            else if (direction.distance(Vector3i.east()) == 0)
                new_direction = Vector3i.north();
            direction.set(new_direction);
            focus.add(direction);
        }
        focus.subY(1);
    }
}
