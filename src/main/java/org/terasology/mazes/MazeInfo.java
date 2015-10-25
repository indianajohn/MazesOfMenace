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

import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by john on 10/17/15.
 */
public class MazeInfo {
    private void generateRoom(Vector3i room_center, int generate_width, int generate_height) {
        for (int nX = room_center.x() - generate_width; nX < room_center.x() + generate_width; nX++)
        {
            for (int nZ = room_center.z() - generate_height; nZ < room_center.z() + generate_height; nZ++)
            {
                if (nX < 0)
                    nX = 0;
                if (nZ < 0)
                    nZ = 0;
                Vector3i focus_tile = new Vector3i(nX, room_center.y(), nZ);
                if (!maze_map.containsKey(focus_tile))
                    maze_map.put(new Vector3i(nX,room_center.y(),nZ),new AirTile(this));
            }
        }
    }
    void tunnelBetween(Vector3i first, Vector3i second)
    {
        Vector3i focus = new Vector3i(first);
        Random random_generator = new Random(seed);
        // Tunnel randomly in the direction of the second room
        while (focus.distance(second) > 2)
        {
            int nXDelta = Integer.signum(second.x() - focus.x());
            int nZDelta = Integer.signum(second.z() - focus.z());

            // If we're directly above/below/to the right of / to the left of the center, shoot straight to it
            if (nXDelta == 0)
                focus.addZ(nZDelta);
            else if (nZDelta == 0)
                focus.addX(nXDelta);
            else
            {
                // Otherwise, choose one of the two possible directions that will get us closer to the second room
                if(random_generator.nextBoolean())
                {
                    focus.addX(nXDelta);
                }
                else
                {
                    focus.addZ(nZDelta);
                }
            }
            // Dig out a room
            if (!maze_map.containsKey(focus))
                maze_map.put(new Vector3i(focus),new AirTile(this));
        }
    }

    public MazeInfo(long input_seed) {
        seed = input_seed;
        maze_map = new HashMap<>();
        Random random_generator = new Random(seed);

        // Generate a staircase down as far as possible.
        for (int nFloor = 0; nFloor > -20; nFloor--)
        {
            //if (nFloor == -3) {
            if (true) {
                maze_map.put(new Vector3i(0, nFloor, 0), new ThroughStairs(this, new Vector3i(0, nFloor, 0)));
            }
            else
            {
                maze_map.put(new Vector3i(0,nFloor,0),new AirTile(this));
            }
        }

        // Generate 5 random vectors that will serve as "room centers" per floor.
        int nRoomCount = 5 + random_generator.nextInt(3);
        Vector3i vectorLastFloorStairs = null;
        for (int nFloor = 1; nFloor < maze_depth; nFloor++) {
            Vector3i vecLastRoomCenter = null;
            Vector3i room_center = null;
            for (int nRoom = 0; nRoom < nRoomCount; nRoom++) {
                if (nRoom == 0) {
                    if (vectorLastFloorStairs == null) {
                        // The first floor, so our first room is at 0,0,0
                        room_center = new Vector3i(0, nFloor, 0);
                    }
                    else
                    {
                        room_center = new Vector3i(vectorLastFloorStairs.x(),nFloor,vectorLastFloorStairs.z());
                    }
                    maze_map.put(new Vector3i(room_center),new UpStairs(this));
                }
                else
                {
                    // Make a random room
                    room_center = new Vector3i(random_generator.nextInt(maze_width), nFloor, random_generator.nextInt(maze_height));
                }
                int generate_width = 2 + random_generator.nextInt(3);
                int generate_height = 2 + random_generator.nextInt(3);
                generateRoom(room_center,generate_width,generate_height);
                if (vecLastRoomCenter == null)
                {
                    vecLastRoomCenter = new Vector3i(room_center);
                }
                else
                {
                    // Connect sequentially created rooms
                    tunnelBetween(vecLastRoomCenter,room_center);
                    vecLastRoomCenter = room_center;
                }
            }
            vectorLastFloorStairs = new Vector3i(room_center);
            maze_map.put(new Vector3i(room_center),new DownStairs(this));
        }
    }

    public HashMap<Vector3i,MazeTile> maze_map;

    public long seed = 500;
    // The number of blocks each room is wide (square)
    public int room_diameter = 3;

    // The height (as in gravity direction) of each room
    public int room_height = 5;

    // The depth in dungeon levels below which the actual dungeon starts.
    public int initial_depth = 5;

    // The diameter of the dungeon.
    public int maze_height = 25;

    public int maze_width = 80;

    // The depth of the dungeon.
    public int maze_depth = 5;

    // The thickness of the floor/ceiling
    public int floor_thickness = 4;

    // The entrance of the maze. The maze extens northwest from this position.
    public Vector3i entrance = new Vector3i(0,0,5);
    public int maxTileX() {
        return entrance.x() + maze_width*room_diameter;
    }
    public int maxTileY()  {
        return entrance.y();
    }
    public int maxTileZ() {
        return entrance.z() + maze_height*room_diameter;
    }
    public int minTileX() {
        return entrance.x();
    }
    public int minTileY()
    {
        return entrance.y() - maze_depth*(room_height+floor_thickness);
    }
    public int minTileZ()
    {
      return entrance.z();
    }
    public boolean isInsideBoundaries(Vector3i position)
    {
        // Catch the tunnel leading down
        if (position.x() >= entrance.x() &&
                position.x() < entrance.x() + room_diameter &&
                position.z() >= entrance.z() &&
                position.z() < entrance.z() + room_diameter &&
                position.y() > minTileY()
                )
        {
            return true;
        }
        return (position.x() > minTileX() &&
                position.y() > minTileY() &&
                position.z() > minTileZ() &&
                position.x() < maxTileX() &&
                position.y() < maxTileY() &&
                position.z() < maxTileZ());
    }
    public Vector3i getRoomID(Vector3i position)
    {
        if (!isInsideBoundaries(position))
        {
            return null;
        }
        int x = (position.x() - entrance.x())/room_diameter;
        // Dungeon level increases as we go down
        int y = - (position.y() - entrance.y())/(room_height+floor_thickness);
        int z = (position.z() - entrance.z())/room_diameter;
        return new Vector3i(x,y,z);
    }
    public Vector3i getPositionInRoom(Vector3i position)
    {
        int x_remainder = (position.x() - entrance.x()) % room_diameter;
        int y_remainder = -(position.y() - entrance.y()) % (room_height+floor_thickness);
        if (position.y() > entrance.y())
        {
            y_remainder = room_height+floor_thickness - (position.y() - entrance.y()) % (room_height+floor_thickness);
        }
        int z_remainder = (position.z() - entrance.z()) % room_diameter;
        return new Vector3i(x_remainder,y_remainder,z_remainder);
    }
}
