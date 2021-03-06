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

/**
 * Created by john on 10/25/15.
 */
public class ThroughStairs extends RoomTemplate {
    public ThroughStairs(MazeInfo maze_info_input, Vector3i global_position) {
        super(maze_info_input);
        // Make it so that stairs connect well between y-levels
        // (The initialization values here are ignored, as they are reset by getSpiralOrigin)
        Vector3i focus = new Vector3i(0,1,0);
        Vector3i direction = Vector3i.west();

        // Spiral clockwise from the lower right hand corner, going down one block per level
        while (focus.y() < maze_info.room_height + maze_info.floor_thickness) {
            room_blocks.put(new Vector3i(focus), false);
            spiralDown(focus,direction);
        }
    }
}
