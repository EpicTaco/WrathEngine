/**
 *  Wrath Engine 
 *  Copyright (C) 2015  Trent Spears
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package wrath.client.graphics;

import org.lwjgl.util.vector.Vector3f;
import wrath.common.entities.Player;

/**
 * Represents the Player Camera for a 2D or 3D game.
 * @author Trent Spears
 */
public class Camera
{
    private final Player player;
    private Vector3f position = new Vector3f(0,0,0);
    //x = pitch, y = yaw, z = roll
    private Vector3f orientation = new Vector3f(0,0,0);
    
    public Camera(Player player)
    {
        this.player = player;
    }
    
    public Player getPlayer()
    {
        return player;
    }
    
    public Vector3f getPosition()
    {
        return position;
    }
    
    public Vector3f getOrientation()
    {
        return orientation;
    }
    
    public void setOrientation(float pitch, float yaw, float roll)
    {
        orientation.x = pitch;
        orientation.y = yaw;
        orientation.z = roll;
    }
    
    public void setOrientation(Vector3f orientation)
    {
        this.orientation = orientation;
    }
    
    /**
     * Sets the camera's position.
     * @param x The X-coordinate of the camera.
     * @param y The Y-coordinate of the camera.
     * @param z The Z-coordinate of the camera.
     */
    public void setPosition(float x, float y, float z)
    {
        position.x = x;
        position.y = y;
        position.z = z;
    }
    
    public void setPosition(Vector3f position)
    {
        this.position = position;
    }
    
    public void transformOrientation(float dp, float dy, float dr)
    {
        orientation.x += dp;
        orientation.y += dy;
        orientation.z += dr;
    }
    
    /**
     * Increments the position of the camera by the specified amount.
     * @param dx The amount to increase the position on the X-Axis.
     * @param dy The amount to increase the position on the Y-Axis.
     * @param dz The amount to increase the position on the Z-Axis.
     */
    public void transformPosition(float dx, float dy, float dz)
    {
        position.x += dx;
        position.y += dy;
        position.z += dz;
    }
}
