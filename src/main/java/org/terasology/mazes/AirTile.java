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
 * Created by john on 10/18/15.
 */
public class AirTile extends MazeTile {
    public AirTile(MazeInfo maze_info_input) {
        super(maze_info_input);
    }
    public boolean containsRelativePosition(Vector3i position)
    {
        return true;
    }
}
